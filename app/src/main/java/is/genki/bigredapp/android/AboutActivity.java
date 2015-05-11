package is.genki.bigredapp.android;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**
 * A simple activity to give the user some information about the app.
 */

public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        // see http://stackoverflow.com/questions/2734270/how-do-i-make-links-in-a-textview-clickable
        ((TextView) findViewById(R.id.github_link))
                .setMovementMethod(LinkMovementMethod.getInstance());
    }

}
