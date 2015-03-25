package is.genki.bigredapp.android;

import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private static final String KEY_FRAGMENT = "DiningLocationActivity.FRAGMENT";

    private String mDiningHall;
    private String mDiningHallUrl;
    private PlaceholderFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_location);

        mFragment = new PlaceholderFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mFragment)
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
                                if (result == null) throw new JSONException("No result");
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

                                mFragment.addMenus(menus);
                            } catch (JSONException e) {
                                mFragment.noMenus();
                            }
                        }
                    }.setContext(this).execute(url);
                }
            }
        }
        else {
            mDiningHall = savedInstanceState.getString(KEY_DINING_HALL);
            mDiningHallUrl = savedInstanceState.getString(KEY_DINING_HALL_URL);
            setTitle(savedInstanceState.getString(KEY_DINING_HALL));

            // Restore the fragment's instance
            mFragment = (PlaceholderFragment)
                    getSupportFragmentManager().getFragment(savedInstanceState, KEY_FRAGMENT);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_DINING_HALL, mDiningHall);
        outState.putString(KEY_DINING_HALL_URL, mDiningHallUrl);

        // Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, KEY_FRAGMENT, mFragment);

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
        private static final String KEY_HAS_MENUS = "PlaceholderFragment.HAS_MENUS";

        private RecyclerView mRecyclerView;
        private LinearLayoutManager mLayoutManager;
        private MealMenuAdapter mAdapter;
        private View mLoadingPanel;
        private View mNoMenusPanel;
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
            mNoMenusPanel = rootView.findViewById(R.id.menuNonePanel);

            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.menu_toolbar);
            ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);
            // Setting setDisplayHomeAsUpEnabled doesn't work for some reason.
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });

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
            if (mMenus != null && mMenus.size() > 0) {
                outState.putBoolean(KEY_HAS_MENUS, true);
                ArrayList<String> meals = new ArrayList<>();
                ArrayList<String> menus = new ArrayList<>();

                for (MealMenu m : mMenus) {
                    meals.add(m.meal);
                    menus.add(m.menu);
                }

                outState.putStringArrayList(KEY_MEALS, meals);
                outState.putStringArrayList(KEY_MENUS, menus);
            }
            else {
                outState.putBoolean(KEY_HAS_MENUS, false);
            }

            super.onSaveInstanceState(outState);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (savedInstanceState != null) {
                if (savedInstanceState.getBoolean(KEY_HAS_MENUS)) {
                    mMenus = new ArrayList<>();

                    ArrayList<String> meals = savedInstanceState.getStringArrayList(KEY_MEALS);
                    ArrayList<String> menus = savedInstanceState.getStringArrayList(KEY_MENUS);
                    for (int i = 0; i < meals.size(); i++) {
                        mMenus.add(new MealMenu(meals.get(i), menus.get(i)));
                    }

                    addMenus(mMenus);
                }
                else {
                    noMenus();
                }
            }
        }

        public void noMenus() {
            mLoadingPanel.setVisibility(View.GONE);
            mNoMenusPanel.setVisibility(View.VISIBLE);
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
