package com.libusy.dillonwastrack.libusy.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.util.ArrayMap;
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
import com.libusy.dillonwastrack.libusy.activities.MainActivity;
import com.libusy.dillonwastrack.libusy.singletons.NetworkManager;
import com.libusy.dillonwastrack.libusy.R;
import com.libusy.dillonwastrack.libusy.callbacks.HeatmapCallback;
import com.libusy.dillonwastrack.libusy.callbacks.MarkerCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by dillonwastrack on 10/11/16.
 */

public class MapViewFragment extends Fragment implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMarkerClickListener
        {

    private GoogleMap googleMap; // the main map fragment


    protected LatLng rodgers; // Rodgers coordinates
    protected LatLng mclure; // McLure coordinates
    protected LatLng gorgas; // Gorgas coordinates
    protected LatLng bruno; // Bruno coordinates

    private HeatmapTileProvider mProvider; // provider for heat map tiling

    private ArrayList<LatLng> userMarkerList; // list of markers used in the heatmap. pulled from the api

    private TileOverlay mOverlay; // used for the heatmap

    private ArrayMap<String, LatLng> markerList;

    private Activity mainActivity;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private DrawerArrowDrawable backArrow;

    private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.rodgers = new LatLng(33.2134, -87.5427); // Rodgers Library coordinates
        this.mclure = new LatLng(33.2104, -87.5490); // McLure Library coordinates
        this.gorgas = new LatLng(33.2118, -87.5460); // Gorgas Library coordinates
        this.bruno = new LatLng(33.2111, -87.5493); // Bruno Library coordinates

        setHasOptionsMenu(true);

        mOnBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                syncActionBarArrowState();
            }
        };

        return inflater.inflate(R.layout.fragment_gmaps, container, false);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);
        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if(fragment != null)
        {
            getChildFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

            @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            mainActivity =(Activity) context;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = activity;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        fragment.getMapAsync(this);

        mDrawerLayout = (DrawerLayout) mainActivity.findViewById(R.id.drawer_layout);
        mActivityTitle = mainActivity.getTitle().toString();

        setupDrawer();

        NavigationView nav = (NavigationView)mainActivity.findViewById(R.id.navigation_view);
        nav.getMenu().clear();
        nav.inflateMenu(R.menu.map_navigation_menu);

        MenuItem switchItem = nav.getMenu().findItem(R.id.heatmap);
        CompoundButton switchView = (CompoundButton) MenuItemCompat.getActionView(switchItem);
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        NetworkManager.getInstance().readUserMarkers(new ArrayList<LatLng>(), new HeatmapCallback() {
                            @Override
                            public void onSuccess(ArrayList<LatLng> markerList) {
                                userMarkerList = markerList;
                                if(markerList.isEmpty())
                                {
                                    mDrawerLayout.closeDrawer(GravityCompat.START);
                                    Toast.makeText(mainActivity, "No check-ins to display on heatmap.", Toast.LENGTH_SHORT).show();
                                    buttonView.setChecked(false);
                                    return;
                                }
                                mProvider = new HeatmapTileProvider.Builder()
                                        .data(userMarkerList)
                                        .build();
                                mDrawerLayout.closeDrawer(GravityCompat.START);
                                // Add a tile overlay to the map, using the heat map tile provider.
                                mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                                Toast.makeText(mainActivity, "Heatmap on", Toast.LENGTH_SHORT).show();

                            }

                        });
                    } catch (JSONException e) {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        Toast.makeText(mainActivity, "Problem reading list of locations.", Toast.LENGTH_SHORT).show();
                    }

                } else if (!isChecked) {
                    if(mOverlay != null)
                    {
                        mOverlay.remove();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        Toast.makeText(mainActivity, "Heatmap off", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.rodgers_library:
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rodgers, 19)); // move camera
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;

                    case R.id.mclure_library:
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mclure, 19)); // move camera
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;

                    case R.id.bruno_library:
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bruno, 19)); // move camera
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;

                    case R.id.gorgas_library:
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gorgas, 19)); // move camera
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;

                    case R.id.check_in:
                        MainActivity activity = (MainActivity) mainActivity;
                        activity.checkIn();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;

                    case R.id.available_computers:
                    {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.lib.ua.edu/computers/"));
                        mainActivity.startActivity(intent);
                        return true;
                    }


                    case R.id.library_software:
                    {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://guides.lib.ua.edu/software"));
                        mainActivity.startActivity(intent);
                        return true;
                    }

                    case R.id.find_a_place_to_study:
                    {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.lib.ua.edu/using-the-library/find-a-place-to-study/"));
                        mainActivity.startActivity(intent);
                        return true;
                    }

                    case R.id.book_a_team_room:
                    {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://ua.libcal.com/booking/groupstudy"));
                        mainActivity.startActivity(intent);
                        return true;
                    }

                    case R.id.library_databases:
                    {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://guides.lib.ua.edu/az.php"));
                        mainActivity.startActivity(intent);
                        return true;
                    }

                }
                return false;
            }
        });

    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.location_menu, menu);

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

            case R.id.check_in:
                MainActivity activity = (MainActivity) mainActivity;
                activity.checkIn();
                return true;

        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (mDrawerToggle.isDrawerIndicatorEnabled() &&
                mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == android.R.id.home &&
                getFragmentManager().popBackStackImmediate()) {
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
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(mainActivity);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(mainActivity);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(mainActivity);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });


        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    mainActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

        }

        this.googleMap.setMyLocationEnabled(true);

        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.gorgas, 16)); // move camera


        try {
            initializeMarkers();
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }

    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(mainActivity,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mDrawerToggle.setDrawerIndicatorEnabled(true);
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                syncActionBarArrowState();
            }
        };


        mDrawerToggle.setDrawerArrowDrawable(new DrawerArrowDrawable(mainActivity));
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        getFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        syncActionBarArrowState();
    }

    private void initializeMarkers() throws AuthFailureError {
        // populate the markerList, add markers from db to map
        NetworkManager.getInstance().readMarkers(new ArrayMap<String, LatLng>(), mainActivity, this.googleMap, new MarkerCallback() {

            @Override
            public void onSuccess(ArrayMap<String, LatLng> result) {
                markerList = result;
            }
        });

    }

    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        getFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);
        mainActivity.setTitle(R.string.app_name);
        ActionBar ab = ((AppCompatActivity) mainActivity).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
    }

    private void syncActionBarArrowState() {
        try {
            int backStackEntryCount = getFragmentManager().getBackStackEntryCount();
            mDrawerToggle.setDrawerIndicatorEnabled(backStackEntryCount == 0);
        }catch (NullPointerException e){}

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);
    }
}
