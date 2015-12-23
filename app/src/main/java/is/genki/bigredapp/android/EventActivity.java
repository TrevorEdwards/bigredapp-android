package is.genki.bigredapp.android;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.dining_location_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            setTitle(extras.getString(KEY_TITLE));
        }

        //TODO: Loading an image:
        /**
          public static Drawable LoadImageFromWebOperations(String url) {
         try {
         InputStream is = (InputStream) new URL(url).getContent();
         Drawable d = Drawable.createFromStream(is, "src name");
         return d;
         } catch (Exception e) {
         return null;
         }
         }
         */


    }

}
