package is.genki.bigredapp.tests;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import is.genki.bigredapp.android.AboutFragment;
import is.genki.bigredapp.android.MainActivity;
import is.genki.bigredapp.android.R;

/**
 * A sample of the testing we could be doing on an activity that doesn't need testing.
 * https://developer.android.com/training/activity-testing/activity-basic-testing.html
 */
public class AboutFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity myMainActivity;
    private TextView aboutText;

    public AboutFragmentTest(){
        super(MainActivity.class);
    }

    public void testPreconditions() {
        assertNotNull("myAboutActivity is null", myMainActivity);
        assertNotNull("aboutText is null", aboutText);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myMainActivity = getActivity();

        Bundle args = new Bundle();
        Fragment fragment = new AboutFragment();
        fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        myMainActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        //wait for it to load
        Thread.sleep( 5000 );
        aboutText =
                (TextView) myMainActivity
                        .findViewById(R.id.textView2);
    }

    /**
     * Test that the about text is what we set it to be....
     */
    public void testAboutText() {
        final String expected =
                myMainActivity.getString(R.string.about_description);
        final String actual = aboutText.getText().toString();
        assertEquals(expected, actual);
    }
}
