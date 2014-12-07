package su.ias.zbarintegrator;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {

	private final String TAG = "CameraPreview";

	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	Camera mCamera;
	PreviewCallback mPreviewCallback;
	AutoFocusCallback mAutoFocusCallback;

	int screenOrientation;


	public CameraPreview(Context context, PreviewCallback previewCallback, AutoFocusCallback autoFocusCb, int orientation) {

		super(context);

		screenOrientation = orientation;

		mPreviewCallback = previewCallback;
		mAutoFocusCallback = autoFocusCb;
		mSurfaceView = new SurfaceView(context);
		mSurfaceView.setZOrderOnTop(false);
		addView(mSurfaceView);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);

		setBackgroundColor(Color.WHITE);
	}



	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
			requestLayout();
		}
	}



	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		// We purposely disregard child measurements because act as a
		// wrapper to a SurfaceView that centers the camera preview instead
		// of stretching it.

		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

		Log.i("qr", "widthMeasureSpec = " + widthMeasureSpec);
		Log.i("qr", "heightMeasureSpec = " + heightMeasureSpec);

		Log.w("qr", "width = " + width);
		Log.w("qr", "height = " + height);

		setMeasuredDimension(width, height);

		if (mSupportedPreviewSizes != null) {

			if(screenOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
	        || screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,width, height);

			} else if(screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
			 	    ||screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
				        mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, height, width);
			} else {
				Log.e("my info", "jopa s orientaciey!!!: " + screenOrientation);
			}

		} else {
			Log.e("my info", "mSupportedPreviewSizes == null");
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && getChildCount() > 0) {
			final View child = getChildAt(0);

			final int width = r - l;
			final int height = b - t;

			int previewWidth = width;
			int previewHeight = height;

			if (mPreviewSize != null) {
				previewWidth = mPreviewSize.width;
				previewHeight = mPreviewSize.height;
			}

			Log.e("qr", "previewWidth = " + previewWidth);
			Log.e("qr", "previewHeight = " + previewHeight);
			
			
			//child.layout(0, 0, width, height);

			
			
			
			
			if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
					|| screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
					
					Log.i("my info", "Превью не повернуто!");
					
					if ((float)width/height < (float)previewWidth/previewHeight){
						Log.i("my info", "Вписываем по высоте");
						
						float scaleFactor = (float) height/previewHeight;
						
						child.layout((int)(width/2f - scaleFactor*previewWidth/2), 0, height, (int)(width/2f + scaleFactor*previewWidth/2));
					}
					
					else {
						Log.i("my info", "Вписываем по ширине");
						float scaleFactor = (float) width/previewWidth;
						child.layout(0, (int)(height/2f - scaleFactor*previewHeight/2), width, (int)(height/2f + scaleFactor*previewHeight/2));
					}
			}

			else if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
					|| screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
				Log.i("my info", "Превью повернуто!");
				
				if ((float)width/height < (float)previewHeight/previewWidth){
					Log.i("my info", "Вписываем по высоте");
					
					float scaleFactor = (float) height/previewWidth;
					
					child.layout((int)(width/2f - scaleFactor*previewHeight/2), 0, height, (int)(width/2f + scaleFactor*previewHeight/2));
				}
				
				else {
					Log.i("my info", "Вписываем по ширине");
					float scaleFactor = (float) width/previewHeight;
					child.layout(0, (int)(height/2f - scaleFactor*previewWidth/2), width, (int)(height/2f + scaleFactor*previewWidth/2));
				}
			}
			
			else {
				
				Log.i("my info", "Что получилось - то получилось...");
				child.layout(0, 0, width, height);
			}
		

			// Center the child SurfaceView within the parent.

			/*
			 * if (width * previewHeight > height * previewWidth) { final int
			 * scaledChildWidth = previewWidth * height / previewHeight;
			 * child.layout((width - scaledChildWidth) / 2, 0, (width +
			 * scaledChildWidth) / 2, height); } else { final int
			 * scaledChildHeight = previewHeight * width / previewWidth;
			 * child.layout(0, (height - scaledChildHeight) / 2, width, (height
			 * + scaledChildHeight) / 2); }
			 */

		}
	}

	public void hideSurfaceView() {
		mSurfaceView.setVisibility(View.INVISIBLE);
	}

	public void showSurfaceView() {
		mSurfaceView.setVisibility(View.VISIBLE);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		if (mCamera != null) {
			mCamera.cancelAutoFocus();
			mCamera.stopPreview();
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (holder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		if (mCamera != null) {
			// Now that the size is known, set up the camera parameters and
			// begin
			// the preview.
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			requestLayout();

			mCamera.setParameters(parameters);
			mCamera.setPreviewCallback(mPreviewCallback);
			mCamera.startPreview();
			mCamera.autoFocus(mAutoFocusCallback);
		}
	}

}
