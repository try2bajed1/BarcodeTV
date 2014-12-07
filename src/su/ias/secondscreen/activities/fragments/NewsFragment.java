package su.ias.secondscreen.activities.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import su.ias.secondscreen.R;
import su.ias.secondscreen.activities.MainActivity;
import su.ias.secondscreen.activities.SelectedArticleActivity;
import su.ias.secondscreen.data.NewsData;
import su.ias.secondscreen.utils.BitmapDrawer;
import su.ias.secondscreen.utils.Utils;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 24.03.2014
 * Time: 12:54
 */


public class NewsFragment extends BaseFragment {

    public static final String SELECTED_ARTICLE = "S_A";
    public static final String SELECTED_POS = "S_POS";
    private ListView newsLV;
    private String qrStr;


    @Override
    public void onAttach(android.app.Activity activity) {

        super.onAttach(activity);

        extraData = getArguments().getString(MainActivity.RECIEVED_DATA, "");

    }




    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_single_list, container, false);
        newsLV = (ListView) view.findViewById(R.id.content_list);

        return view;
    }




    @Override
    public void onResume() {
        super.onResume();

        if(!extraData.equals("") && newsLV.getAdapter() == null){
            qrStr = ((MainActivity)getActivity()).qrCode;
            try {

                fillList(new JSONObject(extraData));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }



    public NewsData getArticleFromJson(JSONObject articleJson) {

        NewsData singleArticle = new NewsData();

        try {
            if(articleJson.has("date")) {
                singleArticle.date = articleJson.getLong("date");
            }
            singleArticle.title = articleJson.getString("title");
            singleArticle.text  = articleJson.getString("text");

            ArrayList<String> imagesArr = new ArrayList<String>();
            if(articleJson.has("images")) {
                JSONArray imgsJsonArr = articleJson.getJSONArray("images");
                for (int i = 0; i < imgsJsonArr.length(); i++) {
                    JSONObject singleImgJson = (JSONObject) imgsJsonArr.get(i);
                    String imgUrl = Utils.getSmallestImgURL(singleImgJson);
                    imagesArr.add(imgUrl);
                }
            }

            singleArticle.imagesArr = imagesArr;

            if(articleJson.has("video")) {
                JSONObject videoJson = (JSONObject) articleJson.get("video");
                singleArticle.videoUrl = videoJson.getString("link");

                JSONObject videoImageJson = videoJson.getJSONObject("images");

                singleArticle.videoPreivewUrl = videoImageJson.getString((String) videoImageJson.keys().next()); // берем 1й попавшийся ключ
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return singleArticle;
    }





    private void fillList(JSONObject jsonObject) {

        final ArrayList<NewsData> newsArr = new ArrayList<NewsData>();

        try {

            JSONArray jsonItems = jsonObject.getJSONArray("items");
            for (int i = 0; i < jsonItems.length(); i++) {
                NewsData article = getArticleFromJson((JSONObject) jsonItems.get(i));
                newsArr.add(article);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }



        newsLV.setAdapter(new NewsAdapter(newsArr));
        newsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), SelectedArticleActivity.class);
                intent.putExtra(SELECTED_ARTICLE, newsArr.get(position));
                intent.putExtra(SELECTED_POS, position);
                startActivity(intent);
            }
        });

        newsLV.setScrollY(lastY);
    }





    private class NewsAdapter extends BaseAdapter {

        private ArrayList<NewsData> newsArr;
        private LayoutInflater layoutInflater;
        private int lastPosition = -1;

        private NewsAdapter(ArrayList<NewsData> newsArr) {
            this.newsArr = newsArr;
            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE); //LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return newsArr.size();
        }

        @Override
        public NewsData getItem(int position) {
            return newsArr.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.layout_news_list_item, null);
            }

            final NewsData singleArticle = newsArr.get(position);


            TextView titleTV = (TextView) convertView.findViewById(R.id.article_title);
            titleTV.setText(singleArticle.title);

            TextView dateTV = (TextView) convertView.findViewById(R.id.news_date);
            if(newsArr.get(position).date != 0) {
                dateTV.setText(Utils.getStrDateFromLong(1000 * singleArticle.date, "dd.MM.yyyy"));
            } else {
                dateTV.setVisibility(View.GONE);
            }

            final ImageView photoImg = (ImageView) convertView.findViewById(R.id.news_img);
            if (singleArticle.imagesArr.size() > 0) {

                final String url = singleArticle.imagesArr.get(0);
                final String path = getActivity().getCacheDir() + "/" + "news_preview"  + "_" + position +"_" + qrStr + ".png";

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


            Animation animation = AnimationUtils.loadAnimation(getActivity(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
            convertView.startAnimation(animation);
            lastPosition = position;

            return convertView;
        }
    }

/*

    @Override
    public void setGridAdapter(ArrayList<UserCardItem> arr){
//        Log.i("@","setGridAdapter active");

        if (arr !=null && getActivity()!=null && !gridHasData()){
            cardsGridView.setAdapter(new CardsGridAdapter(getActivity(), getOnlyActive(arr)));
        }
    }


    private ArrayList<UserCardItem> getOnlyActive(ArrayList<UserCardItem> arr) {
        ArrayList<UserCardItem> filteredArr = new ArrayList<UserCardItem>();
        for (UserCardItem item : arr) {
            if(item.getStatus().equals("Активна")) filteredArr.add(item);
        }

        return  filteredArr;
    }
*/


    private int lastY;

    @Override
    public void onPause() {
        super.onPause();
        Log.i("@","news paused ");
        lastY = newsLV.getScrollY();
    }



    @Override
    public void onDestroy() {


        super.onDestroy();
    }

}
