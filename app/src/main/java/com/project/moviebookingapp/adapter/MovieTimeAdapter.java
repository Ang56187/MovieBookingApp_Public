package com.project.moviebookingapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
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

import java.util.ArrayList;
import java.util.Date;

import static com.project.moviebookingapp.custom.TextViewFormat.getLocalTimeFormat;

public class MovieTimeAdapter extends FirestoreRecyclerAdapter<Showtime,MovieTimeAdapter.MovieTimeViewHolder> {
    private ArrayList<String> bookedSeatList = new ArrayList<>();
    private LayoutInflater mInflater;
    private Activity adapterActivity;
    private Context adapterContext;
    private OnSelectMovieDateListener timeListener;
    private AlphaAnimation buttonClickAnim = new AlphaAnimation(1F, 0.6F);

    private SeatSelectionController seatController;

    // Constructor
    public MovieTimeAdapter(@NonNull FirestoreRecyclerOptions<Showtime> showtime,
                            Context context, OnSelectMovieDateListener onSelectMovieDateListener) {
        super(showtime);
        this.mInflater = LayoutInflater.from(context);
        adapterContext = context;
        adapterActivity = (Activity)context;
        timeListener = onSelectMovieDateListener;
        seatController = new SeatSelectionController(adapterContext);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
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
    public MovieTimeAdapter.MovieTimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.component_time_slot,parent,false);
        return new MovieTimeAdapter.MovieTimeViewHolder(view);
    }

    // set values for contents of a view
    //where you modify your views
    @Override
    public void onBindViewHolder(MovieTimeAdapter.MovieTimeViewHolder holder, int position
            ,@NonNull Showtime showtime) {
        String dateString = getLocalTimeFormat().format(showtime.getStartTime().toDate());
        holder.movieTimeTextView.setText(dateString);
        seatController.retrieveBookedSeats(bookedSeatList, getSnapshots().getSnapshot(position).getId(),
                new OnFirebaseCallback() {
                    @Override
                    public void firebaseCallBack(Object object) {
                        ArrayList<String> seatList = (ArrayList<String>) object;
                        GradientDrawable gradientDrawable = (GradientDrawable) holder.movieTimeTextView.getBackground();
                        int colorInt = 0;
                        colorInt = adapterContext.getResources().getColor(R.color.showtimeAvailableColor);

                        //max seats are 132
                        if(bookedSeatList.size()>66 && bookedSeatList.size()<131){
                            colorInt = adapterContext.getResources().getColor(R.color.showtimeSellingFastColor);
                        }
                        else if( bookedSeatList.size() == 132){
                            colorInt = adapterContext.getResources().getColor(R.color.showtimeSoldOutColor);
                        }

                        gradientDrawable.setColor(Color.TRANSPARENT);
                        gradientDrawable.setStroke(5,colorInt);
                        holder.movieTimeTextView.setTextColor(colorInt);
                    }
                });

        buttonClickAnim.setDuration(300);
        //go to seat selection page
        holder.movieTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeListener.onTimeCallBack(showtime.getStartTime(),position);
                view.startAnimation(buttonClickAnim);
            }

        });

    }

    // Return the size of your dataset
//    @Override
//    public int getItemCount() {
//        return timeDataset.size();
//    }

}

