package su.ias.secondscreen.activities;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import su.ias.secondscreen.IListener;
import su.ias.secondscreen.R;
import su.ias.secondscreen.activities.fragments.*;
import su.ias.secondscreen.app.AppSingleton;
import su.ias.secondscreen.async.SectionDataLoader;
import su.ias.secondscreen.data.BannerData;
import su.ias.secondscreen.data.menu.ProjectThemeData;
import su.ias.secondscreen.data.menu.SlidingMenuItemData;
import su.ias.secondscreen.db.DBAdapter;
import su.ias.secondscreen.utils.BitmapDrawer;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 12.05.2014
 * Time: 12:44
*/



public class MainActivity extends ActionBarActivity implements IListener {

    public static final String QR = "QR";

    public static final String RECIEVED_DATA = "received_data";

    public static final String CONTENT_TYPE_NEWS = "news";
    public static final String CONTENT_TYPE_SHEDULE = "shedule";
    public static final String CONTENT_TYPE_PHOTO = "photo";
    public static final String CONTENT_TYPE_VIDEO = "video";
    public static final String CONTENT_TYPE_ACTORS = "actor";
    public static final String CONTENT_TYPE_PAGE = "page";
    public static final String CONTENT_TYPE_LIST = "list";
    public static final String CLOSE_EVENT = "close_event";


    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar mActionbar;

    private CloseReciever closeReciever;

    private ListView newsLV;
    private ListView slidinMenuLV;
    private ImageView menuBannerIV;

    public String qrCode;
    private ArrayList<SlidingMenuItemData> menuItemsArr;

    protected RelativeLayout framesContainer;
    private String selectedType;
    private int selectedMenuIndex;
    private long lastBackTouch;

    private ProjectThemeData themeData;

