package com.example.santiago.cleanairwalk;


import android.os.Bundle;
import android.preference.PreferenceActivity;


/**
 * Created by santiago on 3/06/15.
 *
 * Name: Prefs type : class
 * Usage: allow display setting to change IP and PORT
 */
public class Prefs extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.prefs);
        addPreferencesFromResource(R.xml.prefs);
    }
}
