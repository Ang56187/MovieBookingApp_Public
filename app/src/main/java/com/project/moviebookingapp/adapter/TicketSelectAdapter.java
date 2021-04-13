package com.project.moviebookingapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.listener.OnSelectTicketListener;
import com.project.moviebookingapp.model.Concession;
import com.project.moviebookingapp.model.Ticket;
import com.project.moviebookingapp.ui.ticket.TicketDetailActivity;

import java.util.ArrayList;

import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateTime;

public class TicketSelectAdapter extends RecyclerView.Adapter<TicketSelectAdapter.TicketSelectViewHolder> {

    private ArrayList<Ticket> ticketDataset;
    private LayoutInflater mInflater;
    private Context adapterContext;
    private Activity adapterActivity;
    private OnSelectTicketListener ticketListener;

    //to allow for toggle-like functionality
    private int lastCheckedPosition = 0;
    private int copyOfLastCheckedPosition = lastCheckedPosition;

    private FirebaseStorage mFireStorage;

    // Constructor
    public TicketSelectAdapter(Context context, ArrayList<Ticket> mDataset,Activity activity) {
        ticketDataset = mDataset;
        this.mInflater = LayoutInflater.from(context);
        adapterContext = context;
        adapterActivity = activity;
        mFireStorage = FirebaseStorage.getInstance();
    }

    //overlapped constructor
    public TicketSelectAdapter(Context context, ArrayList<Ticket> mDataset) {
        ticketDataset = mDataset;
        this.mInflater = LayoutInflater.from(context);
        adapterContext = context;
        adapterActivity = (Activity)context;
        ticketListener = (OnSelectTicketListener) context;
        mFireStorage = FirebaseStorage.getInstance();
    }

    public static class TicketSelectViewHolder extends RecyclerView.ViewHolder{
        private TextView ticketMovieTitleTextView;
        private TextView ticketDateTextView;
        private TextView ticketHallTextView;

        private RadioButton ticketSelectRadioButton;

        private ImageView ticketMoviePosterImageView;

        private TextView ticketStatusTextView;

        private RelativeLayout ticketRelativeLayout;


        public TicketSelectViewHolder(View v){
            super(v);
            ticketMovieTitleTextView = v.findViewById(R.id.ticketMovieTitleTextView);
            ticketDateTextView = v.findViewById(R.id.ticketDateTextView);
            ticketHallTextView = v.findViewById(R.id.ticketHallTextView);

            ticketStatusTextView = v.findViewById(R.id.ticketStatusTextView);

            ticketMoviePosterImageView = v.findViewById(R.id.ticketMoviePosterImageView);

            ticketRelativeLayout = v.findViewById(R.id.ticketRelativeLayout);
            ticketSelectRadioButton = v.findViewById(R.id.ticketSelectRadioButton);
        }

    }

    // Create new views
    @Override
    public TicketSelectAdapter.TicketSelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.component_ticket, parent, false);
        return new TicketSelectAdapter.TicketSelectViewHolder(view);
    }

    // set values for contents of a view
    @Override
    public void onBindViewHolder(TicketSelectAdapter.TicketSelectViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.ticketMovieTitleTextView.setText(ticketDataset.get(position).getMovieName());
        holder.ticketDateTextView.setText(getLocalDateTime().format(ticketDataset.get(position)
                .getShowtime().toDate()));
        holder.ticketHallTextView.setText("Hall "+ticketDataset.get(position).getHallID());

        //images
        //set images of concession
        StorageReference imageRef = mFireStorage.getReference(ticketDataset.get(position)
                .getMoviePosterURL());
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(adapterContext).load(imageURL)
                        .override(100,147)
                        .into(holder.ticketMoviePosterImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(adapterContext, "Error retrieving images.", Toast.LENGTH_SHORT).show();
                Log.d("Error", "Firebase storage error: " + exception);
            }
        });


        holder.ticketSelectRadioButton.setVisibility(View.VISIBLE);
        holder.ticketRelativeLayout.setBackground(ContextCompat.getDrawable(adapterContext, R.drawable.rounded_rectangle));

        holder.ticketStatusTextView.setVisibility(View.GONE);

        //to set toggle func for radio button
        holder.ticketRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyOfLastCheckedPosition = lastCheckedPosition;
                lastCheckedPosition = holder.getAdapterPosition();
                notifyItemChanged(copyOfLastCheckedPosition);
                notifyItemChanged(lastCheckedPosition);
                ticketListener.onTicketCallBack(position);
            }
        });

        holder.ticketSelectRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyOfLastCheckedPosition = lastCheckedPosition;
                lastCheckedPosition = holder.getAdapterPosition();
                notifyItemChanged(copyOfLastCheckedPosition);
                notifyItemChanged(lastCheckedPosition);
                ticketListener.onTicketCallBack(position);
            }
        });

        if(position == lastCheckedPosition){
            holder.ticketRelativeLayout.setClickable(false);
            holder.ticketSelectRadioButton.setClickable(false);
        }
        else{
            holder.ticketRelativeLayout.setClickable(true);
            holder.ticketSelectRadioButton.setClickable(true);
        }

        holder.ticketSelectRadioButton.setChecked(position == lastCheckedPosition);


    }

    // Return the size of your dataset
    @Override
    public int getItemCount() {
        return ticketDataset.size();
    }
}


