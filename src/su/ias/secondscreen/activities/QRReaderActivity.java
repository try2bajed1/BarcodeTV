package su.ias.secondscreen.activities;

import android.content.*;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import su.ias.secondscreen.IListener;
import su.ias.secondscreen.R;
import su.ias.secondscreen.api.ApiAdapter;
import su.ias.secondscreen.app.AppSingleton;
import su.ias.secondscreen.async.ProjectDataLoader;
import su.ias.zbarintegrator.QRCodeReaderActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


public class QRReaderActivity extends QRCodeReaderActivity implements IListener {

    public static final String ASSETS_ARE_EMBEDDED = "assets_are_emb";

    private ProjectDataReceiver projectDataReceiver;
    private String qrCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		TextView text = (TextView) findViewById(R.id.scan_qr_text);
		String string = getString(R.string.scan_from_tv);
		String ofDevice = AppSingleton.deviceType == AppSingleton.SMARTPHONE ? getString(R.string.of_smartphone): getString(R.string.of_tablet);
		string = string.replace("{DEVICE}", ofDevice);

		text.setText(string);

        projectDataReceiver = new ProjectDataReceiver();

        Button btn = (Button) findViewById(R.id.load_projects_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(QRReaderActivity.this, ProjectsHistoryActivity.class));
            }
        });


        // вшиваем контент при первом запуске
        SharedPreferences preferences  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!preferences.getBoolean(ASSETS_ARE_EMBEDDED, false)) {

            List<String> assetsList = null;

            try {
                assetsList = Arrays.asList(getAssets().list(""));
                if(assetsList.contains("fizruk.json")){
                    parseAssetsFile("fizruk.json", "fizruk");
                }
                if(assetsList.contains("kuhnya.json")) {
                    parseAssetsFile("kuhnya.json", "kuhnya");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            preferences.edit().putBoolean(ASSETS_ARE_EMBEDDED, true);
        }

	}



    private void parseAssetsFile(String filename, String qrCode) {
        InputStream is = null;
        try {
            is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonStr = new String(buffer, "UTF-8");
            JSONObject projJson = new JSONObject(jsonStr);

            JSONObject responsePart = (JSONObject) projJson.get("response");
            saveProjectDataToDB(responsePart, qrCode);

        } catch (IOException e) {
            e.printStackTrace();
            Log.i("@","error "+e.toString());
        } catch (JSONException e) {
            Log.i("@","error "+e.toString());
            e.printStackTrace();
        }
    }



    private void saveProjectDataToDB(JSONObject projectJson,String qr) throws JSONException {

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
        String bgSelectedMenuItem = themeJson.getString("bg_cell");
        String bgHead = themeJson.getString("bg_head");
        String menuItemFontColor = themeJson.getString("font_color_menu");
        String abTitleFontColor = themeJson.getString("font_color_head");
        String selectedItemFontColor = themeJson.getString("font_color_cell");
        String dividerColor = themeJson.getString("cell_separator_color");
        String menuBannerJson ="";

        if(projectJson.has("menu_banner")){
            menuBannerJson = projectJson.get("menu_banner").toString();
        }

        AppSingleton.getInstance().getDBAdapter().saveProjectAppearanceTheme(qr, themeName, bgMenu,bgSelectedMenuItem, bgHead, menuItemFontColor,
                                                                             abTitleFontColor, selectedItemFontColor,dividerColor, menuBannerJson);
        //save menu items:
        JSONArray itemsArr = projectJson.getJSONArray("items");
        AppSingleton.getInstance().getDBAdapter().saveProjectMenuItems(qr, itemsArr);
    }



    @Override
	protected int getContentView() {
		return R.layout.activity_qrcode_reader;
	}


	@Override
	protected void initScreenOrientation() {

        String screenLabel = (String) (findViewById(R.id.container)).getContentDescription();
		if (screenLabel.equals(getString(R.string.smartphone_lable))) {
			AppSingleton.deviceType = AppSingleton.SMARTPHONE;
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		} else {
			AppSingleton.deviceType = AppSingleton.TABLET;
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}
	}




	@Override
	protected void processScanResult(String resultString) {

        qrCode = resultString;
        AppSingleton.getInstance().currentQR = qrCode;

        //ApiAdapter.getInstance().getContent(resultString);
        ProjectDataLoader projectDataLoader = new ProjectDataLoader(this);
        projectDataLoader.execute(qrCode);


	}



    @Override
    protected void onResume() {

        super.onResume();

        IntentFilter newsFilter = new IntentFilter(ApiAdapter.REQUEST_ACTION_GET_PROJ_BY_QR);
        newsFilter.addAction(ApiAdapter.VALUE_ERROR);
        registerReceiver(projectDataReceiver, newsFilter);
    }



    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(projectDataReceiver);
    }



    @Override
    public void completeHandler(String jsonFromApi) {
        startActivity(new Intent(QRReaderActivity.this, FullScreenBannerActivity.class)
                           .putExtra(FullScreenBannerActivity.QR, qrCode));
        finish();
    }



    @Override
    public void errorHandler(String errorStr) {

    }



    private class ProjectDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            setProgressBarIndeterminateVisibility(false);

            if (intent.getAction().equals(ApiAdapter.REQUEST_ACTION_GET_PROJ_BY_QR)) {

                startActivity(new Intent(QRReaderActivity.this, FullScreenBannerActivity.class)
                                  .putExtra(FullScreenBannerActivity.QR, qrCode));
                finish();
            }


            if (intent.getAction().equals(ApiAdapter.VALUE_ERROR)) {
                Log.i("@", "here");
                Toast.makeText(QRReaderActivity.this, "Ошибка при передаче данных", Toast.LENGTH_LONG).show();
            }



        }

    }


}
