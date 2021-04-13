package com.project.moviebookingapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.controller.TicketController;
import com.project.moviebookingapp.model.Concession;
import com.project.moviebookingapp.model.Ticket;
import com.project.moviebookingapp.ui.ticket.TicketDetailActivity;

import java.util.ArrayList;
import java.util.List;

import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateTime;

public class TicketListAdapter extends FirestoreRecyclerAdapter<Ticket,TicketListAdapter.TicketViewHolder> {
    private ArrayList<String> ticketDataset;
    private LayoutInflater mInflater;
    private Context adapterContext;
    private Activity adapterActivity;
    private FirebaseStorage mFireStorage;
    private TicketController ticketController;

    // Constructor
    public TicketListAdapter(@NonNull FirestoreRecyclerOptions<Ticket> ticket,
                             Context context,Activity activity) {
            super(ticket);
            this.mInflater = LayoutInflater.from(context);
            adapterContext = context;
            adapterActivity = activity;
            mFireStorage = FirebaseStorage.getInstance();
            ticketController = new TicketController(adapterContext);
            }

    public static class TicketViewHolder extends RecyclerView.ViewHolder{
        private TextView ticketMovieTitleTextView;
        private TextView ticketDateTextView;
        private TextView ticketHallTextView;

        private CardView ticketStatusCardView;
        private TextView ticketStatusTextView;

        private ImageView ticketMoviePosterImageView;

        private RelativeLayout ticketRelativeLayout;

        private ImageButton deleteTicketButton;

        public TicketViewHolder(View v){
            super(v);
            ticketMovieTitleTextView = v.findViewById(R.id.ticketMovieTitleTextView);
            ticketDateTextView = v.findViewById(R.id.ticketDateTextView);
            ticketHallTextView = v.findViewById(R.id.ticketHallTextView);

            ticketStatusCardView = v.findViewById(R.id.ticketStatusCardView);
            ticketStatusTextView = v.findViewById(R.id.ticketStatusTextView);

            ticketMoviePosterImageView = v.findViewById(R.id.ticketMoviePosterImageView);

            ticketRelativeLayout = v.findViewById(R.id.ticketRelativeLayout);

            deleteTicketButton = v.findViewById(R.id.deleteTicketButton);
        }
    }

        // Create new views
        @Override
        public TicketListAdapter.TicketViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View view = mInflater.inflate(R.layout.component_ticket, parent, false);
            return new TicketListAdapter.TicketViewHolder(view);
        }

        // set values for contents of a view
        @Override
        public void onBindViewHolder(TicketViewHolder holder, int position,Ticket ticket) {

            if(ticket.getIsUsed()){
                holder.deleteTicketButton.setVisibility(View.VISIBLE);
            }
            else{
                holder.deleteTicketButton.setVisibility(View.GONE);
            }

            holder.ticketMovieTitleTextView.setText(ticket.getMovieName());
            holder.ticketDateTextView.setText(getLocalDateTime().format(ticket.getShowtime().toDate()));
            holder.ticketHallTextView.setText("Hall "+ticket.getHallID());

            if(ticket.getIsUsed()){
                holder.ticketStatusCardView.setBackgroundTintList(
                        ColorStateList.valueOf(
                                adapterContext.getResources().getColor(R.color.unavailableColor)));
                holder.ticketStatusTextView.setText("Used");
            }else{
                holder.ticketStatusCardView.setBackgroundTintList(
                        ColorStateList.valueOf(
                                adapterContext.getResources().getColor(R.color.availableColor)));
                holder.ticketStatusTextView.setText("Active");
            }

            //on clicks here (both are only created for debugging, will consider removing it
            //upon final release
            holder.deleteTicketButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ticketController.deleteTicket(getSnapshots().getSnapshot(position).getId());
                }
            });
            holder.ticketStatusCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ticketController.updateTicketStatus(
                            getSnapshots().getSnapshot(position).getId(),ticket.getIsUsed());
                }
            });

            //images
            //set images of concession
            StorageReference imageRef = mFireStorage.getReference(ticket.getMoviePosterURL());
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


            holder.ticketRelativeLayout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(adapterContext, TicketDetailActivity.class);
                    ArrayList<String> seatList = new ArrayList<>(ticket.getSeat());
                    //INTENT: there's an extra data here for movie title
                    intent.putExtra("movieName",ticket.getMovieName());
                    intent.putExtra("ticketID",getSnapshots().getSnapshot(position).getId());
                    intent.putExtra("showtimeSeconds",ticket.getShowtime().getSeconds());
                    intent.putExtra("hallID",ticket.getHallID());
                    intent.putExtra("seatList",seatList);
                    intent.putExtra("moviePosterURL",ticket.getMoviePosterURL());
                    intent.putExtra("showtimeID",ticket.getShowtimeID());
                    intent.putExtra("isUsed",ticket.getIsUsed());
                    intent.putExtra("seatPrice",ticket.getSeatPrice());
                    adapterContext.startActivity(intent);
                    adapterActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });

        }

//        // Return the size of your dataset
//        @Override
//        public int getItemCount() {
//            return ticketDataset.size();
//        }
}


