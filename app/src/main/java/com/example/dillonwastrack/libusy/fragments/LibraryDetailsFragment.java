package com.example.dillonwastrack.libusy.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dillonwastrack.libusy.R;
import com.example.dillonwastrack.libusy.models.Library;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.vision.text.Text;

/**
 * Created by dillonwastrack on 10/31/16.
 */

public class LibraryDetailsFragment extends Fragment {

    private Library library;

    private Activity mainActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.library = getArguments().getParcelable("library");
        setHasOptionsMenu(true);
        ActionBar ab = ((AppCompatActivity) mainActivity).getSupportActionBar();
        // ab.setDisplayHomeAsUpEnabled();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        //Log.d("LibraryDetails", library.libraryName);
        //Log.d("instance", savedInstanceState.toString());
        return inflater.inflate(R.layout.library_details, container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set image
        ImageView libraryImage = (ImageView) mainActivity.findViewById(R.id.library_image);
        int libraryImageResource = getResourceId(this.library.libraryId, "drawable", mainActivity.getPackageName());
        libraryImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        libraryImage.setImageResource(libraryImageResource);
        libraryImage.setColorFilter(Color.rgb(80, 80, 80), android.graphics.PorterDuff.Mode.MULTIPLY);

        TextView titleText = (TextView) mainActivity.findViewById(R.id.library_title);
        titleText.setText(this.library.libraryName);


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
}
