package com.example.dillonwastrack.libusy;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.SwitchCompat;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapViewFragment extends Fragment implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap googleMap; // the main map fragment

    private Marker mRodgersMarker; // the marker for Rodgers
    private Marker mMclureMarker; // the marker for McLure
    private Marker mGorgasMarker; // the marker for Gorgas
    private Marker mBrunoMarker; // the marker for Bruno
    private Marker userLocationMarker; // the marker for the user's location

    private LatLng rodgers; // Rodgers coordinates
    private LatLng mclure; // McLure coordinates
    private LatLng gorgas; // Gorgas coordinates
    private LatLng bruno; // Bruno coordinates
    private LatLng userLatLng; // User coordinates

    private GoogleApiClient mGoogleApiClient; // google API client for getting user location

    private Location mLastLocation; // last known location of the user

    private HeatmapTileProvider mProvider; // provider for heat map tiling

    private ArrayList<LatLng> userMarkerList; // list of markers used in the heatmap. pulled from the api

    private boolean heatMapActive; // boolean for using the heatmap toggle switch

    private TileOverlay mOverlay; // used for the heatmap


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.rodgers = new LatLng(33.2134, -87.5427); // Rodgers Library coordinates
        this.mclure = new LatLng(33.2104, -87.5490); // McLure Library coordinates
        this.gorgas = new LatLng(33.2118, -87.5460); // Gorgas Library coordinates
        this.bruno = new LatLng(33.2111, -87.5493); // Bruno Library coordinates

        heatMapActive = false;

        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_gmaps, container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        MapFragment fragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.location_menu, menu);

        // register heat map switch listeners
        final MenuItem toggleservice = menu.findItem(R.id.heatmap);
        final SwitchCompat heatMapSwitch = (SwitchCompat) toggleservice.getActionView();
        final Context homeActivity = this.getActivity();
        heatMapSwitch.setThumbResource(R.drawable.heatmap);
        heatMapSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    try {
                        readMarkersFromAPI(new ServerCallback() {
                            @Override
                            public void onSuccess(String response) {
                                mProvider = new HeatmapTileProvider.Builder()
                                        .data(userMarkerList)
                                        .build();
                                // Add a tile overlay to the map, using the heat map tile provider.
                                mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                                Toast.makeText(getActivity() ,"Heatmap engaged!", Toast.LENGTH_LONG).show();
                                //googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                            }

                        });
                    } catch (JSONException e) {
                        Toast.makeText(homeActivity, "Problem reading list of locations.", Toast.LENGTH_SHORT).show();
                    }

                }

                else if (!isChecked)
                {
                    mOverlay.remove();
                    Toast.makeText(getActivity() ,"Heatmap disengaged!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        MenuItem checkable = menu.findItem(R.id.heatmap);
        checkable.setChecked(heatMapActive);
    }

    /**
     *
     * The menu for selecting locations,
     * always uses the overflow seciton.
     * does not contain the heatmap.
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

        this.googleMap.setMyLocationEnabled(true);

        try {
            initializeMarkers();
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }

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
    private void initializeMarkers() throws AuthFailureError
    {

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
    public boolean onMarkerClick(final Marker marker)
    {

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
    public void getBusynessLevel(final Marker marker, String libraryName) throws AuthFailureError
    {
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        String url ="https://libusy.herokuapp.com/busyness/getlevel/"+libraryName;
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

    }

    /**
     * The algorithm. Determines how busy
     * the library is based on its
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

        if (level < 2.5)
        {
            return "Busy";
        }

        return "Very Busy";
    }


    @Override
    public void onConnected(Bundle connectionHint)
    {
        if(ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                    this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

        }
        this.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (this.mLastLocation != null)
        {
            //place marker at current position
            this.userLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(this.userLatLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            this.userLocationMarker = this.googleMap.addMarker(markerOptions);


            //zoom to current position:
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(this.userLatLng).zoom(14).build();

            this.googleMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }
        // request location updates
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        //mLocationRequest.setFastestInterval(3000); //3 seconds
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, mLocationRequest, this);

        // see if the user wants to check in
        // find closest library
        // if library within a mile or so, ask to check in

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean hasCheckedIn = sharedPref.getBoolean("hasCheckedIn", false);
        if (! hasCheckedIn && mLastLocation != null)
        {
            Pair<String, Double> libraryAndDistance = getClosestLibrary();

            if (libraryAndDistance.second < 10) // user is within 10 meters
            {
                CheckInDialogFragment newFragment = new CheckInDialogFragment();
                Bundle args = new Bundle();
                args.putString("library", libraryAndDistance.first); // whatever the closest library is
                newFragment.setArguments(args);
                newFragment.show(getFragmentManager(), "check-in");
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    public void onStart()
    {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop()
    {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        this.mLastLocation = location;


        if (this.userLocationMarker != null)
        {
            this.userLocationMarker.remove();
        }

        this.userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(this.userLatLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        this.userLocationMarker = this.googleMap.addMarker(markerOptions);


        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(this.userLatLng).zoom(14).build();

        this.googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean hasCheckedIn = sharedPref.getBoolean("hasCheckedIn", false);
        if (! hasCheckedIn)
        {
            Pair<String, Double> libraryAndDistance = getClosestLibrary();

            if (libraryAndDistance.second < 10)
            {
                CheckInDialogFragment newFragment = new CheckInDialogFragment();
                Bundle args = new Bundle();
                args.putString("library", libraryAndDistance.first); // whatever the closest library is
                newFragment.setArguments(args);
                newFragment.show(getFragmentManager(), "check-in");
            }

        }
    }

    /**
     * This distance algorithm uses the haversine
     * great circle approximation to determine
     * the distance between two points in
     * meters.
     *
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second pount
     * @return distance between 2 points in meters
     */
    private double distance(double lat1, double lon1, double lat2, double lon2)
    {
        // haversine great circle distance approximation, returns meters
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60; // 60 nautical miles per degree of separation
        dist = dist * 1852; // 1852 meters per nautical mile
        return (dist);
    }

    /**
     * Used for converting degrees to
     * radians.
     *
     * @param deg degrees
     * @return conversion from degrees to radians
     */
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /**
     * Used for converting radians to
     * degrees.
     *
     * @param rad radians
     * @return conversion from radians to degrees
     */
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    /**
     * Determines which of the four
     * main libraries that the user is
     * closest to.
     *
     * @return String representing the library (for making an api call)
     */
    private Pair<String, Double> getClosestLibrary()
    {
        ArrayMap<String, LatLng> locationList = new ArrayMap<String, LatLng>();
        locationList.put("rodgers", this.rodgers);
        locationList.put("bruno", this.bruno);
        locationList.put("gorgas", this.gorgas);
        locationList.put("mclure", this.mclure);

        String closestLibraryName = "";
        Double shortestDistance = Double.MAX_VALUE;
        for (ArrayMap.Entry<String, LatLng> loc : locationList.entrySet())
        {
            Double distanceToLocation = distance(this.userLatLng.latitude,
                                                 this.userLatLng.longitude,
                                                 loc.getValue().latitude,
                                                 loc.getValue().longitude);

            if (distanceToLocation < shortestDistance)
            {
                shortestDistance = distanceToLocation;
                closestLibraryName = loc.getKey();
            }
        }
        return new Pair<>(closestLibraryName, shortestDistance);
    }

    /**
     * Use the api to get the list of dropped pins,
     * read into JSONArray, create LatLng
     * objects from JSONObjects, add
     * them to the userMarkerList
     *
     * @param callback the callback for when the request is complete
     * @throws JSONException
     */
    private void readMarkersFromAPI(final ServerCallback callback) throws JSONException
    {
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        String url = "https://libusy.herokuapp.com/usermarkers";
        StringRequest jsonRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            userMarkerList = new ArrayList<LatLng>();
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                double lat = object.getDouble("lat");
                                double lng = object.getDouble("lng");
                                userMarkerList.add(new LatLng(lat, lng));
                            }
                            callback.onSuccess(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("level", "you suck");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonRequest);
    }

}
