package is.genki.bigredapp.android;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

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

        //Rotate the camera to 45 degrees
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(position.target)
                                .tilt(30)
                                .zoom(position.zoom)
                                .build()));
            }
        });

        // Let's make mcgraw tower the center
        mMap.addMarker(new MarkerOptions().position(mcgraw).title("McGraw Tower"));
       // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CORNELL.getCenter(), 14));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mcgraw,17));
    }
}