package com.example.dillonwastrack.libusy;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CheckInFragment extends Fragment{



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_check_in, container,false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.check_in_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        Button veryBusy = (Button) getActivity().findViewById(R.id.btnVeryBusy);
        Button busy = (Button) getActivity().findViewById(R.id.btnBusy);
        Button notBusy = (Button) getActivity().findViewById(R.id.btnNotBusy);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final String libraryName = sharedPref.getString("nearestLibrary", "mclure");

        TextView checkInText = (TextView) getActivity().findViewById(R.id.instructionText);
        checkInText.setText("Please select how busy "+ libraryName + " library is.");

        // set button listeners
        veryBusy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                NetworkManager.getInstance().postLibraryBusynessLevel(libraryName, "3", new ServerCallback() {
                    @Override
                    public void onSuccess(String response) {
                        respond();
                    }

                });
            }
        });

        busy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                NetworkManager.getInstance().postLibraryBusynessLevel(libraryName, "2", new ServerCallback() {
                    @Override
                    public void onSuccess(String response) {
                        respond();
                    }

                });
            }
        });


        notBusy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                NetworkManager.getInstance().postLibraryBusynessLevel(libraryName, "1", new ServerCallback() {
                    @Override
                    public void onSuccess(String response) {
                        respond();
                    }

                });
            }
        });

    }

    private void respond()
    {
        // save that user has checked in
        Toast.makeText(this.getActivity(), "Thank you for your response!", Toast.LENGTH_LONG).show();
        MainActivity.hasCheckedIn = true;
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().remove(this).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commit();
        // post user location to heatmap
        if(!MainActivity.addedToHeatmap)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Double userLat = Double.longBitsToDouble(sharedPref.getLong("userLat", 0));
            Double userLng = Double.longBitsToDouble(sharedPref.getLong("userLng", 0));
            NetworkManager.getInstance().postUserLocation(userLat, userLng);
        }

    }
}
