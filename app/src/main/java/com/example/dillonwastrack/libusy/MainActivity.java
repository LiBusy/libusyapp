package com.example.dillonwastrack.libusy;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends AppCompatActivity implements CheckInDialogFragment.CheckInDialogListener{

    private BottomBar mBottomBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        this.getSupportActionBar().setShowHideAnimationEnabled(false);
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
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        FragmentManager fm = getFragmentManager();
        String lib = dialog.getArguments().getString("library");
        Log.v("library_main_activity", lib);
        CheckInFragment newFragment = new CheckInFragment(); // TODO use shared preferences to store if user has already checked in
        Bundle args = new Bundle();
        args.putString("library", lib);
        newFragment.setArguments(args);
        fm.beginTransaction().replace(R.id.contentContainer, newFragment).commit();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.d("negative-click", "no");
    }
}
