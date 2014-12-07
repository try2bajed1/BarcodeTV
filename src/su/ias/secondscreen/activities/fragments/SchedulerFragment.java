package su.ias.secondscreen.activities.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.squareup.timessquare.CalendarPickerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import su.ias.secondscreen.R;
import su.ias.secondscreen.activities.MainActivity;
import su.ias.secondscreen.app.AppSingleton;
import su.ias.secondscreen.data.AdvData;
import su.ias.secondscreen.utils.Utils;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: n.senchurin
 * Date: 24.03.2014
 * Time: 12:54
 */


public class SchedulerFragment extends BaseFragment {

//    private CalendarPickerView calendarView;
    private ArrayList<AdvData> advListFromJsonArr; // Adv  means advertisement

//    private ArrayList<Long> advsTimeStampsArr;

    private ArrayList<Integer> eachAdvDayOfYearArr; // array with days of year matching for each advertisement
    private ArrayList<Integer> uniqueDaysOfYearArr; // todo: may be use int[]
    private ArrayList<ArrayList<AdvData>> sortedByDayAdvsArr = new ArrayList<ArrayList<AdvData>>();
    private ViewPager shedulerPager;

    //    private ImageView asListIcon;
    //private TextView asListLabel;

//    private ImageView asCalendarIcon;
//    private TextView asCalendarLabel;

    private static boolean adapterIsSet = false;

