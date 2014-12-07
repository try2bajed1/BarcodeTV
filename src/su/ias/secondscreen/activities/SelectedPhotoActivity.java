package su.ias.secondscreen.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import su.ias.secondscreen.R;
import su.ias.secondscreen.activities.fragments.PhotosFragment;
import su.ias.secondscreen.components.TouchImageView;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 14.05.2014
 * Time: 14:58
 */
public class SelectedPhotoActivity extends Activity {

    private String imgUrl;

    private TextView nameTV;
    private TouchImageView touchImg;
    private ViewPager imagesVP;
    private VideoView videoView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selected_photo);

        imgUrl = getIntent().getStringExtra(PhotosFragment.SELECTED_PHOTO_URL);

        touchImg = (TouchImageView) findViewById(R.id.touch_image);

        final String path = getCacheDir() + "/" + "fullSizePhoto.png";

        //new DownloadImageTask(touchImg).execute(imgUrl);

        ImageLoader.getInstance().displayImage(imgUrl,touchImg,new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                ((TouchImageView)view).setZoom(1f);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });

        //BitmapDrawer.INSTANCE.drawBitmap(path, imgUrl, touchImg, BitmapDrawer.DRAW_FILL, true, true);

        /*ViewTreeObserver observer = touchImg.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {

                touchImg.getViewTreeObserver().removeOnPreDrawListener(this);
                BitmapDrawer.INSTANCE.drawBitmap(path, imgUrl, touchImg, BitmapDrawer.DRAW_FILL, true, true);
                return false;
            }

        });*/

    }





    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            ((TouchImageView)bmImage).setZoom(1f);
        }

    }






}
