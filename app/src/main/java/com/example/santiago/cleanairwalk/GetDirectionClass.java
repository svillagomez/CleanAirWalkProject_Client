package com.example.santiago.cleanairwalk;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by santiago on 31/03/15.
 *
 * Name : GetDirectionClass type : class
 * Usage: Class that get route from our Google directions API
 */
public class GetDirectionClass extends AsyncTask<String,Void,String> {

    GoogleMap map;
    private Polyline google_route;
    private Marker my_google_stats;
    private JSONObject total_distance;

    public GetDirectionClass(GoogleMap map_ref){
        map = map_ref;
    }

    /**
     * method : doInBackground
     * usage: make http conection to server to retrieve a route
     * params: url_param: the whole address including parameters
     * return: rcvdData : a route
     */
    @Override
    protected String doInBackground(String... url_param) {
        String rcvdData = "";

        try{
            MyHttpConnection http_obj = new MyHttpConnection();
//            rcvdData = http_obj.read_Url(url_param[0]);
//            Log.e("sera",url_param[0].toString());
            rcvdData = http_obj.read_Url(url_param[0]);

        }
        catch (Exception e){
            Log.d("Error in Get Direction", e.toString());
            e.getStackTrace();
        }

        return rcvdData;
    }

    /**
     * method : onPostExecute
     * usage: perform operations after doInBackground finishes
     * params: s => output from doInBackground
     * return: void
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        new ParserTask().execute(s);
    }


    /**
     * Name : ParserTask type : class
     * Usage: Class that parse and plot a route
     */
    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {


        /**
         * method : doInBackground
         * usage: convert an parser a json route
         * params: jsonData (containing a route)
         * return: structure containing a route
         */
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
//                total_distance = jObject.getJSONObject("distance");
//                Log.e("SERIA:",jObject.toString());
                RouteJsonParser parser = new RouteJsonParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }


        /**
         * method : onPostExecute
         * usage: get every route segment an displays on screen
         * params: routes => route structure come doInBackground
         * return: void
         */
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;
            LatLng current_position = map.getCameraPosition().target;
//            my_google_stats

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    current_position = new LatLng(lat, lng);

                    points.add(current_position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(3);
                polyLineOptions.color(Color.BLUE);
            }
            google_route = map.addPolyline(polyLineOptions);

        }
    }


    /**
     * method : remove_google_route
     * usage: need to delete route from screen ( to process a new route)
     * params: void
     * return: void
     */
    public void remove_google_route(){
        if( google_route != null) {
            google_route.remove();
        }
        if(my_google_stats != null) {
            my_google_stats.remove();
        }
//        map.clear();
    }
}
