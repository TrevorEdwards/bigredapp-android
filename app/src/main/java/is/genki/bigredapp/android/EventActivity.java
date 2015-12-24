package is.genki.bigredapp.android;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.event_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString(KEY_TITLE);

            setTitle(title.substring(0,title.indexOf(":")));
            ((TextView) findViewById(R.id.title)).setText(title.substring(title.indexOf(":")+1,title.length()));
            String description = Html.fromHtml(extras.getString(KEY_DESCRIPTION)).toString();
            description = description.substring(0,description.indexOf("View on site |"));
            ((TextView) findViewById(R.id.description)).setText(description);
            ((TextView) findViewById(R.id.link)).setText(extras.getString(KEY_LINK));

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
                Drawable d = Drawable.createFromStream(is, "src name");

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
