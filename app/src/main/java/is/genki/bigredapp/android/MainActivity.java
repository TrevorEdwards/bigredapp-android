package is.genki.bigredapp.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ListViewFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Fragment containing a ListView of dining halls.
     */
    public static class ListViewFragment extends Fragment {

        private ListView listView;

        public ListViewFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            listView = (ListView) rootView.findViewById(R.id.listView);

            // https://developer.android.com/training/basics/network-ops/connecting.html#connection
            ConnectivityManager connMgr = (ConnectivityManager)
                    getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                String diningListUrl = "http://redapi-tious.rhcloud.com/dining";
                new DisplayDiningHallsTask().execute(diningListUrl);
            } else {
                Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }

            return rootView;
        }

        /**
         * https://developer.android.com/training/basics/network-ops/connecting.html#AsyncTask
         * Uses AsyncTask to create a task away from the main UI thread. This task takes a
         * URL string and uses it to create an HttpUrlConnection. Once the connection
         * has been established, the AsyncTask downloads the contents of the webpage as
         * an InputStream. Finally, the InputStream is converted into a string, which is
         * then used to display a UI by the AsyncTask's onPostExecute method.
         */
        private class DisplayDiningHallsTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... urls) {

                // params comes from the execute() call: params[0] is the url.
                try {
                    return downloadUrl(urls[0]);
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Unable to retrieve data", Toast.LENGTH_LONG).show();
                    return null;
                }
            }

            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(String result) {
                try {
                    // convert result to JSONArray, then get diningHall string list
                    JSONArray jsonArray = new JSONArray(result);
                    ArrayList<String> diningList = new ArrayList<>();
                    int len = jsonArray.length();
                    for (int i=0;i<len;i++){
                        diningList.add(jsonArray.get(i).toString());
                    }

                    // http://developer.android.com/guide/topics/ui/declaring-layout.html#AdapterViews
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_list_item_1, diningList);
                    listView.setAdapter(adapter);
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Data has invalid format", Toast.LENGTH_LONG).show();
                }
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
}
