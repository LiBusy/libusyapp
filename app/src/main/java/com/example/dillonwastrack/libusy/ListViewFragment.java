package com.example.dillonwastrack.libusy;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.dillonwastrack.libusy.adapters.LibraryListAdapter;
import com.example.dillonwastrack.libusy.callbacks.LocationCallback;
import com.example.dillonwastrack.libusy.models.Library;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


/**
 * Created by dillonwastrack on 10/11/16.
 */

public class ListViewFragment extends Fragment {


    private ArrayList<Library> locations;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .hide(autocompleteFragment)
                .commit();

        final View contentView = inflater.inflate(R.layout.fragment_list, container, false);

        NetworkManager.getInstance().readLocationsIntoList(new ArrayList<Library>(), getActivity(), new LocationCallback() {
            @Override
            public void onSuccess(ArrayList<Library> result) {
                locations = result;
                RecyclerView listView = (RecyclerView) contentView.findViewById(R.id.rv);
                LibraryListAdapter mAdapter = new LibraryListAdapter(contentView.getContext(), result);
                mAdapter.notifyDataSetChanged();
                listView.setVisibility(View.VISIBLE);
                ProgressBar mProgressBar = (ProgressBar) getActivity().findViewById(R.id.progress_bar);
                mProgressBar.setVisibility(View.GONE);
                listView.setAdapter(mAdapter);
                listView.setHasFixedSize(true);
                listView.setLayoutManager(new LinearLayoutManager(getActivity()));

            }
        });
        return contentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.check_in:
                MainActivity activity = (MainActivity) getActivity();
                activity.checkIn();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
