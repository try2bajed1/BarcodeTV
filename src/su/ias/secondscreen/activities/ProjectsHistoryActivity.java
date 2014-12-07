package su.ias.secondscreen.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import org.json.JSONException;
import org.json.JSONObject;
import su.ias.secondscreen.R;
import su.ias.secondscreen.app.AppSingleton;
import su.ias.secondscreen.components.ResizableImageView;
import su.ias.secondscreen.db.DBAdapter;
import su.ias.secondscreen.utils.Utils;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 14.05.2014
 * Time: 14:58
 */
public class ProjectsHistoryActivity extends ActionBarActivity  {


    private ListView projectsLV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_qr_history);

        projectsLV = (ListView) findViewById(R.id.projects_list);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
//        mActionBar.setTitle("Проекты");
//        mActionBar.setBackgroundDrawable(getResources().getDrawable(R.color.news_header_color));



       /*
        {

        "400x300":"http:\/\/secondscreen.hosting2.ias.su\/media\/project\/2ff9f672bbc7266c69050de304f02bb818d2904b_400x308.jpg",
        "800x600":"http:\/\/secondscreen.hosting2.ias.su\/media\/project\/2ff9f672bbc7266c69050de304f02bb818d2904b.jpg",
        "2048x1536":"http:\/\/secondscreen.hosting2.ias.su\/media\/project\/2ff9f672bbc7266c69050de304f02bb818d2904b_2048x1600.jpg"
        }
         */


        Cursor projectsCursor = AppSingleton.getInstance().getDBAdapter().getScannedQRHistoryCursor();
        projectsLV.setAdapter(new ProjectsCursorAdapter(ProjectsHistoryActivity.this, projectsCursor));
        projectsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor localCursor =  ((CursorAdapter)projectsLV.getAdapter()).getCursor();
                localCursor.moveToPosition(position);

                String qrCode = localCursor.getString(localCursor.getColumnIndex(DBAdapter.FLD_QR));
                AppSingleton.getInstance().currentQR = qrCode;

                startActivity(new Intent(ProjectsHistoryActivity.this, FullScreenBannerActivity.class)
                                    .putExtra(FullScreenBannerActivity.QR, qrCode));
                finish();

            }
        });

        AppSingleton.getInstance().getDBAdapter().getSortedByDayAdvsArr("fizruk");
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
                startActivity(new Intent(ProjectsHistoryActivity.this,QRReaderActivity.class));
                return true;

            case android.R.id.home:
//                startActivity(new Intent(this, NewsActivity.class));
                //finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }





    public class ProjectsCursorAdapter extends CursorAdapter {

        private final LayoutInflater mInflater;
        private final Context mContext;
        //private final Cursor mCursor

        private ImageView projectImg;



        public ProjectsCursorAdapter(Context context, Cursor c) {
            super(context, c);
            mInflater = LayoutInflater.from(context);
            mContext = context;
        }



        @Override
        public void bindView(View view, Context context, final Cursor cursor) {

            //{FLD_ID, FLD_QR, FLD_PROJ_TITLE,FLD_PROJECT_IMAGE_INFO}

            projectImg = (ResizableImageView) view.findViewById(R.id.project_image);

            final String imageInfo = cursor.getString(cursor.getColumnIndex(DBAdapter.FLD_PROJECT_IMAGE_INFO));
            try {

                final String path = getCacheDir() + "/proj_image__" + ".png";
                final String minResImgUrl = Utils.getSmallestImgURL(new JSONObject(imageInfo));

                //BitmapDrawer.INSTANCE.drawBitmap(path, minResImgUrl, projectImg, BitmapDrawer.DRAW_FILL, true, true);
                new DownloadImageTask(projectImg).execute(minResImgUrl);
//                Log.i("@", "url "+minResImgUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.projects_list_item, parent, false);
            return view;
        }

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
            //((TouchImageView)bmImage).setZoom(1f);
        }
    }



}
