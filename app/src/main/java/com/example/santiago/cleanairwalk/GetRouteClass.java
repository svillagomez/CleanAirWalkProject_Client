package com.example.santiago.cleanairwalk;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;

/**
 * Created by santiago on 7/05/15.
 * http://stackoverflow.com/questions/3505930/make-an-http-request-with-android
 *
 * Name : GetDirectionClass type : class
 * Usage: Class that get route from our Google directions API
 */
public class GetRouteClass extends AsyncTask  <String, Void, String> {

    GoogleMap map;

    private Polyline route = null;
    private ArrayList<Polyline> route_segments;
    private Marker end_marker;
    private double distance;
    private String connection_exception_error;
    private ErrorWindow errorWindow_here;


//    default range values
    private static double THR_HIGH = 3.0*(150)/4.0;
    private static double THR_MID = 2.0*(150)/4.0;
    private static double THR_LOW = 1.0*(150)/4.0;

    public GetRouteClass(GoogleMap map_ref,Marker end_marker_ref){
        map = map_ref;
        route_segments = new ArrayList<Polyline>();
        end_marker = end_marker_ref;
        distance = 0;
//        errorWindow_here = new ErrorWindow();
    }

    /**
     * method : doInBackground
     * usage: make http conection to our own server to retrieve a route
     * params: uri type String : the whole address including parameters
     * return: rcvdData type String : a route
     */
    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String json_response = null;
        HttpGet getRequest = new HttpGet(uri[0]);

        try {
            response = httpclient.execute(getRequest);
            HttpEntity entity = response.getEntity();

            if (entity != null){
                json_response = response.getStatusLine().getReasonPhrase();
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
            e.printStackTrace();
        }
        catch (ConnectException e) {
            connection_exception_error = "Unable to connect to server";
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
            //TODO Handle problems..
        }
        return json_response;
    }

    /**
     * method : onPostExecute
     * usage: perform operations after doInBackground finishes
     * params: result => output from doInBackground
     * return: void
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        new ParserTask().execute(result);
    }

    /**
     * Name : ParserTask type : class
     * Usage: Class that parse and plot a route
     */
    private class ParserTask extends
            AsyncTask<String, Void, ArrayList<LatLng> > {

        ArrayList<Double> pollution_Arr;

        private ParserTask(){
            pollution_Arr = new ArrayList<Double>();
        }


        /**
         * method : doInBackground
         * usage: convert an parser a json route
         * params: jsonData (containing a route)
         * return: type ArrayList structure containing a route
         */
        @Override
        protected ArrayList<LatLng> doInBackground(
                String... jsonData) {
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            JSONObject jObject;

            try {
                jObject = new JSONObject(jsonData[0]);
                double lat;
                double lon;
                double pollution_value;
                int START_LAT_IDX = 0;
                int START_LON_IDX = 1;
                int END_LAT_IDX = 2;
                int END_LON_IDX = 3;
                int POLLUTION_IDX = 4;
                int LENGTH_IDX = 5;


                for (int j = 0; j < jObject.length();j++) {
                    lat = ((JSONArray)(jObject.get(Integer.toString(j)))).getDouble(START_LAT_IDX);
                    lon = ((JSONArray)(jObject.get(Integer.toString(j)))).getDouble(START_LON_IDX);
                    pollution_value = ((JSONArray)(jObject.get(Integer.toString(j)))).getDouble(POLLUTION_IDX);
                    distance += ((JSONArray)(jObject.get(Integer.toString(j)))).getDouble(LENGTH_IDX);
                    LatLng position = new LatLng(lat, lon);
                    points.add(position);
                    pollution_Arr.add(pollution_value);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return points;
        }

        /**
         * method : onPostExecute
         * usage: get every route segment information
         * params: points => route structure that come doInBackground
         * return: void
         */
        @Override
        protected void onPostExecute(ArrayList<LatLng> points) {
            if (points.size() == 0){
                return;
            }
            add_polylines_to_map(points);
            end_marker.setSnippet("Distance = "+String.valueOf(Math.round(distance*100)/100.0)+"Km."+"\n"+"Google: blue");
            end_marker.showInfoWindow();
        }


        /**
         * method : add_polylines_to_map
         * usage: add route to map
         * params: points tupe ArrayLIst=> points structure
         * return: void
         */
        private void add_polylines_to_map(ArrayList<LatLng> points){
            PolylineOptions polyLineOptions;


            for(int i=0; i<points.size()-1;i++){
                polyLineOptions = new PolylineOptions();
                polyLineOptions.add(points.get(i));
                polyLineOptions.add(points.get(i+1));
                polyLineOptions.width(12);
                polyLineOptions.color(classify_pollution(pollution_Arr.get(((int)(i)))));
                route_segments.add(map.addPolyline(polyLineOptions));
            }


        }


        /**
         * method : classify_pollution
         * usage: add classify polliton to be coloured later on
         * params: pollution type Double=> single pollution value
         * return: int (representing colour)
         */
        private int classify_pollution(Double pollution){
            int the_color;
            int RED_COLOR = Color.argb(200,202, 16, 16);
            int ORANGE_COLOR = Color.argb(200,225, 90, 16);
            int YELLOW_COLOR = Color.argb(200,220, 210, 42);
            int GREEN_COLOR = Color.argb(200,102, 210, 22);

            if(pollution>THR_HIGH){
                the_color = RED_COLOR;
            }
            else if(pollution>THR_MID){
                the_color = ORANGE_COLOR;
            }
            else if(pollution>THR_LOW) {
                the_color = YELLOW_COLOR;
            }
            else{
                the_color = GREEN_COLOR;
            }

            return the_color;
        }

    }

    /**
     * method : remove_google_route
     * usage: need to delete route from screen ( to process a new route)
     * params: void
     * return: void
     */
    public void remove_route(){
        if(route_segments != null){
            for (int i=0; i<route_segments.size();i++){
                route_segments.get(i).remove();
            }
        }
    }


    // setters
    public static void setTHR_HIGH(double THR_HIGH) {
        GetRouteClass.THR_HIGH = THR_HIGH;
    }

    public static void setTHR_MID(double THR_MID) {
        GetRouteClass.THR_MID = THR_MID;
    }

    public static void setTHR_LOW(double THR_LOW) {
        GetRouteClass.THR_LOW = THR_LOW;
    }
}