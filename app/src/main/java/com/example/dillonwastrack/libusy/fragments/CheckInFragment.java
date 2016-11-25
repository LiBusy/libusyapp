package com.example.dillonwastrack.libusy.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.dillonwastrack.libusy.activities.MainActivity;
import com.example.dillonwastrack.libusy.singletons.NetworkManager;
import com.example.dillonwastrack.libusy.R;
import com.example.dillonwastrack.libusy.callbacks.ServerCallback;

public class CheckInFragment extends Fragment{


    private Activity mainActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        ActionBar ab = ((AppCompatActivity) mainActivity).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
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

        Button veryBusy = (Button) mainActivity.findViewById(R.id.btnVeryBusy);
        Button busy = (Button) mainActivity.findViewById(R.id.btnBusy);
        Button notBusy = (Button) mainActivity.findViewById(R.id.btnNotBusy);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);

        final String libraryName = sharedPref.getString("nearestLibrary", "");

        TextView checkInText = (TextView) mainActivity.findViewById(R.id.instructionText);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            mainActivity = (Activity) context;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = activity;
    }

    private void respond()
    {
        ((MainActivity) mainActivity).respondToCheckIn();
    }

}
