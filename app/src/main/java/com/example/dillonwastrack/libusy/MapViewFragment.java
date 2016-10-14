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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private GoogleMap googleMap;

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.location_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.rodgers_library:
                LatLng rodgers = new LatLng(33.2134, -87.5427); // Rodgers Library coordinates
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rodgers, 17)); // move camera
                return true;

            case R.id.mclure_library:
                LatLng mclure = new LatLng(33.2104, -87.5490); // McLure Library coordinates
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mclure, 17)); // move camera
                return true;

            case R.id.bruno_library:
                LatLng bruno = new LatLng(33.2111, -87.5493); // Bruno Library coordinates
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bruno, 17)); // move camera
                return true;

            case R.id.gorgas_library:
                LatLng gorgas = new LatLng(33.2118, -87.5460); // Bruno Library coordinates
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gorgas, 17)); // move camera
                return true;
        }

        return super.onOptionsItemSelected(item); // important line
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

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


        this.googleMap.setMyLocationEnabled(true);
        initializeMarkers(this.googleMap);
        zoomToUserLocation(this.googleMap, myLocation);

    }

    private void initializeMarkers(GoogleMap googleMap)
    {
        this.googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(33.2134, -87.5427))
                .title("Rodgers Library"));

        this.googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(33.2104, -87.5490))
                .title("Mclure Library"));

        this.googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(33.2111, -87.5493))
                .title("Bruno Library"));

        this.googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(33.2118, -87.5460))
                .title("Gorgas Library"));

        //LatLng latLng = new LatLng(33.2134, -87.5427); // TODO move this stuff to another method, it isn't initializing markers
        //this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }

    private void zoomToUserLocation(GoogleMap googleMap, Location myLocation)
    {

        //set map type
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Get latitude of the current location
        double latitude = myLocation.getLatitude();

        // Get longitude of the current location
        double longitude = myLocation.getLongitude();

        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        // Show the current location in Google Map
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        //googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You are here!"));

        // googleMap.addMarker(new MarkerOptions().title("Hello Google Maps!").position(marker));
    }
}
