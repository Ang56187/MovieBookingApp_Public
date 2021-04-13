package com.project.moviebookingapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.controller.movie.SeatSelectionController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.listener.OnSelectMovieDateListener;
import com.project.moviebookingapp.model.Showtime;
import com.project.moviebookingapp.ui.movie.MovieSeatSelectionActivity;

import java.util.ArrayList;

import static com.project.moviebookingapp.custom.TextViewFormat.getLocalTimeFormat;

public class MovieTimeToggleAdapter extends FirestoreRecyclerAdapter<Showtime,MovieTimeToggleAdapter.MovieTimeViewHolder> {
    private ArrayList<String> bookedSeatList = new ArrayList<>();

    private LayoutInflater mInflater;
    private Activity adapterActivity;
    private Context adapterContext;
    //to allow for toggle-like functionality
    private int lastCheckedPosition = 0;
    private int copyOfLastCheckedPosition = lastCheckedPosition;
    //as callback to the activity class
    private OnSelectMovieDateListener timeListener;

    private SeatSelectionController seatController;

    // Constructor
    public MovieTimeToggleAdapter(@NonNull FirestoreRecyclerOptions<Showtime> showtime,
                                  Context context, int lastPosition) {
        super(showtime);
        this.mInflater = LayoutInflater.from(context);
        adapterContext = context;
        adapterActivity = (Activity)context;
        timeListener = (OnSelectMovieDateListener)context;
        lastCheckedPosition = lastPosition;
        seatController = new SeatSelectionController(context);
    }
    //overlapped


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    //send message back to Activity/Fragment
    public class MovieTimeViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView movieTimeTextView;

        public MovieTimeViewHolder(View v) {
            super(v);
            movieTimeTextView = v.findViewById(R.id.movieTimeTextView);

        }
    }

    // Create new views
    @Override
    public MovieTimeToggleAdapter.MovieTimeViewHolder onCreateViewHolder(ViewGroup parent,
                                                                         int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.component_time_slot,parent,false);
        return new MovieTimeToggleAdapter.MovieTimeViewHolder(view);
    }

    // set values for contents of a view
    //where you modify your views
    @Override
    public void onBindViewHolder(MovieTimeToggleAdapter.MovieTimeViewHolder holder, int position
            ,Showtime showtime) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        seatController.retrieveBookedSeats(bookedSeatList, getSnapshots().getSnapshot(position).getId(),
                new OnFirebaseCallback() {
                    @Override
                    public void firebaseCallBack(Object object) {
                        ArrayList<String> seatList = (ArrayList<String>) object;
                        GradientDrawable gradientDrawable = (GradientDrawable) holder.movieTimeTextView.getBackground();
                        int colorInt = Color.WHITE;

                        colorInt = adapterContext.getResources().getColor(R.color.showtimeAvailableColor);

                        //max seats are 132
                        if(bookedSeatList.size()>66 && bookedSeatList.size()<131){
                            colorInt = adapterContext.getResources().getColor(R.color.showtimeSellingFastColor);
                        }
                        else if( bookedSeatList.size() == 132){
                            colorInt = adapterContext.getResources().getColor(R.color.showtimeSoldOutColor);
                        }

                        gradientDrawable.setStroke(5,colorInt);
                        holder.movieTimeTextView.setTextColor(colorInt);

                        holder.movieTimeTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                copyOfLastCheckedPosition = lastCheckedPosition;
                                lastCheckedPosition = holder.getAdapterPosition();
                                notifyItemChanged(copyOfLastCheckedPosition);
                                notifyItemChanged(lastCheckedPosition);
                            }
                        });


                        if(position == lastCheckedPosition){
                            holder.movieTimeTextView.setClickable(false);
                            gradientDrawable.setColor(colorInt);
                            holder.movieTimeTextView.setTextColor(adapterContext.getResources().getColor(R.color.primaryBlueColor));
                            timeListener.onTimeCallBack(position
                                    ,getSnapshots().getSnapshot(position).getId(),showtime);
                        }
                        else{
                            gradientDrawable.setColor(Color.TRANSPARENT);
                            holder.movieTimeTextView.setTextColor(colorInt);
                            holder.movieTimeTextView.setClickable(true);
                        }

                    }
                });


        String dateString = getLocalTimeFormat().format(showtime.getStartTime().toDate());
        holder.movieTimeTextView.setText(dateString);

    }

    public void resetTimePosition(){
        lastCheckedPosition = 0;
    }

    // Return the size of your dataset
//    @Override
//    public int getItemCount() {
//        return timeDataset.size();
//    }

}


