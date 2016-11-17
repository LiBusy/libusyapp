package com.example.dillonwastrack.libusy;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.example.dillonwastrack.libusy.callbacks.MarkerCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;


/**
 * Created by dillonwastrack on 10/11/16.
 */

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private BottomBar mBottomBar;

    public static boolean nearLibrary = false;
    public static boolean hasCheckedIn = false;
    public static boolean addedToHeatmap = false;

    private GoogleApiClient mGoogleApiClient;

    protected ArrayMap<String, LatLng> locations;

    private Location mLastLocation;

    private LatLng userLatLng;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        NetworkManager.getInstance(this.getApplicationContext());

        setContentView(R.layout.activity_main);

        // set up bottom navigation
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener()
        {
            @Override
            public void onTabSelected(@IdRes int tabId)
            {
                FragmentManager fm = getFragmentManager();
                if (tabId == R.id.tab_map)
                {
                    fm.beginTransaction().replace(R.id.contentContainer, new MapViewFragment()).commit();
                }
                else if (tabId == R.id.tab_list)
                {
                    fm.beginTransaction().replace(R.id.contentContainer, new ListViewFragment()).commit();
                }
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        NetworkManager.getInstance().readLocations(new ArrayMap<String, LatLng>(), this, new MarkerCallback() {
            @Override
            public void onSuccess(ArrayMap<String, LatLng> result) {
                locations = result;

                checkDistance();

            }
        });


    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("query", query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    public LatLng getUserLatLng() {
        return userLatLng;
    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
//        if (nearLibrary && !hasCheckedIn)
//        {
//            setCheckInAlarm();
//        }
        super.onStop();
    }

    /**
     * Set the alarm for asking the
     * user to check in.
     *
     */
//    private void setCheckInAlarm()
//    {
//
//        AlarmManager am = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
//        Intent intent = new Intent(getBaseContext(), OnCheckInAlarmReceive.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                MainActivity.this, 0, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Calendar calCurrent = Calendar.getInstance();
//        long tenSeconds = 10 * 1000; // change to 10 minutes or whatever
//
//        am.set(AlarmManager.RTC_WAKEUP, calCurrent.getTimeInMillis() + tenSeconds, pendingIntent); // 10 seconds for now
//    }

    protected void onDestroy()
    {
        super.onDestroy();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

        }

        this.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (this.mLastLocation != null)
        {
            this.userLatLng = new LatLng(this.mLastLocation.getLatitude(), this.mLastLocation.getLongitude());
        }

        checkDistance();
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

    @Override
    public void onLocationChanged(Location location) {
        this.userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        this.checkDistance();


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
        for (ArrayMap.Entry<String, LatLng> loc : locations.entrySet())
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

    public void checkIn() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!nearLibrary) {
            Toast.makeText(this, "You must be in a library to check in.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (hasCheckedIn) {
            String nearestLibrary = sharedPref.getString("nearestLibrary", "the");
            Toast.makeText(this, "You have already checked into " + nearestLibrary + " library.", Toast.LENGTH_SHORT).show();
            return;
        }
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(R.id.contentContainer, new CheckInFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();
    }

    private void checkDistance()
    {
        if(locations != null && userLatLng != null)
        {
            Pair<String, Double> libraryAndDistance = getClosestLibraryAndDistance(userLatLng);

            if (libraryAndDistance.second < 50) // user is within 50 meters
            {
                nearLibrary = true;
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("nearestLibrary", libraryAndDistance.first);
                editor.putLong("userLat", Double.doubleToRawLongBits(userLatLng.latitude)); // save current location
                editor.putLong("userLng", Double.doubleToRawLongBits(userLatLng.longitude)); // save current location
                editor.apply();
            }

            else
            {
                nearLibrary = false;
            }
        }
    }


}
