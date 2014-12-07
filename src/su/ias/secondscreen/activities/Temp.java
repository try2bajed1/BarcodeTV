package su.ias.secondscreen.activities;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.util.*;

public class Temp extends Activity {
	
	private ImageView bannerImg;
	
	
	public static final String QR = "QR";
    private String qrCode;
    private ArrayList<Long> timeStampsArr;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        HashMap<Integer, ArrayList<Long>> resultHM = new HashMap<Integer, ArrayList<Long>>();

        timeStampsArr = getTempDates();
        Collections.sort(timeStampsArr);

        ArrayList<Integer> daysArr = convertTimeStampArrayToDayArray(timeStampsArr);

        LinkedHashSet<Integer> hs = new LinkedHashSet<Integer>();
        hs.addAll(daysArr);
        daysArr.clear();
        daysArr.addAll(hs); // now unique

        for (Integer uniqueDay : daysArr) {
            resultHM.put(uniqueDay, getAdvByDayBelonging(uniqueDay));
        }

        //Log.i("@", "result " + resultHM);
    }




    private ArrayList<Long> getAdvByDayBelonging(int day) {

        ArrayList<Long> sameDayAdvsArr = new ArrayList<Long>();

        for (Long item : timeStampsArr) {
            int convertedValue = timeToDay(item);
            if (convertedValue == day) {
                sameDayAdvsArr.add(item);
            }
        }

        return sameDayAdvsArr;
    }



    private int timeToDay(Long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.DAY_OF_YEAR);
    }



    private ArrayList<Integer> convertTimeStampArrayToDayArray(ArrayList<Long> timeArr) {

        ArrayList<Integer> daysArr = new ArrayList<Integer>();

        for (int i = 0; i < timeArr.size(); i++) {
            int convertedVal = timeToDay(timeArr.get(i));
            daysArr.add(convertedVal);
        }

        return  daysArr;
    }



    private ArrayList<Long> getTempDates() {

        ArrayList<Long> datesArr = new ArrayList<Long>();

        Calendar c1 = Calendar.getInstance();
        for (int i = 0; i < 5; i++) {
            c1.add(Calendar.DAY_OF_YEAR, 1);
            datesArr.add(c1.getTimeInMillis());
        }

        c1.add(Calendar.HOUR_OF_DAY, 1);
        datesArr.add(c1.getTimeInMillis());

        c1.add(Calendar.HOUR_OF_DAY, 1);
        datesArr.add(c1.getTimeInMillis());

        c1.add(Calendar.HOUR_OF_DAY, 1);
        datesArr.add(c1.getTimeInMillis());

        return datesArr;
    }



           /*ArrayList<HashMap<Integer,Long>> totalShedule = new ArrayList<HashMap<Integer, Long>>();


        for (int i = 0; i < datesTempArr.size(); i++) {

            long advTime = datesTempArr.get(i);
            cal.setTimeInMillis(advTime);
            int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

            HashMap <Integer,Long> shedItem = new HashMap<Integer, Long>();
            shedItem.put(dayOfYear, advTime);
            totalShedule.add(shedItem);
        }


        HashMap<Integer, ArrayList<Long>> resultHM = new HashMap<Integer, ArrayList<Long>>();
        for (int i = 0; i < totalShedule.size(); i++) {

        }*/


    protected void loadContentView() {
        Log.i("@", "onLoadContentview");
    }
}

