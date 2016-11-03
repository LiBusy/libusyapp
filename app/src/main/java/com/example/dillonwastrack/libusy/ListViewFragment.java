package com.example.dillonwastrack.libusy;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dillonwastrack.libusy.adapters.LibraryListAdapter;
import com.example.dillonwastrack.libusy.callbacks.LocationCallback;
import com.example.dillonwastrack.libusy.models.Library;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;

import java.util.ArrayList;


/**
 * Created by dillonwastrack on 10/11/16.
 */

public class ListViewFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    //private GoogleApiClient mGoogleApiClient;

    //private ArrayList<Library> libraries;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //TODO remember to show attributions for showing google info
        Log.d("here", "here");
        // Create an instance of GoogleAPIClient.
//        if (mGoogleApiClient == null)
//        {
//            mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .addApi(Places.GEO_DATA_API)
//                    .addApi(Places.PLACE_DETECTION_API)
//                    .build();
//        }

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .hide(autocompleteFragment)
                .commit();

        return inflater.inflate(R.layout.fragment_list, container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        NetworkManager.getInstance().readLocationsIntoList(new ArrayList<Library>(), getActivity(), new LocationCallback() {
            @Override
            public void onSuccess(ArrayList<Library> result) {
                RecyclerView listView = (RecyclerView)getActivity().findViewById(R.id.rv);
                LibraryListAdapter mAdapter = new LibraryListAdapter(getActivity(), result);
                listView.setAdapter(mAdapter);
                listView.setHasFixedSize(true);
                listView.setLayoutManager(new LinearLayoutManager(getActivity()));

            }
        });
//        RecyclerView rv = (RecyclerView)getActivity().findViewById(R.id.rv);
//        rv.setHasFixedSize(true);
//        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
//        rv.setLayoutManager(llm);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
//        String placeId = "ChIJm1aVx6EChogRmEiXFGdgQlM";
//        Log.d("placeId", placeId);
//        Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
//                .setResultCallback(new ResultCallback<PlaceBuffer>() {
//                    @Override
//                    public void onResult(PlaceBuffer places) {
//                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
//                            final Place myPlace = places.get(0);
//                            Log.d("place_found", "Place found: " + myPlace.getName());
//                        } else {
//                            Log.d("place_not_found", "Place not found");
//                        }
//                        places.release();
//                    }
//                });

    }

    public void onStart()
    {
        //mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop()
    {
        //mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
