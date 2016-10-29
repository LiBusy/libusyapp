package com.example.dillonwastrack.libusy;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private BottomBar mBottomBar;

    public static boolean nearLibrary = false;
    public static boolean hasCheckedIn = false;
    public static boolean hasReceivedNotification = false;
    public static boolean addedToHeatmap = false;
    public static boolean checkedInFromNotification = false;

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

        Boolean showCheckIn = getIntent().getBooleanExtra("showCheckIn", false);

        if (showCheckIn)
        {
            FragmentManager fm = getFragmentManager();
            CheckInFragment newFragment = new CheckInFragment();
            fm.beginTransaction().replace(R.id.contentContainer, newFragment).commit();
        }

    }

    @Override
    protected void onStop() {
        if (nearLibrary && !hasCheckedIn)
        {
            setCheckInAlarm();
        }
        super.onStop();
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



}
