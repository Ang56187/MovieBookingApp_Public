package com.project.moviebookingapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.listener.OnSelectMovieDateListener;
import com.project.moviebookingapp.model.Showtime;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateToggleFormat;

public class MovieDateAdapter extends FirestoreRecyclerAdapter<Showtime,MovieDateAdapter.MovieDateViewHolder> {
    private LayoutInflater mInflater;
    private Context adapterContext;
    private Activity adapterActivity;
    //to allow for toggle-like functionality
    private int lastCheckedPosition = 0;
    private int copyOfLastCheckedPosition = lastCheckedPosition;
    //as callback to the activity class
    private OnSelectMovieDateListener dateListener;

    private FirestoreRecyclerOptions<Showtime> showtime;

    private boolean isClicked;

    // Constructor
    public MovieDateAdapter(@NonNull FirestoreRecyclerOptions<Showtime> showtime, Context context,
                            OnSelectMovieDateListener onSelectMovieDateListener) {
        super(showtime);
        this.showtime = showtime;
        this.mInflater = LayoutInflater.from(context);
        adapterContext = context;
        adapterActivity = (Activity)context;
        dateListener = onSelectMovieDateListener;
    }

    //overlapped constructor for seat selection to retrieve date position from movie detail activity
    public MovieDateAdapter(@NonNull FirestoreRecyclerOptions<Showtime> showtime, Context context,
                            OnSelectMovieDateListener onSelectMovieDateListener,int lastPosition) {
        super(showtime);
        this.showtime = showtime;
        this.mInflater = LayoutInflater.from(context);
        adapterContext = context;
        adapterActivity = (Activity)context;
        dateListener = onSelectMovieDateListener;
        lastCheckedPosition = lastPosition;
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    //send message back to Activity/Fragment
    public class MovieDateViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView ratingScoreTextView;
        public ToggleButton toggleBtn;

        public MovieDateViewHolder(View v) {
            super(v);
            toggleBtn = v.findViewById(R.id.movieDateToggleButton);
        }
    }

    // Create new views
    @Override
    public MovieDateAdapter.MovieDateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.component_date_toggle,parent,false);
        return new MovieDateAdapter.MovieDateViewHolder(view);
    }

    // set values for contents of a view
    //where you modify your views
    @Override
    public void onBindViewHolder(MovieDateAdapter.MovieDateViewHolder holder, int position,
                                 @NonNull Showtime showtime) {
        if(showtime.getStartTime() == null){
            holder.toggleBtn.setVisibility(View.GONE);
            holder.toggleBtn.setLayoutParams(new LinearLayout.LayoutParams(0,0));
            holder.toggleBtn.setChecked(false);
            holder.toggleBtn.setClickable(false);
            return;
        }

        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        holder.toggleBtn.setVisibility(View.VISIBLE);
        holder.toggleBtn.setLayoutParams(layoutParams);
        layoutParams.setMarginStart(40);

        String dateString = getLocalDateToggleFormat().format(showtime.getStartTime().toDate());
        holder.toggleBtn.setTextOff(dateString);
        holder.toggleBtn.setTextOn(dateString);

        holder.toggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isClicked = true;
                copyOfLastCheckedPosition = lastCheckedPosition;
                lastCheckedPosition = holder.getAdapterPosition();
                notifyItemChanged(copyOfLastCheckedPosition);
                notifyItemChanged(lastCheckedPosition);
            }
        });

        if(position == lastCheckedPosition){
            holder.toggleBtn.setClickable(false);
            dateListener.onDateCallBack(showtime.getStartTime(),position);
        }
        else{
            holder.toggleBtn.setClickable(true);
        }
        holder.toggleBtn.setChecked(position == lastCheckedPosition);

    }


    //its to reset the position of time selected by clicking only
    // to avoid position being 0 when going next page to seat selection
    public boolean getIsClicked(){
        return isClicked;
    }
    public void setIsClicked(boolean isClicked){
        this.isClicked = isClicked;
    }

    public int getItemCount(){
        return showtime.getSnapshots().size();
    }

//    // Return the size of your dataset
//    @Override
//    public int getItemCount() {
//        return dateDataset.size();
//    }

}

