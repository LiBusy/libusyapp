package com.example.dillonwastrack.libusy;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dillonwastrack on 10/11/16.
 */

public class ListViewFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
//        ((AppCompatActivity) getActivity()).getSupportActionBar().hide(); // hide action bar
        return inflater.inflate(R.layout.fragment_list, container,false);
    }
}
