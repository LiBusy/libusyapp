package com.example.dillonwastrack.libusy;

import android.*;
import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.content.Context.LOCATION_SERVICE;


public class MapViewFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        ((AppCompatActivity) getActivity()).getSupportActionBar().show(); // show action bar
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_gmaps, container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapFragment fragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

       if(ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
       {
           ActivityCompat.requestPermissions(this.getActivity(),
                   new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                   1);

       }

        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location, permission requested in onMapReady
        Location myLocation = locationManager.getLastKnownLocation(provider);


        googleMap.setMyLocationEnabled(true);
        zoomToUserLocation(googleMap, myLocation);
        initializeMarkers(googleMap);

    }

    private void initializeMarkers(GoogleMap googleMap)
    {
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(33.2134, -87.5427))
                .title("Rodgers Library"));

        LatLng latLng = new LatLng(33.2134, -87.5427);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }

    private void zoomToUserLocation(GoogleMap googleMap, Location myLocation)
    {

        //set map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Get latitude of the current location
        double latitude = myLocation.getLatitude();

        // Get longitude of the current location
        double longitude = myLocation.getLongitude();

        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        // Show the current location in Google Map
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        //googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You are here!"));

        // googleMap.addMarker(new MarkerOptions().title("Hello Google Maps!").position(marker));
    }
}
