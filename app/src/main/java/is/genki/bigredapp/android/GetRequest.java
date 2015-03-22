package is.genki.bigredapp.android;

import android.app.Activity;
import android.content.Context;
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
 * To use, instantiate GetRequest, passing in an overridden onPostExecute,
 * past in the context to setContext, then call execute on the url
 */
public class GetRequest extends AsyncTask<String, Void, String> {

    public Context context;

    public GetRequest setContext(Context c) {
        this.context = c;
        return this;
    }

    /**
     * The async task to be done, returns the result to onPostExecute.
     * @param urls, currently only works with the first given.
     * @return string of json from given url.
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
     * Establishes HttpUrlConnection, retrieves the web page content as a InputStream
     * @return string of the InputStream
     * @throws IOException
     */
    private String downloadUrl(String myUrl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            is = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            if (is != null) {
                is.close();
            }
        }
    }

}