package su.ias.secondscreen.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Здесь находятся методы для операций над файловой системой
 * 
 * @author pkorchagin
 * 
 */
public class FileUtils {

	/**
	 * Метод возвращает размер переданной директории в байтах
	 * 
	 * @param directory
	 * @return
	 */
	public static long folderSize(File directory) {
		long length = 0;

		for (File file : directory.listFiles()) {
			if (file.isFile())
				length += file.length();
			else
				length += folderSize(file);
		}
		return length;
	}

	/**
	 * Метод стирает содержимое указанной папки
	 * 
	 * @param folder
	 */
	public static void clearFolder(File folder) {

		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				clearFolder(file);
			}

			file.delete();
		}
	}

	/**
	 * Метод создает копию файла. Указываются файл источник и файл-копия
	 * 
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
	public static void copyFile(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * Метод загружает контент с указанного адреса в указанный файл
	 * 
	 * @param file
	 * @param url
	 * @param asyncTask  -  возможно метод выполняется в указанной асинхронной задаче
	 * @return
	 */
	public static boolean loadFile(File file, String url, AsyncTask<?, ?, ?> asyncTask) {

		boolean fileLoadingResult = false;

		try {

			Log.i("my info", "Загружаем файл " + file.getAbsolutePath());

			URL fileURL = new URL(url);

			HttpURLConnection connection = (HttpURLConnection) fileURL.openConnection();
			connection.setConnectTimeout(10000);
			InputStream input = connection.getInputStream();

			OutputStream output = new FileOutputStream(file);

			Log.d("my info", "Загружаем с адреса: " + url);

			try {

				byte[] buffer = new byte[1024];
				int bytesRead = 0;
				while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
					output.write(buffer, 0, bytesRead);

					// В случае отмены возможной асинхронной задачи - прерываем загрузку
					if (asyncTask != null && asyncTask.isCancelled()) {
						
						try {
							output.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						return false;
					}
				}

				Log.d("my info", "размер загруженного файла: " + file.length()/ 1024);

				fileLoadingResult = true;
			} catch (Exception ex) {

				Log.e("my info", "Не удалось загрузить файл: " + ex.toString());
				fileLoadingResult = false;

			} finally {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (Exception ex) {
			Log.e("my info", "Ошибка: " + ex.toString());
			return false;
		}

		if (fileLoadingResult) {
			Log.i("my info", "Файл загружен: " + file.getAbsolutePath());
		}

		return fileLoadingResult;

	}

}
