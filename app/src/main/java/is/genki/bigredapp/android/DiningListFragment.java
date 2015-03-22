package is.genki.bigredapp.android;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Fragment containing a ListView of dining halls.
 * Each dining hall name can be tapped to get it's menu for today.
 */
public class DiningListFragment extends Fragment {

    private final String BASE_URL = "http://redapi-tious.rhcloud.com/dining";
    private final String[] MEALS_LIST = {"Breakfast", "Lunch", "Dinner"};
    private ListView mListView;
    private ArrayList<String> mDiningList;

    public DiningListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listView);

        // http://developer.android.com/guide/topics/ui/declaring-layout.html#HandlingUserSelections
        AdapterView.OnItemClickListener mListViewClickHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                handleMenuResponse(mDiningList.get(position));
            }
        };
        mListView.setOnItemClickListener(mListViewClickHandler);

        getDininglist();

        return rootView;
    }

    /**
     * https://developer.android.com/training/basics/network-ops/connecting.html#connection
     * Makes a Toast if there is no internet.
     * @param c the context within which to check connectivity.
     * @return the device is connected.
     */
    private static boolean isConnected(Context c) {
        ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            Toast.makeText(c, "No Internet Connection", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * Populates mDiningList with the String list of dining halls.
     * Sets mListView's adapter to this list.
     */
    private void getDininglist() {
        if (isConnected(getActivity())) {
            // Async Task to get the list of dining halls
            new GetRequest() {
                @Override
                protected void onPostExecute(String result) {
                    try {
                        // convert result to JSONArray, then get diningHall string list
                        JSONArray jsonArray = new JSONArray(result);
                        mDiningList = new ArrayList<>();
                        int len = jsonArray.length();
                        for (int i=0; i<len; i++){
                            mDiningList.add(jsonArray.getString(i));
                        }

                        // http://developer.android.com/guide/topics/ui/declaring-layout.html#AdapterViews
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_list_item_1, mDiningList);
                        mListView.setAdapter(adapter);
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Data has invalid format", Toast.LENGTH_LONG).show();
                    }
                }
            }.setContext(getActivity()).execute(BASE_URL);
        }
    }

    /**
     * Displays today's menu for the given dining hall.
     * If there is none, displays a Toast saying so.
     * @param diningHall the dining hall's id as a String, as per RedAPI.
     */
    private void handleMenuResponse(final String diningHall) {
        // Do something in response to the click
        final String mealCsv = MEALS_LIST[0] + "," + MEALS_LIST[1] + "," + MEALS_LIST[2];
        final String url = BASE_URL + "/menu/" + diningHall + "/" + mealCsv + "/MEALS";
        if (isConnected(getActivity())) {
            // Async Task to get the menu for a dining hall
            new GetRequest() {
                @Override
                protected void onPostExecute(String result) {
                    try {
                        String menu = "";
                        JSONObject jsonResult = new JSONObject(result);
                        for (String meal : MEALS_LIST) {
                            menu = menu + meal + ":\n";
                            JSONArray jsonArray = jsonResult.getJSONObject(meal).getJSONArray(diningHall);
                            int len = jsonArray.length();
                            for (int i=0; i<len; i++) {
                                menu = menu + jsonArray.getJSONObject(i).getString("name")+", ";
                            }
                            menu = menu + "\n\n";
                        }
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Today's Menu").setMessage(menu)
                                .create().show();
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "No menu for this location yet!", Toast.LENGTH_LONG).show();
                    }
                }
            }.setContext(getActivity()).execute(url);
        }
    }
}