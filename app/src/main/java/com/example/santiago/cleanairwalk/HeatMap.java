package com.example.santiago.cleanairwalk;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by santiago on 10/04/15.
 */
public class HeatMap {
    public static void addHetMap(GoogleMap myGoogleMap){
        List<WeightedLatLng> mylist = null;

        List uno = new ArrayList();

        uno.add(new WeightedLatLng(new LatLng(-37.797723, 144.961681),6.77));
        uno.add(new WeightedLatLng(new LatLng(-37.797791,144.966574),8.99));
        uno.add(new WeightedLatLng(new LatLng(-37.801657,144.963570),1.33));
        uno.add(new WeightedLatLng(new LatLng(-37.802267,144.972239),12.33));
        uno.add(new WeightedLatLng(new LatLng(-37.803159,144.966488),5.32));

        mylist = uno;


        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .weightedData(mylist)
                .build();

        TileOverlay mOverlay = myGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));


    }
}
