package is.genki.bigredapp.android;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSearchActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);


        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch( String query ){

        String[] from = new String[] {"locname"};
        int[] to = new int[] { R.id.item1 };

        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<>();
        ArrayList<Map.Entry<String,Pair<Double,Double>>> results = SingletonMapData.getInstance().searchString(query);
        for(Map.Entry<String,Pair<Double,Double>> ent : results){
            HashMap<String, String> map = new HashMap<>();
            map.put("locname", ent.getKey());
            fillMaps.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.search_item, from, to);

        // Bind to our new adapter.
        setListAdapter(adapter);
    }

}
