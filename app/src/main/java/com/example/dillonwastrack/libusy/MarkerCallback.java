package com.example.dillonwastrack.libusy;

import android.util.ArrayMap;

import com.google.android.gms.maps.model.LatLng;

public interface MarkerCallback{
    void onSuccess(ArrayMap<String, LatLng> result);
}