    private SectionDataLoader sectionDataLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragments_holder);

        qrCode = getIntent().getStringExtra(QR);
        themeData = AppSingleton.getInstance().getDBAdapter().getTheme(qrCode);
        AppSingleton.getInstance().currentProjTheme = themeData;

        slidinMenuLV = (ListView) findViewById(R.id.sliding_listview);
        String clrStr = "#"+ themeData.menuBcgColor;
        slidinMenuLV.setBackgroundDrawable(new ColorDrawable(Color.parseColor(clrStr)));
        slidinMenuLV.setDivider(new ColorDrawable(Color.parseColor("#"+ themeData.listDividerColor)));
        slidinMenuLV.setDividerHeight(1);
        initDrawer();

        fillMenu();

        framesContainer = (RelativeLayout) findViewById(R.id.fragments_container);

        closeReciever = new CloseReciever();

        setProgressBarIndeterminateVisibility(true);


        /*if(sectionDataLoader != null && sectionDataLoader.getStatus().equals(AsyncTask.Status.RUNNING)) {
            sectionDataLoader.cancel(true);
        }
        sectionDataLoader = new SectionDataLoader(this);
        sectionDataLoader.execute(qrCode, String.valueOf(menuItemsArr.get(selectedMenuIndex).id));
*/
        loadDataForSelectedSection(menuItemsArr.get(selectedMenuIndex).id, selectedType);

    }



    private void loadDataForSelectedSection(int sectionId, String sectionType) {
        if(sectionDataLoader != null ) {
            if(sectionDataLoader.getStatus().equals(AsyncTask.Status.RUNNING)) {
                sectionDataLoader.cancel(true);
            }
        }

        sectionDataLoader = new SectionDataLoader(this);
        sectionDataLoader.execute(qrCode, String.valueOf(sectionId), sectionType);

    }



    private void fillMenu() {

        menuItemsArr = AppSingleton.getInstance().getDBAdapter().getMenuItems(qrCode);

        selectedMenuIndex = 0; // getSelectedMenuIndex(selectedType);
        selectedType = menuItemsArr.get(0).type;
        menuItemsArr.get(selectedMenuIndex).selected = true;
        mActionbar.setTitle(menuItemsArr.get(selectedMenuIndex).title);


        slidinMenuLV.setAdapter(new SliceMenuListAdapter(MainActivity.this, menuItemsArr));
        slidinMenuLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (menuItemsArr.get(position).id == -100) {
                    finish();
                    startActivity(new Intent(MainActivity.this, ProjectsHistoryActivity.class));
                    return;
                }

                mDrawer.closeDrawer(Gravity.LEFT);

                // если нажали на другой пункт - меняем фрагмент и перерисовыываем меню.
                // если на уже выбранный, то меню просто закроется.
                if (!menuItemsArr.get(position).selected) {

                    mActionbar.setTitle(menuItemsArr.get(position).title);
                    menuItemsArr.get(selectedMenuIndex).selected = false;

                    selectedType = menuItemsArr.get(position).type;
                    selectedMenuIndex = position;
                    menuItemsArr.get(selectedMenuIndex).selected = true;
                    ((SliceMenuListAdapter) slidinMenuLV.getAdapter()).notifyDataSetChanged(); // redraw due to selection

                    int sectionId = menuItemsArr.get(selectedMenuIndex).id;
                    loadDataForSelectedSection(sectionId, selectedType);
                }
            }
        });

        menuBannerIV = (ImageView) findViewById(R.id.sliding_menu_img);

        final BannerData bannerData = AppSingleton.getInstance().getDBAdapter().getBannerData(qrCode, DBAdapter.MENU_BANNER);
        if (bannerData == null) {
            menuBannerIV.setVisibility(View.GONE);
        } else {
            final String url = bannerData.imagesArr.get(0);
            final String path = getCacheDir() + "/" + "menu_banner_"+qrCode + ".png";

            ViewTreeObserver observer = menuBannerIV.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    menuBannerIV.getViewTreeObserver().removeOnPreDrawListener(this);

                    BitmapDrawer.INSTANCE.drawBitmap(path, url, menuBannerIV, BitmapDrawer.DRAW_FILL, true, true);
                    return false;
                }
            });
        }

        menuBannerIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bannerData.partnerUrl));
                startActivity(browserIntent);

            }
        });

    }




    @SuppressLint("InlinedApi")
    protected void initDrawer() {

        mDrawer = (android.support.v4.widget.DrawerLayout) findViewById(R.id.drawer);
        if (mDrawer == null) return;

        // Создадим drawer toggle для управления индикатором сверху
        mDrawerToggle = new android.support.v4.app.ActionBarDrawerToggle(this, mDrawer, R.drawable.ic_drawer, R.string.opened, R.string.closed);

        // Назначим его drawer-у как слушателя
        mDrawer.setDrawerListener(mDrawerToggle);

        // Для красоты добавим тень с той же гравитацией
        mDrawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        // Включим кнопки на action bar
        mActionbar = getSupportActionBar();
        mActionbar.setDisplayShowTitleEnabled(true);
        mActionbar.setDisplayHomeAsUpEnabled(true);
        mActionbar.setHomeButtonEnabled(true);

        //mActionbar.setBackgroundDrawable(new Color());
//        mActionbar.setBackgroundDrawable(getResources().getDrawable(R.color.news_header_color));

        String clrStr = "#"+ themeData.actionBarBcgColor;
        mActionbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(clrStr)));


        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView abTitle = (TextView) findViewById(titleId);
        abTitle.setTextColor(Color.parseColor("#"+themeData.actionBarTextColor));

    }



    private int getSelectedMenuIndex(String type) {

        for (int i = 0; i < menuItemsArr.size(); i++) {
            if(menuItemsArr.get(i).type.equals(type)){
                return i;
            }
        }

        return -1;
    }

    private int getMenuItemIdByType(String type) {

        for (SlidingMenuItemData itemData : menuItemsArr) {
            if(type.equals(itemData.type)){
                return itemData.id;
            }
        }

        return -1;
    }




    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter newsFilter = new IntentFilter(CLOSE_EVENT);
        registerReceiver(closeReciever, newsFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(closeReciever);
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mDrawerToggle != null) mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_qr, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Если событие обработано переключателем, то выходим
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) return true;

        switch (item.getItemId()) {

            case R.id.action_qr:
                finish();
                startActivity(new Intent(MainActivity.this, QRReaderActivity.class));
                return true;

            case android.R.id.home:

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }



    protected void preventFastExit() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBackTouch > 2000) {
            Toast.makeText(getApplicationContext(), "Для выхода нажмите \"назад\" еще раз", Toast.LENGTH_SHORT).show();
            lastBackTouch = currentTime;
        } else {
            finish();
        }
    }



    @Override
    public void onBackPressed() {
        preventFastExit();
    }


