package is.genki.bigredapp.android;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;

import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Fragment to show the open status of Cornell's libraries
 * Source for information: http://mannservices.mannlib.cornell.edu/LibServices/showAllLibraryHoursForAcademicSemester.do?output=json
 */
public class LibraryFragment extends ListFragment {

    private Activity mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

    }

    // Gets and returns the json object for library hours
    private JSONObject fetchLibraryHours(){
        return null;
    }


    /**Parses JSON for a library to generate its hours object.
     * Sets closeUnix to null if the library is closed today either normall or due
     * to a special circumstance.
     */
    private HoursObject grabTodaysHours(JSONObject lib){

        return null;
    }

    //Checks to see if a library is currently open based on its hours object
    private boolean isOpen(HoursObject lib){
        return false;
    }

    //Holds the open time for a library today
    private class HoursObject {
        //Set these as null if the library is closed.
        Long openUnix;
        Long closeUnix;

        public HoursObject(Long openUnix, Long closeUnix){
            this.openUnix = openUnix;
            this.closeUnix = closeUnix;
        }
    }

}
