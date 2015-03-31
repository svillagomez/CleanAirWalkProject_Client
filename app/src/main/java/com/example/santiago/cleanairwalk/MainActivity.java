package com.example.santiago.cleanairwalk;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;



//public class MainActivity extends ActionBarActivity implements OnMapReadyCallback{
public class MainActivity extends ActionBarActivity
        implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback{

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mLatitudeText;
    private String mLongitudeText;

    MapFragment mapFragment;

    GoogleMap myGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildGoogleApiClient();


    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if( mLastLocation != null ){
            mLatitudeText = String.valueOf(mLastLocation.getLatitude());
            mLongitudeText = String.valueOf(mLastLocation.getLongitude());
        }

        mapFragment = MapFragment.newInstance();
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        myGoogleMap = mapFragment.getMap();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TO DO averiguar que se debe hacer
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TO DO TODO
    }
    //    @Override
//    public void onLocationChanged(Location location) {
//        Location newLocation = location
//        mLatitudeText = String.valueOf(newLocation.getLatitude());
//        mLongitudeText = String.valueOf(newLocation.getLongitude());
//    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
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

        LatLng loc_latLong_obj =
                new LatLng(Double.parseDouble(mLatitudeText) ,Double.parseDouble(mLongitudeText));

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(loc_latLong_obj, 15)));

        googleMap.addMarker(new MarkerOptions()
                .position(loc_latLong_obj)
                .title("Marker"));


        PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(-37.7897669, 144.9411422))
                .add(new LatLng(-37.7911086,144.9785698))  // North of the previous point, but at the same longitude
                .add(new LatLng(-37.8100244,144.9769556))  // Same latitude, and 30km to the west
                .add(new LatLng(-37.8117041,144.9462382))  // Same longitude, and 16km to the south
                .add(new LatLng(-37.7897669, 144.9411422)); // Closes the polyline.

        rectOptions.width(8);
        rectOptions.color(Color.RED);

// Get back the mutable Polyline
        Polyline polyline = googleMap.addPolyline(rectOptions);

        googleMap = mapFragment.getMap();


        String url = getMapsApiDirectionsUrl();
        GetDirectionClass downloadTask = new GetDirectionClass(myGoogleMap);
        downloadTask.execute(url);
    }

//    ESTO DEBERIA IR EN OTRO ARCHIVO

    private static final LatLng BOURKE = new LatLng(-37.881113,145.174000);

    private String getMapsApiDirectionsUrl() {

//        https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=API_KEY


//        String waypoints = "waypoints=optimize:true|"
//                + BOURKE.latitude + "," + BOURKE.longitude;
//
//        String sensor = "sensor=false";
//        String params = waypoints + "&" + sensor;
//        String output = "json";
//        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + params;

        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=-37.818563,144.959880&destination=-37.800720,144.966958";
//        String url_2 = "https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=AIzaSyBjaqV-AszBVCM-gLEHsWUNwPfW_XbFbP8";

        return url;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);


    }
}
