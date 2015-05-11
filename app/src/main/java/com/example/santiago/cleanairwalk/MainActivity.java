package com.example.santiago.cleanairwalk;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



//public class MainActivity extends ActionBarActivity implements OnMapReadyCallback{
public class MainActivity extends ActionBarActivity
        implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback{

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mLatitudeText;
    private String mLongitudeText;
    private ImageButton directions_btn;

    MapFragment mapFragment;

    GoogleMap myGoogleMap;


//    Marker my_centered_marker;
    private ImageButton centered_btn_layout;

    private Button coord_ok;
    private Button coord_cancel;

    private LatLng start_coordinates;
    private LatLng end_coordinates;


    private Marker marker_start;
    private Marker marker_end;

    private GetRouteClass routing_task;
    private LinearLayout btns_layout_view;

//    Fragment buttons_layout_fragment;

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
    public void onMapReady(final GoogleMap googleMap) {


        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

//        my_centered_marker = myGoogleMap.addMarker( new MarkerOptions()
//                        .position(myGoogleMap.getCameraPosition().target)
//                        .visible(false)
//        );

        coord_ok = (Button) findViewById(R.id.btn_dir_ok);

        coord_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LatLng actual_position = myGoogleMap.getCameraPosition().target;
                if (start_coordinates == null){
                    marker_start = myGoogleMap.addMarker(new MarkerOptions()
                            .position(actual_position)
                    .title("Start"));
                    start_coordinates = new LatLng(actual_position.latitude,actual_position.longitude);
                }
                else if(end_coordinates == null){
                    marker_end = myGoogleMap.addMarker(new MarkerOptions()
                            .position(actual_position)
                            .title("Destination"));
                    end_coordinates = new LatLng(actual_position.latitude,actual_position.longitude);
                    coord_ok.setText("GO!!");
                    coord_ok.setTextColor(Color.GREEN);
                }
                else{
                    String url_python = getMapsApiDirectionsUrl(start_coordinates,end_coordinates);
                    routing_task = new GetRouteClass(myGoogleMap);
                    routing_task.execute(url_python);
//                    my_centered_marker.setVisible(false);
                    coord_ok.setVisibility(View.INVISIBLE);
                    centered_btn_layout.setVisibility(View.INVISIBLE);
                }

            }
        });

        coord_cancel = (Button) findViewById(R.id.btn_dir_cancel);

        coord_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_coordinates = end_coordinates = null;
                coord_ok.setText("OK");
                coord_ok.setTextColor(Color.BLACK);
                centered_btn_layout.setVisibility(View.INVISIBLE);

                if (marker_start != null) {
                    marker_start.remove();
                }
                if (marker_end != null){
                    marker_end.remove();
                }

                if(routing_task != null) {
                    routing_task.remove_route();
                }
                if(btns_layout_view != null) {
                    btns_layout_view.setVisibility(View.INVISIBLE);
                }


            }
        });


        googleMap.setMyLocationEnabled(true);

        LatLng loc_latLong_obj =
                new LatLng(Double.parseDouble(mLatitudeText) ,Double.parseDouble(mLongitudeText));

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(loc_latLong_obj, 15)));

        //Set map controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        directions_btn = (ImageButton) findViewById(R.id.directions_button);
        directions_btn.setVisibility(View.VISIBLE);

        directions_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btns_layout_view = (LinearLayout) findViewById(R.id.decisions_btns_layout);
                btns_layout_view.setVisibility(View.VISIBLE);
                centered_btn_layout = (ImageButton) findViewById(R.id.centered_icon);
                centered_btn_layout.setVisibility(View.VISIBLE);
                coord_ok.setVisibility(View.VISIBLE);
//                my_centered_marker.setVisible(true);

            }
        });


        myGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                LatLng centerOfMap;
//                Log.d("coord",myGoogleMap.getCameraPosition().target.toString());
//                my_centered_marker.setPosition(myGoogleMap.getCameraPosition().target);
            }
        });




////      add marker to defaul init location
//        googleMap.addMarker(new MarkerOptions()
//                .position(loc_latLong_obj)
//                .title("Marker"));


//        PolylineOptions rectOptions = new PolylineOptions()
//                .add(new LatLng(-37.7897669, 144.9411422))
//                .add(new LatLng(-37.7911086,144.9785698))  // North of the previous point, but at the same longitude
//                .add(new LatLng(-37.8100244,144.9769556))  // Same latitude, and 30km to the west
//                .add(new LatLng(-37.8117041,144.9462382))  // Same longitude, and 16km to the south
//                .add(new LatLng(-37.7897669, 144.9411422)); // Closes the polyline.
//
//        rectOptions.width(8);
//        rectOptions.color(Color.RED);
//
////Get back the mutable Polyline
//        Polyline polyline = googleMap.addPolyline(rectOptions);

//        googleMap = mapFragment.getMap();


//        myGoogleMap.setOnMapClickListener( new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//                Projection projection = myGoogleMap.getProjection();
//                Point coordinate = projection.toScreenLocation(latLng);
//
//                Log.e("coord son:", latLng.latitude + " " + latLng.longitude);
//                Log.d("UNO","DOS");
//            }
//        });

//        myGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
//            @Override
//            public void onMapLongClick(LatLng latLng) {
//                Projection projection = myGoogleMap.getProjection();
//                Point coordinate = projection.toScreenLocation(latLng);
//
//                Log.e("coord son:", latLng.latitude + " " + latLng.longitude);
//                Log.d("UNO","DOS");
//
//                myGoogleMap.addMarker(new MarkerOptions()
//                        .position(latLng)
//                        .title("Marker"));
//            }
//        });

        HeatMap.addHetMap(myGoogleMap);

//        String url_python = getMapsApiDirectionsUrl();
//        GetRouteClass task = new GetRouteClass(myGoogleMap);
//        task.execute(url_python);
//        String url = getMapsApiDirectionsUrl();
//        GetDirectionClass downloadTask = new GetDirectionClass(myGoogleMap);
//        downloadTask.execute(url);
    }

//    ESTO DEBERIA IR EN OTRO ARCHIVO

//    private static final LatLng BOURKE = new LatLng(-37.881113,145.174000);

    private String getMapsApiDirectionsUrl(LatLng start, LatLng end) {

//        https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=API_KEY


//        String waypoints = "waypoints=optimize:true|"
//                + BOURKE.latitude + "," + BOURKE.longitude;
//
//        String sensor = "sensor=false";
//        String params = waypoints + "&" + sensor;
//        String output = "json";
//        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + params;


        String start_str_coord = "start="+Double.toString(start.latitude)+"&start="+Double.toString(start.longitude);
        String end_str_coord = "&end="+Double.toString(end.latitude)+"&end="+Double.toString(end.longitude);
//        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=-37.818563,144.959880&destination=-37.800720,144.966958&waypoints=-37.816313,144.964192|-37.812230,144.962234|-37.813641,144.957277|-37.809099,144.955351|-37.808870,144.957958|-37.806505,144.961499|-37.803351,144.964997|-37.802114,144.966767&mode=bicycling";
        String url = "http://192.168.1.13:5667/q?";
        String new_url = "http://192.168.1.13:5667/q?"+start_str_coord+end_str_coord;

        Log.e("ESTo mandaria:",new_url);

//        String url_2 = "https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=AIzaSyBjaqV-AszBVCM-gLEHsWUNwPfW_XbFbP8";
        return new_url;
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);


    }
}
