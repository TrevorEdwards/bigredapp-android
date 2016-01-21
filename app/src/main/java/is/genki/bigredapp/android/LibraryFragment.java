package is.genki.bigredapp.android;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;

/**
 * Fragment to show the open status of Cornell's libraries
 */
public class LibraryFragment extends ListFragment {

    public static final String HOURS_URL = "http://mannservices.mannlib.cornell.edu/LibServices/showAllLibraryHoursForAcademicSemester.do?output=json";

    public static final int MILLIS_IN_DAY = 86400000;

    private Activity mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        fetchLibraryHours();

        new AlertDialog.Builder(mContext)
                .setTitle("Notice")
                .setMessage("This feature is new and still experimental.  Please check www.library.cornell.edu/libraries for the most accurate information.")
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }



    // Gets and processes the json object for library hours
    private void fetchLibraryHours(){
        if (SingletonRequestQueue.isConnected(mContext)) {
            JsonObjectRequest jsonObjectRequest = (JsonObjectRequest)
                    new JsonObjectRequest(Request.Method.GET, HOURS_URL,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        ArrayList<HoursObject> allHours = initializeAlwaysOpenLibraries();
                                        Iterator<String> libNames = response.keys();
                                        while (libNames.hasNext()) {
                                            String current = libNames.next();
                                            allHours.add(grabTodaysHours(response.getJSONObject(current),current));
                                        }
                                        Collections.sort(allHours);
                                        ArrayAdapter<HoursObject> adapter = new LibraryListAdapter(mContext, R.layout.list_row_library, allHours);
                                        setListAdapter(adapter);
                                    } catch (JSONException e){
                                        e.printStackTrace();
                                    }
                                }
                            }, SingletonRequestQueue.getErrorListener(mContext))
                            .setRetryPolicy(SingletonRequestQueue.getRetryPolicy());
            SingletonRequestQueue.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
        }

    }


    /**Parses JSON for a library to generate its hours object.
     * Sets closeUnix to null if the library is closed today either normally or due
     * to a special circumstance.
     */
    private HoursObject grabTodaysHours(JSONObject lib, String name){
        Long start = null;
        Long end = null;
        try {
        JSONObject hoursPackage = lib.getJSONObject("semesterHoursPackage");

        //Find out the day of the week and grab the normal hours for that
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK); //Sunday = 1, Sat = 7

        //Exceptions are defined in ranges with times for each range.
        long now = cal.getTimeInMillis();
            JSONArray exceptions = lib.getJSONArray("exceptionHoursList");
            for(int i = 0; i < exceptions.length(); i++){
                JSONObject ex = exceptions.getJSONObject(i);
                String endDay = ex.getString("endDay");
                String startDay = ex.getString("startDay");
                if(!endDay.equals("null") && !startDay.equals(("null"))){
                    Long endDayLong = Long.parseLong(endDay) * 1000 + MILLIS_IN_DAY; //Their ranges are exclusive
                    Long startDayLong = Long.parseLong(startDay) * 1000;
                    if(now >= startDayLong && now <= endDayLong){
                        //We are in an exception, so we will just parse the custom hours list
                        hoursPackage = ex.getJSONObject("exceptionHoursPackage");
                        break;
                    }
                }
            }

            String op = "null";
            String cl = "null";
            switch (day) {
                case 1:
                    op = hoursPackage.getString("sunOpen");
                    cl = hoursPackage.getString("sunClose");
                    break;
                case 2:
                    op = hoursPackage.getString("monOpen");
                    cl = hoursPackage.getString("monClose");
                    break;
                case 3:
                    op = hoursPackage.getString("tueOpen");
                    cl = hoursPackage.getString("tueClose");
                    break;
                case 4:
                    op = hoursPackage.getString("wedOpen");
                    cl = hoursPackage.getString("wedClose");
                    break;
                case 5:
                    op = hoursPackage.getString("thuOpen");
                    cl = hoursPackage.getString("thuClose");
                    break;
                case 6:
                    op = hoursPackage.getString("friOpen");
                    cl = hoursPackage.getString("friClose");
                    break;
                case 7:
                    op = hoursPackage.getString("satOpen");
                    cl = hoursPackage.getString("satClose");
                    break;
            }
            if (! op.equals("null"))
                start = Long.parseLong(op) * 1000;
            if (! cl.equals("null"))
                end = Long.parseLong(cl) * 1000;
            if(start != null && end != null && start.equals(end)){
                start = null;
                end = null;
            }
        } catch (JSONException e){
            e.printStackTrace();
        }


        return new HoursObject(start,end,name);
    }

    //Checks to see if a library is currently open based on its hours object
    private boolean isOpen(HoursObject lib){
        long now = Calendar.getInstance().getTimeInMillis();
        return (lib.closeUnix == null || lib.openUnix> now || lib.closeUnix < now);
    }

    //Holds the open time for a library today
    private class HoursObject implements Comparable<HoursObject> {
        //Set these as null if the library is closed.
        Long openUnix;
        Long closeUnix;
        String name;

        public HoursObject(Long openUnix, Long closeUnix, String name){
            this.openUnix = openUnix;
            this.closeUnix = closeUnix;
            this.name = name;
        }

        //Show open libraries, then order alphabetically
        public int compareTo(HoursObject o){
            if(o == null) return 1;
            if(isOpen(this) && !isOpen(o)) return 1;
            else if(!isOpen(this) && isOpen(o)) return -1;
            return(this.name.compareTo(o.name));
        }
    }

    /**
     * http://developer.android.com/guide/topics/ui/declaring-layout.html#FillingTheLayout
     * Returns a custom view for an array of CalEvents on their way through the mListView adapter
     */
    public class LibraryListAdapter extends ArrayAdapter<HoursObject> {

        final int mResource;
        final LayoutInflater mInflater;
        Calendar mRightNowCal;

        public LibraryListAdapter(Context context, int res, ArrayList<HoursObject> items) {
            super(context, res, items);
            this.mResource = res;
            mInflater = LayoutInflater.from(context);
            mRightNowCal = Calendar.getInstance();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HoursObject hoursObject = getItem(position);
            mRightNowCal = Calendar.getInstance();

            // http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
            // http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
            LibraryViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(this.mResource, parent, false);
                holder = new LibraryViewHolder();
                holder.nameTextView = (TextView) convertView.findViewById(R.id.library_name);
                holder.hoursTextView = (TextView) convertView.findViewById(R.id.library_hours);
                holder.libraryName = hoursObject.name;
                convertView.setTag(holder);
            } else {
                holder = (LibraryViewHolder) convertView.getTag();
                holder.libraryName = hoursObject.name;
            }

            Resources res = mContext.getResources();
            holder.hoursTextView.setTextColor(res.getColor(R.color.closedColor));
            String htext = "closed";
            if (hoursObject.openUnix != null && hoursObject.closeUnix != null) {
                htext = "open from " + DateUtils.formatDateRange(mContext, hoursObject.openUnix, hoursObject.closeUnix, DateUtils.FORMAT_SHOW_TIME);
                if (isOpen(hoursObject)) {
                    holder.hoursTextView.setTextColor(res.getColor(R.color.openGreen));
                } else {
                    holder.hoursTextView.setTextColor(res.getColor(R.color.almostOpenGreen));
                }
            }
            holder.hoursTextView.setText(htext);
            holder.nameTextView.setText(hoursObject.name);

            return convertView;
        }

        // http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
        class LibraryViewHolder {
            TextView nameTextView;
            TextView hoursTextView;
            String libraryName;
        }
    }

    //Cornell has 24/7 libraries, so this generates an ArrayList with those
    private ArrayList<HoursObject> initializeAlwaysOpenLibraries(){
        ArrayList<HoursObject> hoursObjectArrayList = new ArrayList<>();
        //Hard coded libraries for now
        hoursObjectArrayList.add(generateAlwaysOpen("Carpenter Library (24/7)"));
        hoursObjectArrayList.add(generateAlwaysOpen("Medical Center Archives (24/7)"));
        hoursObjectArrayList.add(generateAlwaysOpen("Medical Library (24/7)"));
        hoursObjectArrayList.add(generateAlwaysOpen("Physical Sciences Library (24/7)"));

        return hoursObjectArrayList;
    }

    //Generates an hours object for a 24/7 library
    private HoursObject generateAlwaysOpen(String libraryName){
        //Calculate the milli times for the day's start and end
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Long dayStart = today.getTimeInMillis();
        today.set(Calendar.HOUR_OF_DAY, 23);
        today.set(Calendar.MINUTE, 59);
        today.set(Calendar.SECOND, 59);
        today.set(Calendar.MILLISECOND, 99);
        Long dayEnd = today.getTimeInMillis();

        return new HoursObject(dayStart, dayEnd, libraryName);
    }

}
