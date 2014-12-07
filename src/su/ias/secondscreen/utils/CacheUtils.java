package su.ias.secondscreen.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;
//import android.util.Log;

public class CacheUtils {

	private LruCache<String, Bitmap> bitmapsCache;

	private static CacheUtils cacheUtils;
	
	private int maxCacheSize;

	public CacheUtils() {

		int systemMaxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
		maxCacheSize = systemMaxMemory / 4;

		bitmapsCache = new LruCache<String, Bitmap>(maxCacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return CacheUtils.this.sizeOf(bitmap);
			}
		};
	}

	public static CacheUtils getInstance() {
		if (cacheUtils == null) {
			cacheUtils = new CacheUtils();
		}

		return cacheUtils;
	}

	/**
	 * Метод возвращает закэшированное изображение, асоциированное с ключом
	 * @param key
	 * @return
	 */
	public Bitmap getBitmapFromMemCache(String key) {
		return bitmapsCache.get(key);
	}

	/**
	 * Метод добавляет изображение в кэш (при условии отсутствия в кэше изображения с передаваемым ключом)
	 * @param key
	 * @param bitmap
	 */
	public void addBitmapToCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			bitmapsCache.put(key, bitmap);
			//Log.i("pic", "битмапка добавлена в кэш с ключом " + key);
			//Log.w("pic", "размер кэша: " + bitmapsCache.size() + " / " + maxCacheSize);
		}
	}
	
	/**
	 * Метод очищает кэш
	 */
	public void clearCache(){
		bitmapsCache.evictAll();
	}
	
	

	/**
	 * Метод возвращает размер изображения в байтах
	 * @param data
	 * @return
	 */
	@SuppressLint("NewApi")
	private int sizeOf(Bitmap data) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			return (data.getRowBytes() * data.getHeight()) / 1024;
		} else {
			return data.getByteCount() / 1024;
		}
	}

}
