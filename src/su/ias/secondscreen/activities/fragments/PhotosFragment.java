package su.ias.secondscreen.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import su.ias.secondscreen.R;
import su.ias.secondscreen.activities.MainActivity;
import su.ias.secondscreen.activities.SelectedPhotoActivity;
import su.ias.secondscreen.adapters.PhotosGridAdapter;
import su.ias.secondscreen.data.ImageData;
import su.ias.secondscreen.utils.Utils;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 24.03.2014
 * Time: 12:54
 */


public class PhotosFragment extends BaseFragment {


    public static final String SELECTED_PHOTO_URL = "sel_ph_url";
    private GridView photosGrid;



    @Override
    public void onAttach(android.app.Activity activity) {

        super.onAttach(activity);

        extraData = getArguments().getString(MainActivity.RECIEVED_DATA, "");


    }




    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_photos, container, false);
        photosGrid = (GridView) view.findViewById(R.id.photos_grid);
        return view;
    }




    @Override
    public void onResume() {
        super.onResume();

        if(!extraData.equals("")){
            try {
                fillGrid(new JSONObject(extraData));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }



    private void fillGrid(JSONObject jsonObject) {

        final ArrayList<ImageData> imageDataArr = new ArrayList<ImageData>();

        try {

            JSONArray jsonItems = jsonObject.getJSONArray("items");
            for (int i = 0; i < jsonItems.length(); i++) {
                ImageData imageData = getImageDataFromJson(jsonItems.getJSONObject(i));
                imageDataArr.add(imageData);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        String qr = ((MainActivity)getActivity()).qrCode;

        photosGrid.setAdapter(new PhotosGridAdapter(getActivity(),qr, imageDataArr));
        photosGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), SelectedPhotoActivity.class);
                intent.putExtra(SELECTED_PHOTO_URL, imageDataArr.get(position).fullSizeUrl);
                startActivity(intent);
            }
        });
    }



    private ImageData getImageDataFromJson(JSONObject imageJson) {
        ImageData imageData = new ImageData();
        imageData.previewUrl = Utils.getSmallestImgURL(imageJson);
        imageData.fullSizeUrl = Utils.getBiggestImgURL(imageJson);

        return  imageData;
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
