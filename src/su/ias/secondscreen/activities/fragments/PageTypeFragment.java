package su.ias.secondscreen.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import su.ias.secondscreen.R;
import su.ias.secondscreen.activities.MainActivity;
import su.ias.secondscreen.activities.SelectedVideoActivity;
import su.ias.secondscreen.adapters.ActorImagesPagerAdapter;
import su.ias.secondscreen.app.AppSingleton;
import su.ias.secondscreen.data.ActorData;
import su.ias.secondscreen.utils.BitmapDrawer;
import su.ias.secondscreen.utils.Utils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 24.03.2014
 * Time: 12:54
 */


public class PageTypeFragment extends BaseFragment {


    private TextView nameTV;
    private TextView actorTextTV;
    private ViewPager imagesVP;
    private RelativeLayout videoPreviewContainer;
    private ImageView previewImg;

    @Override
    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);
        //mode = getArguments().getInt(CARD_TYPE, 0);
        extraData = getArguments().getString(MainActivity.RECIEVED_DATA, "");
    }



    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_page, container, false);

        nameTV = (TextView) view.findViewById(R.id.actor_name);
        actorTextTV = (TextView) view.findViewById(R.id.selected_article_text);
        imagesVP  = (ViewPager) view.findViewById(R.id.news_viewpager);
        previewImg = (ImageView) view.findViewById(R.id.video_preview_img);
        videoPreviewContainer = (RelativeLayout) view.findViewById(R.id.video_container);

        return view;
    }




    @Override
    public void onResume() {
        super.onResume();

        if(!extraData.equals("") ){

            // qrStr = ((MainActivity)getActivity()).qrCode;

            try {
                fillList(new JSONObject(extraData));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }



    private ActorData actorData;

    private void fillList(JSONObject jsonObject) {

        try {
            JSONArray items = jsonObject.getJSONArray("items");
            JSONObject pageJson = items.getJSONObject(0);
            actorData = getActorFromJson(pageJson);
        } catch (JSONException e) {
            e.printStackTrace();
            actorData = new ActorData();
        }

        //actorData = (ActorData) getIntent().getSerializableExtra(ActorsFragment.SELECTED_ACTOR);
        //pos = getIntent().getIntExtra(ActorsFragment.SELECTED_POS, 0);

        int rand = new Random().nextInt(100);

        nameTV.setText(actorData.name);
//        actorTextTV.setText(actorData.text);
        actorTextTV.setText(Html.fromHtml(actorData.text));
//        tv.setMovementMethod(LinkMovementMethod.getInstance());
        actorTextTV.setMovementMethod(LinkMovementMethod.getInstance());// clickable <a href />
        imagesVP.setAdapter(new ActorImagesPagerAdapter(getActivity(), rand, actorData.imagesArr));

        final String url = actorData.videoPreivewUrl;
        final String path = getActivity().getCacheDir() + "/" + "page_video_preview" +"_"+ AppSingleton.getInstance().currentQR+ "_"+rand+".png";


        videoPreviewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SelectedVideoActivity.class)
                        .putExtra(VideosFragment.SELECTED_VIDEO, actorData.videoUrl));
            }
        });


        if(actorData.videoUrl != null) {

            //final ImageView previewImg = (ImageView) findViewById(R.id.video_preview_img);

            ViewTreeObserver observer = previewImg.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    previewImg.getViewTreeObserver().removeOnPreDrawListener(this);
                    BitmapDrawer.INSTANCE.drawBitmap(path, url, previewImg, BitmapDrawer.DRAW_FILL, true, true);
                    return false;
                }

            });

        } else {
            videoPreviewContainer.setVisibility(View.GONE);
        }
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
                Log.i("@", " singleActor.videoPreivewUrl " + singleActor.videoPreivewUrl);
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

}
