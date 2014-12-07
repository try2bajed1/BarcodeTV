package su.ias.secondscreen.activities.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 24.03.2014
 * Time: 12:54
 */



public class BaseFragment extends Fragment {

    protected String extraData;



    @Override
    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);
        //mode = getArguments().getInt(CARD_TYPE, 0);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
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
