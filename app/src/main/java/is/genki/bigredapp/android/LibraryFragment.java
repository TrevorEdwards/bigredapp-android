package is.genki.bigredapp.android;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.TimeZone;

/**
 * Fragment to show the open status of Cornell's libraries
 */
public class LibraryFragment extends ListFragment {

    public static final String HOURS_URL = "http://mannservices.mannlib.cornell.edu/LibServices/showAllLibraryHoursForAcademicSemester.do?output=json";

    private Activity mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        fetchLibraryHours();

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
                                        ArrayList<HoursObject> allHours = new ArrayList<>();
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
        Iterator<String> it = lib.keys();
        while (it.hasNext())
            System.out.println(it.next());
        System.out.println(lib);
        Long start = null;
        Long end = null;

        //Find out the day of the week and grab the normal hours for that
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK); //Sunday = 1, Sat = 7
        try {
            String op = "null";
            String cl = "null";
            switch (day) {
                case 1:
                    op = lib.getJSONObject("semesterHoursPackage").getString("sunOpen");
                    cl = lib.getJSONObject("semesterHoursPackage").getString("sunClose");
                    break;
                case 2:
                    op = lib.getJSONObject("semesterHoursPackage").getString("monOpen");
                    cl = lib.getJSONObject("semesterHoursPackage").getString("monClose");
                    break;
                case 3:
                    op = lib.getJSONObject("semesterHoursPackage").getString("tueOpen");
                    cl = lib.getJSONObject("semesterHoursPackage").getString("tueClose");
                    break;
                case 4:
                    op = lib.getJSONObject("semesterHoursPackage").getString("wedOpen");
                    cl = lib.getJSONObject("semesterHoursPackage").getString("wedClose");
                    break;
                case 5:
                    op = lib.getJSONObject("semesterHoursPackage").getString("thuOpen");
                    cl = lib.getJSONObject("semesterHoursPackage").getString("thuClose");
                    break;
                case 6:
                    op = lib.getJSONObject("semesterHoursPackage").getString("friOpen");
                    cl = lib.getJSONObject("semesterHoursPackage").getString("friClose");
                    break;
                case 7:
                    op = lib.getJSONObject("semesterHoursPackage").getString("satOpen");
                    cl = lib.getJSONObject("semesterHoursPackage").getString("satClose");
                    break;
            }
            if (! op.equals("null"))
                start = Long.parseLong(op);
            if (! cl.equals("null"))
                end = Long.parseLong(cl);
        } catch (JSONException e){
            e.printStackTrace();
        }

        //TODO: Now parse the exception list in case today is different.

        return new HoursObject(start,end,name);
    }

    //Checks to see if a library is currently open based on its hours object
    private boolean isOpen(HoursObject lib){
        long now = Calendar.getInstance().getTimeInMillis();
        return (lib.closeUnix == null || lib.openUnix * 1000> now || lib.closeUnix * 1000 < now);
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

//            if (mTextColor == 0) {
//                mTextColor = holder.hoursTextView.getCurrentTextColor();
//            }

           // TODO setHoursText(holder.hoursTextView, mRightNowCal, hoursObject.calEventList);

            Resources res = mContext.getResources();
            holder.hoursTextView.setTextColor(res.getColor(R.color.closedColor));
            String htext = "closed";
            if (hoursObject.openUnix != null && hoursObject.closeUnix != null) {
                htext = "Open from " + DateUtils.formatDateRange(mContext, hoursObject.openUnix * 1000, hoursObject.closeUnix * 1000, DateUtils.FORMAT_SHOW_TIME);
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

}
