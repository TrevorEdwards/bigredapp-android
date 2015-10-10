package is.genki.bigredapp.android;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//https://developers.google.com/android/reference/com/google/android/gms/maps/GoogleMap
public class MapActivity extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static ActionBarActivity mContext;
    private LatLngBounds CORNELL = new LatLngBounds (new LatLng(42.401988, -76.522393), new LatLng(42.501668, -76.432340));

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (ActionBarActivity) getActivity();
        //setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) mContext.getSupportFragmentManager()
                .findFragmentById(R.id.container);
        System.out.println(mapFragment);
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
        LatLng mcgraw = new LatLng(42.447587, -76.485013);
        mMap.setMyLocationEnabled(true); //Show where the user is


        // Let's make mcgraw tower the center
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mcgraw, 17));
        getBuildingData();
    }

    private void getBuildingData() {
        if (SingletonRequestQueue.isConnected(mContext)) {
            final String buildingURL = "https://cornelldata.org/api/v0/map-data/buildings";
            JsonArrayRequest jsonArrRequest = (JsonArrayRequest)
                    new JsonArrayRequest(Request.Method.GET, buildingURL,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        int length = response.length();
                                        for( int i = 0; i < length; i++ ){
                                            JSONObject build = response.getJSONObject(i);
                                            String name = build.getString("Name");
                                            double lat = Double.parseDouble(build.getString("Latitude"));
                                            double lon = Double.parseDouble(build.getString("Longitude"));
                                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(name));
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