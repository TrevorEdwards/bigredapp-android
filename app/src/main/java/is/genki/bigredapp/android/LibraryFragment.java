package is.genki.bigredapp.android;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Fragment to show the open status of Cornell's libraries
 * Source for information: http://mannservices.mannlib.cornell.edu/LibServices/showAllLibraryHoursForAcademicSemester.do?output=json
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
                                            allHours.add(grabTodaysHours(response.getJSONObject(current)));
                                        }
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
     * Sets closeUnix to null if the library is closed today either normal or due
     * to a special circumstance.
     */
    private HoursObject grabTodaysHours(JSONObject lib){
        return null;
    }

    //Checks to see if a library is currently open based on its hours object
    private boolean isOpen(HoursObject lib){
        long now = Calendar.getInstance().getTimeInMillis();
        return (lib.closeUnix == null || lib.openUnix > now || lib.closeUnix < now);
    }

    //Holds the open time for a library today
    private class HoursObject {
        //Set these as null if the library is closed.
        Long openUnix;
        Long closeUnix;
        String name;

        public HoursObject(Long openUnix, Long closeUnix, String name){
            this.openUnix = openUnix;
            this.closeUnix = closeUnix;
            this.name = name;
        }
    }

}