    @Override
    public void onAttach(android.app.Activity activity) {

        super.onAttach(activity);

        extraData = getArguments().getString(MainActivity.RECIEVED_DATA);

        sortedByDayAdvsArr = AppSingleton.getInstance().getDBAdapter().getSortedByDayAdvsArr(AppSingleton.getInstance().currentQR);
    }



    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_shedule, container, false);

        shedulerPager = (ViewPager) view.findViewById(R.id.shelude_view_pager);

        final ImageView asListIcon = (ImageView) view.findViewById(R.id.as_list_icon);
        final TextView asListLabel = (TextView) view.findViewById(R.id.as_list_label);

        final ImageView asCalendarIcon = (ImageView) view.findViewById(R.id.as_calendar_icon);
        final TextView asCalendarLabel = (TextView) view.findViewById(R.id.as_calendar_label);

        LinearLayout asListEventField = (LinearLayout) view.findViewById(R.id.as_list_event_field);
        asListEventField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shedulerPager.setCurrentItem(0);
            }
        });

        LinearLayout asCalendarEventField = (LinearLayout) view.findViewById(R.id.as_calendar_event_field);
        asCalendarEventField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shedulerPager.setCurrentItem(1);
            }
        });


        shedulerPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageSelected(int i) {
                shedulerPager.getAdapter().notifyDataSetChanged();
                if(i == 0){

                    asListIcon.setImageDrawable(getResources().getDrawable(R.drawable.small_icon_list_orange100));
                    asCalendarIcon.setImageDrawable(getResources().getDrawable(R.drawable.small_icon_calendar_gray100));

                    asCalendarLabel.setTextColor(getResources().getColor(R.color.text_color_light));
                    asCalendarLabel.setTypeface(null,Typeface.NORMAL);
                    asListLabel.setTextColor(getResources().getColor(R.color.text_color_dark));
                    asListLabel.setTypeface(null,Typeface.BOLD);
                }

                if (i == 1) {
                    asListIcon.setImageDrawable(getResources().getDrawable(R.drawable.small_icon_list_gray100));
                    asCalendarIcon.setImageDrawable(getResources().getDrawable(R.drawable.small_icon_calendar_orange100));

                    asCalendarLabel.setTextColor(getResources().getColor(R.color.text_color_dark));
                    asCalendarLabel.setTypeface(null,Typeface.BOLD);

                    asListLabel.setTextColor(getResources().getColor(R.color.text_color_light));
                    asListLabel.setTypeface(null,Typeface.NORMAL);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {}

        });

        return view;
    }



    @Override
    public void onResume() {

        super.onResume();

        if(!extraData.equals("")){
            try {
                if (!adapterIsSet) {
                    parseData(new JSONObject(extraData));
                    adapterIsSet = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }



    private ArrayList<Long> getAdvsArrByDayBelonging(int dayToCheck) {

        ArrayList<Long> sameDayAdvsArr = new ArrayList<Long>();
/*
        for (Long advTimeStamp : advsTimeStampsArr) {
            if (convertTimeStampToDayOfYear(advTimeStamp) == dayToCheck)
                sameDayAdvsArr.add(advTimeStamp);
        }
*/
        return sameDayAdvsArr;
    }



    //todo: too many Calendar variables declarations
    private int convertTimeStampToDayOfYear(Long timeInMilliSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliSeconds);
        return calendar.get(Calendar.DAY_OF_YEAR);
    }




    private ArrayList<Integer> convertTimeStampArrayToDayArray(ArrayList<Long> timeArr) {

        ArrayList<Integer> daysArr = new ArrayList<Integer>();

        for (Long aTimeArr : timeArr) {
           // daysArr.add(convertTimeStampToDayOfYear(aTimeArr));
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




    private void parseData(JSONObject jsonObject) {

        advListFromJsonArr = new ArrayList<AdvData>();
        eachAdvDayOfYearArr = new ArrayList<Integer>();

        try {

            JSONArray itemsArr = jsonObject.getJSONArray("items");
            Calendar calendar = Calendar.getInstance();

            int len = itemsArr.length();
            for (int i = 0; i < len; i++) {
                AdvData advData = new AdvData();
                JSONObject dataJson = itemsArr.getJSONObject(i);
                advData.title = dataJson.getString("title");
                advData.startDateMillsSecs = 1000 * dataJson.getLong("date_start"); // unixtime gives seconds after 1970
                advData.endDateMillsSecs   = 1000 * dataJson.getLong("date_end");
                calendar.setTimeInMillis(advData.startDateMillsSecs);
                advData.dayOfYear = calendar.get(Calendar.DAY_OF_YEAR); //convertTimeStampToDayOfYear(data.startDateMillsSecs);
                advListFromJsonArr.add(advData);

                eachAdvDayOfYearArr.add(advData.dayOfYear);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            eachAdvDayOfYearArr.clear();
        }

        Collections.sort(advListFromJsonArr);

        shedulerPager.setAdapter(new ShedulerPageAdapter(getActivity()));
    }



    @Override
    public void onPause() {
        super.onPause();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        adapterIsSet = false;
    }


    private void saveEventToCalendar(String title, long startMills,long endMills) {
        // сохраняем в бд приложения
        AppSingleton.getInstance().getDBAdapter().saveAddedToCalendar(AppSingleton.getInstance().currentQR, startMills);

        // сохраняем в календарь на девайс напрямую (min sdk = 14)
        ContentResolver cr = getActivity().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DTSTART, startMills);
        values.put(CalendarContract.Events.DTEND, endMills);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        cr.insert( CalendarContract.Events.CONTENT_URI, values);

        Toast.makeText(getActivity(), "Событие добавлено в календарь", Toast.LENGTH_LONG).show();
    }



    private class SheduleListAdapter extends BaseAdapter {

        private ArrayList<ArrayList<AdvData>> dataArr;
        private LayoutInflater layoutInflater;

        private SheduleListAdapter(ArrayList<ArrayList<AdvData>> dataArr) {
            this.dataArr = dataArr;
            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE); //LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return dataArr.size();
        }

        @Override
        public ArrayList<AdvData> getItem(int position) {
            return dataArr.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View listItemView;

            if (convertView == null) {
                listItemView = layoutInflater.inflate(R.layout.layout_sheduler_list_item, null);
            } else{
                listItemView = convertView;
            }

            ArrayList<AdvData> sameDayArr = dataArr.get(position);
            LinearLayout advsContainer = (LinearLayout) listItemView.findViewById(R.id.same_day_advs_container);

            TextView dayTv = (TextView) listItemView.findViewById(R.id.date_belong_to);
            String htmlDate = "<b>"+ Utils.getDayNameFromTimeStamp(sameDayArr.get(0).startDateMillsSecs) +"  </b>"
                                   + Utils.getStrDateFromLong(sameDayArr.get(0).startDateMillsSecs, "dd.MM.yyyy");// можно брать любой другой индекс - у всех одинаковое значение

            dayTv.setText(Html.fromHtml(htmlDate));

            advsContainer.removeAllViews();
            for (final AdvData advData : sameDayArr) {

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                                                 RelativeLayout.LayoutParams.WRAP_CONTENT);
                RelativeLayout recordRL = (RelativeLayout) layoutInflater.inflate(R.layout.layout_same_day_record,null);
                advsContainer.addView(recordRL, lp);

                TextView time_h_m = (TextView) recordRL.findViewById(R.id.time_h_m);
                time_h_m.setText(Utils.getStrDateFromLong(advData.startDateMillsSecs, "hh:mm"));

                TextView title = (TextView) recordRL.findViewById(R.id.adv_title);
                title.setText(advData.title);

                final ImageView addBtn = (ImageView) recordRL.findViewById(R.id.add_to_calendar_btn);
                addBtn.setImageDrawable(getResources().getDrawable(advData.addedToCalendar ? R.drawable.event_done100 : R.drawable.add_to_calendar));
                if(!advData.addedToCalendar){
                    addBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            addBtn.setOnClickListener(null);
                            addBtn.setImageDrawable(getResources().getDrawable(R.drawable.event_done100));
                            advData.addedToCalendar = true;
                            String title = advData.title + " выходит в эфир";

                            saveEventToCalendar(title, advData.startDateMillsSecs, advData.endDateMillsSecs);
                        }
                    });
                }
            }

            return listItemView;
        }
    }




    private class ShedulerPageAdapter extends PagerAdapter {

        private Context context;
        private LayoutInflater inflater;

        public ShedulerPageAdapter(Context context) {
            this.context = context;
        }


        @Override
        public int getCount() {
            return 2;
        }


        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (object);
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = null;

            if (position == 0) {
                itemView = inflater.inflate(R.layout.fragment_single_list, container, false);
                setAsList(itemView);
            }

            if (position == 1) {
                itemView = inflater.inflate(R.layout.layout_calendar_page, container, false);
                setCalendar(itemView);
            }


            if (itemView != null) {
                container.addView(itemView);
            }

            return itemView;
        }


        private ArrayList<Date> getDatesToHighlight() {
            ArrayList<Date> highlightDates = new ArrayList<Date>();
            if (sortedByDayAdvsArr != null && sortedByDayAdvsArr.size() > 0) {
                Calendar calendar = Calendar.getInstance();
                for (ArrayList<AdvData> sameDayAdvs : sortedByDayAdvsArr) {
                    calendar.setTimeInMillis(sameDayAdvs.get(0).startDateMillsSecs);
                    highlightDates.add(calendar.getTime());
                }
            }

            return highlightDates;
        }


        private ArrayList<AdvData> getAdvsListByDayBelonging() {

            for (ArrayList<AdvData> advsList : sortedByDayAdvsArr) {

            }

            return new ArrayList<AdvData>();
        }

        private void setCalendar(View itemView) {
            Log.i("@","set as calendar");

            final LinearLayout sameDayContainerLL = (LinearLayout) itemView.findViewById(R.id.same_day_advs_container);
            final TextView currDateTV = (TextView) itemView.findViewById(R.id.date_belong_to);

            final Calendar startDate = Calendar.getInstance();
//            startDate.setTimeInMillis(advListFromJsonArr.get(0).startDateMillsSecs);
            startDate.setTimeInMillis(sortedByDayAdvsArr.get(0).get(0).startDateMillsSecs); // время начало первой передачи первого дня

            final Calendar endDate = Calendar.getInstance();
//            endDate.setTimeInMillis(advListFromJsonArr.get(advListFromJsonArr.size() - 1).startDateMillsSecs);
            endDate.setTimeInMillis(sortedByDayAdvsArr.get(sortedByDayAdvsArr.size()-1).get(0).startDateMillsSecs); //время начало первой передачи для последнего дня
            endDate.add(Calendar.DAY_OF_YEAR, 1); // баг в компоненте. последняя дата почему то не включается. приходится увеличивать день на единицу
            Log.i("@", "end date "+endDate.toString());
            CalendarPickerView  calendarView = (CalendarPickerView) itemView.findViewById(R.id.calendar_view);

            calendarView.init(startDate.getTime(), endDate.getTime())
                        .withHighlightedDates(getDatesToHighlight())
                        .inMode(CalendarPickerView.SelectionMode.SINGLE);


            //calendarView.highlightDates();

            final LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE); //LayoutInflater.from(mContext);

            calendarView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {

                @Override
                public void onDateSelected(Date date) {

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);

                    ArrayList<AdvData> sameDayAdvsArr = new ArrayList<AdvData>();
                    for (AdvData singleAdv : advListFromJsonArr) {
                        if (singleAdv.dayOfYear == cal.get(Calendar.DAY_OF_YEAR)) {
                            sameDayAdvsArr.add(singleAdv);
                        }
                    }
                    Collections.sort(sameDayAdvsArr);


                    if (sameDayAdvsArr.size() == 0) {
                        currDateTV.setText("Отсутсвуют данные для выбранной даты");
//                        Toast.makeText(getActivity(), "Отсутсвуют данные для выбранной даты", Toast.LENGTH_SHORT).show();
                    } else {
                        String htmlDate = "<b>" + Utils.getDayNameFromTimeStamp(sameDayAdvsArr.get(0).startDateMillsSecs) +"  </b>"
                                                + Utils.getStrDateFromLong(sameDayAdvsArr.get(0).startDateMillsSecs, "dd.MM.yyyy");
                        currDateTV.setText(Html.fromHtml(htmlDate));
                    }

                    sameDayContainerLL.removeAllViews();

                    for (int i = 0; i < sameDayAdvsArr.size(); i++) {

                        final int currIndex = i;
                        final AdvData advData = sameDayAdvsArr.get(i);

                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                                                         RelativeLayout.LayoutParams.WRAP_CONTENT);
                        RelativeLayout recordRL = (RelativeLayout) layoutInflater.inflate(R.layout.layout_same_day_record, null);
                        sameDayContainerLL.addView(recordRL, lp);

                        TextView time_h_m = (TextView) recordRL.findViewById(R.id.time_h_m);
                        time_h_m.setText(Utils.getStrDateFromLong(advData.startDateMillsSecs, "hh:mm"));

                        TextView title = (TextView) recordRL.findViewById(R.id.adv_title);
                        title.setText(advData.title);

                        final ImageView addBtn = (ImageView) recordRL.findViewById(R.id.add_to_calendar_btn);
                        addBtn.setImageDrawable(getResources().getDrawable(advData.addedToCalendar ? R.drawable.event_done100 : R.drawable.add_to_calendar));

                        if (!advData.addedToCalendar) {

                            addBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    addBtn.setOnClickListener(null);
                                    addBtn.setImageDrawable(getResources().getDrawable(R.drawable.event_done100));

                                    String title = advData.title + " выходит в эфир";
                                    saveEventToCalendar(title, advData.startDateMillsSecs, advData.endDateMillsSecs);

                                    advData.addedToCalendar = true;

                                }

                            });
                        }
                    }
                }

                @Override
                public void onDateUnselected(Date date) {

                }
            });


            //  todo: доделать вывод передач для сегодняшнего дня
            Calendar cal = Calendar.getInstance();
            ArrayList<AdvData> sameDayAdvsArr = new ArrayList<AdvData>();
            for (AdvData singleAdv : advListFromJsonArr) {
                if (singleAdv.dayOfYear == cal.get(Calendar.DAY_OF_YEAR)) {
                    sameDayAdvsArr.add(singleAdv);
                }
            }
            Collections.sort(sameDayAdvsArr);


            if (sameDayAdvsArr.size() == 0) {
                currDateTV.setText("");
            } else {
                String htmlDate = "<b>" + Utils.getDayNameFromTimeStamp(sameDayAdvsArr.get(0).startDateMillsSecs) +"  </b>"
                                        + Utils.getStrDateFromLong(sameDayAdvsArr.get(0).startDateMillsSecs, "dd.MM.yyyy");
                currDateTV.setText(Html.fromHtml(htmlDate));
            }

           /* sameDayContainerLL.removeAllViews();


            for (AdvData advData : sameDayAdvsArr) {

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                             LinearLayout.LayoutParams.WRAP_CONTENT);

                LinearLayout recordLL = new LinearLayout(getActivity());
                recordLL.setGravity(Gravity.LEFT);
                recordLL.setOrientation(LinearLayout.HORIZONTAL);

                TextView time_h_m = new TextView(getActivity());
                time_h_m.setTypeface(Typeface.DEFAULT_BOLD);
                time_h_m.setText(Utils.getStrDateFromLong(advData.startDateMillsSecs, "hh:mm"));
                time_h_m.setPadding(0, 10, 30, 10);
                time_h_m.setTextColor(getResources().getColor(R.color.text_color_dark));
                time_h_m.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
                recordLL.addView(time_h_m);


                TextView title = new TextView(getActivity());
                title.setText(advData.title);
                time_h_m.setTextColor(getResources().getColor(R.color.text_color_dark));
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                recordLL.addView(title);
                //recordLL.setLayoutParams(lp);

                sameDayContainerLL.addView(recordLL);

            }*/
            //*****************************

        }






        private void setAsList(View itemView) {
                     Log.i("@","set as list");
            ListView advsLV = (ListView) itemView.findViewById(R.id.content_list);
            advsLV.setAdapter(new SheduleListAdapter(sortedByDayAdvsArr));

        }



        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // Remove viewpager_item.xml from ViewPager
            (container).removeView((View) object);
        }


    }











}
