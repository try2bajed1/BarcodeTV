package su.ias.secondscreen.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import su.ias.secondscreen.data.AdvData;
import su.ias.secondscreen.data.BannerData;
import su.ias.secondscreen.data.menu.ProjectThemeData;
import su.ias.secondscreen.data.menu.SlidingMenuItemData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA. User: n.senchurin Date: 24.02.14 Time: 17:12
 */
public class DBAdapter {

    private static final String DATABASE_NAME = "second_screeen.db";
    private static final int DATABASE_VERSION = 1;


    public static final String FLD_ID = "_id";


    public static final String TABLE_PROJECTS = "table_projects";
    public static final String FLD_QR = "qr";
    public static final String FLD_PROJ_TITLE = "proj_title";
    public static final String FLD_FULLSCREEN_BANNER_INFO = "fs_banner_info";
    public static final String FLD_PROJECT_IMAGE_INFO = "project_image_info";



    public static final String TABLE_PROJECT_MENU_ITEMS = "table_project_items";
    public static final String FLD_PARENT_PROJECT_QR = "proj_qr";
    public static final String FLD_ITEM_ID = "item_id";
    public static final String FLD_ITEM_TITLE = "item_title";
    public static final String FLD_ITEM_TYPE = "item_type";
    public static final String FLD_MENU_ORDER = "item_menu_order";


    public static final String TABLE_SHEDULER = "table_sheduler";
    public static final String FLD_ADV_TITLE = "adv_title";
    public static final String FLD_TIME_START = "time_start";
    public static final String FLD_TIME_FINISH = "time_finish";
    public static final String FLD_DAY_OF_YEAR = "day_of_year";
    public static final String FLD_ADDED_TO_CALENDAR = "added_to_calendar";




    public static final String TABLE_PROJECT_THEMES  = "table_themes";
    public static final String FLD_THEME_NAME = "theme_name";
    public static final String FLD_BG_MENU = "bg_menu";
    public static final String FLD_BG_MENU_SELECTED = "bg_menu_selected";
    public static final String FLD_BG_HEAD = "bg_head";
    public static final String FLD_FONT_COLOR_MENU_ITEM = "menu_item_font_color";
    public static final String FLD_FONT_COLOR_HEADER = "actionbar_title_font_color";
    public static final String FLD_FONT_COLOR_SELECTED_ITEM = "selected_item_font_color";
    public static final String FLD_LIST_DIVIDER_COLOR = "list_divider_color";
    public static final String FLD_MENU_BANNER_JSON = "menu_banner_json";

    public static final int FS_BANNER = 0;
    public static final int MENU_BANNER = 1;


    // Переменная для хранения объекта БД
    private SQLiteDatabase db;

    // Контекст приложения для
    private final Context context;

    // Экземпляр вспомогательного класса для открытия и обновления БД
    private CDBHelper dbHelper;

    private boolean transactionOpened;

    public DBAdapter(Context _context) {

        context = _context;
        dbHelper = new CDBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        transactionOpened = false;
    }

    /**
     * Либо создаем новую бд, либо берем существующую
     *
     * @return
     * @throws android.database.SQLException
     */
    public DBAdapter open() throws SQLException {

        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }

