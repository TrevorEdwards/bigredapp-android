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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterChooserActivity extends ListActivity {

    private ActionBarActivity mContext;
    private ArrayList<String> categories;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);

        setupView();
    }


    private void setupView(){

        categories = new ArrayList<>();
        String[] from = new String[] {"category"};
        int[] to = new int[] { R.id.item1 };

        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<>();
        Set<String> cats = SingletonMapData.getInstance().getCategories();

        //first add a none option
        HashMap<String, String> map2 = new HashMap<>();
        map2.put("category", "None");
        categories.add("None");
        fillMaps.add(map2);

        for(String cat : cats){
            HashMap<String, String> map = new HashMap<>();
            map.put("category", cat);
            categories.add(cat);
            fillMaps.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.search_item, from, to);

        // Bind to our new adapter.
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick (ListView l, View v, int position, long id) {
        String cat = categories.get( position );
        GoogleMap map = MapFragment.mMap;
        map.clear();
        if( !cat.equals("None")) {
            BitmapDescriptor colorPalette = nameToColor(cat);
            for (Map.Entry<String, Pair<Double, Double>> ent : SingletonMapData.getInstance().mapForCategory(cat).entrySet()) {
                LatLng coords = (new LatLng(ent.getValue().first, ent.getValue().second));
                MapFragment.mMap.addMarker(
                        new MarkerOptions()
                                .position(coords)
                                .title(ent.getKey())
                                .icon(colorPalette));
            }
        }
        this.finish();

    }

    /**
     * Converts a name into a BitMap color
     * @param name The key for the color conversion
     */
    private BitmapDescriptor nameToColor(String name){
        return BitmapDescriptorFactory.defaultMarker(Math.abs(name.hashCode()) % 360);
    }

}
