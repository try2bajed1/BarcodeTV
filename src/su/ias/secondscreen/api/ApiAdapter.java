package su.ias.secondscreen.api;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import su.ias.secondscreen.app.AppSingleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class ApiAdapter {

    public static final String SERVER_URL = "http://***";
    public static final String LOGIN = "**";
    public static final String PASSWORD = "**";


    public static final String REQUEST = "request";
    public static final String ACTION = "action";
    public static final String PARAMS = "params";
    public static final String RESPONSE = "response";
    public static final String RESPONSE_HEADER = "response_header";
    public static final String RESPONSE_CODE = "response_code";
    public static final String RESPONSE_DESCR = "response_descr";

    public static final String QR = "qr";
    public static final String SECTION = "section";

    public static final String REQUEST_ACTION_GET_PROJ_BY_QR = "get_content_by_qr";
    public static final String REQUEST_ACTION_SECTION = "get_section_by_qr";


    public static final String VALUE_OK = "ok";
    public static final String VALUE_ERROR = "error";

    public static final String ACTION_REQUEST_COMPLETED = "ACTION_REQUEST_COMPLETED";
    public static final String PARTNERS_SAVED_TO_DB = "partners_saved_to_db";

    public static final String REQUEST_ACTION = "REQUEST_ACTION";
    public static final String REQUEST_STATUS = "REQUEST_STATUS";
    public static final String REQUEST_CONTENT = "REQUEST_CONTENT";


    public static final String LOADED_TYPE_PROJECT = "LOADED_TYPE_PROJECT";
    public static final String LOADED_TYPE_PROJECT_ITEM = "LOADED_TYPE_PROJECT_ITEM";



    private static ApiAdapter adapter;
    private Context context;


    public ApiAdapter() {
        context = AppSingleton.getInstance().getApplicationContext();
    }



    public static ApiAdapter getInstance() {
        if (adapter == null) {
            adapter = new ApiAdapter();
        }
        return adapter;
    }



    /**
     * Метод создает запрос с набором параметров не более 1-го уровня
     * вложенности. параметры передаются в виде массива строк из 2х элементов:
     * 1й элемент - имя параметра, 2й - значение
     *
     * @param params
     *
     */
    private void makeSimpleRequest(String[]... params) {
        JSONObject jsonReq = new JSONObject();
        try {
            for (String[] strings : params) {
                jsonReq.put(strings[0], strings[1]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new RequestMaker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jsonReq.toString());

    }



/*
    {
        "request":{
                "action":"get_section_by_qr",
                "params":{
                    "qr":"пористая мочалка выпила почти всю воду из ванны моего соседа",
                    "section":"1" }
        }
    }

    */



    public JSONObject getSectionReqJson(String qrStr,int sectionIndex)  {

        JSONObject postJson = new JSONObject();
        JSONObject reqJson = new JSONObject();

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

        return postJson;
    }





    public void getSection(String qrStr, int sectionIndex) {
        new RequestMaker().execute(REQUEST_ACTION_SECTION, getSectionReqJson(qrStr, sectionIndex).toString());
    }




    public JSONObject getContentReqJson(String qrStr)  {

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

        return postJson;
    }




    public void getContent(String qrStr) {
        new RequestMaker().execute(REQUEST_ACTION_GET_PROJ_BY_QR, getContentReqJson(qrStr).toString());
    }




    private class RequestMaker extends AsyncTask<String, Void, Boolean> {

        protected String loadedDataType; // project_json or project_menu_item_json
        protected String actionName;
        private JSONObject headerJson;
        protected JSONObject jsonResponse;

        protected DefaultHttpClient client;

        protected String errorCode;
        protected String errorMessage;



        @Override
        protected Boolean doInBackground(String... params) {

            boolean result = false;

            try {

                actionName = params[0];
                headerJson = new JSONObject(params[1]);

//              Log.i("@","headerJson " + headerJson);

                client = new DefaultHttpClient();
                client.getParams().setBooleanParameter("http.protocol.expect-continue", false);

                HttpPost httppost = new HttpPost(SERVER_URL);
                httppost.setHeader("Authorization", "Basic " + Base64.encodeToString((LOGIN + ":" + PASSWORD).getBytes(), Base64.NO_WRAP));

                StringEntity entity = new StringEntity(headerJson.toString(), HTTP.UTF_8);
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

//                Log.i("@", "ответ от сервера  " + line);

                jsonResponse = new JSONObject(line);

                result = true;

                //processResponce();
                //result = processResponce();

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

            Intent resultIntent = new Intent();

            if (result) {

                try {

                    JSONObject responseHeaderJson = jsonResponse.getJSONObject(RESPONSE_HEADER);
                    int responseCode = responseHeaderJson.getInt(RESPONSE_CODE);
                    if (responseCode == 0) {

                        JSONObject responsePart = (JSONObject) jsonResponse.get(RESPONSE);

                        if (actionName.equals(REQUEST_ACTION_GET_PROJ_BY_QR)) {
                            saveProjectDataToDB(responsePart);
                            resultIntent.setAction(actionName);
                        }

                        if (actionName.equals(REQUEST_ACTION_SECTION)) {
                            resultIntent.setAction(actionName);
                            resultIntent.putExtra(REQUEST_CONTENT, responsePart.toString());
                        }

                    } else {
                        resultIntent.setAction(VALUE_ERROR);
                        resultIntent.putExtra(REQUEST_CONTENT, responseHeaderJson.getString(RESPONSE_DESCR));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                resultIntent.setAction(VALUE_ERROR);
            }

            context.sendBroadcast(resultIntent);
        }




        private void saveProjectDataToDB(JSONObject projectJson) throws JSONException {
//            Log.i("@",projectJson.toString());

            //save common info
            JSONObject requestJson = (JSONObject)headerJson.get("request");
            String qr = ((JSONObject)requestJson.get("params")).getString(QR);

            String projTitle = projectJson.getString("title");
            String imageInfo = projectJson.get("image").toString();
            String fsBannerInfo = "";
            if(projectJson.has("main_banner")) {
                fsBannerInfo = projectJson.get("main_banner").toString();
            }
            AppSingleton.getInstance().getDBAdapter().saveCommonProjectData(qr, projTitle, imageInfo, fsBannerInfo);

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

            AppSingleton.getInstance().getDBAdapter().saveProjectAppearanceTheme(qr, themeName, bgMenu,bgMenuSelected, bgHead, menuItemFontColor,
                                                                                 abTitleFontColor, selectedItemFontColor,dividerColor, menuBannerJson);


            //save menu items:
            JSONArray itemsArr = projectJson.getJSONArray("items");
            AppSingleton.getInstance().getDBAdapter().saveProjectMenuItems(qr, itemsArr);

        }






        /*private boolean processResponce() throws JSONException {

            // Сначала проверяем полученный статус ответа
            if (jsonResponse.getString(NAME_STATUS).equals(VALUE_ERROR)) {

                errorCode = jsonResponse.getString(NAME_CODE);

                if (jsonResponse.has(NAME_MESSAGE)) {
                    errorMessage = jsonResponse.getString(NAME_MESSAGE);
                }

                return true;
            }

            // Далее проводим обработку ответа по конкретному типу запроса
            if (requestAction.equals(REQUEST_ACTION_GET_PARTNERS_INFO)) {
                return processGetPartnersIdResponce();
            }

            if (requestAction.equals(REQUEST_ACTION_GET_PARTNERS)) {
                return processGetPartnersResponce();
            } else if (requestAction.equals(REQUEST_ACTION_LOGIN)) {
                processLoginResponce();

            } else if (requestAction.equals(REQUEST_ACTION_GET_ACCOUNT)) {
                processGetAccountResponce();
            } else if (requestAction.equals(REQUEST_ACTION_GET_CONTACTS)) {
                processGetContactsResponce();
            } else if (requestAction.equals(REQUEST_ACTION_GET_CARDS)) {
                processUserCardsResponce();
            }

            return true;
        }*/


    }

}
