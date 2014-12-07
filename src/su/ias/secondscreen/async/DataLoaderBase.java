package su.ias.secondscreen.async;

import android.os.AsyncTask;
import su.ias.secondscreen.IListener;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 02.06.2014
 * Time: 12:48
 */

public class DataLoaderBase extends AsyncTask<String, Void, Boolean> {

    protected IListener listener = null;

    protected String loadedDataType; // project_json or project_menu_item_json
    protected String qrValue;


    protected static final String SERVER_URL = "http://***";
    protected static final String LOGIN = "android";
    protected static final String PASSWORD = "android";


    public static final String REQUEST = "request";
    public static final String ACTION = "action";
    public static final String PARAMS = "params";
    public static final String RESPONSE = "response";
    public static final String RESPONSE_HEADER = "response_header";
    public static final String RESPONSE_CODE = "response_code";
    public static final String RESPONSE_DESCR = "response_descr";



    public DataLoaderBase(IListener listener) {
        this.listener = listener;
    }


    @Override
    protected Boolean doInBackground(String... params) {
        return false;
    }


}
