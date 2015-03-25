package is.genki.bigredapp.android;

import android.app.AlertDialog;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DiningLocationActivity extends ActionBarActivity {

    public static final String KEY_DINING_HALL = "DiningLocationActivity.DINING_HALL";
    public static final String KEY_DINING_HALL_URL = "DiningLocationActivity.DINING_HALL_URL";

    private static final String KEY_MEALS = "DiningLocationActivity.MEALS";
    private static final String KEY_MENUS = "DiningLocationActivity.MENUS";

    private String mDiningHall;
    private String mDiningHallUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_location);
        final PlaceholderFragment fragment = new PlaceholderFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                final String diningHall = extras.getString(KEY_DINING_HALL);
                String url = extras.getString(KEY_DINING_HALL_URL);

                setTitle(diningHall);
                mDiningHall = diningHall;
                mDiningHallUrl = url;

                if (GetRequest.isConnected(this)) {
                    // Async Task to get the menu for a dining hall
                    new GetRequest() {
                        @Override
                        protected void onPostExecute(String result) {
                            try {
                                List<MealMenu> menus = new ArrayList<>();
                                JSONObject jsonResult = new JSONObject(result);
                                for (String meal : DiningListFragment.MEALS_LIST) {
                                    StringBuilder menu = new StringBuilder();
                                    JSONArray jsonArray = jsonResult.getJSONObject(meal).getJSONArray(diningHall);
                                    int len = jsonArray.length();
                                    for (int i=0; i<len; i++) {
                                        menu.append(jsonArray.getJSONObject(i).getString("name"));
                                        menu.append(", ");
                                    }

                                    menus.add(new MealMenu(meal, menu.toString()));
                                }

                                fragment.addMenus(menus);
                            } catch (JSONException e) {
                                Toast.makeText(DiningLocationActivity.this,
                                        "No menu for this location yet!",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }.setContext(this).execute(url);
                }
            }
        }
        else {
            setTitle(savedInstanceState.getString(KEY_DINING_HALL));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_DINING_HALL, mDiningHall);
        outState.putString(KEY_DINING_HALL_URL, mDiningHallUrl);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dining_location, menu);
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
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private RecyclerView mRecyclerView;
        private LinearLayoutManager mLayoutManager;
        private MealMenuAdapter mAdapter;
        private View mLoadingPanel;
        private List<MealMenu> mMenus;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_dining_location, container, false);

            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.menuList);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(mLayoutManager);

            mLoadingPanel = rootView.findViewById(R.id.menuLoadingPanel);

            return rootView;
        }

        public void addMenus(List<MealMenu> menus) {
            mMenus = menus;
            mLoadingPanel.setVisibility(View.GONE);
            mAdapter = new MealMenuAdapter(menus);
            mRecyclerView.setAdapter(mAdapter);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            ArrayList<String> meals = new ArrayList<>();
            ArrayList<String> menus = new ArrayList<>();

            for (MealMenu m : mMenus) {
                meals.add(m.meal);
                menus.add(m.menu);
            }

            outState.putStringArrayList(KEY_MEALS, meals);
            outState.putStringArrayList(KEY_MENUS, menus);

            super.onSaveInstanceState(outState);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (savedInstanceState != null) {
                mMenus = new ArrayList<>();

                ArrayList<String> meals = savedInstanceState.getStringArrayList(KEY_MEALS);
                ArrayList<String> menus = savedInstanceState.getStringArrayList(KEY_MENUS);
                for (int i = 0; i < meals.size(); i++) {
                    mMenus.add(new MealMenu(meals.get(i), menus.get(i)));
                }

                addMenus(mMenus);
            }
        }
    }

    public static class MealMenu {
        public String meal;
        public String menu;

        public MealMenu(String meal, String menu) {
            this.meal = meal;
            this.menu = menu;
        }
    }

    public static class MealMenuViewHolder extends RecyclerView.ViewHolder {
        protected TextView mMeal;
        protected TextView mMenu;

        public MealMenuViewHolder(View v) {
            super(v);

            mMeal = (TextView) v.findViewById(R.id.menu_meal);
            mMenu = (TextView) v.findViewById(R.id.menu_body);
        }
    }

    public static class MealMenuAdapter extends RecyclerView.Adapter<MealMenuViewHolder> {
        private List<MealMenu> menus;

        public MealMenuAdapter(List<MealMenu> menus) {
            this.menus = menus;
        }

        @Override
        public MealMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.menu_card, parent, false);

            return new MealMenuViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MealMenuViewHolder holder, int position) {
            MealMenu menu = menus.get(position);
            holder.mMeal.setText(menu.meal);
            holder.mMenu.setText(menu.menu);
        }

        @Override
        public int getItemCount() {
            return menus.size();
        }
    }
}
