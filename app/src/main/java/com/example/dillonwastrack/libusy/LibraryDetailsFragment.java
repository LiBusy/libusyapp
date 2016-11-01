package com.example.dillonwastrack.libusy;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;

/**
 * Created by dillonwastrack on 10/31/16.
 */

public class LibraryDetailsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.library_details, container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(((MainActivity) this.getActivity()).getSelectedPlace() != null)
        {
            Place place = ((MainActivity) this.getActivity()).getSelectedPlace();
            TextView tView = (TextView) getActivity().findViewById(R.id.library_markup);
            tView.setText(place.getName());
        }
        super.onViewCreated(view, savedInstanceState);
    }
}
