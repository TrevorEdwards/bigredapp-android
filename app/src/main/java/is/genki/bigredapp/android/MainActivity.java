package is.genki.bigredapp.android;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.RequestQueue;

import java.util.logging.Filter;


public class MainActivity extends ActionBarActivity  {

    private String[] mAppActivities;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Menu mOptionsMenu;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // See the "SingletonRequestQueue" class. Initializes the RequestQueue
        //noinspection UnusedAssignment
        RequestQueue queue = SingletonRequestQueue.getInstance(this).getRequestQueue();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DiningListFragment())
                    .commit();
        } else {
            System.out.println(savedInstanceState);
        }

        setupSliderDrawer();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mOptionsMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, mOptionsMenu);
        setMapEnabled(false);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }else if( item.getItemId() == R.id.action_filter){
            this.startActivity(new Intent(this, FilterChooserActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        Fragment fragment = null;
        Class act = null;
        // Create a new fragment or activity based on what we selected
        switch (position) {
            case 0:
                fragment = new DiningListFragment();
                setMapEnabled(false);
                break;
            case 1:
                fragment = new MapFragment();
                setMapEnabled(true);
                break;
            case 2:
                fragment = new AboutFragment();
                setMapEnabled(false);
                break;
            default:
                fragment = new DiningListFragment();
        }
        if( fragment != null){

            Bundle args = new Bundle();
            fragment.setArguments(args);

            // Insert the fragment by replacing any existing fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();

        } else if (act != null){
            this.startActivity(new Intent(this, act));
        }


        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mAppActivities[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void hideOption(int id)
    {
        MenuItem item = mOptionsMenu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id)
    {
        MenuItem item = mOptionsMenu.findItem(id);
        item.setVisible(true);
    }

    /**
     * Slider drawer setup factored out for readability
     */
    private void setupSliderDrawer(){

        mAppActivities = new String[] {"Dining","Map","About"};
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mAppActivities));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        // Set first item selected and change title
        mDrawerList.setItemChecked(0, true);
        setTitle(mAppActivities[0]);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        //Set up hamburger menu
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                toolbar, /* our toolbar */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // getActionBar().setTitle(mDrawerTitle);
            }
        };
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    /**
     *  Sets up the toolbar for the menu fragment
     */
    private void setMapEnabled(boolean enab){
        if( enab ){
            showOption(R.id.action_search);
            showOption(R.id.action_filter);
        }else{
            hideOption(R.id.action_search);
            hideOption(R.id.action_filter);
        }
    }


}
