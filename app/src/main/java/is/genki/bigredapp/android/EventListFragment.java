package is.genki.bigredapp.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.ListFragment;
import android.util.Xml;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows a list of events from Cornell's event data
 * Created by Trevor Edwards on 12/20/2015.
 */
public class EventListFragment extends ListFragment {

    private static Context mContext;
    public static final String REQUEST_STRING = "http://events.cornell.edu/calendar.xml";

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
        super.onListItemClick(l, v, position, id); //TODO Refactor to a custom view object that can be passed
        EventObj ref = (EventObj) l.getAdapter().getItem(position);
        Intent intent = new Intent(mContext, EventActivity.class);
        intent.putExtra(EventActivity.KEY_TITLE, ref.title);
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
        StringRequest stringRequest = (StringRequest)
                new StringRequest(Request.Method.GET, REQUEST_STRING,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                List cEvents = convertEvents(response);
                                if( cEvents != null){
                                    ArrayAdapter<EventObj> mAdapter = new ArrayAdapter<EventObj>(mContext, android.R.layout.simple_list_item_1, cEvents);
                                    setListAdapter(mAdapter);
                                }
                            }
                        }, SingletonRequestQueue.getErrorListener(mContext))
                        .setRetryPolicy(SingletonRequestQueue.getRetryPolicy());
        SingletonRequestQueue.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    /**
     * Converts event xml into a usable state
     */
    private List convertEvents(String xml){
        //See http://developer.android.com/training/basics/network-ops/xml.html
        EventXMLParser exmlp = new EventXMLParser();
        try{
            return exmlp.parse(xml);
        }
        catch( Exception e){
            e.printStackTrace();
            return null;
        }
    }

    class EventObj {

        String title;
        String description;
        String link;
        String date;
        String media;
        String lat;
        String lon;

        public EventObj(String dt, String ds, String lk, String dat, String med, String lat, String lon){
            title = dt;
            description = ds;
            link = lk;
            date = dat;
            media = med;
            this.lat = lat;
            this.lon = lon;
        }

        public String toString(){
            return title.substring(0,Math.min(title.length(),80)); //Limit description lengths
        }
    }

    class EventXMLParser {
        //See http://developer.android.com/training/basics/network-ops/xml.html

        public List parse(String inStr) throws XmlPullParserException, IOException {
            InputStream in = new ByteArrayInputStream(inStr.getBytes(StandardCharsets.UTF_8));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                return readFeed(parser);
            } finally {
                in.close();
            }
        }

        private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
            List<EventObj> entries = new ArrayList<>();

            parser.require(XmlPullParser.START_TAG, null, "rss"); //TODO Convert to align with XML schema
            parser.next();
            parser.require(XmlPullParser.START_TAG, null, "channel");

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the entry tag
                if (name.equals("item")) {
                    entries.add(readEventObj(parser));
                } else {
                    skip(parser);
                }
            }
            return entries;
        }

        private EventObj readEventObj(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, null, "item");
            String title = null;
            String description = null;
            String link = null;
            String date = null;
            String media = null;
            String lat = null;
            String lon = null;

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                switch(name){
                    case "title":  title = readGeneric(parser,"title");
                        break;
                    case "description": description = readGeneric(parser,"description");
                        break;
                    case "link":  link = readGeneric(parser,"link");
                        break;
                    case "dc:date":  date = readGeneric(parser, "dc:date");
                        break;
                    case "geo:lat":  lat = readGeneric(parser, "geo:lat");
                        break;
                    case "geo:long":  lon = readGeneric(parser, "geo:long");
                        break;
                    case "media:content": media = readMedia(parser);
                        break;
                    default: skip(parser);
                        break;
                }
            }
            return new EventObj(title, description, link, date, media, lat, lon);
        }

        private String readGeneric(XmlPullParser parser,String pull) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, null, pull);
            String title = readText(parser);
            parser.require(XmlPullParser.END_TAG, null, pull);
            return title;
        }

        private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
            String result = "";
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.getText();
                parser.nextTag();
            }
            return result;
        }

        private String readMedia(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, null, "media:content");
            String result = parser.getAttributeValue(1);
            parser.next();
            parser.require(XmlPullParser.END_TAG, null, "media:content");
            return result;
        }

        private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                }
            }
        }
    }

}
