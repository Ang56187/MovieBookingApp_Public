package com.project.moviebookingapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.controller.movie.FavouriteController;
import com.project.moviebookingapp.controller.movie.MovieController;
import com.project.moviebookingapp.model.Movie;
import com.project.moviebookingapp.model.MovieRevenue;
import com.project.moviebookingapp.ui.admin.AdminRevenueActivity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;

import static com.project.moviebookingapp.custom.TextViewFormat.retrieveLocalCurrencyFormat;

public class MovieRevenueAdapter extends RecyclerView.Adapter<MovieRevenueAdapter.RevenueViewHolder>  {
    private ArrayList<MovieRevenue> movieDataset;
    private LayoutInflater mInflater;
    private Context adapterContext;
    private Activity adapterActivity;

    // Constructor for adapter
    public MovieRevenueAdapter(Context context,ArrayList<MovieRevenue> mDataset) {
        this.mInflater = LayoutInflater.from(context);
        movieDataset = mDataset;
        adapterContext = context;
        adapterActivity = (AdminRevenueActivity) context;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class RevenueViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView movieNameTextView;
        TextView dayTextView;
        TextView revenueTextView;
        TextView ticketSoldTextView;

        public RevenueViewHolder(View v) {
            super(v);
            movieNameTextView = v.findViewById(R.id.movieNameTextView);
            dayTextView = v.findViewById(R.id.dayTextView);
            revenueTextView = v.findViewById(R.id.revenueTextView);
            ticketSoldTextView = v.findViewById(R.id.ticketSoldTextView);
        }

    }

    // Create new views
    @Override
    public MovieRevenueAdapter.RevenueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.component_movie_revenue, parent, false);
        return new MovieRevenueAdapter.RevenueViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onBindViewHolder(MovieRevenueAdapter.RevenueViewHolder holder, int position) {
        Date startDate = new Date(movieDataset.get(position).getStartDateSeconds()*1000);
        LocalDate startLocalDate = Instant.ofEpochMilli(startDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        holder.dayTextView.setText(String.valueOf(ChronoUnit.DAYS.between(startLocalDate,
                LocalDate.now())));

        holder.ticketSoldTextView.setText(String.valueOf(
                movieDataset.get(position).getTicketCount()));

        holder.revenueTextView.setText(
                retrieveLocalCurrencyFormat(movieDataset.get(position).getTotalMovieRevenue()));

        holder.movieNameTextView.setText(movieDataset.get(position).getMovieName());
    }


    // Return the size of your dataset
    @Override
    public int getItemCount() {
        return movieDataset.size();
    }

}
