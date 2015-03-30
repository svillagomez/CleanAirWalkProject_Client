package com.example.santiago.cleanairwalk;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback{

    MapFragment mapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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




    @Override
    public void onMapReady(GoogleMap googleMap) {
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        googleMap.setMyLocationEnabled(true);

        Criteria criteria = new Criteria();

        String provider = locationManager.getBestProvider(criteria,true);

        Location lastKnownlocation = locationManager.getLastKnownLocation(provider);


        LatLng loc_latLong_obj =
                new LatLng(lastKnownlocation.getLatitude(),lastKnownlocation.getLongitude());

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(loc_latLong_obj,15)));

        googleMap.addMarker(new MarkerOptions()
                .position(loc_latLong_obj)
                .title("Marker"));


        PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(-37.7897669, 144.9411422))
                .add(new LatLng(-37.7911086,144.9785698))  // North of the previous point, but at the same longitude
                .add(new LatLng(-37.8100244,144.9769556))  // Same latitude, and 30km to the west
                .add(new LatLng(-37.8117041,144.9462382))  // Same longitude, and 16km to the south
                .add(new LatLng(-37.7897669, 144.9411422)); // Closes the polyline.

// Get back the mutable Polyline
        Polyline polyline = googleMap.addPolyline(rectOptions);

    }

}
