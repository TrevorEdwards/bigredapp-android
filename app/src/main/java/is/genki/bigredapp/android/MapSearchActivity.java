package is.genki.bigredapp.android;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSearchActivity extends ListActivity {

    private ArrayList<Map.Entry<String,Pair<Double,Double>>> coords;
    private ActionBarActivity mContext;

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

        coords = new ArrayList<>();
        String[] from = new String[] {"locname"};
        int[] to = new int[] { R.id.item1 };

        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<>();
        ArrayList<Map.Entry<String,Pair<Double,Double>>> results = SingletonMapData.getInstance().searchString(query);

        //No results, return to map
        if(results.size() == 0){
            Toast.makeText(this, "No Results", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }

        for(Map.Entry<String,Pair<Double,Double>> ent : results){
            HashMap<String, String> map = new HashMap<>();
            map.put("locname", ent.getKey());
            coords.add(ent);
            fillMaps.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.search_item, from, to);

        // Bind to our new adapter.
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick (ListView l, View v, int position, long id) {
        MapFragment.mMap.clear();
        LatLng loc = new LatLng(coords.get(position).getValue().first, coords.get(position).getValue().second);
        MapFragment.mMap.addMarker(
                new MarkerOptions()
                        .position(loc)
                        .title(coords.get(position).getKey()));
        MapFragment.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 17),2000,null);
        this.finish();

    }

}
