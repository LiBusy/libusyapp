package com.example.dillonwastrack.libusy;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends AppCompatActivity implements CheckInDialogFragment.CheckInDialogListener{

    private BottomBar mBottomBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        NetworkManager.getInstance(this.getApplicationContext());
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("hasCheckedIn", false);
        editor.apply();

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

    protected void onDestroy()
    {
        super.onDestroy();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("hasCheckedIn", false);
        editor.commit();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        FragmentManager fm = getFragmentManager();
        String lib = dialog.getArguments().getString("library");
        CheckInFragment newFragment = new CheckInFragment();
        Bundle args = new Bundle();
        args.putString("library", lib);
        newFragment.setArguments(args);
        fm.beginTransaction().replace(R.id.contentContainer, newFragment).commit();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // user chose not to check in, stop bothering them
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("hasCheckedIn", true);
        editor.apply();
    }


}
