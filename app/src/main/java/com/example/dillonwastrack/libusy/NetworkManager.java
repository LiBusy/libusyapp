package com.example.dillonwastrack.libusy;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NetworkManager
{
    private static final String TAG = "NetworkManager";
    private static NetworkManager instance = null;

    private static final String prefixURL = "https://libusy.herokuapp.com/";

    //for Volley API
    public RequestQueue requestQueue;

    private NetworkManager(Context context)
    {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized NetworkManager getInstance(Context context)
    {
        if (null == instance)
            instance = new NetworkManager(context);
        return instance;
    }

    //this is so you don't need to pass context each time
    public static synchronized NetworkManager getInstance()
    {
        if (null == instance)
        {
            throw new IllegalStateException(NetworkManager.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first");
        }
        return instance;
    }

    public void getBusynessLevelFromApi(final Marker marker, String libraryName) throws AuthFailureError
    {
        String url ="https://libusy.herokuapp.com/busyness/getlevel/"+libraryName;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        marker.setSnippet(createBusynessTextFromResponse(response));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("level", "you suck");
            }
        });
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);

    }

    public void postLibraryBusynessLevel(final Context homeActivity, String libraryName, String level, final ServerCallback callback)
    {
        String url ="https://libusy.herokuapp.com/busyness/checkin/"+libraryName+"/"+level;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("level", "you suck");
            }
        });
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    /**
     *
     * @param callback the callback for when the request is complete
     * @throws JSONException
     */
    public void readMarkersFromAPI(final ArrayList<LatLng> userMarkerList, final HeatmapCallback callback) throws JSONException
    {
        String url = "https://libusy.herokuapp.com/usermarkers";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //userMarkerList = new ArrayList<LatLng>();
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                double lat = object.getDouble("lat");
                                double lng = object.getDouble("lng");
                                userMarkerList.add(new LatLng(lat, lng));
                            }
                            callback.onSuccess(userMarkerList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("level", "you suck");
            }
        });
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    /**
     * The algorithm. Determines how busy
     * the library is based on its
     * busyness rating.
     *
     * @param response the response from the api
     * @return String to display in the marker snippet
     */
    private String createBusynessTextFromResponse(String response)
    {
        Float level = Float.parseFloat(response);

        if (level < 1.5)
        {
            return "Not Busy";
        }

        if (level < 2.5)
        {
            return "Busy";
        }

        return "Very Busy";
    }

}
