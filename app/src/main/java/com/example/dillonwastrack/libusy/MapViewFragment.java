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
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.content.Context.LOCATION_SERVICE;


public class MapViewFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback , GoogleMap.OnMarkerClickListener {

    private GoogleMap googleMap;

    private Marker mRodgers;
    private Marker mMclure;
    private Marker mGorgas;
    private Marker mBruno;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
//        ((AppCompatActivity) getActivity()).getSupportActionBar().show(); // show action bar
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_gmaps, container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        MapFragment fragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.location_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    /**
     *
     * The menu for selecting locations
     *
     * @param item: a menu item
     * @return boolean
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
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

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {

        this.googleMap = googleMap;

        if(ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                    this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

        }
        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);


        this.googleMap.setMyLocationEnabled(true);

        try {
            initializeMarkers();
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }

        zoomToUserLocation(this.googleMap, myLocation);

    }

    private void initializeMarkers() throws AuthFailureError {
        this.mRodgers = this.googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(33.2134, -87.5427))
                .title("Rodgers Library"));

        getBusynessLevel(this.mRodgers, "rodgers");

        this.mMclure = this.googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(33.2104, -87.5490))
                .title("Mclure Library"));

        getBusynessLevel(this.mMclure, "mclure");

        this.mBruno = this.googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(33.2111, -87.5493))
                .title("Bruno Library"));

        getBusynessLevel(this.mBruno, "bruno");

        this.mGorgas = this.googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(33.2118, -87.5460))
                .title("Gorgas Library"));

        getBusynessLevel(this.mGorgas, "gorgas");

        //this.googleMap.setOnMarkerClickListener(this);

    }

    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {

        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        String url ="http://libusy.herokuapp.com/busyness/getlevel/rodgers";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        marker.setSnippet(response);
                        // mTextView.setText("Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mTextView.setText("That didn't work!");
                Log.d("level", "you suck");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        // Log.d("request body",stringRequest.getBody().toString());
        //Float level = Float.parseFloat(stringRequest.getBody().toString());
        //return level;
        return false;
    }

    public void getBusynessLevel(final Marker marker, String libraryName) throws AuthFailureError {
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        String url ="http://libusy.herokuapp.com/busyness/getlevel/"+libraryName;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        marker.setSnippet(createBusynessTextFromResponse(response));
                        // mTextView.setText("Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mTextView.setText("That didn't work!");
                Log.d("level", "you suck");
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
       // Log.d("request body",stringRequest.getBody().toString());
        //Float level = Float.parseFloat(stringRequest.getBody().toString());
        //return level;

    }

    private String createBusynessTextFromResponse(String response)
    {
        Float level = Float.parseFloat(response);

        if (level < 1.5)
        {
            return "Not Busy";
        }
        else if (level < 2.5)
        {
            return "Busy";
        }
        else
        {
            return "Very Busy";
        }
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

        // googleMap.addMarker(new MarkerOptions().title("You are here.").position(marker));
    }
}
