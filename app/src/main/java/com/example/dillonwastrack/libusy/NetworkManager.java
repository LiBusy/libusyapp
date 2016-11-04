package com.example.dillonwastrack.libusy;

import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dillonwastrack.libusy.callbacks.HeatmapCallback;
import com.example.dillonwastrack.libusy.callbacks.LocationCallback;
import com.example.dillonwastrack.libusy.callbacks.MarkerCallback;
import com.example.dillonwastrack.libusy.callbacks.ServerCallback;
import com.example.dillonwastrack.libusy.models.Library;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NetworkManager
{
    private static NetworkManager instance = null;

    private static final String key = "333C949CDEEBAB5ED3C747AF3EBBE"; // api key

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

    public void postLibraryBusynessLevel(String libraryName, String level, final ServerCallback callback)
    {
        String url ="https://libusy.herokuapp.com/busyness/checkin/"+libraryName+"/"+level+"?key="+key;
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

    public void postUserLocation(String lat, String lng, String nearestLibrary)
    {

        String url ="https://libusy.herokuapp.com/usermarkers/postmarker/"+lat+"/"+lng+"/"+nearestLibrary+"?key="+key;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
    public void readUserMarkers(final ArrayList<LatLng> userMarkerList, final HeatmapCallback callback) throws JSONException
    {
        String url = "https://libusy.herokuapp.com/usermarkers"+"?key="+key;
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
     * For reading the list of markers from the database,
     * and adding them to the map. Also adds their
     * locations to an ArrayList of LatLngs
     * for use in calculating user
     * proximity.
     *
     * @param locations
     * @param context
     * @param gMap
     * @param callback
     */
    public void readMarkers(final ArrayMap<String, LatLng> locations, final Context context, final GoogleMap gMap, final MarkerCallback callback)
    {
        String url = "https://libusy.herokuapp.com/markers"+"?key="+key;

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
                                locations.put(object.getString("library"), new LatLng(lat, lng));
                                gMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lat, lng))
                                        .title(object.getString("title"))
                                        .snippet(object.getString("busyness")
                                                +"\n"
                                                +object.getString("check_ins")
                                                +"\n"
                                                +object.getString("open_now"))); //TODO SET MARKER TAG TO PLACE ID HERE
                            }
                            callback.onSuccess(locations);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error reading list of locations", Toast.LENGTH_SHORT).show();
            }
        });
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    public void readLocations(final ArrayMap<String, LatLng> locations, final Context context, final MarkerCallback callback)
    {
        String url = "https://libusy.herokuapp.com/markers"+"?key="+key;

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
                                locations.put(object.getString("library"), new LatLng(lat, lng));
                            }
                            callback.onSuccess(locations);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error reading list of locations", Toast.LENGTH_SHORT).show();
            }
        });
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    public void readLocationsIntoList(final ArrayList<Library> locations, final Context context, final LocationCallback callback)
    {
        String url = "https://libusy.herokuapp.com/markers"+"?key="+key;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //userMarkerList = new ArrayList<LatLng>();
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                Library library = new Library();
                                JSONObject object = array.getJSONObject(i);
                                library.lat = object.getDouble("lat");
                                library.lng = object.getDouble("lng");
                                library.libraryName = object.getString("title");
                                library.placeId = object.getString("place_id");
                                library.address = object.getString("address");
                                library.phoneNumber = object.getString("phone_number");
                                library.openNow = object.getString("open_now");
                                library.busyness = object.getString("busyness");
                                library.checkIns = object.getString("check_ins");
                                locations.add(library);
                                //locations.put(object.getString("library"), new LatLng(lat, lng));

                            }
                            callback.onSuccess(locations);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error reading list of locations", Toast.LENGTH_SHORT).show();
            }
        });
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }


}
