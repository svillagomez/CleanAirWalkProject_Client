package com.example.santiago.cleanairwalk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * Created by santiago on 7/05/15.
 *
 * Name : MainActivity type : class
 * Usage: Class that executes main logic of Android app
 */
public class MainActivity extends ActionBarActivity
        implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback{

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mLatitudeText;
    private String mLongitudeText;
    private ImageButton directions_btn;

    MapFragment mapFragment;

    //A Google map instance
    GoogleMap myGoogleMap;

    private ImageButton centered_btn_layout;

    //    buttons to handle route requests on screen
    private Button coord_ok;
    private Button coord_cancel;

    private LatLng start_coordinates;
    private LatLng end_coordinates;


    private Marker marker_start;
    private Marker marker_end;

    private GetRouteClass routing_task;
    private GetDirectionClass google_route_task;
    private LinearLayout btns_layout_view;

    private ErrorWindow errors_window;

    // used to display pollution value ranges
    private TextView threshold_1;
    private TextView threshold_2;
    private TextView threshold_3;
    private TextView threshold_4;

    private GetPollutionClass get_max_value_task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildGoogleApiClient();
    }

    /**
     * method : onConnected
     * usage: draw map instance once is is correctly loaded
     * params: bundle
     * return: void
     */
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
        //TO DO
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TO DO TODO
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    /**
     * method : onCreateOptionsMenu
     * usage: inflate options windows
     * params: menu type Menu: the options
     * return: boolean default value
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * method : onOptionsItemSelected
     * usage: handle tap request on options menu
     * params: a menu item
     * return: boolean default value
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent p = new Intent(this,Prefs.class);
            startActivity(p);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * method : onMapReady
     * usage: set functionality of items when map s ready to use
     * params: googleMap type GoogleMap: the Google map instance
     * return: void
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        getThresholdTextViewReference();
        setDefaultThresholds();

        queryMaxPollutionValue();

        coord_ok = (Button) findViewById(R.id.btn_dir_ok);


        /**
         * method : setOnClickListener
         * usage: set functionality of "OK " button
         * params: View.OnClickListener()
         */
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
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_arrival_small))
                            .title("Destination"));
                    end_coordinates = new LatLng(actual_position.latitude,actual_position.longitude);
                    coord_ok.setText("!!GO!!");
                    coord_ok.setTextColor(Color.MAGENTA);
                }
                else{
                    String url_python = getMapsApiDirectionsUrl(start_coordinates,end_coordinates);
                    routing_task = new GetRouteClass(myGoogleMap,marker_end);
                    routing_task.execute(url_python);

                    queryGoogleMapsRoute(start_coordinates,end_coordinates);
//                    my_centered_marker.setVisible(false);
                    coord_ok.setVisibility(View.INVISIBLE);
                    centered_btn_layout.setVisibility(View.INVISIBLE);
                    coord_cancel.setText("DONE!!");
                    directions_btn.setVisibility(View.INVISIBLE);
                }

            }
        });

        coord_cancel = (Button) findViewById(R.id.btn_dir_cancel);

        /**
         * method : setOnClickListener
         * usage: set functionality of "CANCEL " button
         * params: View.OnClickListener()
         */
        coord_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_coordinates = end_coordinates = null;
                coord_ok.setText("OK");
                coord_ok.setTextColor(Color.BLACK);
                centered_btn_layout.setVisibility(View.INVISIBLE);
                coord_cancel.setText("Cancel");
                directions_btn.setVisibility(View.VISIBLE);

                if (marker_start != null) {
                    marker_start.remove();
                }
                if (marker_end != null){
                    marker_end.remove();
                }

                if(routing_task != null) {
                    routing_task.remove_route();
                }

                if(google_route_task != null){
                    google_route_task.remove_google_route();
                }
                if(btns_layout_view != null) {
                    btns_layout_view.setVisibility(View.INVISIBLE);
                }


            }
        });


        googleMap.setMyLocationEnabled(true);

        LatLng loc_latLong_obj =
                new LatLng(Double.parseDouble(mLatitudeText) ,Double.parseDouble(mLongitudeText));

