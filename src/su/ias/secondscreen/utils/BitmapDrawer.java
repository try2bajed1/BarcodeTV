package su.ias.secondscreen.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;

public enum BitmapDrawer {

	INSTANCE;

	// Броадкасты:
	public static final String PICTURE_LOADED = "PICTURE_LOADED";
	public static final String DRAWING_COMPLETED = "DRAWING_COMPLETED";

	// Содержание броадкастов:
	public static final String LOADED_PICTURE_URL = "LOADED_PICTURE_URL";
	public static final String PICTURE_PATH = "PICTURE_PATH";
	public static final String IMAGEVIEW_ID = "IMAGEVIEW_ID";
	public static final String IS_DEFAULT_BITMAP = "IS_DEFAULT_BITMAP";

	// Draw rules:
	public static final int DRAW_CENTER = 0;
	public static final int DRAW_TOP = 1;
	public static final int DRAW_LEFT = 2;
	public static final int DRAW_RIGHT = 3;
	public static final int DRAW_BOTTOM = 4;
	// public static final int DRAW_LEFT_TOP_CORNER = 5;
	// public static final int DRAW_RIGHT_TOP_CORNER = 6;
	// public static final int DRAW_LEFT_BOTTOM_CORNER = 7;
	// public static final int DRAW_RIGHT_BOTTOM_CORNER = 8;
	public static final int DRAW_ALL = 10;
	public static final int DRAW_FILL = 11;

	private Bitmap defaultBitmap;
	private boolean loaderReady;

	private LinkedHashMap<ImageView, LoadingQueueItem> queue;

	private BitmapDrawer() {

		defaultBitmap = null;
		queue = new LinkedHashMap<ImageView, BitmapDrawer.LoadingQueueItem>();
		loaderReady = true;
	}

	/**
	 * Переданное изображение будет выводится если ссылка на изображение
	 * невалидная или null
	 * 
	 * @param bitmap
	 */
	public void setDefaultBitmap(Bitmap bitmap) {
		defaultBitmap = bitmap;
	}

	public void drawBitmap(String path, String url, ImageView view,
			Integer drawRules, boolean allowDownloading,
			boolean clearBeforeDrawing) {

		if (clearBeforeDrawing) {
			view.setImageDrawable(null);
		}

		// Сначала пробуем получить картинку из кэша
		//Log.i("pic", "Пробуем взять картинку из кэша для ключа " + path);
		Bitmap bitmap = CacheUtils.getInstance().getBitmapFromMemCache(path);

		// Если в кэше нету - запускаем загрузку изображения в отдельном потоке
		if (bitmap == null) {
			//Log.e("pic", "В кэше не найдено для ид " + view.getId());
			// Для соблюдения корректного порядка в очереди - в cлучае если view
			// для загрузки повторяется - устаревший дубликат удаляем
			if (queue.containsKey(view)) {
				queue.remove(view);
				//Log.i("pic","Устаревший дубликат удален из очереди для вьюхи с ид "+ view.getId());
			}
			queue.put(view, new LoadingQueueItem(path, url, view, drawRules));
			//Log.i("pic", "Поставлена в очередь вьюха с ид " + view.getId());
			updateQueque(allowDownloading);
		} else {
			//Log.e("pic", "Найдено в кэше  для ид " + view.getId());
			makeDrawing(view, bitmap, drawRules, path, false);
		}

	}

	private void makeDrawing(ImageView imageView, Bitmap bitmap, Integer drawRules, String path, boolean isDefaultBitmap) {

		if (drawRules != null) {

			if (drawRules == DRAW_ALL) {
				imageView.setScaleType(ScaleType.CENTER_INSIDE);
			} else {
				imageView.setScaleType(ScaleType.CENTER_CROP);
			}
		}

		imageView.setImageBitmap(bitmap);
		bitmap = null;

		/*
		 * По завершению отрисовки изображения оповещаем всех заинтересованных
		 */
		Intent intent = new Intent(DRAWING_COMPLETED);
		intent.putExtra(PICTURE_PATH, path);
		intent.putExtra(IMAGEVIEW_ID, imageView.getId());
		intent.putExtra(IS_DEFAULT_BITMAP, isDefaultBitmap);

		imageView.getContext().sendBroadcast(intent);

	}

