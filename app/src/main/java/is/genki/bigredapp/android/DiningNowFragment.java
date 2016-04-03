package is.genki.bigredapp.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Fragment containing a WebView of Cornell's Dining Now site
 * TODO: Add swipe-to-refresh, see:
 * https://developer.android.com/samples/SwipeRefreshListFragment/src/
 *  com.example.android.swiperefreshlistfragment/SwipeRefreshListFragmentFragment.html
 */
public class DiningNowFragment extends Fragment {

    public static final String DINING_URL = "http://now.dining.cornell.edu/";

    private static Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dining_now, container, false);

        WebView myWebView = (WebView) view.findViewById(R.id.webview);
        myWebView.setWebViewClient(new MyWebViewClient());
        myWebView.loadUrl(DINING_URL);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        Toast.makeText(mContext, "This app is not in any way affiliated with Cornell University or Cornell Dining.  See cornell.edu for the most accurate info.  See the about tab for any feedback about this app.", Toast.LENGTH_LONG).show();

        return view;
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals("now.dining.cornell.edu")) {
                //Allow the URL to load
                return false;
            }
            System.out.println(Uri.parse(url).getHost());
            // The link is not for a page on now.dining, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    }

}
