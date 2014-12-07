package su.ias.secondscreen.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import su.ias.secondscreen.R;
import su.ias.secondscreen.activities.fragments.ActorsFragment;
import su.ias.secondscreen.activities.fragments.VideosFragment;
import su.ias.secondscreen.adapters.ActorImagesPagerAdapter;
import su.ias.secondscreen.app.AppSingleton;
import su.ias.secondscreen.data.ActorData;
import su.ias.secondscreen.utils.BitmapDrawer;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 14.05.2014
 * Time: 14:58
 */
public class SelectedActorActivity extends ActionBarActivity  {

    private ActorData actorData;

    private TextView nameTV;
//    private TextView dateTV;
    private TextView actorTextTV;
    private ViewPager imagesVP;
    private VideoView videoView;
    private ImageView previewImg;

    private RelativeLayout videoPreviewContainer;

    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selected_actor);

        actorData = (ActorData) getIntent().getSerializableExtra(ActorsFragment.SELECTED_ACTOR);
        pos = getIntent().getIntExtra(ActorsFragment.SELECTED_POS, 0);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(actorData.name);


        String clrStr = "#"+ AppSingleton.getInstance().currentProjTheme.actionBarBcgColor;
        mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(clrStr)));

        nameTV = (TextView) findViewById(R.id.actor_name);
        nameTV.setText(actorData.name);


        actorTextTV = (TextView) findViewById(R.id.selected_article_text);
        actorTextTV.setText(actorData.text);

        imagesVP  = (ViewPager) findViewById(R.id.news_viewpager);
        imagesVP.setAdapter(new ActorImagesPagerAdapter(this,pos, actorData.imagesArr));

        previewImg = (ImageView) findViewById(R.id.video_preview_img);
        final String url = actorData.videoPreivewUrl;
        final String path = getCacheDir() + "/" + "actor_video_preview" +"_"+ AppSingleton.getInstance().currentQR+ "_"+pos+".png";
        Log.i("@", "actor "+path);
        videoPreviewContainer = (RelativeLayout) findViewById(R.id.video_container);

        if(actorData.videoUrl != null) {

            final ImageView previewImg = (ImageView) findViewById(R.id.video_preview_img);
//            final String url = actorData.videoPreivewUrl;
//            final String path = getCacheDir() + "/" + "actor_videos_preview" + "_" + pos + ".png";
            ViewTreeObserver observer = previewImg.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    previewImg.getViewTreeObserver().removeOnPreDrawListener(this);
                    BitmapDrawer.INSTANCE.drawBitmap(path, url, previewImg, BitmapDrawer.DRAW_FILL, true, true);
                    return false;
                }

            });


            videoPreviewContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SelectedActorActivity.this, SelectedVideoActivity.class)
                            .putExtra(VideosFragment.SELECTED_VIDEO, actorData.videoUrl));
                }
            });

        } else {
            videoPreviewContainer.setVisibility(View.GONE);
        }




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
                sendBroadcast(new Intent(MainActivity.CLOSE_EVENT));
                return true;

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



}
