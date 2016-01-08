package is.genki.bigredapp.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple activity to give the user some information about the app.
 */

public class AboutFragment extends Fragment {

    private static AppCompatActivity mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (AppCompatActivity) getActivity();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // see http://stackoverflow.com/questions/2734270/how-do-i-make-links-in-a-textview-clickable
        ((TextView) mContext.findViewById(R.id.github_link))
                .setMovementMethod(LinkMovementMethod.getInstance());
    }

}
