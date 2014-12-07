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
import su.ias.secondscreen.app.AppSingleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 02.06.2014
 * Time: 12:48
 */

public class ProjectDataLoader extends DataLoaderBase {

    protected String loadedDataType; // project_json or project_menu_item_json
    //protected String actionName;
//    private JSONObject headerJson;
    protected JSONObject jsonResponse;


    protected String errorCode;
    protected String errorMessage;


/*
    public static final String REQUEST = "request";
    public static final String ACTION = "action";
    public static final String PARAMS = "params";
    public static final String RESPONSE = "response";
    public static final String RESPONSE_HEADER = "response_header";
    public static final String RESPONSE_CODE = "response_code";
    public static final String RESPONSE_DESCR = "response_descr";
*/

    public static final String QR = "qr";
    public static final String SECTION = "section";

    public static final String REQUEST_ACTION_GET_PROJ_BY_QR = "get_content_by_qr";
    public static final String REQUEST_ACTION_SECTION = "get_section_by_qr";



    /*  Constructor added  WebServiceListener here */
    public ProjectDataLoader(IListener listener) {
        super(listener);
    }



    @Override
    protected Boolean doInBackground(String... params) {

        boolean result = false;

        try {

            qrValue = params[0];

            DefaultHttpClient client = new DefaultHttpClient();
            client.getParams().setBooleanParameter("http.protocol.expect-continue", false);

            HttpPost httppost = new HttpPost(SERVER_URL);
            httppost.setHeader("Authorization", "Basic " + Base64.encodeToString((LOGIN + ":" + PASSWORD).getBytes(), Base64.NO_WRAP));

            StringEntity entity = new StringEntity(getHeaderEntity(qrValue), HTTP.UTF_8);
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

                    saveProjectDataToDB(responsePart);
                    listener.completeHandler("");

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



    /*
        {
            "request":{
                    "action":"get_section_by_qr",
                    "params":{"qr":"qr_value"}
            }
        }

        */
    private String getHeaderEntity(String qrStr)  {

        JSONObject postJson = new JSONObject();
        JSONObject reqJson  = new JSONObject();
        try {
            reqJson.put(ACTION, REQUEST_ACTION_GET_PROJ_BY_QR);
            JSONObject params = new JSONObject();
            params.put(QR, qrStr); // пока неизвестно в каком формате some_qr
            reqJson.put(PARAMS, params);
            postJson.put(REQUEST, reqJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return postJson.toString();
    }


    private void saveProjectDataToDB(JSONObject projectJson) throws JSONException {
        //Log.i("@",projectJson.toString());

        //save common info
        String projTitle = projectJson.getString("title");
        String imageInfo = projectJson.get("image").toString();
        String fsBannerInfo = "";
        if(projectJson.has("main_banner")) {
            fsBannerInfo = projectJson.get("main_banner").toString();
        }
        AppSingleton.getInstance().getDBAdapter().saveCommonProjectData(qrValue, projTitle, imageInfo, fsBannerInfo);

        // save colors theme:
        JSONObject themeJson = projectJson.getJSONObject("theme");
        String themeName = themeJson.getString("name");
        String bgMenu = themeJson.getString("bg_menu");
        String bgMenuSelected = themeJson.getString("bg_cell");
        String bgHead = themeJson.getString("bg_head");
        String menuItemFontColor = themeJson.getString("font_color_menu");
        String abTitleFontColor = themeJson.getString("font_color_head");
        String selectedItemFontColor = themeJson.getString("font_color_cell");
        String dividerColor = themeJson.getString("cell_separator_color");
        String menuBannerJson ="";

        if(projectJson.has("menu_banner")){
            menuBannerJson = projectJson.get("menu_banner").toString();
        }

        AppSingleton.getInstance().getDBAdapter().saveProjectAppearanceTheme(qrValue, themeName, bgMenu,bgMenuSelected, bgHead, menuItemFontColor,
                abTitleFontColor, selectedItemFontColor,dividerColor, menuBannerJson);


        //save menu items:
        JSONArray itemsArr = projectJson.getJSONArray("items");
        AppSingleton.getInstance().getDBAdapter().saveProjectMenuItems(qrValue, itemsArr);

    }


}
