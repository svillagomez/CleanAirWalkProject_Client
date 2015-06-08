package com.example.santiago.cleanairwalk;

import android.os.AsyncTask;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;

/**
 * Created by santiago on 16/05/15.
 *
 * Name : GetPollutionClass type : class
 * Usage: Class that get pollution max value from our our own server
 */
public class GetPollutionClass  extends AsyncTask<String,Void,String> {

    private TextView tView_1;
    private TextView tView_2;
    private TextView tView_3;
    private TextView tView_4;
    private static double max_pollution_value = 150;


    public GetPollutionClass(TextView Tv1,TextView Tv2,TextView Tv3,TextView Tv4){
        tView_1 = Tv1;
        tView_2 = Tv2;
        tView_3 = Tv3;
        tView_4 = Tv4;
    }


    /**
     * method : doInBackground
     * usage: make http connection to server to retrieve pollution data
     * params: uri: the whole address including parameters
     * return: rcvdData type string : a route
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
     * usage: change text that show pollution ranges
     * params: result (came from doInBackground)
     * return: void
     */
    @Override
    protected void onPostExecute(String result) {
        double valor;
        super.onPostExecute(result);
        Parser my_parser = new Parser();
        valor = my_parser.parse(result);
        tView_1.setText("<"+String.format("%.2f",valor/4));
        tView_2.setText("<"+String.format("%.2f",2*valor/4));
        tView_3.setText("<"+String.format("%.2f",3*valor/4));
        tView_4.setText("<"+String.format("%.2f",valor));
    }


    private class Parser{
        JSONObject jObject;

        public Parser(){
        }

        /**
         * method : parse
         * usage: parse the json that include the pollution data from the server
         * params: jsonString type: string ( json data)
         * return: double: pollution value in double type format
         */
        public double parse(String jsonString){
            double ret_val = 150.0;

            try{
                jObject = new JSONObject(jsonString);
                ret_val = (Double)(jObject.get("0"));
            }
            catch (Exception e){
                e.printStackTrace();
            }
            max_pollution_value = ret_val;

            GetRouteClass.setTHR_HIGH(3.0*getMax_pollution_value()/4.0);
            GetRouteClass.setTHR_MID(2.0*getMax_pollution_value()/4.0);
            GetRouteClass.setTHR_LOW(1.0*getMax_pollution_value()/4.0);

            return ret_val;
        }
    }

    public static double getMax_pollution_value() {
        return max_pollution_value;
    }
}
