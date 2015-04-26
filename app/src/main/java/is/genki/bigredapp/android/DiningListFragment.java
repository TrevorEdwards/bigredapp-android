package is.genki.bigredapp.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fragment containing a ListView of dining halls.
 * Each dining hall name can be tapped to get it's menu for today.
 */
public class DiningListFragment extends Fragment {

    private static final long MS_IN_2_WEEKS = 1209600000;
    private static final int MS_IN_HOUR = 3600000;
    public static final String BASE_URL = "http://redapi-tious.rhcloud.com/dining";
    private static final String DINING_LIST_KEY = "DINING_LIST_KEY";
    private static final String DINING_LIST_DATE_KEY = "DINING_LIST_DATE_KEY";
    private static final int NUM_DAYS_OF_EVENTS_TO_GET = 6;
    
    private static Context mContext;
    private ListView mListView;
    private ArrayList<String> mDiningList;
    private SharedPreferences mPreferences;
    private DateFormat mDateFormat;
    private static int mTextColor;

    public DiningListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        mDiningList = new ArrayList<>();

        // to parse: 2015-04-25T16:30:00.000Z
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listView);

        // http://developer.android.com/guide/topics/ui/declaring-layout.html#HandlingUserSelections
        AdapterView.OnItemClickListener mListViewClickHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                String diningHall = mDiningList.get(position);
                Intent intent = new Intent(mContext, DiningLocationActivity.class);
                intent.putExtra(DiningLocationActivity.KEY_DINING_HALL, diningHall);

                // ViewCompat.setTransitionName(view, "shared_transition");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(
                        v, 0, 0, v.getWidth(), v.getHeight());
                mContext.startActivity(intent, options.toBundle());
            }
        };
        mListView.setOnItemClickListener(mListViewClickHandler);

        mPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        // check for a cached dining list to use
        final String cachedDiningList = mPreferences.getString(DINING_LIST_KEY, null);
        final long cachedDiningListDate = mPreferences.getLong(DINING_LIST_DATE_KEY, 0);
        if (cachedDiningList == null || cachedDiningListDate == 0 ||
                System.currentTimeMillis() - cachedDiningListDate >= MS_IN_2_WEEKS) {
            // no cached diningList that's younger than a week, so let's request a new one
            getDiningList();
        } else {
            // we have a cached diningList that's younger than a week, so let's use it
            try {
                JSONArray jsonDiningList = new JSONArray(cachedDiningList);
                int len = jsonDiningList.length();
                for (int i = 0; i < len; i++) {
                    mDiningList.add((String) jsonDiningList.get(i));
                }
                // now we can get the calendar events for the list of dining halls
                getDiningCalendarEvents();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return rootView;
    }

    // helper for getDiningCalendarEvents()
    private static String getRequestCalFormat(Calendar cal) {
        return cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) +
                cal.get(Calendar.DAY_OF_MONTH) + "," + cal.get(Calendar.YEAR);
    }

    /**
     * Handles the response from getDiningCalendarEvents().
     * Sets mListView's adapter on the parsed information.
     */
    private void handleDiningCalendarEvents (String result) {
        try {
            if (result == null) throw new JSONException("Request timed out");

            ArrayList<NameCalEventList> nameCalEventLists = new ArrayList<>();

            Calendar cal = Calendar.getInstance();
            int offsetUTC = (TimeZone.getDefault().getOffset(cal.getTimeInMillis())) / MS_IN_HOUR;

            // parse the calendar data into a ArrayList<NameCalEventList>
            JSONObject jsonResult = new JSONObject(result);
            for (String name : mDiningList) {

                JSONArray jsonEventList = jsonResult.getJSONArray(name);
                int len = jsonEventList.length();
                ArrayList<CalEvent> calEventList = new ArrayList<>();
                for (int i=0; i<len; i++) {
                    JSONObject jsonEvent = jsonEventList.getJSONObject(i);

                    CalEvent calEvent = new CalEvent();
                    String summary = jsonEvent.getString("summary").toLowerCase();
                    // we only care about open events
                    if (!summary.equals("closed")) {
                        Date startDate = mDateFormat.parse(jsonEvent.getString("start"));
                        cal = Calendar.getInstance();
                        cal.setTime(startDate);
                        cal.add(Calendar.HOUR_OF_DAY, offsetUTC);
                        calEvent.startCal = cal;

                        Date endDate = mDateFormat.parse(jsonEvent.getString("end"));
                        cal = Calendar.getInstance();
                        cal.setTime(endDate);
                        cal.add(Calendar.HOUR_OF_DAY, offsetUTC);
                        calEvent.endCal = cal;

                        calEventList.add(calEvent);
                    }
                }
                Collections.sort(calEventList);
                nameCalEventLists.add(new NameCalEventList(name, calEventList));
            }


            // http://developer.android.com/guide/topics/ui/declaring-layout.html#AdapterViews
            ArrayAdapter<NameCalEventList> adapter = new DiningHallListAdapter(mContext,
                    R.layout.dining_list_row, nameCalEventLists);
            mListView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Uses a GetRequest to RedAPI to get the calendar events for all the dining halls.
     * Assumes mDiningList has already been set.
     * Calls handleDiningCalendarEvents on what is returned.
     */
    private void getDiningCalendarEvents() {
        final StringBuilder builder = new StringBuilder();
        for(String s : mDiningList) {
            builder.append(s).append(',');
        }
        final String commaSeparatedDiningHalls = builder.toString();

        // create the date range of CalEvents to get for each location
        final Calendar rightNow = Calendar.getInstance();
        String dateRange = getRequestCalFormat(rightNow) + "-";
        rightNow.add(Calendar.DATE, NUM_DAYS_OF_EVENTS_TO_GET);
        dateRange = dateRange + getRequestCalFormat(rightNow);
        final String diningHallCalendarsUrl = DiningListFragment.BASE_URL + "/event/" + commaSeparatedDiningHalls + "/" + dateRange;
        Log.d("DiningListFragment", diningHallCalendarsUrl);

        new GetRequest() {
            @Override
            protected void onPostExecute(String result) {
                handleDiningCalendarEvents(result);
            }
        }.setContext(mContext).execute(diningHallCalendarsUrl);
    }

    /**
     * Uses a GetRequest to RedAPI to populate mDiningList with the String list of dining halls.
     * After it is done, calls getDiningCalendarEvents() now that mDiningList is set
     */
    private void getDiningList() {
        if (GetRequest.isConnected(getActivity())) {
            // Async Task to get the list of dining halls
            new GetRequest() {
                @Override
                protected void onPostExecute(String result) {
                    try {
                        // cache the result
                        mPreferences.edit().putString(DINING_LIST_KEY, result).apply();
                        mPreferences.edit().putLong(DINING_LIST_DATE_KEY, System.currentTimeMillis()).apply();

                        // convert result to JSONArray, then get diningHall string list
                        JSONArray jsonArray = new JSONArray(result);
                        int len = jsonArray.length();
                        for (int i=0; i<len; i++) {
                            mDiningList.add(jsonArray.getString(i));
                        }

                        // now get the dining calendar events using this data
                        getDiningCalendarEvents();
                    } catch (JSONException e) {
                        Toast.makeText(mContext, "Data has invalid format", Toast.LENGTH_LONG).show();
                    }
                }
            }.setContext(mContext).execute(BASE_URL);
        }
    }

    /**
     * @param cal - a standard Calendar object
     * @return Returns a human-friendly string of hh:mm am|pm of the given cal
     */
    private static String getTime(Calendar cal) {
        String minutePad = "";
        int minute = cal.get(Calendar.MINUTE); // returns [0,59]
        if (minute < 10) {
            minutePad += "0";
        }
        String ret = cal.get(Calendar.HOUR) + ":" + minutePad + minute + " " +
                cal.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US).toLowerCase();
        switch (ret) {
            case "0:00 am":
                return "midnight";
            case "0:00 pm":
                return "noon";
            default:
                return ret;
        }
    }

    /**
     * @param rightNow - Calendar event for current time
     * @param calEventList - List of CalEvents of a location
     * Basically, the idea is to loop through the CalEvents, checking current time compared to it.
     * If we are before it, we stop, saying we are closed until the event starts.
     * If we are in it, we stop, saying we are open until the event ends.
     * If nothing is found (this is looping through the next NUM_DAYS_OF_EVENTS_TO_GET days),
     *  then just return "closed".
     */
    private static void setHoursText(TextView hoursTextView, Calendar rightNow, ArrayList<CalEvent> calEventList) {
        String hoursText = "closed";
        boolean isOpen = false;
        boolean isAlmostOpen = false; // if it is closed now but will be open within next 2 hours
        for (CalEvent e : calEventList) {
            Calendar startCal = e.startCal;
            Calendar endCal = e.endCal;
            if (rightNow.after(startCal) && rightNow.before(endCal)) {
                isOpen = true;
                hoursText = "open until " + getTime(endCal);
                break; // stop loop here
            } else if (rightNow.before(startCal)) {
                String dayText = "";
                int eventDayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
                int rightNowDayOfWeek = rightNow.get(Calendar.DAY_OF_WEEK);

                int dayDiff = eventDayOfWeek - rightNowDayOfWeek; // difference in day between event and today
                if (dayDiff == 0) {
                    if (startCal.get(Calendar.HOUR_OF_DAY) - rightNow.get(Calendar.HOUR_OF_DAY) <= 2) {
                        isAlmostOpen = true;
                    }
                    hoursText = "opens at " + dayText + getTime(startCal);
                    break; // stop loop here
                } else if (dayDiff == 1) {
                    dayText += "tomorrow, ";
                } else {
                    dayText += startCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) + ", ";
                }
                hoursText = "closed until " + dayText + getTime(startCal);
                break; // stop loop here
            }
        }
        hoursTextView.setText(hoursText);
        int textColor = 0;
        Resources res = mContext.getResources();
        if (isOpen) {
            textColor = res.getColor(R.color.openGreen);
        } else if (isAlmostOpen) {
            textColor = res.getColor(R.color.almostOpenGreen);
        } else {
            textColor = mTextColor;
        }
        hoursTextView.setTextColor(textColor);
    }

    /**
     * Classes for the DiningHallListAdapter
     */
    public static class CalEvent implements Comparable<CalEvent> {
        public Calendar startCal;
        public Calendar endCal;

        @Override
        public int compareTo(CalEvent cOther) {
            return this.startCal.compareTo(cOther.startCal);
        }
    }

    public static class NameCalEventList {
        public String name;
        public ArrayList<CalEvent> calEventList;

        public NameCalEventList(String name, ArrayList<CalEvent> calEventList) {
            this.name = name;
            this.calEventList = calEventList;
        }
    }

    /**
     * http://developer.android.com/guide/topics/ui/declaring-layout.html#FillingTheLayout
     * Returns a custom view for an array of CalEvents on their way through the mListView adapter
     */
    public class DiningHallListAdapter extends ArrayAdapter<NameCalEventList> {

        int mResource;
        LayoutInflater mInflater;
        final Pattern p = Pattern.compile("\\b([a-z])");
        Calendar mRightNowCal;

        public DiningHallListAdapter(Context context, int res, ArrayList<NameCalEventList> items) {
            super(context, res, items);
            this.mResource = res;
            mInflater = LayoutInflater.from(context);
            mRightNowCal = Calendar.getInstance();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NameCalEventList nameCalEventList = getItem(position);

            // http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
            // http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
            DiningListViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(this.mResource, parent, false);
                holder = new DiningListViewHolder();
                holder.nameTextView = (TextView) convertView.findViewById(R.id.dining_list_row_name);
                holder.hoursTextView = (TextView) convertView.findViewById(R.id.dining_list_row_hours);
                convertView.setTag(holder);
            } else {
                holder = (DiningListViewHolder) convertView.getTag();
            }

            if (mTextColor == 0) {
                mTextColor = holder.hoursTextView.getCurrentTextColor();
            }

            setHoursText(holder.hoursTextView, mRightNowCal, nameCalEventList.calEventList);
//            holder.hoursTextView.setText(getHoursText(mRightNowCal, nameCalEventList.calEventList));

            // parse the name to make it pretty
            String name = nameCalEventList.name;
            name = name.replace("_", " ");
            Matcher m = p.matcher(name);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, m.group(1).toUpperCase());
            }
            m.appendTail(sb);
            holder.nameTextView.setText(sb.toString());

            return convertView;
        }

        // http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
        class DiningListViewHolder {
            TextView nameTextView;
            TextView hoursTextView;
        }
    }
}