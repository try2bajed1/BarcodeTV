package su.ias.secondscreen.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;
import su.ias.secondscreen.R;
import su.ias.secondscreen.activities.fragments.VideosFragment;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 14.05.2014
 * Time: 14:58
 */
public class SelectedVideoActivity extends Activity {

    private VideoView videoView;
    private String extraUrlStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selected_video);

        extraUrlStr = getIntent().getStringExtra(VideosFragment.SELECTED_VIDEO);
        if(extraUrlStr == null) finish();

        videoView = (VideoView) findViewById(R.id.video_player);
        MediaController mc = new MediaController(this);
        mc.setAnchorView(videoView);
        mc.setMediaPlayer(videoView);
        Uri uri = Uri.parse(extraUrlStr);
        videoView.setMediaController(mc);
        videoView.setVideoURI(uri);
        videoView.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_qr, menu);
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_qr:
                finish();
//                startActivity(new Intent(DataCentersInfoActivity.this,EngineersActivity.class));
                return true;

            case android.R.id.home:
//                startActivity(new Intent(this, NewsActivity.class));
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



}
