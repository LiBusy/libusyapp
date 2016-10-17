package com.example.dillonwastrack.libusy;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class CheckInFragment extends Fragment{



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
//        ((AppCompatActivity) getActivity()).getSupportActionBar().hide(); // hide action bar
        return inflater.inflate(R.layout.fragment_check_in, container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        Button veryBusy = (Button) getActivity().findViewById(R.id.btnVeryBusy);
        Button busy = (Button) getActivity().findViewById(R.id.btnBusy);
        Button notBusy = (Button) getActivity().findViewById(R.id.btnNotBusy);

        final Context homeActivity = this.getActivity();
        final String libraryName = this.getArguments().getString("library");
        veryBusy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(homeActivity);
                String url ="http://libusy.herokuapp.com/busyness/checkin/"+libraryName+"/3";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                FragmentManager fm = getFragmentManager();
                                fm.beginTransaction().replace(R.id.contentContainer, new MapViewFragment()).commit();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("level", "you suck");
                    }
                });
                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            }
        });
    }
}
