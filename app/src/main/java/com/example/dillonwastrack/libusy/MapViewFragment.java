package com.example.dillonwastrack.libusy;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.SwitchCompat;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
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

import org.json.JSONException;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class MapViewFragment extends Fragment implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap googleMap; // the main map fragment

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

    private ArrayMap<String, LatLng> markerList; // list of monitored locations


    private TileOverlay mOverlay; // used for the heatmap

    int PLACE_PICKER_REQUEST = 1;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.rodgers = new LatLng(33.2134, -87.5427); // Rodgers Library coordinates
        this.mclure = new LatLng(33.2104, -87.5490); // McLure Library coordinates
        this.gorgas = new LatLng(33.2118, -87.5460); // Gorgas Library coordinates
        this.bruno = new LatLng(33.2111, -87.5493); // Bruno Library coordinates

        setHasOptionsMenu(true);


        return inflater.inflate(R.layout.fragment_gmaps, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.location_menu, menu);

        // register heat map switch listeners
        final MenuItem toggleService = menu.findItem(R.id.heatmap);
        final SwitchCompat heatMapSwitch = (SwitchCompat) toggleService.getActionView();
        final Context homeActivity = this.getActivity();
        heatMapSwitch.setThumbResource(R.drawable.heatmap);
        heatMapSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        NetworkManager.getInstance().readUserMarkers(new ArrayList<LatLng>(), new HeatmapCallback() {
                            @Override
                            public void onSuccess(ArrayList<LatLng> markerList) {
                                userMarkerList = markerList;
                                mProvider = new HeatmapTileProvider.Builder()
                                        .data(userMarkerList)
                                        .build();
                                // Add a tile overlay to the map, using the heat map tile provider.
                                mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                                // TODO maybe make markers invisible to see heatmap better
                                Toast.makeText(getActivity(), "Heatmap engaged!", Toast.LENGTH_SHORT).show();

                            }

                        });
                    } catch (JSONException e) {
                        Toast.makeText(homeActivity, "Problem reading list of locations.", Toast.LENGTH_SHORT).show();
                    }

                } else if (!isChecked) {
                    mOverlay.remove();
                    Toast.makeText(getActivity(), "Heatmap disengaged!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     *
     * The menu for selecting locations, always
     * goes in the overflow section
     * of the app bar.
     *
     * @param item: a menu item
     * @return boolean
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.rodgers_library:
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.rodgers, 19)); // move camera
                return true;

            case R.id.mclure_library:
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.mclure, 19)); // move camera
                return true;

            case R.id.bruno_library:
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.bruno, 19)); // move camera
                return true;

            case R.id.gorgas_library:
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.gorgas, 19)); // move camera
                return true;

            case R.id.check_in:
                this.checkIn();
                return true;

            case R.id.search:
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(this.getActivity()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void checkIn() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!MainActivity.nearLibrary) {
            Toast.makeText(getActivity(), "You must be in a library to check in.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (MainActivity.hasCheckedIn) {
            String nearestLibrary = sharedPref.getString("nearestLibrary", "the");
            Toast.makeText(getActivity(), "You have already checked into " + nearestLibrary + " library.", Toast.LENGTH_SHORT).show();
            return;
        }
        SwitchCompat hMap = (SwitchCompat) getActivity().findViewById(R.id.heatmap);
        hMap.setChecked(false);
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(R.id.contentContainer, new CheckInFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();
    }


    /**
     * Method called when the map is created.
     * @param googleMap the map
     *
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getActivity());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getActivity());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getActivity());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });


        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    private void initializeMarkers() throws AuthFailureError {
        // populate the markerList, add markers from db to map
        NetworkManager.getInstance().readMarkers(new ArrayMap<String, LatLng>(), getActivity(), this.googleMap, new MarkerCallback() {

            @Override
            public void onSuccess(ArrayMap<String, LatLng> result) {
                markerList = result;
            }
        });

        //this.googleMap.setOnMarkerClickListener(this); // TODO consider not using this

    }

    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        return false;
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

        }
        this.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (this.userLocationMarker != null) {
            this.userLocationMarker.remove();
        }

        if (this.mLastLocation != null) {
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
        mLocationRequest.setInterval(5000); //10 minutes
        //mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {

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

        if(markerList != null)
        {
            Pair<String, Double> libraryAndDistance = getClosestLibraryAndDistance(this.userLatLng);

            if (libraryAndDistance.second < 50) // user is within 50 meters
            {
                MainActivity.nearLibrary = true;
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                //SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("nearestLibrary", libraryAndDistance.first);
                editor.putLong("userLat", Double.doubleToRawLongBits(this.userLatLng.latitude)); // save current location
                editor.putLong("userLng", Double.doubleToRawLongBits(this.userLatLng.longitude)); // save current location
                editor.apply();
            }

            else
            {
                MainActivity.nearLibrary = false;
            }
        }

        
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this.getActivity());
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this.getActivity(), toastMsg, Toast.LENGTH_LONG).show();
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
    private Pair<String, Double> getClosestLibraryAndDistance(LatLng userLocation)
    {

        String closestLibraryName = "";
        Double shortestDistance = Double.MAX_VALUE;
        for (ArrayMap.Entry<String, LatLng> loc : markerList.entrySet())
        {
            Double distanceToLocation = distance(userLocation.latitude,
                                                userLocation.longitude,
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

}
