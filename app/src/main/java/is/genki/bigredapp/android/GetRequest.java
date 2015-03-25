package is.genki.bigredapp.android;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * https://developer.android.com/training/basics/network-ops/connecting.html#AsyncTask
 * To use, instantiate GetRequest (while passing in an overridden onPostExecute),
 * then call setContext (passing in the current context) on that object,
 * then call execute (passing in the url) on that object
 */
public class GetRequest extends AsyncTask<String, Void, String> {

    private Context context;

    public GetRequest setContext(Context c) {
        this.context = c;
        return this;
    }

    /**
     * @param urls currently only works with the first given.
     * @return body of given url page
     * The async task to be done, returns the result to onPostExecute.
     */
    @Override
    protected String doInBackground(String... urls) {
        try {
            return downloadUrl(urls[0]);
        } catch (IOException e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, "Unable to retrieve data", Toast.LENGTH_LONG).show();
                }
            });
            return null;
        }
    }

    /**
     * https://developer.android.com/training/basics/network-ops/connecting.html#download
     * @param myUrl: url string
     * @return string of the InputStream
     * @throws IOException
     * Establishes HttpUrlConnection, retrieves the web page content as a InputStream
     */
    private String downloadUrl(String myUrl) throws IOException {
        InputStream inputStream = null;
        try {
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000); // milliseconds
            conn.setConnectTimeout(15000); // milliseconds
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect(); // starts the query
            inputStream = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static boolean isConnected(Activity activity) {
        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            Toast.makeText(activity, "No Internet Connection", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}