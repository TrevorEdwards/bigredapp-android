package is.genki.bigredapp.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Shows information for a specific event
 * Created by Trevor Edwards on 12/23/2015.
 */
public class EventActivity extends ActionBarActivity {

    public static final String KEY_TITLE = "EventActivity.TITLE";
    public static final String KEY_LINK = "EventActivity.LINK";
    public static final String KEY_MEDIA = "EventActivity.MEDIA";
    public static final String KEY_DESCRIPTION = "EventActivity.DESCRIPTION";
    public static final String KEY_DATE = "EventActivity.DATE";
    public static final String KEY_LATITUDE = "EventActivity.LATITUDE";
    public static final String KEY_LONGITUDE = "EventActivity.LONGITUDE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.event_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            //Find and parse title
            String title = extras.getString(KEY_TITLE);
            String formatTitle = "";
            if(title != null) {
                setTitle(title.substring(0, title.indexOf(":")));
                formatTitle = title.substring(title.indexOf(":") + 1, title.length()).trim();
                ((TextView) findViewById(R.id.title)).setText(formatTitle);
            }

            //Find and sanitize description
            String description = Html.fromHtml(extras.getString(KEY_DESCRIPTION)).toString();
            description = description.substring(0,description.indexOf("View on site |"));

            //Setup map link button if coordinates exist
            final String lat = extras.getString(KEY_LATITUDE);
            final String lon = extras.getString(KEY_LONGITUDE);
            Button b = (Button) findViewById(R.id.map);

            final String tempTitle = formatTitle;

            if(lat != null){
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Geo URL format: geo:latitude,longitude
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+lat+","+lon+"?q="+lat+","+lon+"("+tempTitle+")?z=16"));
                        startActivity(i);
                    }
                });
            } else{
                //We have no geo data so no need to have the button
                ((ViewManager)b.getParent()).removeView(b);
            }

            Button b2 = (Button) findViewById(R.id.link);

            final String link = extras.getString(KEY_LINK);

            b2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                        startActivity(i);
                    }
                });

            //Date formatting
            //Sample date:  2015-12-25T00:00:00-05:00
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            long timeInMilliseconds = 0;
            try {
                String date = extras.getString(KEY_DATE);
                if(date != null) {
                    Date mDate = sdf.parse(date.replace("T", " "));
                    timeInMilliseconds = mDate.getTime();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
           String readableDate =
                   DateUtils.getRelativeTimeSpanString(
                            timeInMilliseconds,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS)
                           .toString();

            ((TextView) findViewById(R.id.time)).setText(readableDate);

            ((TextView) findViewById(R.id.description)).setText(description);

            //Load and display image for the event
            new RetrieveFeedTask().execute(extras.getString(KEY_MEDIA));
        }

    }



    //We can't just load an image and stop everything, so here is an AsyncTask
    class RetrieveFeedTask extends AsyncTask<String, Void, Drawable> {

        private Exception exception;

        protected Drawable doInBackground(String... url) {
            try {
                InputStream is = (InputStream) new URL(url[0]).getContent();
                Drawable d = Drawable.createFromStream(is, "");

                //Get size for resizing image
                Point size = new Point();
                getWindowManager().getDefaultDisplay().getSize(size);
                Bitmap b = ((BitmapDrawable)d).getBitmap();
                Bitmap bitmapResized = Bitmap.createScaledBitmap(b, size.x, size.y/4, false);
                return new BitmapDrawable(getResources(), bitmapResized);
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(Drawable d) {
           if(this.exception != null) this.exception.printStackTrace();
            ((ImageView) findViewById(R.id.imageView)).setImageDrawable(d);
        }
    }

}
