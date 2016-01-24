package is.genki.bigredapp.android;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends AppCompatActivity {

    private String[] mAppActivities;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Menu mOptionsMenu;
    private ActionBarDrawerToggle mDrawerToggle;
    private int selectedDrawer;
    private static final String SELECTED_STRING = "SELECTED_DRAWER";
    private Drawable selectableBackgroundDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createSelectableBackgroundDrawable();

        // See the "SingletonRequestQueue" class. Initializes the RequestQueue
        SingletonRequestQueue.getInstance(this).getRequestQueue();

        setupSliderDrawer();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DiningNowFragment())
                    .commit();
        } else {
            selectedDrawer = savedInstanceState.getInt(SELECTED_STRING,-1);
            if(selectedDrawer != -1) {
                selectItem(selectedDrawer); //Just set view to dining halls
            } else{
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new DiningNowFragment())
                        .commit();
                selectItem(0); //Just set view to dining halls
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mOptionsMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, mOptionsMenu);
        if(selectedDrawer != 1)
            setMapEnabled(false);
        else
            setMapEnabled(true);

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
        //int id = item.getItemId();

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
        Fragment fragment;
        selectedDrawer = position;
        // Create a new fragment or activity based on what we selected
        switch (position) {
            case 0:
                fragment = new DiningNowFragment();
                setMapEnabled(false);
                break;
            case 1:
                fragment = new MapFragment();
                setMapEnabled(true);
                break;
            case 2:
                fragment = new LibraryFragment();
                setMapEnabled(false);
                break;
            case 3:
                fragment = new EventListFragment();
                setMapEnabled(false);
                break;
            case 4:
                fragment = new PeopleSearchFragment();
                setMapEnabled(false);
                break;
            case 5:
                fragment = new AboutFragment();
                setMapEnabled(false);
                break;
            default:
                fragment = new DiningNowFragment();
        }
            Bundle args = new Bundle();
            fragment.setArguments(args);

            // Insert the fragment by replacing any existing fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();


        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
//        for(int i = 0; i< mDrawerList.getChildCount(); i++) {
//            if (i == position)
//                mDrawerList.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.list_drawer_highlight));
//            else
//                mDrawerList.getChildAt(i).re
//                mDrawerList.getChildAt(i).setBackgroundDrawable(selectableBackgroundDrawable);
//        }
        setTitle(mAppActivities[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void hideOption(int id)
    {
        if(mOptionsMenu != null) {
            MenuItem item = mOptionsMenu.findItem(id);
            item.setVisible(false);
        }
    }

    private void showOption(int id)
    {
        if(mOptionsMenu != null) {
            MenuItem item = mOptionsMenu.findItem(id);
            item.setVisible(true);
        } else
            System.out.println("sad");
    }

    /**
     * Slider drawer setup factored out for readability
     */
    private void setupSliderDrawer(){

        mAppActivities = new String[] {"Dining","Map","Libraries", "Events", "People Search", "About"};
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.list_item_drawer, mAppActivities));
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);

        // Save the current selected drawer
        savedInstanceState.putInt(SELECTED_STRING, selectedDrawer);
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

    private void createSelectableBackgroundDrawable()
    {
        // Create an array of the attributes we want to resolve
        // using values from a theme
        // android.R.attr.selectableItemBackground requires API LEVEL 11
        int[] attrs = new int[] { android.R.attr.selectableItemBackground /* index 0 */};

        // Obtain the styled attributes. 'themedContext' is a context with a
        // theme, typically the current Activity (i.e. 'this')
        TypedArray ta = obtainStyledAttributes(attrs);

        // Now get the value of the 'listItemBackground' attribute that was
        // set in the theme used in 'themedContext'. The parameter is the index
        // of the attribute in the 'attrs' array. The returned Drawable
        // is what you are after
        selectableBackgroundDrawable = ta.getDrawable(0 /* index */);

        // Finally free resources used by TypedArray
        ta.recycle();

    }

}