/*
    {
        "title": "Заголовок",
            "date": "1399903560",
            "text": "Тут и там, прям как там и тут",
            "images": [{ "100x100"  : "http://jjj/jj.jpg",
                         "2048x1600": "http://jjj/jj.jpg"} ,

                      { "400x308"  : "http://jjj/jj.jpg",
                         "2048x1600": "http://jjj/jj.jpg"}],
        "video": {
        "link": "http://jjj/jj.mp4",
                "image": {"1280x720": "http://jjj/jj.jpg",
                          "800x600": "http://jjj/jj.jpg"}
                }
    } */



    private Fragment getFragmentByType(String selectedType) {

        if (selectedType.equals(CONTENT_TYPE_NEWS)) {
            return new NewsFragment();
        }
        if (selectedType.equals(CONTENT_TYPE_LIST)) {
            return new ListTempFragment();
        }

        if (selectedType.equals(CONTENT_TYPE_SHEDULE)) {
            return new SchedulerFragment();
        }

        if (selectedType.equals(CONTENT_TYPE_VIDEO)) {
            return new VideosFragment();
        }

        if (selectedType.equals(CONTENT_TYPE_ACTORS)) {
            return new ActorsFragment();
        }

        if (selectedType.equals(CONTENT_TYPE_PHOTO)) {
            return new PhotosFragment();
        }

        if (selectedType.equals(CONTENT_TYPE_PAGE)) {
            return new PageTypeFragment();
        }

        return new ErrorFragment();
    }



    @Override
    public void completeHandler(String jsonFromApi) {

        setProgressBarIndeterminateVisibility(false);

        Fragment fragmentToLoad = getFragmentByType(selectedType);
        Bundle args = new Bundle();
        args.putString(MainActivity.RECIEVED_DATA, jsonFromApi);

        fragmentToLoad.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.replace(framesContainer.getId(), fragmentToLoad);
        fragmentTransaction.commit();

    }


    @Override
    public void errorHandler(String errorStr) {
        Toast.makeText(MainActivity.this, "Ошибка", Toast.LENGTH_LONG).show();
    }



    protected class SliceMenuListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater layoutInflater;
        public ArrayList<SlidingMenuItemData> itemsArr;

        // todo: add ViewHolder

        public SliceMenuListAdapter(Context c, ArrayList<SlidingMenuItemData> itemsArr) {

            mContext = c;
            layoutInflater = LayoutInflater.from(mContext);
            this.itemsArr = itemsArr;
        }


        public int getCount() {
            return itemsArr.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }


        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.slice_menu_list_item, null);
            }

            // set selector programmatically
            StateListDrawable st = new StateListDrawable();
            st.addState(new int[] {android.R.attr.state_pressed }, new ColorDrawable(Color.parseColor("#" + themeData.actionBarBcgColor)));
            st.addState(new int[]{android.R.attr.state_enabled},   new ColorDrawable(Color.parseColor("#"+themeData.menuBcgColor)));
            convertView.setBackgroundDrawable(st);

            SlidingMenuItemData sliceItem = itemsArr.get(position);

            View space = convertView.findViewById(R.id.selected_menu_item_stripe);

            TextView itemTV = ((TextView) convertView.findViewById((R.id.slice_menu_item_text)));
            itemTV.setText(sliceItem.title);

            if(sliceItem.selected){
                itemTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                space.setVisibility(View.VISIBLE);

                itemTV.setTextColor(Color.parseColor("#"+themeData.listItemTextColorSelected));
                space.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#"+ themeData.actionBarBcgColor)));
                convertView.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#"+themeData.menuBcgColorSelected)));
            } else {
                itemTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                space.setVisibility(View.INVISIBLE);
                itemTV.setTextColor(Color.parseColor("#"+themeData.listItemTextColorDefault));
                convertView.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#" + themeData.menuBcgColor)));
            }

            return convertView;
        }
    }





    private class CloseReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(CLOSE_EVENT)){
                finish();
                startActivity(new Intent(MainActivity.this,QRReaderActivity.class));
            }

        }
    }








}