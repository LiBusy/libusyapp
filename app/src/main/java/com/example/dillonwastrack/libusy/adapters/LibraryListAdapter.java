package com.example.dillonwastrack.libusy.adapters;

import android.content.Context;
import android.database.Observable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dillonwastrack.libusy.MainActivity;
import com.example.dillonwastrack.libusy.R;
import com.example.dillonwastrack.libusy.models.Library;
import com.google.android.gms.vision.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LibraryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private LayoutInflater inflater;
    List<Library> data = Collections.emptyList();
    Library current;
    int currentPos=0;

    OnItemClickListener mItemClickListener;

    public LibraryListAdapter(Context context, List<Library> data){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_card, parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Get current position of item in recyclerview to bind data and assign values from list
        MyHolder myHolder= (MyHolder) holder;
        Library current=data.get(position);
        myHolder.libraryName.setText(current.libraryName);
        myHolder.openNow.setText(current.openNow);
        myHolder.busyness.setText(current.busyness);
        myHolder.checkIns.setText(current.checkIns);
        myHolder.hours.setText("Open Today: "+current.hours);
        MainActivity m = (MainActivity) this.context;

        if (m.getUserLatLng() != null)
        {
            double distanceInMeters = distance(m.getUserLatLng().latitude,
                    m.getUserLatLng().longitude,
                    current.lat,
                    current.lng);
            myHolder.distanceAway.setText(String.format(Locale.US,"%.2f meters away", distanceInMeters));
        }

        else
        {
            myHolder.distanceAway.setText("Your location cannot be determined.");
        }
        //myHolder.textType.setText("Category: " + current.catName);
        //myHolder.textPrice.setText("Rs. " + current.price + "\\Kg");
        //myHolder.textPrice.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

        // load image into imageview using glide
//        Glide.with(context).load("http://192.168.1.7/test/images/" + current.fishImage)
//                .placeholder(R.drawable.ic_img_error)
//                .error(R.drawable.ic_img_error)
//                .into(myHolder.ivFish);

    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position, String id);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView libraryName;
        //ImageView ivFish;
        TextView openNow;
        TextView busyness;
        TextView checkIns;
        TextView distanceAway;
        TextView hours;
        //TextView textPrice;

        @Override
        public void onClick(View v) {
           // System.out.println("onClick");
            TextView tv = (TextView) v.findViewById(R.id.library_name);
            String id = tv.getText().toString();
            mItemClickListener.onItemClick(v, getAdapterPosition(), id); //OnItemClickListener mItemClickListener;
        }

        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            libraryName = (TextView) itemView.findViewById(R.id.library_name);
            //ivFish= (ImageView) itemView.findViewById(R.id.ivFish);
            openNow = (TextView) itemView.findViewById(R.id.library_open_now);
            busyness = (TextView) itemView.findViewById(R.id.library_busyness);
            checkIns = (TextView) itemView.findViewById(R.id.library_check_ins);
            distanceAway = (TextView) itemView.findViewById(R.id.library_distance);
            hours = (TextView) itemView.findViewById(R.id.library_hours);

            itemView.setOnClickListener(this);
            //textType = (TextView) itemView.findViewById(R.id.textType);
            //textPrice = (TextView) itemView.findViewById(R.id.textPrice);
        }

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
