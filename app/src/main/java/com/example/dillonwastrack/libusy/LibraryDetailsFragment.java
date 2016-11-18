package com.example.dillonwastrack.libusy;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
