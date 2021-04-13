package com.project.moviebookingapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.model.Movie;
import com.project.moviebookingapp.model.Rating;

import java.util.ArrayList;

//used for ReviewsListActivity
public class ReviewAdapter  extends FirestoreRecyclerAdapter<Rating, ReviewAdapter.ReviewViewHolder> {
    private LayoutInflater mInflater;
    private Context context;

    private FirebaseStorage mFireStorage;

    // Constructor
    public ReviewAdapter(@NonNull FirestoreRecyclerOptions<Rating> rating,
                         Context context) {
        super(rating);
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        mFireStorage = FirebaseStorage.getInstance();

    }

    //get components from the view where recyclerview resides
    public static class ReviewViewHolder extends RecyclerView.ViewHolder{
        private TextView nameTextView;
        private TextView reviewScoreTextView;
        private TextView writtenReviewTextView;
        private RatingBar userReviewRatingBar;
        private ImageView reviewerImageView;


        public ReviewViewHolder(View v){
            super(v);
            nameTextView = v.findViewById(R.id.reviewerNameTextView);
            userReviewRatingBar = v.findViewById(R.id.userReviewRatingBar);
            reviewScoreTextView = v.findViewById(R.id.reviewScoreTextView);
            writtenReviewTextView = v.findViewById(R.id.writtenReviewTextView);
            reviewerImageView = v.findViewById(R.id.reviewerImageView);
        }
    }

    // obtain view where recyclerview resides in
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.component_user_review, parent, false);
        return new ReviewAdapter.ReviewViewHolder(view);
    }

    // set values for contents of a view
    @Override
    public void onBindViewHolder(ReviewAdapter.ReviewViewHolder holder, int position,Rating rating) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.nameTextView.setText(rating.getUserName());
        holder.userReviewRatingBar.setRating(rating.getRatingScore().floatValue());
        holder.reviewScoreTextView.setText(rating.getRatingScore().toString());
        holder.writtenReviewTextView.setText(rating.getTextReview());

        //set image for profile pic
        if(rating.getProfileImgURL() != null) {
            StorageReference imageRef = mFireStorage.getReference(
                    rating.getProfileImgURL());
            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String imageURL = uri.toString();
                    Glide.with(context).load(imageURL)
                            .override(38, 38)
                            .into(holder.reviewerImageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d("Error", "Firebase storage error: " + exception);
                }
            });
        }
    }

    // Return the size of your dataset
//    @Override
//    public int getItemCount() {
//        return reviewDataset.size();
//    }

}