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
import su.ias.secondscreen.activities.SelectedActorActivity;
import su.ias.secondscreen.data.ActorData;
import su.ias.secondscreen.utils.BitmapDrawer;
import su.ias.secondscreen.utils.Utils;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 24.03.2014
 * Time: 12:54
 */


public class ActorsFragment extends BaseFragment {


    public static final String SELECTED_ACTOR = "SEL_ACTOR";
    public static final String SELECTED_POS = "SEL_POS"; // for cahe

    private ListView actorsLV;
    private String qrStr;

    @Override
    public void onAttach(android.app.Activity activity) {

        super.onAttach(activity);
        extraData = getArguments().getString(MainActivity.RECIEVED_DATA, "");

    }



    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_single_list, container, false);

        actorsLV = (ListView) view.findViewById(R.id.content_list);

        return view;
    }




    @Override
    public void onResume() {
        super.onResume();

        if(!extraData.equals("") && actorsLV.getAdapter() == null){
            try {
                qrStr = ((MainActivity)getActivity()).qrCode;
                fillList(new JSONObject(extraData));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }



    private void fillList(JSONObject jsonObject) {

        final ArrayList<ActorData> actorsArr = new ArrayList<ActorData>();

        try {

            JSONArray jsonItems = jsonObject.getJSONArray("items");
            for (int i = 0; i < jsonItems.length(); i++) {
                ActorData actor = getActorFromJson((JSONObject) jsonItems.get(i));
                actorsArr.add(actor);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        actorsLV.setAdapter(new ActorsAdapter(actorsArr));
        actorsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), SelectedActorActivity.class);
                intent.putExtra(SELECTED_ACTOR, actorsArr.get(position));
                intent.putExtra(SELECTED_POS, position);
                startActivity(intent);
            }
        });

    }




    public ActorData getActorFromJson(JSONObject actorJson) {

        ActorData singleActor = new ActorData();
        try {

//            singleActor.date  = actorJson.getLong("date");
            singleActor.name = actorJson.getString("title");
            singleActor.text  = actorJson.getString("text");

            ArrayList<String> imagesArr = new ArrayList<String>();
            if(actorJson.has("images")) {
                JSONArray imgsJsonArr = actorJson.getJSONArray("images");
                for (int i = 0; i < imgsJsonArr.length(); i++) {
                    JSONObject singleImgJson = (JSONObject) imgsJsonArr.get(i);
                    String imgUrl = Utils.getSmallestImgURL(singleImgJson);
                    imagesArr.add(imgUrl);
                }
            }
            singleActor.imagesArr = imagesArr;

            if(actorJson.has("video")) {
                JSONObject videoJson = (JSONObject) actorJson.get("video");
                singleActor.videoUrl = videoJson.getString("link");

                JSONObject videoImageJson = videoJson.getJSONObject("images");
                singleActor.videoPreivewUrl = videoImageJson.getString((String) videoImageJson.keys().next());
                Log.i("@", " singleActor.videoPreivewUrl "+singleActor.videoPreivewUrl);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return singleActor;
    }








    @Override
    public void onPause() {
        super.onPause();
    }



    @Override
    public void onDestroy() {


        super.onDestroy();
    }



    private class ActorsAdapter extends BaseAdapter {

        private ArrayList<ActorData> newsArr;
        private LayoutInflater layoutInflater;


        private ActorsAdapter(ArrayList<ActorData> newsArr) {
            this.newsArr = newsArr;
            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE); //LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return newsArr.size();
        }

        @Override
        public ActorData getItem(int position) {
            return newsArr.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.layout_actors_list_item, null);
            }

            final ActorData singleActor = newsArr.get(position);

            TextView actorNameTV = (TextView) convertView.findViewById(R.id.article_title);
            actorNameTV.setText(singleActor.name);

            final ImageView photoImg = (ImageView) convertView.findViewById(R.id.actor_img);

            if (singleActor.imagesArr.size() > 0) {

                final String url = singleActor.imagesArr.get(0);
                final String path = getActivity().getCacheDir() + "/" + "actors_preview"  + "_" + position +"_"+qrStr+ ".png";

                ViewTreeObserver observer = photoImg.getViewTreeObserver();
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {
                        photoImg.getViewTreeObserver().removeOnPreDrawListener(this);
                        BitmapDrawer.INSTANCE.drawBitmap(path, url, photoImg, BitmapDrawer.DRAW_FILL, true, true);
                        return false;
                    }

                });
            }

            return convertView;
        }
    }

}
