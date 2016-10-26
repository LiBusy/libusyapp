package com.example.dillonwastrack.libusy;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements CheckInDialogFragment.CheckInDialogListener{

    private BottomBar mBottomBar;

    public static boolean nearLibrary = false;
    public static boolean hasCheckedIn = false;
    public static boolean checkInDialogOpen = false;
    public static boolean hasReceivedNotification = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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

    }

    /**
     * Called when kill app, press home button, or lock screen
     */
    @Override
    protected void onStop() {
        super.onStop();
        checkInDialogOpen = false; // TODO in case user closed app with dialog still open, mostly for debugging.
        if (nearLibrary && !hasCheckedIn)
        {
            setCheckInAlarm();
        }
    }

    /**
     * Set the alarm for asking the
     * user to check in.
     *
     */
    private void setCheckInAlarm()
    {

        AlarmManager am = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
        Intent intent = new Intent(getBaseContext(), OnCheckInAlarmReceive.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                MainActivity.this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calCurrent = Calendar.getInstance();
        long tenSeconds = 10 * 1000; // change to 10 minutes or whatever

        am.set(AlarmManager.RTC_WAKEUP, calCurrent.getTimeInMillis() + tenSeconds, pendingIntent); // 10 seconds for now
    }

    protected void onDestroy()
    {
        super.onDestroy();
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
        hasCheckedIn = true;
    }


}
