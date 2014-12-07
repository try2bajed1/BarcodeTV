package su.ias.secondscreen.activities.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import su.ias.secondscreen.R;
import su.ias.secondscreen.activities.MainActivity;
import su.ias.secondscreen.activities.SelectedVideoActivity;
import su.ias.secondscreen.data.VideoData;
import su.ias.secondscreen.utils.BitmapDrawer;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 24.03.2014
 * Time: 12:54
 */


public class VideosFragment extends BaseFragment {

    public static final String SELECTED_VIDEO = "selected_video";

    private ListView videosLV;

    @Override
    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);

        extraData = getArguments().getString(MainActivity.RECIEVED_DATA, "");
    }



    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {

//        return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_single_list, container, false);
        videosLV = (ListView) view.findViewById(R.id.content_list);

        return view;
    }




    @Override
    public void onResume() {
        super.onResume();

        if(!extraData.equals("") && videosLV.getAdapter() == null){
            try {
                fillList(new JSONObject(extraData) );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }





    private void fillList(JSONObject jsonObject) {

        final ArrayList<VideoData> videosArr = new ArrayList<VideoData>();

        try {

            JSONArray jsonItems = jsonObject.getJSONArray("items");
            for (int i = 0; i < jsonItems.length(); i++) {
                VideoData videoData = getVideoFromJson( jsonItems.getJSONObject(i).getJSONObject("video"));
                videosArr.add(videoData);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        videosLV.setAdapter(new VideosAdapter(videosArr));
        videosLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), SelectedVideoActivity.class);
                intent.putExtra(SELECTED_VIDEO, videosArr.get(position).videoUrl);
                startActivity(intent);
            }
        });



    }



    public VideoData getVideoFromJson(JSONObject videoJson) {

        VideoData singleVideo = new VideoData();
        Log.i("@", videoJson.toString());
        try {

            singleVideo.title = videoJson.getString("title");
            singleVideo.videoUrl = videoJson.getString("link");

            JSONObject videoImageJson = videoJson.getJSONObject("images");
            singleVideo.videoPreivewUrl = videoImageJson.getString((String) videoImageJson.keys().next());


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return singleVideo;
    }




    @Override
    public void onPause() {
        super.onPause();
    }



    @Override
    public void onDestroy() {


        super.onDestroy();
    }


    private class VideosAdapter extends BaseAdapter {

        private ArrayList<VideoData> videosArr;
        private LayoutInflater layoutInflater;


        private VideosAdapter(ArrayList<VideoData> videosArr) {
            this.videosArr = videosArr;
            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE); //LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return videosArr.size();
        }

        @Override
        public VideoData getItem(int position) {
            return videosArr.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.layout_videos_list_item, null);
            }

            final VideoData videoData = videosArr.get(position);

            TextView videoTitleTV = (TextView) convertView.findViewById(R.id.video_title);
            videoTitleTV.setText(videoData.title);


            final ImageView previewImg = (ImageView) convertView.findViewById(R.id.video_preview_img);
            final String url = videoData.videoPreivewUrl;
            final String path = getActivity().getCacheDir() + "/" + "videos_preview" + "_" + position + ".png";

            ViewTreeObserver observer = previewImg.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    previewImg.getViewTreeObserver().removeOnPreDrawListener(this);
                    BitmapDrawer.INSTANCE.drawBitmap(path, url, previewImg, BitmapDrawer.DRAW_FILL, true, true);
                    return false;
                }

            });

            return convertView;
        }
    }

}
