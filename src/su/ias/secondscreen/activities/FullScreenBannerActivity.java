package su.ias.secondscreen.activities;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import su.ias.secondscreen.R;
import su.ias.secondscreen.app.AppSingleton;
import su.ias.secondscreen.data.BannerData;
import su.ias.secondscreen.db.DBAdapter;
import su.ias.secondscreen.utils.BitmapDrawer;


public class FullScreenBannerActivity extends Activity {
	
	private ImageView bannerImg;
    private ImageView closeImg;
	public static final String QR = "QR";
    private String qrCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_fullscreen_banner);
        
        bannerImg = (ImageView)findViewById(R.id.banner);
        closeImg = (ImageView)findViewById(R.id.close_btn);
        qrCode = getIntent().getStringExtra(QR);

        //--- temp
        finish();
        startActivity(new Intent(FullScreenBannerActivity.this, MainActivity.class).putExtra(MainActivity.QR, qrCode));


        final BannerData bannerData = AppSingleton.getInstance().getDBAdapter().getBannerData(qrCode, DBAdapter.FS_BANNER);
        if (bannerData == null) {
            finish();
            startActivity(new Intent(FullScreenBannerActivity.this, MainActivity.class).putExtra(MainActivity.QR, qrCode));
        }   else {

            final String url = bannerData.imagesArr.get(0);
            final String path = getCacheDir() + "/" + "fs_banner_"+qrCode+ ".png";

            ViewTreeObserver observer = bannerImg.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    bannerImg.getViewTreeObserver().removeOnPreDrawListener(this);

                    BitmapDrawer.INSTANCE.drawBitmap(path, url, bannerImg, BitmapDrawer.DRAW_FILL, true, true);
                    return false;
                }
            });


            bannerImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bannerData.partnerUrl));
                    startActivity(browserIntent);
                }
            });


            closeImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    startActivity(new Intent(FullScreenBannerActivity.this, MainActivity.class).putExtra(MainActivity.QR, qrCode));
                }
            });


        }





    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(FullScreenBannerActivity.this, MainActivity.class).putExtra(MainActivity.QR, qrCode));
        super.onBackPressed();
    }

    protected void loadContentView() {
        Log.i("@", "onLoadContentview");
    }
}
