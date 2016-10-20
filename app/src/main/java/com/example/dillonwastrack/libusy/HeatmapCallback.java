package com.example.dillonwastrack.libusy;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public interface HeatmapCallback{
    void onSuccess(ArrayList<LatLng> result);
}
