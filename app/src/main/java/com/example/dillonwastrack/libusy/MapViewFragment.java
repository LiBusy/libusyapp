package com.example.dillonwastrack.libusy;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.example.dillonwastrack.libusy.callbacks.HeatmapCallback;
import com.example.dillonwastrack.libusy.callbacks.MarkerCallback;
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
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

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
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.location_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
        // register heat map switch listeners
        final MenuItem toggleService = menu.findItem(R.id.heatmap);
        final SwitchCompat heatMapSwitch = (SwitchCompat) toggleService.getActionView();
        final Context homeActivity = mainActivity;
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
                                Toast.makeText(mainActivity, "Heatmap engaged!", Toast.LENGTH_SHORT).show();

                            }

                        });
                    } catch (JSONException e) {
                        Toast.makeText(homeActivity, "Problem reading list of locations.", Toast.LENGTH_SHORT).show();
                    }

                } else if (!isChecked) {
                    mOverlay.remove();
                    Toast.makeText(mainActivity, "Heatmap disengaged!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) mainActivity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(mainActivity.getComponentName()));

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
                MainActivity activity = (MainActivity) mainActivity;
                activity.checkIn();
                return true;

            case R.id.search:

//                PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
//                        getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
//
//                if (autocompleteFragment.isHidden())
//                {
//
//                    FragmentManager fm = getFragmentManager();
//                    fm.beginTransaction()
//                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
//                            .show(autocompleteFragment)
//                            .commit();
//                    EditText searchInput = (EditText) mainActivity.findViewById(R.id.place_autocomplete_search_input);
//                    searchInput.performClick();
//
//                }
//                else
//                {
//                    FragmentManager fm = getFragmentManager();
//                    fm.beginTransaction()
//                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
//                            .hide(autocompleteFragment)
//                            .commit();
//                }

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

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                //TODO pass Place ID (tag) to LibraryDetailsFragment
            }
        });

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

    private void initializeMarkers() throws AuthFailureError {
        // populate the markerList, add markers from db to map
        NetworkManager.getInstance().readMarkers(new ArrayMap<String, LatLng>(), mainActivity, this.googleMap, new MarkerCallback() {

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


    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
    }


}