	@SuppressLint("NewApi")
	protected void updateQueque(boolean allowDownloading) {

		if (loaderReady && queue.size() > 0) {

			Object[] keys = queue.keySet().toArray();
			LoadingQueueItem item = queue.get((ImageView) keys[0]);

			BitmapLoader bitmapLoader = new BitmapLoader(item.getImageView(), item.getDrawRules(), allowDownloading);
			bitmapLoader.execute(item.getPath(), item.getUrl());

			/*
			 * if (android.os.Build.VERSION.SDK_INT >=
			 * android.os.Build.VERSION_CODES.HONEYCOMB) {
			 * bitmapLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
			 * item.getPath(), item.getUrl()); } else {
			 * bitmapLoader.execute(item.getPath(), item.getUrl()); }
			 */
		}
	}

	public void clearQueue() {
		queue.clear();
	}

	protected class BitmapLoader extends AsyncTask<Object, Void, Void> {


		@Override
		protected void onPreExecute() {
			loaderReady = false;
		};


		private final WeakReference<ImageView> imageViewReference;
		private int reqHeight;
		private int reqWidth;
		private Integer drawRules;
		private boolean allowDownloading;

		private String url;
		private String path;

		private Bitmap bitmap;

		private boolean isDefaultBitmap;

		public BitmapLoader(ImageView imageView, Integer drawRules, boolean allowDownloading) {

			imageViewReference = new WeakReference<ImageView>(imageView);
			reqHeight = imageView.getHeight();
			reqWidth = imageView.getWidth();
			this.drawRules = drawRules;
			this.allowDownloading = allowDownloading;
			isDefaultBitmap = false;
		}

