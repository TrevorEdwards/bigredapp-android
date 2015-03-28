package is.genki.bigredapp.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fragment containing a ListView of dining halls.
 * Each dining hall name can be tapped to get it's menu for today.
 */
public class DiningListFragment extends Fragment {

    private static final String BASE_URL = "http://redapi-tious.rhcloud.com/dining";
    public static final String[] MEALS_LIST = {"Breakfast", "Lunch", "Dinner"};
    private ListView mListView;
    private ArrayList<String> mDiningList;

    public DiningListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listView);

        // http://developer.android.com/guide/topics/ui/declaring-layout.html#HandlingUserSelections
        AdapterView.OnItemClickListener mListViewClickHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                handleMenuResponse(v, mDiningList.get(position));
            }
        };
        mListView.setOnItemClickListener(mListViewClickHandler);

        getDiningList();

        return rootView;
    }

    /**
     * Populates mDiningList with the String list of dining halls.
     * Sets mListView's adapter to this list.
     */
    private void getDiningList() {
        if (GetRequest.isConnected(getActivity())) {
            // Async Task to get the list of dining halls
            new GetRequest() {
                @Override
                protected void onPostExecute(String result) {
                    try {
                        // Convert result to JSONArray, then get diningHall string list
                        JSONArray jsonArray = new JSONArray(result);
                        mDiningList = new ArrayList<>();
                        int len = jsonArray.length();
                        for (int i=0; i<len; i++){
                            mDiningList.add(jsonArray.getString(i));
                        }

                        // http://developer.android.com/guide/topics/ui/declaring-layout.html#AdapterViews
                        ArrayAdapter<String> adapter = new PrettyNameArrayAdapter(getActivity(),
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
     * @param view
     * @param diningHall the dining hall's id as a String, as per RedAPI.
     * Displays today's menu for the given dining hall.
     */
    private void handleMenuResponse(View view, final String diningHall) {
        Intent intent = new Intent(getActivity(), DiningLocationActivity.class);
        final String mealCsv = MEALS_LIST[0] + "," + MEALS_LIST[1] + "," + MEALS_LIST[2];
        final String url = BASE_URL + "/menu/" + diningHall + "/" + mealCsv + "/MEALS";
        intent.putExtra(DiningLocationActivity.KEY_DINING_HALL, diningHall);
        intent.putExtra(DiningLocationActivity.KEY_DINING_HALL_URL, url);

        // ViewCompat.setTransitionName(view, "shared_transition");
        ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(
                view, 0, 0, view.getWidth(), view.getHeight());
        getActivity().startActivity(intent, options.toBundle());
    }

    /**
     * http://developer.android.com/guide/topics/ui/declaring-layout.html#FillingTheLayout
     * Returns a custom view for an array of strings on their way through the mListView adapter
     */
    public class PrettyNameArrayAdapter extends ArrayAdapter<String> {

        int mResource;
        LayoutInflater mInflater;
        final Pattern p = Pattern.compile("\\b([a-z])");

        public PrettyNameArrayAdapter(Context context, int res, ArrayList<String> items) {
            super(context, res, items);
            this.mResource = res;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Turns a RedAPI string id into a nice looking name
            // Temporary fix for https://github.com/genkimarshall/bigredapp-android/issues/2
            String name = getItem(position);
            name = name.replace("_", " ");
            Matcher m = p.matcher(name);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, m.group(1).toUpperCase());
            }
            m.appendTail(sb);

            // http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
            // http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
            DiningListViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(this.mResource, parent, false);
                holder = new DiningListViewHolder();
                holder.label = (TextView) convertView;
                convertView.setTag(holder);
            } else {
                holder = (DiningListViewHolder) convertView.getTag();
            }
            holder.label.setText(sb.toString());
            return convertView;
        }

        // http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
        class DiningListViewHolder {
            TextView label;
        }
    }
}