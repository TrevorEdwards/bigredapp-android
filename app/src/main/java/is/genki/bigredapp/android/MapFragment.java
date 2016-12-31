package is.genki.bigredapp.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//https://developers.google.com/android/reference/com/google/android/gms/maps/GoogleMap
public class MapFragment extends SupportMapFragment implements OnMapReadyCallback {

    public static GoogleMap mMap;
    private static AppCompatActivity mContext;

    public static final String BUILDING_SOURCE = "https://redevents.herokuapp.com/maps/buildings";
    public static final String BIKE_SOURCE = "https://redevents.herokuapp.com/maps/bikeracks";
    public static final String BUS_STOP_SOURCE = "https://redevents.herokuapp.com/maps/stops";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (AppCompatActivity) getActivity();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) mContext.getSupportFragmentManager()
                .findFragmentById(R.id.container);
        //Preload data for map
        if (SingletonMapData.getInstance().getCategories().size() == 0)
            getMapData();

        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //setup variables we want
        mMap.setBuildingsEnabled(true);
        LatLng mcgraw = new LatLng(42.447587, -76.485013); //centering on mcgraw is very cornell
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true); //Show where the user is


        // Let's make mcgraw tower the center
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mcgraw, 17));
    }

    /**
     * Loads SingletonMapData with all of the data from cornelldata
     */
    private void getMapData() {
        if (SingletonRequestQueue.isConnected(mContext)) {
            final Pair<String, String>[] urls = new Pair[]{
                    new Pair<String, String>("Buildings", BUILDING_SOURCE),
                    new Pair<String, String>("Bike Racks", BIKE_SOURCE),
                    new Pair<String, String>("Bus Stops", BUS_STOP_SOURCE)
            };

            for (final Pair<String, String> sp : urls) {
                JsonArrayRequest jsonArrRequest = (JsonArrayRequest)
                        new JsonArrayRequest(Request.Method.GET, sp.second,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        try {
                                            int length = response.length();
                                            for (int i = 0; i < length; i++) {
                                                JSONObject build = response.getJSONObject(i);
                                                double lat = Double.parseDouble(build.getString("Latitude"));
                                                double lon = Double.parseDouble(build.getString("Longitude"));
                                                if( sp.first.equals("Bike Racks")){
                                                    SingletonMapData.getInstance().addLocation(sp.first, "Bike Rack " + i, lat, lon);
                                                }else {
                                                    String name = build.getString("Name");
                                                    SingletonMapData.getInstance().addLocation(sp.first, name, lat, lon);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            //Do nothing
                                        }
                                    }
                                }, SingletonRequestQueue.getErrorListener(mContext))
                                .setRetryPolicy(SingletonRequestQueue.getRetryPolicy());
                SingletonRequestQueue.getInstance(mContext).addToRequestQueue(jsonArrRequest);

            }
        }
    }
}