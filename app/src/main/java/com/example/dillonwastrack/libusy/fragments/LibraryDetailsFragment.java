package com.example.dillonwastrack.libusy.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.dillonwastrack.libusy.R;
import com.example.dillonwastrack.libusy.activities.MainActivity;
import com.example.dillonwastrack.libusy.models.Library;

import java.util.Locale;

/**
 * Created by dillonwastrack on 10/31/16.
 */

public class LibraryDetailsFragment extends Fragment {

    private Library library;

    private Activity mainActivity;

    TextView libraryName;
    TextView openNow;
    TextView busyness;
    TextView checkIns;
    TextView distanceAway;
    TextView hours;
    TextView totalCheckIns;
    TextView veryBusyVotes;
    TextView busyVotes;
    TextView notBusyVotes;
    ImageView libraryImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle arguments = getArguments();
        if(arguments.containsKey("library"))
        {
            this.library = getArguments().getParcelable("library");
            mainActivity.setTitle(this.library.libraryName);
        }

        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.library_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        libraryName = (TextView) mainActivity.findViewById(R.id.library_name);
        libraryName.setText(library.libraryName);

        openNow = (TextView) mainActivity.findViewById(R.id.library_open_now);
        openNow.setText(library.openNow);

        busyness = (TextView) mainActivity.findViewById(R.id.library_busyness);
        busyness.setText(library.busyness);

        checkIns = (TextView) mainActivity.findViewById(R.id.library_check_ins);
        checkIns.setText(library.checkIns);

        distanceAway = (TextView) mainActivity.findViewById(R.id.library_distance);
        MainActivity m = (MainActivity) mainActivity;

        if (m.getUserLatLng() != null)
        {
            double distanceInMeters = distance(m.getUserLatLng().latitude,
                    m.getUserLatLng().longitude,
                    library.lat,
                    library.lng);
            distanceAway.setText(String.format(Locale.US,"%.2f meters away", distanceInMeters));
        }

        else
        {
            distanceAway.setText(R.string.location_cannot_be_determined);
        }

        hours = (TextView) mainActivity.findViewById(R.id.library_hours);
        hours.setText(mainActivity.getString(R.string.open_today, library.hours));

        libraryImage = (ImageView) mainActivity.findViewById(R.id.library_image);
        int libraryImageResource = getResourceId(library.libraryId, "drawable", mainActivity.getPackageName());
        libraryImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        libraryImage.setImageResource(libraryImageResource);
        libraryImage.setColorFilter(Color.rgb(80, 80, 80), android.graphics.PorterDuff.Mode.MULTIPLY);
        final CardView cv = (CardView)mainActivity.findViewById(R.id.learn_more);
        cv.setTag(R.id.library_name, libraryName);
        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.lib.ua.edu/libraries/"+library.libraryId+"/"));
                mainActivity.startActivity(intent);
            }
        });

        totalCheckIns = (TextView) mainActivity.findViewById(R.id.total_check_ins);
        totalCheckIns.setText(mainActivity.getString(R.string.total_check_ins, library.totalCheckIns));

        veryBusyVotes = (TextView) mainActivity.findViewById(R.id.very_busy_votes);
        veryBusyVotes.setText(mainActivity.getString(R.string.very_busy_votes, library.veryBusyVotes));

        busyVotes = (TextView) mainActivity.findViewById(R.id.busy_votes);
        busyVotes.setText(mainActivity.getString(R.string.busy_votes, library.busyVotes));

        notBusyVotes = (TextView) mainActivity.findViewById(R.id.not_busy_votes);
        notBusyVotes.setText(mainActivity.getString(R.string.not_busy_votes, library.notBusyVotes));

    }

    @Override
    public void onStop() {
        super.onStop();
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
            mainActivity =(Activity) context;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = activity;
    }

    public int getResourceId(String pVariableName, String pResourcename, String pPackageName)
    {
        try {
            return mainActivity.getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar ab = ((AppCompatActivity) mainActivity).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
    }


    /**
     * This distance algorithm uses the haversine
     * great circle approximation to determine
     * the distance between two points in
     * meters.
     *
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second pount
     * @return distance between 2 points in meters
     */
    private double distance(double lat1, double lon1, double lat2, double lon2)
    {
        // haversine great circle distance approximation, returns meters
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60; // 60 nautical miles per degree of separation
        dist = dist * 1852; // 1852 meters per nautical mile
        return (dist);
    }

    /**
     * Used for converting degrees to
     * radians.
     *
     * @param deg degrees
     * @return conversion from degrees to radians
     */
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /**
     * Used for converting radians to
     * degrees.
     *
     * @param rad radians
     * @return conversion from radians to degrees
     */
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
