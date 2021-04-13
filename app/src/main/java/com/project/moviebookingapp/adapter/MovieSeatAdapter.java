package com.project.moviebookingapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.icu.text.NumberFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.moviebookingapp.R;
import com.project.moviebookingapp.listener.OnSelectSeatListener;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

public class MovieSeatAdapter  extends RecyclerView.Adapter<MovieSeatAdapter.SeatViewHolder> {
    //for ticket price
    private double seatPrice;
    //for seat layout
    private ArrayList<String> seatDataset;
    private ArrayList<String> selectedSeatDataset;
    private ArrayList<String> bookedSeatDataset;

    //for parameters sent by activity
    private LayoutInflater mInflater;
    private Context adapterContext;

    //listener to activity
    private OnSelectSeatListener seatListener;

    // Constructor
    public MovieSeatAdapter(Context context,ArrayList<String> mDataset,ArrayList<String> sDataset,ArrayList<String> bDataset,
                           OnSelectSeatListener onSelectSeatListener) {
        seatDataset = mDataset; //for empty unbooked seats
        selectedSeatDataset = sDataset; //for selected seats by current users
        bookedSeatDataset = bDataset; //for booked seats by other users

        this.mInflater = LayoutInflater.from(context);
        adapterContext = context;

        //set listener
        seatListener = onSelectSeatListener;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class SeatViewHolder extends RecyclerView.ViewHolder {
        public TextView normalTextView;
        public ImageView seatImageView;

        public SeatViewHolder(View v,int viewType) {
            super(v);
            if(viewType == 1){
                normalTextView = v.findViewById(R.id.normalTextView);
            }
            else if(viewType == 2){
                seatImageView = v.findViewById(R.id.seatImageView);
            }
        }
    }

    // Create new views
    @Override
    public MovieSeatAdapter.SeatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = new View(adapterContext);
        switch(viewType){
            case 1:
                view = mInflater.inflate(R.layout.component_normal_text,null);
                return new MovieSeatAdapter.SeatViewHolder(view,viewType);
            case 2:
                view = mInflater.inflate(R.layout.component_hall_seat, parent, false);
                return new MovieSeatAdapter.SeatViewHolder(view,viewType);
            case 3:
                view = mInflater.inflate(R.layout.component_hall_seat, null);
                return new MovieSeatAdapter.SeatViewHolder(view,viewType);
        }
        return new MovieSeatAdapter.SeatViewHolder(view,0);
    }

    // set values for contents of a view
    @Override
    public void onBindViewHolder(MovieSeatAdapter.SeatViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Matcher matchChar = Pattern.compile("([A-Z])").matcher(seatDataset.get(position));
        Matcher matchString = Pattern.compile("([0-9A-Z])+").matcher(seatDataset.get(position));
        Matcher matchInt = Pattern.compile("^[0-9]*$").matcher(seatDataset.get(position));

        //for text numbering for seats
        if (matchChar.matches() || matchInt.matches()){
            //set numbering at left/right sides and bottom
            holder.normalTextView.setText(seatDataset.get(position));
            //set text size for bottom of seats numbering
//            if(matchInt.matches()){
//                holder.normalTextView.setTextSize(12);
//            }
        }
        //for seat buttons
        else if(matchString.matches() && !seatDataset.get(position).equals("-")){
            //set seats to allow for on click functionality
            holder.seatImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!selectedSeatDataset.contains(seatDataset.get(position))){
                        selectedSeatDataset.add(seatDataset.get(position));
                        holder.seatImageView.setBackgroundResource(R.drawable.ic_seat_selected);
                    }
                    else if (selectedSeatDataset.contains(seatDataset.get(position))){
                        selectedSeatDataset.remove(seatDataset.get(position));
                        holder.seatImageView.setBackgroundResource(R.drawable.ic_seat);
                    }
                    seatListener.onSeatCallBack(seatDataset.get(position));

                }
            });//end onclick
            //to reset seat layout again once date/time changed

            if(selectedSeatDataset.contains(seatDataset.get(position))){
                holder.seatImageView.setBackgroundResource(R.drawable.ic_seat_selected);
                holder.seatImageView.setClickable(true);
            }
            else if (!selectedSeatDataset.contains(seatDataset.get(position))){
                holder.seatImageView.setBackgroundResource(R.drawable.ic_seat);
            }

            if(bookedSeatDataset.contains(seatDataset.get(position))){
                holder.seatImageView.setBackgroundResource(R.drawable.ic_seat_booked);
                holder.seatImageView.setClickable(false);
            }

        }//end if

    }//end onBindViewHolder

    // Return the size of your dataset
    @Override
    public int getItemCount() {
        return seatDataset.size();
    }

    @Override
    public int getItemViewType(int position) {
        // generate different view types (text=1 or image=2)
        Matcher matchChar = Pattern.compile("([A-Z])").matcher(seatDataset.get(position));
        Matcher matchString = Pattern.compile("([0-9A-Z])+").matcher(seatDataset.get(position));
        Matcher matchInt = Pattern.compile("([0-9])+").matcher(seatDataset.get(position));

        int returnInt = 0;
        if (matchChar.matches() || matchInt.matches()){
            returnInt =  1;
        }
        else if(matchString.matches() && !seatDataset.get(position).equals("-")){
            returnInt =  2;
        }
        return returnInt;
    }
}