package com.example.dillonwastrack.libusy.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dillonwastrack.libusy.R;
import com.example.dillonwastrack.libusy.models.Library;
import com.google.android.gms.vision.text.Text;

import java.util.Collections;
import java.util.List;

public class LibraryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private LayoutInflater inflater;
    List<Library> data = Collections.emptyList();
    Library current;
    int currentPos=0;

    // create constructor to innitilize context and data sent from MainActivity
    public LibraryListAdapter(Context context, List<Library> data){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    // Inflate the layout when viewholder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.list_card, parent,false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Get current position of item in recyclerview to bind data and assign values from list
        MyHolder myHolder= (MyHolder) holder;
        Library current=data.get(position);
        myHolder.libraryName.setText(current.libraryName);
        myHolder.openNow.setText(current.openNow);
        myHolder.busyness.setText(current.busyness);
        myHolder.checkIns.setText(current.checkIns);
        //myHolder.textType.setText("Category: " + current.catName);
        //myHolder.textPrice.setText("Rs. " + current.price + "\\Kg");
        //myHolder.textPrice.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

        // load image into imageview using glide
//        Glide.with(context).load("http://192.168.1.7/test/images/" + current.fishImage)
//                .placeholder(R.drawable.ic_img_error)
//                .error(R.drawable.ic_img_error)
//                .into(myHolder.ivFish);

    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{

        TextView libraryName;
        //ImageView ivFish;
        TextView openNow;
        TextView busyness;
        TextView checkIns;
       // TextView textType;
        //TextView textPrice;

        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            libraryName = (TextView) itemView.findViewById(R.id.library_name);
            //ivFish= (ImageView) itemView.findViewById(R.id.ivFish);
            openNow = (TextView) itemView.findViewById(R.id.library_open_now);
            busyness = (TextView) itemView.findViewById(R.id.library_busyness);
            checkIns = (TextView) itemView.findViewById(R.id.library_check_ins);
            //textType = (TextView) itemView.findViewById(R.id.textType);
            //textPrice = (TextView) itemView.findViewById(R.id.textPrice);
        }

    }

}
