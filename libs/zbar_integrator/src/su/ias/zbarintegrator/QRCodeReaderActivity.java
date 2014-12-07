package su.ias.zbarintegrator;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import net.sourceforge.zbar.*;

public class QRCodeReaderActivity extends Activity implements ZBarConstants,
		PreviewCallback {

	private CameraPreview mPreview;
	private Camera mCamera;
	private ImageScanner mScanner;
	private Handler mAutoFocusHandler;
	private boolean mPreviewing = true;

	private FrameLayout previewLayout;

	static {
		System.loadLibrary("iconv");
	}

	private OrientationEventListener orientationEventListener;
	private static int lastSavedScreenRotation = -1;
	public static int lastSavedOrientation = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (!isCameraAvailable()) {
			// Cancel request if there is no rear-facing camera.
			cancelRequest();
			return;
		}

		setContentView(getContentView());

		initScreenOrientation();

		previewLayout = (FrameLayout) findViewById(R.id.previewLayout);

		mAutoFocusHandler = new Handler();

		// lastSavedOrientation = getScreenOrientation();

		// Create and configure the ImageScanner;
		setupScanner();

		orientationEventListener = new OrientationEventListener(this,
				SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int orientation) {
				//updateCameraRotation();
				getScreenOrientation();
			}
		};

	}

	/**
	 * override for special programmatically orientation settings
	 */
	protected void initScreenOrientation() {

	}

	/**
	 * Override for different layout
	 * 
	 * @return
	 */
	protected int getContentView() {
		return R.layout.activity_qrcode_reader;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Open the default i.e. the first rear facing camera.

		Log.w("my info", "Создаем mCamera");
		mCamera = Camera.open();
		if (mCamera == null) {
			// Cancel request if mCamera is null.
			cancelRequest();
			return;
		}


		if (orientationEventListener.canDetectOrientation()) {
			Log.i("qr", "Отслеживаем смену ориентации");
			orientationEventListener.enable();
		} else {
			Log.e("qr", "Отслеживание смены ориентации недоступно!!!");
		}

		/*
		 * switch (lastSavedOrientation) {
		 * 
		 * case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
		 * mCamera.setDisplayOrientation(90);
		 * 
		 * Log.i("qr", "SCREEN_ORIENTATION_PORTRAIT"); break;
		 * 
		 * case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
		 * mCamera.setDisplayOrientation(270); Log.i("qr",
		 * "SCREEN_ORIENTATION_REVERSE_PORTRAIT"); break;
		 * 
		 * }
		 */

		//updateCameraRotation();
		getScreenOrientation();

		// Create a RelativeLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		previewLayout.removeAllViews();

		Log.i("my info", "Создаем превью из onResume");
		mPreview = new CameraPreview(this, this, autoFocusCB,
				lastSavedOrientation);
		previewLayout.addView(mPreview);

		mPreview.setCamera(mCamera);
		// mPreview.showSurfaceView();
		mPreviewing = true;

	}

	@Override
	protected void onPause() {
		super.onPause();

		orientationEventListener.disable();
		lastSavedOrientation = -1;
		lastSavedScreenRotation = -1;

		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mPreview.setCamera(null);

			mCamera.cancelAutoFocus();
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();

			// According to Jason Kuang on
			// http://stackoverflow.com/questions/6519120/how-to-recover-camera-preview-from-sleep,
			// there might be surface recreation problems when the device goes
			// to sleep. So lets just hide it and
			// recreate on resume
			// mPreview.hideSurfaceView();

			mPreviewing = false;
			mCamera = null;

			previewLayout.removeAllViews();
			mPreview = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void setupScanner() {
		mScanner = new ImageScanner();
		mScanner.setConfig(0, Config.X_DENSITY, 3);
		mScanner.setConfig(0, Config.Y_DENSITY, 3);

		mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
		mScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);
	}

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (mCamera != null && mPreviewing) {
				mCamera.autoFocus(autoFocusCB);
			}
		}
	};

	// Mimic continuous auto-focusing
	Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			mAutoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

	public boolean isCameraAvailable() {
		PackageManager pm = getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	public void cancelRequest() {
		Intent dataIntent = new Intent();
		dataIntent.putExtra(ERROR_INFO, "Camera unavailable");
		setResult(Activity.RESULT_CANCELED, dataIntent);
		finish();
	}

	private void updateCameraRotation(int currentOrientation) {
		Log.i("my info", "updateCameraRotation()");

		/*
		 * switch (lastSavedOrientation) {
		 * 
		 * case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
		 * mCamera.setDisplayOrientation(90);
		 * 
		 * Log.i("qr", "SCREEN_ORIENTATION_PORTRAIT"); break;
		 * 
		 * case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
		 * mCamera.setDisplayOrientation(270); Log.i("qr",
		 * "SCREEN_ORIENTATION_REVERSE_PORTRAIT"); break;
		 * 
		 * }
		 */

		
		if (mCamera != null && currentOrientation != lastSavedOrientation) {

			Log.i("my info", "updateCameraRotation");

			mCamera.stopPreview();

			switch (currentOrientation) {

			case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
				mCamera.setDisplayOrientation(90);
				Log.w("my info", "портретная ориентация");
				break;

			case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
				mCamera.setDisplayOrientation(270);
				Log.w("my info", "перевернутая портретная ориентация");
				break;

			case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
				mCamera.setDisplayOrientation(0);
				Log.w("my info", "альбомная ориентация");
				break;

			case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
				mCamera.setDisplayOrientation(180);
				Log.w("my info", "перевернутая альбомная ориентация");
				break;

			}

			mCamera.startPreview();

			lastSavedOrientation = currentOrientation;
			Log.i("my info", "Сохранена ориентация " + lastSavedOrientation);
		} else {
			
			if (mCamera == null){
				Log.e("my info", "mCamera is NULL!!!!");
			}
			
			Log.e("my info", "Чота не срослось... lastSavedOrientation = "
					+ lastSavedOrientation + ", currentOrien" +
                    "tation = "
					+ currentOrientation);
			
			lastSavedScreenRotation = -1;
		}

	}



	public int getScreenOrientation() {
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		
//		Log.i("my info", "currentRotation = " + rotation);

		if (rotation == lastSavedScreenRotation) {
			return lastSavedOrientation;
		}

		lastSavedScreenRotation = rotation;

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		int orientation;
		// if the device's natural orientation is portrait:
		if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
				&& height > width
				|| (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)
				&& width > height) {
			switch (rotation) {
			case Surface.ROTATION_0:
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			case Surface.ROTATION_90:
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_180:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				break;
			case Surface.ROTATION_270:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				break;
			default:
				Log.e("qr", "Unknown screen orientation. Defaulting to portrait.");
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			}
		}
		// if the device's natural orientation is landscape or if the device
		// is square:
		else {
			switch (rotation) {
			case Surface.ROTATION_0:
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_90:
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			case Surface.ROTATION_180:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				break;
			case Surface.ROTATION_270:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				break;
			default:
				Log.e("qr", "Unknown screen orientation. Defaulting to landscape.");
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			}
		}

		Log.w("qr", "ориентация экрана: " + orientation);

		updateCameraRotation(orientation);
		
		return orientation;
	}

	// ----------------------------
	// methods of PreviewCallback:
	// ----------------------------

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		Camera.Size size = parameters.getPreviewSize();

		Image barcode = new Image(size.width, size.height, "Y800");
		barcode.setData(data);

		int result = mScanner.scanImage(barcode);

		if (result != 0) {
			mCamera.cancelAutoFocus();
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mPreviewing = false;
			SymbolSet syms = mScanner.getResults();
			for (Symbol sym : syms) {
				String symData = sym.getData();
				if (!TextUtils.isEmpty(symData)) {

					Log.i("qr", "SCAN_RESULT: " + symData);
					Log.i("qr", "SCAN_RESULT_TYPE: " + sym.getType());

					Log.i("qr", "QR-Code: " + Symbol.QRCODE);
					
					
					processScanResult(symData);

					/*
					 * Intent dataIntent = new Intent();
					 * dataIntent.putExtra(SCAN_RESULT, symData);
					 * dataIntent.putExtra(SCAN_RESULT_TYPE, sym.getType());
					 * setResult(Activity.RESULT_OK, dataIntent); finish();
					 */
					break;
				}
			}
		}
	}
	
	
	protected void processScanResult(String resultString){
		
	}

	/*
	 * public void applyCameraRotation() {
	 * 
	 * 
	 * }
	 */

}
