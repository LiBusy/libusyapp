package com.example.dillonwastrack.libusy;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class CheckInFragment extends Fragment{



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_check_in, container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        Button veryBusy = (Button) getActivity().findViewById(R.id.btnVeryBusy);
        Button busy = (Button) getActivity().findViewById(R.id.btnBusy);
        Button notBusy = (Button) getActivity().findViewById(R.id.btnNotBusy);

        final Context homeActivity = this.getActivity();
        final String libraryName = this.getArguments().getString("library");

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
        fm.beginTransaction().replace(R.id.contentContainer, new MapViewFragment()).commit();
    }
}
