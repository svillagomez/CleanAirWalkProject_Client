package com.example.santiago.cleanairwalk;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by santiago on 31/03/15.
 */
public class GetDirectionClass extends AsyncTask<String,Void,String> {

    GoogleMap map;

    public GetDirectionClass(GoogleMap map_ref){
        map = map_ref;
    }

    @Override
    protected String doInBackground(String... url_param) {
        String rcvdData = "";

        try{
            MyHttpConnection http_obj = new MyHttpConnection();
            rcvdData = http_obj.read_Url(url_param[0]);
        }
        catch (Exception e){
            Log.d("Error in Get Direction", e.toString());
            e.getStackTrace();
        }

        return rcvdData;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        new ParserTask().execute(s);
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                RouteJsonParser parser = new RouteJsonParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(5);
                polyLineOptions.color(Color.GREEN);
            }
            map.addPolyline(polyLineOptions);
        }
    }
}