        return this;
    }


    public void close() {
        db.close();
    }



    public void clearTable(String tableName) {
        db.delete(tableName, null, null);
    }




    public Cursor getScannedQRHistoryCursor() {

//        String selection = FLD_QR + " =? ";
//        String[] selectionArgs = new String[]{"dd"};

        return db.query(TABLE_PROJECTS,//
                new String[]{FLD_ID, FLD_QR, FLD_PROJ_TITLE, FLD_PROJECT_IMAGE_INFO} ,
                null, null, null, null, null, null);
    }




    public ProjectThemeData getTheme(String qr) {

        ProjectThemeData themeData = new ProjectThemeData();

        String selection = FLD_QR + " =? ";
        String[] selectionArgs = new String[]{qr};

        Cursor projectsCursor = db.query(TABLE_PROJECT_THEMES,//
                new String[]{FLD_ID, FLD_THEME_NAME, FLD_BG_MENU,FLD_BG_MENU_SELECTED, FLD_BG_HEAD, FLD_FONT_COLOR_MENU_ITEM, FLD_FONT_COLOR_HEADER, FLD_FONT_COLOR_SELECTED_ITEM,FLD_LIST_DIVIDER_COLOR},
                selection, selectionArgs, null, null, null, null);

        if (projectsCursor.getCount() != 0) {
            projectsCursor.moveToFirst();
            themeData.themeName =                 projectsCursor.getString(projectsCursor.getColumnIndex(FLD_THEME_NAME));
            themeData.menuBcgColor =              projectsCursor.getString(projectsCursor.getColumnIndex(FLD_BG_MENU));
            themeData.menuBcgColorSelected =      projectsCursor.getString(projectsCursor.getColumnIndex(FLD_BG_MENU_SELECTED));
            themeData.actionBarBcgColor =         projectsCursor.getString(projectsCursor.getColumnIndex(FLD_BG_HEAD));
            themeData.listItemTextColorDefault =  projectsCursor.getString(projectsCursor.getColumnIndex(FLD_FONT_COLOR_MENU_ITEM));
            themeData.actionBarTextColor =        projectsCursor.getString(projectsCursor.getColumnIndex(FLD_FONT_COLOR_HEADER));
            themeData.listItemTextColorSelected = projectsCursor.getString(projectsCursor.getColumnIndex(FLD_FONT_COLOR_SELECTED_ITEM));
            themeData.listDividerColor =          projectsCursor.getString(projectsCursor.getColumnIndex(FLD_LIST_DIVIDER_COLOR));
        }

        return themeData;
    }



    public void saveProjectAppearanceTheme(String qr, String themeName, String bgMenu,String bgSelectedMenuItem, String bgHead,
                                           String menuItemFontColor, String abTitleFontColor,
                                           String selectedItemFontColor, String dividerColor, String menuBannerJson){
        try {

            ContentValues insertAppearanceCV = new ContentValues();
            insertAppearanceCV.put(FLD_QR, qr);
            insertAppearanceCV.put(FLD_THEME_NAME, themeName);
            insertAppearanceCV.put(FLD_BG_MENU, bgMenu);
            insertAppearanceCV.put(FLD_BG_MENU_SELECTED, bgSelectedMenuItem);
            insertAppearanceCV.put(FLD_BG_HEAD, bgHead);
            insertAppearanceCV.put(FLD_FONT_COLOR_MENU_ITEM, menuItemFontColor);
            insertAppearanceCV.put(FLD_FONT_COLOR_HEADER, abTitleFontColor);
            insertAppearanceCV.put(FLD_FONT_COLOR_SELECTED_ITEM, selectedItemFontColor);
            insertAppearanceCV.put(FLD_LIST_DIVIDER_COLOR, dividerColor);
            insertAppearanceCV.put(FLD_MENU_BANNER_JSON, menuBannerJson);

            db.insertOrThrow(TABLE_PROJECT_THEMES, null, insertAppearanceCV);

        } catch (SQLiteConstraintException e) {

            ContentValues updateAppearanceCV = new ContentValues();
            updateAppearanceCV.put(FLD_THEME_NAME, themeName);
            updateAppearanceCV.put(FLD_BG_MENU, bgMenu);
            updateAppearanceCV.put(FLD_BG_MENU_SELECTED, bgSelectedMenuItem);
            updateAppearanceCV.put(FLD_BG_HEAD, bgHead);
            updateAppearanceCV.put(FLD_FONT_COLOR_MENU_ITEM, menuItemFontColor);
            updateAppearanceCV.put(FLD_FONT_COLOR_HEADER, abTitleFontColor);
            updateAppearanceCV.put(FLD_FONT_COLOR_SELECTED_ITEM, selectedItemFontColor);
            updateAppearanceCV.put(FLD_LIST_DIVIDER_COLOR, dividerColor);
            updateAppearanceCV.put(FLD_MENU_BANNER_JSON, menuBannerJson);

            String selection = FLD_QR + " = ? ";
            String[] args = new String[] {qr};
            db.update(TABLE_PROJECT_THEMES, updateAppearanceCV, selection, args);
        }
    }



    public void clearTableRowsByValue(String tableName, String qr) {
        String selection = FLD_QR + " = ? ";
        String[] args = new String[] {qr};
        db.delete(tableName,selection, args);
    }




    // на случай, если были изменения в кол-ве пунктов меню у проекта.
    public void clearProjectItems(String qr) {
        String selection = FLD_QR + " = ? ";
        String[] args = new String[] {qr};
        db.delete(TABLE_PROJECT_MENU_ITEMS, selection, args);
    }




    public void saveProjectMenuItems(String qr, JSONArray itemsJsonArr) {

        boolean result = true;

        clearTableRowsByValue(TABLE_PROJECT_MENU_ITEMS, qr);
        beginTransaction();

        ContentValues menuItemCV = new ContentValues();
        menuItemCV.put(FLD_QR, qr);

        try {
            for (int i = 0; i < itemsJsonArr.length(); i++) {
                JSONObject menuItemJson = itemsJsonArr.getJSONObject(i);
                menuItemCV.put(FLD_ITEM_TITLE, menuItemJson.getString("title"));
                menuItemCV.put(FLD_ITEM_ID,    menuItemJson.getString("id"));
                menuItemCV.put(FLD_MENU_ORDER, menuItemJson.getString("id_order"));
                menuItemCV.put(FLD_ITEM_TYPE,  menuItemJson.getString("type"));

                db.insert(TABLE_PROJECT_MENU_ITEMS, null, menuItemCV);
            }
        } catch (JSONException e) {
            result = false;
        }

        endTransaction(result);
    }


    public void saveAddedToCalendar(String qr, long startTimeMills) {
        ContentValues updateCalendarFlagCV = new ContentValues();
        updateCalendarFlagCV.put(FLD_ADDED_TO_CALENDAR, "1");

        String selection = FLD_QR + " = ? and " + FLD_TIME_START + "= ? ";
        String[] args = new String[] {qr, String.valueOf(startTimeMills)};
        db.update(TABLE_SHEDULER, updateCalendarFlagCV, selection, args);
    }


    public void saveShedule(String qr, JSONArray itemsJsonArr) {

        boolean result = true;
        beginTransaction();

        try {

            Calendar calendar = Calendar.getInstance();

            for (int i = 0; i < itemsJsonArr.length(); i++) {

                JSONObject advItemJson = itemsJsonArr.getJSONObject(i);
                String advTitle = advItemJson.getString("title");
                long finishTimeMills = 1000 * advItemJson.getLong("date_end");
                long startTimeMills  = 1000 * advItemJson.getLong("date_start");
                calendar.setTimeInMillis(startTimeMills);
                int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

                try {

                    ContentValues advItemCV = new ContentValues();
                    advItemCV.put(FLD_QR, qr);
                    advItemCV.put(FLD_ADV_TITLE, advTitle);
                    advItemCV.put(FLD_TIME_START,  startTimeMills);
                    advItemCV.put(FLD_TIME_FINISH, finishTimeMills);
                    advItemCV.put(FLD_ADDED_TO_CALENDAR, 0);
                    advItemCV.put(FLD_DAY_OF_YEAR, dayOfYear);
                    db.insertOrThrow(TABLE_SHEDULER, null, advItemCV);

                } catch (SQLiteConstraintException e) {

                    ContentValues updateProjectCV = new ContentValues();
                    updateProjectCV.put(FLD_TIME_START, startTimeMills);
                    updateProjectCV.put(FLD_TIME_FINISH, finishTimeMills);
                    // updateProjectCV.put(FLD_ADDED_TO_CALENDAR, 0); значение в этом столбце не перезаписываем
                    updateProjectCV.put(FLD_DAY_OF_YEAR, dayOfYear);

                    String selection = FLD_QR + " = ? and " + FLD_TIME_START + "= ? ";
                    String[] args = new String[] {qr, String.valueOf(startTimeMills)};
                    db.update(TABLE_SHEDULER, updateProjectCV, selection, args);
                }
            }

        } catch (JSONException e) {
            result = false;
        }

        endTransaction(result);
    }





    public ArrayList<ArrayList<AdvData>>  getSortedByDayAdvsArr(String qrValue) {

        ArrayList<ArrayList<AdvData>> sortedByDayAdvsArr = new ArrayList<ArrayList<AdvData>>();
        ArrayList<AdvData> sameDayAdvsArr = new ArrayList<AdvData>();

        String selection = FLD_QR + " = ? ";
        String[] selectionArgs = new String[] {qrValue};

        // select distinct values, so first argument is true
        Cursor uniqueDaysCursor = db.query(true, TABLE_SHEDULER,
                                                new String[] { FLD_DAY_OF_YEAR },
                                                selection, selectionArgs,
                                                FLD_DAY_OF_YEAR, null, null, null);

        if (uniqueDaysCursor.moveToFirst()) {
            while (!uniqueDaysCursor.isAfterLast()) {

                int dayVal = uniqueDaysCursor.getInt(0); // всего один столбец, сделал без getColumnIndex
                selection = FLD_QR + " = ? and "+ FLD_DAY_OF_YEAR +" =? ";
                selectionArgs = new String[] {qrValue, String.valueOf(dayVal)};


                Cursor sameDayAdvsCursor = db.query(TABLE_SHEDULER,
                                                    new String[]{FLD_ADV_TITLE, FLD_TIME_START, FLD_TIME_FINISH, FLD_DAY_OF_YEAR, FLD_ADDED_TO_CALENDAR},
                                                    selection, selectionArgs, null, null, FLD_TIME_START, null);

                int titleColumnIndex = sameDayAdvsCursor.getColumnIndex(FLD_ADV_TITLE);
                int startColumnIndex = sameDayAdvsCursor.getColumnIndex(FLD_TIME_START);
                int finishColumnIndex = sameDayAdvsCursor.getColumnIndex(FLD_TIME_FINISH);
//              int dayOfYearColumnIndex = sameDayAdvsCursor.getColumnIndex(FLD_DAY_OF_YEAR);
                int addedToCalColumnIndex = sameDayAdvsCursor.getColumnIndex(FLD_ADDED_TO_CALENDAR);

                if (sameDayAdvsCursor.moveToFirst()) {
                    sameDayAdvsArr = new ArrayList<AdvData>();
                    while (!sameDayAdvsCursor.isAfterLast()) {

                        AdvData advData = new AdvData();
                        advData.title = sameDayAdvsCursor.getString(titleColumnIndex);
                        advData.startDateMillsSecs = sameDayAdvsCursor.getLong(startColumnIndex);
                        advData.endDateMillsSecs = sameDayAdvsCursor.getLong(finishColumnIndex);
                        advData.dayOfYear = dayVal;
                        advData.addedToCalendar = sameDayAdvsCursor.getInt(addedToCalColumnIndex) == 1;
                        sameDayAdvsArr.add(advData);

                        sameDayAdvsCursor.moveToNext();
                    }
                    sameDayAdvsCursor.close();
                }

                sortedByDayAdvsArr.add(sameDayAdvsArr);

                uniqueDaysCursor.moveToNext();
            }
            uniqueDaysCursor.close();
        }


        return sortedByDayAdvsArr;
    }



    // todo: accelerate (look DDMS)
    public BannerData getBannerData(String qr, int bannerType ) {

        BannerData bannerData = new BannerData();
        String dataStr = "";

        String selection = FLD_QR + " = ? ";
        String[] selectionArgs = new String[] {qr};

        // структура джейсона в ячейках одинакова.
        Cursor itemsCursor;
        if (bannerType == 0) {

            itemsCursor = db.query(TABLE_PROJECTS, //
                                    new String[]{ FLD_ID, FLD_FULLSCREEN_BANNER_INFO },
                                    selection, selectionArgs, null, null, null, null);

            if (itemsCursor.getCount() != 0) {
                itemsCursor.moveToFirst();
                dataStr =  itemsCursor.getString(itemsCursor.getColumnIndex(FLD_FULLSCREEN_BANNER_INFO));
            }

        } else {
            itemsCursor = db.query(TABLE_PROJECT_THEMES,//
                                    new String[]{FLD_ID, FLD_MENU_BANNER_JSON },
                                    selection, selectionArgs, null, null, null, null);
            if (itemsCursor.getCount() != 0) {
                itemsCursor.moveToFirst();
                dataStr = itemsCursor.getString(itemsCursor.getColumnIndex(FLD_MENU_BANNER_JSON));
            }
        }


        if (dataStr == null || dataStr.equals("") ) {
            return null;
        } else {
            try {
                JSONObject dataJson = new JSONObject(dataStr);
                bannerData.partnerUrl = dataJson.getString("link_partner");
                bannerData.counterUrl = dataJson.getString("link_counter");
                ArrayList<String> imgsArr = new ArrayList<String>();
                JSONObject imgJson = dataJson.getJSONObject("image");
                Iterator keys = imgJson.keys();
                while(keys.hasNext()){
                    String key = (String) keys.next();
                    imgsArr.add(imgJson.getString(key));
                }

                bannerData.imagesArr = imgsArr;

            } catch (JSONException e) {
                e.printStackTrace();
                return  null;
            }
        }

        return bannerData;
    }



    public ArrayList<SlidingMenuItemData> getMenuItems(String qr) {

        ArrayList<SlidingMenuItemData> itemsArr = new ArrayList<SlidingMenuItemData>();

        String selection = FLD_QR + " = ? ";
        String[] selectionArgs = new String[] {qr};

        Cursor itemsCursor = db.query(TABLE_PROJECT_MENU_ITEMS,//
                new String[]{FLD_ID, FLD_ITEM_TITLE, FLD_ITEM_ID, FLD_MENU_ORDER, FLD_ITEM_TYPE},
                selection, selectionArgs, null, null, FLD_MENU_ORDER, null);


        if (itemsCursor.getCount() != 0) {
            itemsCursor.moveToFirst();
            while (!itemsCursor.isAfterLast()) {
                SlidingMenuItemData data = new SlidingMenuItemData();
                data.id = itemsCursor.getInt(itemsCursor.getColumnIndex(FLD_ITEM_ID));
                data.title = itemsCursor.getString(itemsCursor.getColumnIndex(FLD_ITEM_TITLE));
                data.type = itemsCursor.getString(itemsCursor.getColumnIndex(FLD_ITEM_TYPE));
                itemsArr.add(data);
                itemsCursor.moveToNext();
            }
        }


        // last added
        SlidingMenuItemData data = new SlidingMenuItemData();
        data.id = -100;
        data.title = "Проекты";
        data.type = "__proj";
        itemsArr.add(data);




        return  itemsArr;

    }




    private void removeRow(String tableName, String whereClause, String[] whereArgs ) {
//        String whereClause = "_id"+"=?";
//        String[]whereArgs = new String[] {String.valueOf(row)};
        db.delete(tableName, whereClause, whereArgs);
    }






    public void saveCommonProjectData(String qr, String title, String imageInfoStr, String fsBannerStr) {

        try {

            ContentValues insertProjectCV = new ContentValues();
            insertProjectCV.put(FLD_QR, qr);
            insertProjectCV.put(FLD_PROJ_TITLE, title);
            insertProjectCV.put(FLD_PROJECT_IMAGE_INFO, imageInfoStr);
            insertProjectCV.put(FLD_FULLSCREEN_BANNER_INFO, fsBannerStr);
            db.insertOrThrow(TABLE_PROJECTS, null, insertProjectCV);

        } catch (SQLiteConstraintException e) {

            ContentValues updateProjectCV = new ContentValues();
            updateProjectCV.put(FLD_PROJ_TITLE, title);
            updateProjectCV.put(FLD_PROJECT_IMAGE_INFO, imageInfoStr);
            updateProjectCV.put(FLD_FULLSCREEN_BANNER_INFO, fsBannerStr);

            String selection = FLD_QR + " = ? ";
            String[] args = new String[] {qr};
            db.update(TABLE_PROJECTS, updateProjectCV, selection, args);
        }
    }



    public boolean projectAlreadyExists(String qrStr){

        String selection = FLD_QR + " =? ";
        String[] selectionArgs = new String[] {qrStr};

        Cursor fieldsCursor = db.query(TABLE_PROJECTS,//
                new String[] { "count(*)" }, //
                selection, selectionArgs, null, null, null, null);
        fieldsCursor.moveToFirst();

        return (fieldsCursor.getInt(0) != 0);
    }



    public void beginTransaction() {
        db.beginTransaction();
        transactionOpened = true;
    }

    public void endTransaction(boolean b) {

        if (transactionOpened) {

            if (b) {
                db.setTransactionSuccessful();
            }

            db.endTransaction();
            transactionOpened = false;
        }
    }




    private static class CDBHelper extends SQLiteOpenHelper {

        public CDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            String concatInstructionStr = "CREATE TABLE " + TABLE_PROJECTS + "(" //
                    + FLD_ID + " INTEGER NOT NULL PRIMARY KEY autoincrement , "
                    + FLD_QR + " TEXT ,"
                    + FLD_PROJ_TITLE + " TEXT ,"
                    + FLD_PROJECT_IMAGE_INFO + " TEXT ,"
                    + FLD_FULLSCREEN_BANNER_INFO + " TEXT );";
            db.execSQL(concatInstructionStr);


            concatInstructionStr = "CREATE TABLE " + TABLE_PROJECT_MENU_ITEMS + "(" //
                    + FLD_ID + " integer primary key autoincrement, " //
                    + FLD_QR + " TEXT, " //
                    + FLD_ITEM_TITLE + " TEXT, " //
                    + FLD_ITEM_ID  + " integer not null, " //
                    + FLD_MENU_ORDER  + " integer not null, " //
                    + FLD_ITEM_TYPE + " text );";
            db.execSQL(concatInstructionStr);


            concatInstructionStr = "CREATE TABLE " + TABLE_SHEDULER + "(" //
                    + FLD_ID + " integer primary key autoincrement, " //
                    + FLD_ADV_TITLE + " TEXT, " //
                    + FLD_QR + " TEXT, " //
                    + FLD_TIME_START  + " TEXT, " //
                    + FLD_TIME_FINISH  + " text, " //
                    + FLD_ADDED_TO_CALENDAR  + " integer, " //
                    + FLD_DAY_OF_YEAR + " integer not null );";
            db.execSQL(concatInstructionStr);


            concatInstructionStr = "CREATE TABLE " + TABLE_PROJECT_THEMES + "(" //
                    + FLD_ID + " integer primary key autoincrement, " //
                    + FLD_QR + " TEXT, " //
                    + FLD_MENU_BANNER_JSON + " TEXT, " //
                    + FLD_THEME_NAME + " TEXT, " //
                    + FLD_BG_MENU + " TEXT, " //
                    + FLD_BG_MENU_SELECTED + " TEXT, " //
                    + FLD_BG_HEAD + " TEXT, " //
                    + FLD_FONT_COLOR_MENU_ITEM + " TEXT, " //
                    + FLD_FONT_COLOR_HEADER + " TEXT, " //
                    + FLD_LIST_DIVIDER_COLOR + " TEXT, " //
                    + FLD_FONT_COLOR_SELECTED_ITEM + " text );";
            db.execSQL(concatInstructionStr);


            //todo: make concat like above
            String setUniqueQR = "CREATE UNIQUE INDEX table_projects_uni_qr ON " + TABLE_PROJECTS + "(" + FLD_QR + ");";
            db.execSQL(setUniqueQR);


            setUniqueQR = "CREATE UNIQUE INDEX table_themes_uni_qr ON " + TABLE_PROJECT_THEMES + "(" + FLD_QR + ");";
            db.execSQL(setUniqueQR);


//          setUniqueQR = "CREATE UNIQUE INDEX table_project_items_uni_qr ON table_project_items(proj_qr);";
//          db.execSQL(setUniqueQR);

            String setUniqueStartTime = "CREATE UNIQUE INDEX table_sheduler_uni_time_start ON "+ TABLE_SHEDULER+  "("+ FLD_TIME_START +" , "+ FLD_QR + ");";
            db.execSQL(setUniqueStartTime);

        }



        @Override
        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {

        }

    }

}
