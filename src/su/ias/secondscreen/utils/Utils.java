package su.ias.secondscreen.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.CalendarContract;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 21.05.2014
 * Time: 16:07
 */
public class Utils {


    public static String getSmallestImgURL(JSONObject imgsJson) {

        HashMap<Integer, String> descrWithAreaHM = getJsonKeysAsHM(imgsJson);

        String minKey = findMinimalAreaKey(descrWithAreaHM);

        try {
            return imgsJson.getString(minKey);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }



    private static HashMap<Integer, String> getJsonKeysAsHM(JSONObject imgsJson) {

        ArrayList<String> nonSplittedArr = new ArrayList<String>();

        Iterator keys = imgsJson.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            nonSplittedArr.add(key);
        }

        HashMap<Integer, String> descrWithAreaHM = new HashMap<Integer, String>();

        for (String imgDescrKey : nonSplittedArr) {
            String[] splitedArr = imgDescrKey.split("x");
            int w = Integer.parseInt(splitedArr[0]);
            int h = Integer.parseInt(splitedArr[1]);
            int area = w * h;
            descrWithAreaHM.put(area, imgDescrKey);
        }

        return descrWithAreaHM;

    }



    public static String getBiggestImgURL(JSONObject imgsJson) {

        HashMap<Integer, String> descrWithAreaHM = getJsonKeysAsHM(imgsJson);
        String maxAreaKey = findMaxAreaKey(descrWithAreaHM);

        try {
            return imgsJson.getString(maxAreaKey);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }



    private static String findAverageAreaKey(HashMap<Integer, String> hm) {
        int minArea = -1;
        for (Integer key : hm.keySet()) {
            if (minArea == -1 || key < minArea) minArea = key;
        }

        return hm.get(minArea);
    }


    private static String findMinimalAreaKey(HashMap<Integer, String> hm) {
        int minArea = -1;
        for (Integer key : hm.keySet()) {
            if (minArea == -1 || key < minArea) minArea = key;
        }

        return hm.get(minArea);
    }



    private static String findMaxAreaKey(HashMap<Integer, String> hm) {
        int maxArea = 0;
        for (Integer key : hm.keySet()) {
            //if (minArea == -1 || key < minArea) minArea = key;
            if(maxArea < key) maxArea = key;
        }

        return hm.get(maxArea);
    }


    public static String getStrDateFromLong(long mills, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date(mills));
    }



    public static String getDayNameFromTimeStamp(long mills) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);

        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {

            case Calendar.MONDAY:
                return "Понедельник";
            case Calendar.TUESDAY:
                return "Вторник";
            case Calendar.WEDNESDAY:
                return "Среда";
            case Calendar.THURSDAY:
                return "Четверг";
            case Calendar.FRIDAY:
                return "Пятница";
            case Calendar.SATURDAY:
                return "Суббота";
            case Calendar.SUNDAY:
                return "Воскресенье";
        }

        return "";
    }




}