		@Override
		protected Void doInBackground(Object... params) {

			bitmap = null;
			path = (String) params[0];
			url = (String) params[1];

			// Сначала проверяем загружен ли файл на устройство
			File imageFile = new File(path);

			synchronized (imageFile) {

				// Если файл существует - формируем изображение
				if (imageFile.exists()) {
					bitmap = decodeSampledBitmapFromFile(path);
				}

				// Если файла не существует - загружаем его (сначала под другим  именем)
				else if (allowDownloading) {

					InputStream input = null;
					File tempFile = new File(path + ".tmp");

					try {

						HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
						connection.setConnectTimeout(10000);

						input = connection.getInputStream();

						OutputStream output = new FileOutputStream(tempFile);

						try {
							byte[] buffer = new byte[1024];
							int bytesRead = 0;
							while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {

								if (!isCancelled()) {
									output.write(buffer, 0, bytesRead);
								} else {
									//Log.i("pic", "загрузка отменена");
									try {
										output.close();
										tempFile.delete();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}

							// В случае удачной загрузки временный файл  переименованный в обычный и получаем из него изображение
							if (tempFile.exists()) {
								tempFile.renameTo(imageFile);

								/*
								 * В случае успешной загрузки изображения
								 * оповещаем всех заинтересованных
								 */
								synchronized (imageViewReference.get()) {
                                    if (imageViewReference.get() != null) {
                                        Context context = imageViewReference.get().getContext();
                                        Intent intent = new Intent(PICTURE_LOADED);
                                        intent.putExtra(LOADED_PICTURE_URL, url);
                                        context.sendBroadcast(intent);
                                    }
                                }
								bitmap = decodeSampledBitmapFromFile(path);
							}

						} catch (Exception ex) {

							//Log.e("pic","Не удалось загрузить файл: "+ ex.toString());

						} finally {
							try {
								output.close();
								if (tempFile.exists()) {
									tempFile.delete();
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

					} catch (Exception ex) {

					}

				}

				if (bitmap == null) {
					bitmap = defaultBitmap;
					isDefaultBitmap = true;
				} else {
					CacheUtils.getInstance().addBitmapToCache(imageFile.getPath(), bitmap);
				}
			}
			return null;
		}



		@Override
		protected void onPostExecute(Void result) {

			if (imageViewReference != null) {

				final ImageView imageView = imageViewReference.get();

				if (imageView != null && queue.containsKey(imageView)) {
					makeDrawing(imageView, bitmap, drawRules, path, isDefaultBitmap);
					bitmap = null;
				}
			}

			removeFromQueue(imageViewReference.get());
			loaderReady = true;
			updateQueque(allowDownloading);
		}
 
		@Override
		protected void onCancelled() {
			loaderReady = true;
			bitmap = null;
		}




		protected Bitmap decodeSampledBitmapFromFile(String path) {

			// Сначала получаем реальные размеры изображения
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);

			// Вычисляем коэффициент сжатия
			options.inSampleSize = calculateInSampleSize(options);

			// Получаем сжатое изображение
			options.inJustDecodeBounds = false;
			Bitmap bitmap = BitmapFactory.decodeFile(path, options);

			// Теперь в зависимости от указанной приоритетной части изображения
			// (при ее указании) подготавливае битмапку, содержащую указанную часть

			if (drawRules != null && bitmap != null) {

				float viewProportion   = ((float) reqWidth) / reqHeight;
				float bitmapProportion = ((float) bitmap.getWidth()) / bitmap.getHeight();

				if (drawRules == DRAW_TOP || drawRules == DRAW_BOTTOM) {

					// Если изображение "выше" чем вьюха:
					if (bitmapProportion < viewProportion) {
						// Сначала выясняем коэффициент масштабирования для подгонки изображения по ширине
						float xScaleFactor =   (float) reqWidth / bitmap.getWidth();
						int finalBitmapHeight = (int) (reqHeight / xScaleFactor);

						// Подрезаем по высоте исходную битмапку (сверху или снизу):
						if (drawRules == DRAW_TOP) {
							bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), finalBitmapHeight);
						}
						if (drawRules == DRAW_BOTTOM) {
							bitmap = Bitmap.createBitmap(bitmap, 0, bitmap.getHeight() - finalBitmapHeight,
									                                bitmap.getWidth(),   finalBitmapHeight);
						}
					} else {
						drawRules = DRAW_ALL;
					}
				}



				if (drawRules == DRAW_LEFT || drawRules == DRAW_RIGHT) {
					// Если изображение "шире" чем вьюха:
					if (bitmapProportion > viewProportion) {

						// Сначала выясняем коэффициент масштабирования для подгонки изображения по высоте
						float yScaleFactor = ((float) reqHeight) / bitmap.getHeight();
						int finalBitmapWidth  = (int) (reqWidth  / yScaleFactor);

						// Подрезаем по высоте исходную битмапку (слева или справа):
						if (drawRules == DRAW_LEFT) {
							bitmap = Bitmap.createBitmap(bitmap, 0, 0, finalBitmapWidth, bitmap.getHeight());
						}

						if (drawRules == DRAW_RIGHT) {
							bitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth() - finalBitmapWidth, 0,
									                             finalBitmapWidth,   bitmap.getHeight());
						}
					} else {
						drawRules = DRAW_ALL;
					}
				}

				if (drawRules == DRAW_ALL) {
					if (bitmap.getWidth() < reqWidth && bitmap.getHeight() < reqHeight) {
						float scale = Math.min((float) reqWidth  / bitmap.getWidth(),
								               (float) reqHeight / bitmap.getHeight());

						int scaledWidth = (int) (bitmap.getWidth() * scale);
						int scaledHeight = (int) (bitmap.getHeight() * scale);

						bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

					}
				}

			}

			return bitmap;
		}


		protected int calculateInSampleSize(BitmapFactory.Options options) {
			// реальные размеры изображения
			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;

			if (height > reqHeight || width > reqWidth) {

				final int halfHeight = height / 2;
				final int halfWidth  = width  / 2;

				// Вычисляем максимальный коэффициент уменьшения изображения
				// (который равен степени числа 2) с учетом, что размеры
				// отмасштабированной картинки должны быть не менее, чем требуемые

				while((halfHeight / inSampleSize) > reqHeight
				   && (halfWidth  / inSampleSize) > reqWidth) {

					inSampleSize *= 2;
				}
			}
			return inSampleSize;
		}

	}



	protected class LoadingQueueItem {

		private String path;
		private String url;
		private WeakReference<ImageView> imageViewReference;
		private Integer drawRules;

		LoadingQueueItem(String path, String url, ImageView imageView, Integer drawRules) {
			this.path = path;
			this.url = url;
			imageViewReference = new WeakReference<ImageView>(imageView);
			this.drawRules = drawRules;
		}

		public String getPath() {
			return path;
		}

		public String getUrl() {
			return url;
		}

		public ImageView getImageView() {
			return imageViewReference.get();
		}

		public Integer getDrawRules() {
			return drawRules;
		}

	}

	public void removeFromQueue(ImageView imageView) {
		if (queue.containsKey(imageView)) {
			queue.remove(imageView);
		}
	}

}
