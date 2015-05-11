package com.example.santiago.cleanairwalk;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.ArrayList;

/**
 * Created by santiago on 7/05/15.
 * http://stackoverflow.com/questions/3505930/make-an-http-request-with-android
 */
public class GetRouteClass extends AsyncTask  <String, Void, String> {

    GoogleMap map;

    private Polyline route = null;
    private ArrayList<Polyline> route_segments;

    public GetRouteClass(GoogleMap map_ref){
        map = map_ref;
        route_segments = new ArrayList<Polyline>();
    }

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
//                InputStream inputStream = entity.getContent();
//                result = convertStreamToString(inputStream);
            }
//            Log.d("CHILANDO",json_response);
//            StatusLine statusLine = response.getStatusLine();
//            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                response.getEntity().writeTo(out);
//                responseString = out.toString();
//                out.close();
//            } else{
//                //Closes the connection.
//                response.getEntity().getContent().close();
//                throw new IOException(statusLine.getReasonPhrase());
//            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
            e.printStackTrace();
        }
//        catch (URISyntaxException e){
//            e.printStackTrace();
//        }
        catch (IOException e) {
            e.printStackTrace();
            //TODO Handle problems..
        }
//        Log.d("RECIBI",responseString);
        return json_response;
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        new ParserTask().execute(result);
    }

    private class ParserTask extends
            AsyncTask<String, Void, ArrayList<LatLng> > {

        ArrayList<Double> pollution_Arr;

        private ParserTask(){
            pollution_Arr = new ArrayList<Double>();
        }

        @Override
        protected ArrayList<LatLng> doInBackground(
                String... jsonData) {
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            JSONObject jObject;

//            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                double lat;
                double lon;
                double pollution_value;

                for (int j = 0; j < jObject.length();j++) {
//                    hh = (jObject.get(Integer.toString(j)));
                    lat = ((JSONArray)(jObject.get(Integer.toString(j)))).getDouble(0);
                    lon = ((JSONArray)(jObject.get(Integer.toString(j)))).getDouble(1);
                    pollution_value = ((JSONArray)(jObject.get(Integer.toString(j)))).getDouble(2);
                    LatLng position = new LatLng(lat, lon);
                    points.add(position);
                    pollution_Arr.add(pollution_value);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return points;
        }

        @Override
        protected void onPostExecute(ArrayList<LatLng> points) {
//            PolylineOptions polyLineOptions = new PolylineOptions();
//            polyLineOptions.addAll(points);
//            polyLineOptions.width(10);
//            polyLineOptions.color(Color.MAGENTA);
//
//
//            route = map.addPolyline(polyLineOptions);
            add_polylines_to_map(points);
        }



        private void add_polylines_to_map(ArrayList<LatLng> points){
            PolylineOptions polyLineOptions;


            for(int i=0; i<points.size()-1;i++){
                polyLineOptions = new PolylineOptions();
                polyLineOptions.add(points.get(i));
                polyLineOptions.add(points.get(i+1));
                polyLineOptions.width(10);
                polyLineOptions.color(classify_pollution(pollution_Arr.get(((int)(i)))));
                route_segments.add(map.addPolyline(polyLineOptions));
            }


        }

        private int classify_pollution(Double pollution){
            int the_color;
            double THR_HIGH = 3.0;
            double THR_MID = 2.0;
            double THR_LOW = 1.0;
            if(pollution>THR_HIGH){
                the_color = Color.RED;
            }
            else if(pollution>THR_MID){
                the_color = Color.BLUE;
            }
            else if(pollution>THR_LOW) {
                the_color = Color.YELLOW;
            }
            else{
                the_color = Color.GREEN;
            }

            return the_color;
        }

    }

    public void remove_route(){
//        if(route != null){
//            route.remove();
//        }
        if(route_segments != null){
            for (int i=0; i<route_segments.size();i++){
                route_segments.get(i).remove();
            }
        }
    }



}