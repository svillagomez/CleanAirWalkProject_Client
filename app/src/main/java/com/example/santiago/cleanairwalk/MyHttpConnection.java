package com.example.santiago.cleanairwalk;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by santiago on 31/03/15.
 */
public class MyHttpConnection {
    public String read_Url(String dirToMapsApi) throws IOException{
        String data = "";

        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        BufferedReader bufferedReader;
        String read_line = "";

        try{
            URL url_obj = new URL(dirToMapsApi);
            httpURLConnection = (HttpURLConnection) url_obj.openConnection();
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();

            while ((read_line = bufferedReader.readLine())!= null){
                stringBuffer.append(read_line);
            }

            data = stringBuffer.toString();
            bufferedReader.close();
        }
        catch (Exception e){
            Log.d("Exception while reading",e.toString());
            e.getStackTrace();
        }
        finally {
            inputStream.close();
            httpURLConnection.disconnect();
        }
        return data;
    }
}