//        move camera to the current/last location
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(loc_latLong_obj, 15)));

        //Set map controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        directions_btn = (ImageButton) findViewById(R.id.directions_button);
        directions_btn.setVisibility(View.VISIBLE);


        /**
         * method : setOnClickListener
         * usage: set functionality of "directions " button
         * params: View.OnClickListener()
         */
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
//                my_centered_marker.setPosition(myGoogleMap.getCameraPosition().target);
            }
        });
    }


    /**
     * method : getMapsApiDirectionsUrl
     * usage: construct the whole URL including parameters (own server) to query a route
     * params: Latitude and longitude values chosen by user
     */
    private String getMapsApiDirectionsUrl(LatLng start, LatLng end) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String server_ip;
        server_ip = prefs.getString("serverIP","127.0.0.2");
        String server_port;
        server_port = prefs.getString("ServerPort","9999");

        String ip_addr = server_ip;
        String start_str_coord = "start="+Double.toString(start.latitude)+"&start="+Double.toString(start.longitude);
        String end_str_coord = "&end="+Double.toString(end.latitude)+"&end="+Double.toString(end.longitude);

        String new_url = "http://"+ip_addr+":"+server_port+"/q?"+start_str_coord+end_str_coord;
        return new_url;
    }

    /**
     * method : getGoogleMapsApiDirectionsUrl
     * usage: constructs the whole URL including parameters (Google server) to query a route
     * params: Latitude and longitude values chosen by user
     */
    private String getGoogleMapsApiDirectionsUrl(LatLng start, LatLng end) {
        String base_url = "https://maps.googleapis.com/maps/api/directions/json?";
        String start_coord_str = Double.toString(start.latitude)+","+Double.toString(start.longitude);
        String end_coord_str = Double.toString(end.latitude)+","+Double.toString(end.longitude);

        String url = base_url + "origin=" + start_coord_str+"&destination="+end_coord_str+"&mode=walking";
        return url;
    }

    /**
     * method : getMaxPollutionQueryUrl
     * usage: construct the whole URL including parameters (own server) to get pollution value
     * params: Void
     */
    private String getMaxPollutionQueryUrl() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String server_ip;
        server_ip = prefs.getString("serverIP","127.0.0.2");
        String server_port;
        server_port = prefs.getString("ServerPort","9999");

        String ip_addr = server_ip;
        String param = "maxValue=";
        String new_url = "http://"+ip_addr+":"+server_port+"/q?"+param;
        return new_url;
    }

    /**
     * method : queryGoogleMapsRoute
     * usage: make an actual request to plot a Google route
     * params: latitude and longitude values chosen by user
     * return: void
     */
    private void queryGoogleMapsRoute(LatLng start, LatLng end){
        String url = getGoogleMapsApiDirectionsUrl(start, end);
        google_route_task = new GetDirectionClass(myGoogleMap);
        google_route_task.execute(url);
    }

    /**
     * method : queryMaxPollutionValue
     * usage: make an actual request to display pollution ranges
     * params: void
     * return: void
     */
    private void queryMaxPollutionValue(){
        String url = getMaxPollutionQueryUrl();
        get_max_value_task = new GetPollutionClass(threshold_1,threshold_2,threshold_3,threshold_4);
        get_max_value_task.execute(url);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);


    }


    /**
     * method : setDefaultThresholds
     * usage: set defaults pollutions ranges if no value is provides ( when no connection/response)
     * params: void
     * return: void
     */
    private void setDefaultThresholds(){
        threshold_1.setText("< 37.50");
        threshold_2.setText("< 75.00");
        threshold_3.setText("< 112.50");
        threshold_4.setText("< 150.00");
    }


    /**
     * method : setDefaultThresholds
     * usage: get the objects to set the pollution thresholds values
     * params: void
     * return: void
     */
    private void getThresholdTextViewReference(){
        threshold_1 = (TextView) findViewById(R.id.range_1);
        threshold_2 = (TextView) findViewById(R.id.range_2);
        threshold_3 = (TextView) findViewById(R.id.range_3);
        threshold_4 = (TextView) findViewById(R.id.range_4);
    }

}
