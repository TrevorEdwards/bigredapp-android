package is.genki.bigredapp.android;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Shows a list of events from Cornell's event data
 */
public class EventListFragment extends ListFragment {

    private static Context mContext;
    public static final String REQUEST_STRING = "https://redevents.herokuapp.com/events";
    public static final long EVENT_DAY_NUMBER = 7; //We only care about events for the next 7 days

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        populateEvents();


        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
       // getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        EventObj ref = (EventObj) l.getAdapter().getItem(position);
        Intent intent = new Intent(mContext, EventActivity.class);
        intent.putExtra(EventActivity.KEY_TITLE, ref.title);
        intent.putExtra(EventActivity.KEY_DATE_STRING, ref.dateString);
        intent.putExtra(EventActivity.KEY_LINK, ref.link);
        intent.putExtra(EventActivity.KEY_MEDIA, ref.media);
        intent.putExtra(EventActivity.KEY_DESCRIPTION, ref.description);
        intent.putExtra(EventActivity.KEY_DATE, ref.date);
        intent.putExtra(EventActivity.KEY_LATITUDE, ref.lat);
        intent.putExtra(EventActivity.KEY_LONGITUDE, ref.lon);

        // ViewCompat.setTransitionName(view, "shared_transition");
        ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(
                v, 0, 0, v.getWidth(), v.getHeight());
        mContext.startActivity(intent, options.toBundle());
    }

    /**
     * Requests Cornell's event data
     */
    private void populateEvents(){
        //Fetch data from the website
        // See the "SingletonRequestQueue" Class
        String request = REQUEST_STRING + "/" + (Calendar.getInstance().getTimeInMillis() +  (EVENT_DAY_NUMBER * 24 * 60 * 60 * 1000));
        JsonArrayRequest jArrRequest =  (JsonArrayRequest)
                new JsonArrayRequest(Request.Method.GET, request,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                ArrayList<EventObj> eventArr = new ArrayList<>();
                                List<EventObj> cEvents = convertEvents(response);
                                eventArr.addAll(cEvents);
                                if( cEvents != null){
                                    ArrayAdapter<EventObj> adapter = new EventListAdapter(mContext, R.layout.list_row_event, eventArr);
                                    setListAdapter(adapter);
                                }
                            }
                        }, SingletonRequestQueue.getErrorListener(mContext))
                        .setRetryPolicy(SingletonRequestQueue.getRetryPolicy());
        SingletonRequestQueue.getInstance(mContext).addToRequestQueue(jArrRequest);
    }

    /**
     * Converts event xml into a usable state
     */
    private List<EventObj> convertEvents(JSONArray eventsArray){
        int length = eventsArray.length();
        ArrayList<EventObj> ret = new ArrayList<>();

        for(int i = 0; i < length; i++){
            try {
                JSONObject jObj = eventsArray.getJSONObject(i);
                String lat = null;
                String lon = null;

                try {
                    lat = jObj.getString("geoLat");
                    lon = jObj.getString("geoLon");
                }
                catch(JSONException e){
                    //Do nothing, we can still use the data
                }

                EventObj obj = new EventObj(
                        jObj.getString("title"),
                        jObj.getString("readableDate"),
                        jObj.getString("description"),
                        jObj.getString("link"),
                        jObj.getString("startTime"),
                        jObj.getString("mediaURL"),
                        lat,
                        lon
                );
                ret.add(obj);
            }
            catch(JSONException e){
                continue;
            }
        }

        return ret;
    }

    class EventObj {

        String title;
        String dateString;
        String description;
        String link;
        String date;
        String media;
        String lat;
        String lon;

        public EventObj(String dt,
                        String dateString,
                        String ds,
                        String lk,
                        String dat,
                        String med,
                        String lat,
                        String lon){
            this.title = dt;
            this.dateString = dateString;
            this.description = ds;
            this.link = lk;
            this.date = dat;
            this.media = med;
            this.lat = lat;
            this.lon = lon;
        }

        public String toString(){
            return title.substring(0,Math.min(title.length(),80)); //Limit description lengths
        }
    }


    public class EventListAdapter extends ArrayAdapter<EventObj> {

        final int mResource;
        final LayoutInflater mInflater;
        Calendar mRightNowCal;

        public EventListAdapter(Context context, int res, ArrayList<EventObj> items) {
            super(context, res, items);
            this.mResource = res;
            mInflater = LayoutInflater.from(context);
            mRightNowCal = Calendar.getInstance();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            EventObj eventObject = getItem(position);
            mRightNowCal = Calendar.getInstance();

            // http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
            // http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
            EventViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(this.mResource, parent, false);
                holder = new EventViewHolder();
                holder.nameTextView = (TextView) convertView.findViewById(R.id.event_name);
                holder.hoursTextView = (TextView) convertView.findViewById(R.id.event_hours);
                convertView.setTag(holder);
                holder.eObj = eventObject;
            } else {
                holder = (EventViewHolder) convertView.getTag();
            }

            Resources res = mContext.getResources();
            holder.hoursTextView.setTextColor(res.getColor(R.color.primaryDark));
            holder.hoursTextView.setText(eventObject.dateString);
            holder.nameTextView.setText(eventObject.title);

            return convertView;
        }

        // http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
        class EventViewHolder {
            TextView nameTextView;
            TextView hoursTextView;
            EventObj eObj;
        }
    }

}
