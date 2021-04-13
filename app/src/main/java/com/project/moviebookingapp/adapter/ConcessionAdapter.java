package com.project.moviebookingapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.controller.concession.ConcessionController;
import com.project.moviebookingapp.listener.OnConcessionListener;
import com.project.moviebookingapp.model.Concession;
import com.project.moviebookingapp.model.Movie;

import java.util.ArrayList;

import static com.project.moviebookingapp.custom.TextViewFormat.retrieveLocalCurrencyFormat;


public class ConcessionAdapter extends FirestoreRecyclerAdapter<Concession,ConcessionAdapter.ConcessionViewHolder> {
    private FirebaseStorage mFireStorage;
    private LayoutInflater mInflater;
    private Context context;
    private Activity activity;

    private OnConcessionListener listener;

    private ArrayList<Concession> addedConcessionList;

    // Constructor
    public ConcessionAdapter(@NonNull FirestoreRecyclerOptions<Concession> concession,
                             Context context,ArrayList<Concession> addedConcessionList) {
        super(concession);
        this.context = context;
        this.activity = (Activity) context;
        this.listener = (OnConcessionListener) context;
        this.mInflater = LayoutInflater.from(context);
        this.addedConcessionList = addedConcessionList;
        mFireStorage = FirebaseStorage.getInstance();
    }

    //get components from the view where recyclerview resides
    public static class ConcessionViewHolder extends RecyclerView.ViewHolder{
        private TextView concessionNameTextView;
        private ImageView concessionImageView;
        private TextView concessionPriceTextView;

        private ImageButton addConcessionQtyButton;
        private ImageButton reduceConcessionQtyButton;
        private TextView concessionQtyTextView;

        public ConcessionViewHolder(View v){
            super(v);
            concessionNameTextView = v.findViewById(R.id.concessionNameTextView);
            concessionImageView = v.findViewById(R.id.concessionImageView);
            concessionPriceTextView = v.findViewById(R.id.concessionPriceTextView);

            addConcessionQtyButton = v.findViewById(R.id.addConcessionQtyButton);
            reduceConcessionQtyButton = v.findViewById(R.id.reduceConcessionQtyButton);
            concessionQtyTextView = v.findViewById(R.id.concessionQtyTextView);
        }
    }

    // obtain view where recyclerview resides in
    @Override
    public ConcessionAdapter.ConcessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.component_concession_item, parent, false);
        return new ConcessionAdapter.ConcessionViewHolder(view);
    }

    // set values for contents of a view
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(ConcessionAdapter.ConcessionViewHolder holder, int position
            ,Concession concession) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.concessionNameTextView.setText(concession.getConcessionName());
        holder.concessionPriceTextView.setText(
                retrieveLocalCurrencyFormat(concession.getConcessionPrice()));

        //set images of concession
        StorageReference imageRef = mFireStorage.getReference(concession.getConcessionImageURL());
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(context).load(imageURL)
                        .override(110,110)
                        .into(holder.concessionImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Error retrieving images.", Toast.LENGTH_SHORT).show();
                Log.d("Eror", "Firebase storage error: " + exception);
            }
        });

        holder.addConcessionQtyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isMatch = false;
                for(Concession c : addedConcessionList){
                    //if item added, only update the qty
                    if(c.getConcessionName().equals(concession.getConcessionName())){
                        isMatch = true;
                        int oldQty = c.getQuantity();
                        c.setQuantity(oldQty+1);
                        holder.concessionQtyTextView.setText("x"+c.getQuantity());
                        listener.concessionCallback(addedConcessionList);
                        return;
                    }
                }

                //if item not yet added, add all details of that item
                if(!isMatch){
                    Concession addedConcession = concession;
                    concession.setConcessionID(getSnapshots().getSnapshot(position).getId());
                    addedConcession.setQuantity(1);
                    addedConcessionList.add(addedConcession);
                    holder.concessionQtyTextView.setText("x"+addedConcession.getQuantity());
                    listener.concessionCallback(addedConcessionList);
                }
            }
        });

        holder.reduceConcessionQtyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(Concession c : addedConcessionList){
                    if(c.getConcessionName().equals(concession.getConcessionName()) && c.getQuantity()>0){
                        int oldQty = c.getQuantity();
                        c.setQuantity(oldQty-1);
                        holder.concessionQtyTextView.setText("x"+c.getQuantity());
                        listener.concessionCallback(addedConcessionList);
                        return;
                    }
                }
            }
        });

    }//end on bind

    // Return the size of your dataset
//    @Override
//    public int getItemCount() {
//        return concessionDataset.size();
//    }

}




