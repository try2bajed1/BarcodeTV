package su.ias.secondscreen.activities;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import su.ias.secondscreen.R;

public class SplashScreenActivity extends Activity {
	
	private Handler handler;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        
        handler = new Handler();

        //setContentView(R.layout.main);
    }



    protected void loadContentView() {
        Log.i("@", "onLoadContentview");
    }
}
