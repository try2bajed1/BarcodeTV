package su.ias.secondscreen.async;

import android.util.Base64;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import su.ias.secondscreen.IListener;
import su.ias.secondscreen.activities.MainActivity;
import su.ias.secondscreen.app.AppSingleton;
import su.ias.secondscreen.data.AdvData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 02.06.2014
 * Time: 12:48
 */

public class SectionDataLoader extends DataLoaderBase {

    protected String loadedDataType; // project_json or project_menu_item_json
    //protected String actionName;
    //private JSONObject headerJson;
    protected JSONObject jsonResponse;

    private int sectionId;
    private String selectedSectionType;


    public static final String QR = "qr";
    public static final String SECTION = "section";

    public static final String REQUEST_ACTION_GET_PROJ_BY_QR = "get_content_by_qr";
    public static final String REQUEST_ACTION_SECTION = "get_section_by_qr";


    /*  Constructor added  WebServiceListener here */
    public SectionDataLoader(IListener listener) {
        super(listener);
    }


    @Override
    protected Boolean doInBackground(String... params) {

        boolean result = false;

        try {

            qrValue = params[0];
            sectionId = Integer.parseInt(params[1]);
            selectedSectionType = params[2];

            DefaultHttpClient client = new DefaultHttpClient();
            client.getParams().setBooleanParameter("http.protocol.expect-continue", false);

            HttpPost httppost = new HttpPost(SERVER_URL);
            httppost.setHeader("Authorization", "Basic " + Base64.encodeToString((LOGIN + ":" + PASSWORD).getBytes(), Base64.NO_WRAP));

            StringEntity entity = new StringEntity(getHeaderEntity(qrValue, sectionId), HTTP.UTF_8);
            entity.setContentType("application/json;charset=UTF-8");
            httppost.setEntity(entity);
            HttpResponse response = client.execute(httppost);

            BufferedReader ins = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line;

            while ((line = ins.readLine()) != null) {
                sb.append(line);
            }
            line = sb.toString();

//          Log.i("@", "ответ от сервера  " + line);

            jsonResponse = new JSONObject(line);

            result = true;

        } catch (UnsupportedEncodingException e) {
            Log.e("@", "error: " + e.toString());
        } catch (ClientProtocolException e) {
            Log.e("@", "error: " + e.toString());
        } catch (IOException e) {
            Log.e("@", "error: " + e.toString());
        } catch (JSONException e) {
            Log.e("@", "error: " + e.toString());
        }

        return result;
    }



    @Override
    protected void onPostExecute(Boolean result) {

        if (result) {
            try {
                JSONObject responseHeaderJson = jsonResponse.getJSONObject(RESPONSE_HEADER);
                int responseCode = responseHeaderJson.getInt(RESPONSE_CODE);
                if (responseCode == 0) {

                    JSONObject responsePart = (JSONObject) jsonResponse.get(RESPONSE);
                    parseData(responsePart);

                    listener.completeHandler(responsePart.toString());

                } else {
                    listener.errorHandler("");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            listener.errorHandler("");
        }
    }


/*  {

    "response_header": {
        "response_code": 0,
        "response_desc": "OK"
    },

    "response": {
        "items": [{
            "title": "Физрук. 26-я серия",
            "date_start": "1400513400",
            "date_end": "1400515200" },    */

    private void parseData(JSONObject responsePartJson) throws JSONException {

        if (selectedSectionType.equals(MainActivity.CONTENT_TYPE_SHEDULE)) {
            AppSingleton.getInstance().getDBAdapter().saveShedule(qrValue, responsePartJson.getJSONArray("items"));
        }


        if (selectedSectionType.equals(MainActivity.CONTENT_TYPE_NEWS)) {

        }

    }


    private void parseShedulerData(JSONObject jsonObject) {

        ArrayList<AdvData> advDataArr = new ArrayList<AdvData>();

        ArrayList<Integer> eachAdvDayOfYearArr = new ArrayList<Integer>();

        try {

            JSONArray itemsArr = jsonObject.getJSONArray("items");
            int len = itemsArr.length();

            Calendar calendar = Calendar.getInstance();

            for (int i = 0; i < len; i++) {

                AdvData advData = new AdvData();
                JSONObject dataJson = itemsArr.getJSONObject(i);
                advData.title = dataJson.getString("title");
                advData.startDateMillsSecs = 1000 * dataJson.getLong("date_start"); // unixtime gives seconds after 1970
                advData.endDateMillsSecs   = 1000 * dataJson.getLong("date_end");
                calendar.setTimeInMillis(advData.startDateMillsSecs);
                advData.dayOfYear = calendar.get(Calendar.DAY_OF_YEAR); //convertTimeStampToDayOfYear(data.startDateMillsSecs);
                advDataArr.add(advData);

                eachAdvDayOfYearArr.add(advData.dayOfYear);

            }

        } catch (JSONException e) {
            e.printStackTrace();
            eachAdvDayOfYearArr.clear();
        }


        Collections.sort(advDataArr);
//        shedulerPager.setAdapter(new ShedulerPageAdapter(getActivity()));

    }


    private String getHeaderEntity(String qrStr, int sectionIndex)  {

        JSONObject postJson = new JSONObject();
        JSONObject reqJson  = new JSONObject();

        try {
            reqJson.put(ACTION, REQUEST_ACTION_SECTION );
            JSONObject params = new JSONObject();
            params.put(QR, qrStr);
            params.put(SECTION, sectionIndex);
            reqJson.put(PARAMS, params);
            postJson.put(REQUEST, reqJson);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return postJson.toString();
    }


}
