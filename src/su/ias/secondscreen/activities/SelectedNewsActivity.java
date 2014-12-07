package su.ias.secondscreen.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;
import su.ias.secondscreen.R;
import su.ias.secondscreen.activities.fragments.NewsFragment;
import su.ias.secondscreen.adapters.NewsImagesPagerAdapter;
import su.ias.secondscreen.data.NewsData;
import su.ias.secondscreen.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 14.05.2014
 * Time: 14:58
 */
public class SelectedNewsActivity extends ActionBarActivity  {

    private NewsData articleData;

    private TextView titleTV;
    private TextView dateTV;
    private TextView articleTextTV;
    private ViewPager imagesVP;
    private VideoView videoView;
    private ListView projectsLV;
    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selected_article);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setBackgroundDrawable(getResources().getDrawable(R.color.news_header_color));

        articleData = (NewsData) getIntent().getSerializableExtra(NewsFragment.SELECTED_ARTICLE);
        pos = getIntent().getIntExtra(NewsFragment.SELECTED_POS, 0);


        titleTV = (TextView) findViewById(R.id.article_title);
        titleTV.setText(articleData.title);

        dateTV = (TextView) findViewById(R.id.news_date);
        dateTV.setText(Utils.getStrDateFromLong(articleData.date * 1000 , "dd.MM.yyyy"));

        articleTextTV = (TextView) findViewById(R.id.selected_article_text);
        articleTextTV.setText(articleData.text);

        imagesVP  = (ViewPager) findViewById(R.id.news_viewpager);
        imagesVP.setAdapter(new NewsImagesPagerAdapter(this,pos, articleData.imagesArr));



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
                sendBroadcast(new Intent(MainActivity.CLOSE_EVENT));
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
