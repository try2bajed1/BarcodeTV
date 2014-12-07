package su.ias.secondscreen.activities.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import su.ias.secondscreen.R;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 24.03.2014
 * Time: 12:54
 */


public class ErrorFragment extends Fragment {



    @Override
    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);
        //mode = getArguments().getInt(CARD_TYPE, 0);
    }



    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_single_list, container, false);

        return view;
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
