package is.genki.bigredapp.android;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Fragment to show the open status of Cornell's libraries
 */
public class LibraryFragment extends ListFragment {

    public static final String HOURS_URL = "http://redevents-trevtrev.rhcloud.com/libraries";

    private Activity mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        fetchLibraryHours();

        Toast.makeText(mContext, "This feature is new.  See library.cornell.edu/libraries for the most accurate info.", Toast.LENGTH_SHORT).show();
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
                                        JSONArray hoursArr = response.getJSONArray("hours");
                                        for(int i = 0; i < hoursArr.length(); i++){
                                            JSONObject current = hoursArr.getJSONObject(i);
                                            allHours.add(grabTodaysHours(current));
                                        }
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
    private HoursObject grabTodaysHours(JSONObject lib){

        try {
            return new HoursObject(lib.getString("hours"), lib.getString("name"));
        } catch(JSONException e){
            return null;
        }
    }

    //Holds the open time for a library today
    private class HoursObject{
        //Set these as null if the library is closed.
        String hours;
        String name;

        public HoursObject(String hours, String name){
            this.hours = hours;
            this.name = name;
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
            holder.hoursTextView.setTextColor(res.getColor(R.color.primaryDark));
            String htext = hoursObject.hours;
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
