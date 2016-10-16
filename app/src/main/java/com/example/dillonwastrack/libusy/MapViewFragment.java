package com.example.dillonwastrack.libusy;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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


public class MapViewFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap googleMap;

    private Marker mRodgersMarker;
    private Marker mMclureMarker;
    private Marker mGorgasMarker;
    private Marker mBrunoMarker;

    private LatLng rodgers;
    private LatLng mclure;
    private LatLng gorgas;
    private LatLng bruno;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.rodgers = new LatLng(33.2134, -87.5427); // Rodgers Library coordinates
        this.mclure = new LatLng(33.2104, -87.5490); // McLure Library coordinates
        this.gorgas = new LatLng(33.2118, -87.5460); // Bruno Library coordinates
        this.bruno = new LatLng(33.2111, -87.5493); // Bruno Library coordinates

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
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.rodgers, 17)); // move camera
                return true;

            case R.id.mclure_library:
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.mclure, 17)); // move camera
                return true;

            case R.id.bruno_library:
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.bruno, 17)); // move camera
                return true;

            case R.id.gorgas_library:
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.gorgas, 17)); // move camera
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Method called when the map is created.
     * @param googleMap the map
     *
     */
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

        // see if the user wants to check in
        CheckInDialogFragment newFragment = new CheckInDialogFragment();
        newFragment.show(getFragmentManager(), "check-in");

    }

    /**
     * Initialize markers for the study
     * areas. Retrieve their busyness
     * levels from the api, and set
     * the marker's snippet
     * accordingly.
     *
     * @throws AuthFailureError
     */
    private void initializeMarkers() throws AuthFailureError {

        this.mRodgersMarker = this.googleMap.addMarker(new MarkerOptions()
                .position(this.rodgers)
                .title("Rodgers Library"));

        getBusynessLevel(this.mRodgersMarker, "rodgers"); // get busyness level from api, set marker text

        this.mMclureMarker = this.googleMap.addMarker(new MarkerOptions()
                .position(this.mclure)
                .title("Mclure Library"));

        getBusynessLevel(this.mMclureMarker, "mclure"); // get busyness level from api, set marker text

        this.mBrunoMarker = this.googleMap.addMarker(new MarkerOptions()
                .position(this.bruno)
                .title("Bruno Library"));

        getBusynessLevel(this.mBrunoMarker, "bruno"); // get busyness level from api, set marker text

        this.mGorgasMarker = this.googleMap.addMarker(new MarkerOptions()
                .position(this.gorgas)
                .title("Gorgas Library"));

        getBusynessLevel(this.mGorgasMarker, "gorgas"); // get busyness level from api, set marker text

        //this.googleMap.setOnMarkerClickListener(this); // TODO consider not using this

    }

    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {

//        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
//        String url ="http://libusy.herokuapp.com/busyness/getlevel/rodgers";
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Display the first 500 characters of the response string.
//                        marker.setSnippet(response);
//                        // mTextView.setText("Response is: "+ response.substring(0,500));
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                //mTextView.setText("That didn't work!");
//                Log.d("level", "you suck");
//            }
//        });
//        // Add the request to the RequestQueue.
//        queue.add(stringRequest);
        // Log.d("request body",stringRequest.getBody().toString());
        //Float level = Float.parseFloat(stringRequest.getBody().toString());
        //return level;
        return false;
    }

    /**
     * Get the busyness level of the library
     * to place on the marker, performs an
     * api request
     *
     * @param marker a google maps marker
     * @param libraryName the name of the library for the api
     * @throws AuthFailureError
     */
    public void getBusynessLevel(final Marker marker, String libraryName) throws AuthFailureError {
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        String url ="http://libusy.herokuapp.com/busyness/getlevel/"+libraryName;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        marker.setSnippet(createBusynessTextFromResponse(response));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("level", "you suck");
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
       // Log.d("request body",stringRequest.getBody().toString());
        //Float level = Float.parseFloat(stringRequest.getBody().toString());
        //return level;

    }

    /**
     * The algorithm. Determines how busy
     * the library is based on it's
     * busyness rating.
     *
     * @param response the response from the api
     * @return String to display in the marker snippet
     */
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

    /**
     * Zoom to the user's current location
     * and place a marker there.
     *
     * @param googleMap the map to place the marker on
     * @param myLocation location object with the user's current location
     */
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
        googleMap.addMarker(new MarkerOptions().position(latLng).title("You are here!"));

    }

}
