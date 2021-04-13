package com.project.moviebookingapp.ui.movie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.ReviewAdapter;
import com.project.moviebookingapp.controller.TicketController;
import com.project.moviebookingapp.controller.movie.ReviewController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Rating;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateTime;

public class ReviewsListActivity extends AppCompatActivity {
    private ArrayList<String> reviewList = new ArrayList<String>();

    private FirestoreRecyclerOptions<Rating> ratingOption;
    private ReviewAdapter reviewAdapter;

    private ReviewController reviewController;
    private TicketController ticketController;
    private FirebaseStorage mFireStorage;
    private SharedPreferences app_preferences;

    //all components variables here
    private TextView reviewQtyTextView;
    private ImageButton reviewListBackButton;
    private RelativeLayout addReviewRelativeLayout;
    private RecyclerView reviewsListRecyclerView;
    private NestedScrollView reviewNestedScrollView;

    private ViewGroup userOwnReviewLayout;
    //within the own review section
    private View userOwnReviewUpperLine;
    private TextView nameTextView;
    private TextView reviewScoreTextView;
    private TextView writtenReviewTextView;
    private RatingBar userReviewRatingBar;
    private ImageView reviewerImageView;
    //top right corner of own review
    private LinearLayout editAndDeleteLinearLayout;
    private ImageButton editReviewButton;
    private ImageButton deleteReviewButton;

    private View userOwnReviewBottomLine;

    //intent
    private String movieID;
    private String movieName;
    private int hour;
    private int minute;
    private long endDateSeconds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews_list);

        //Intents passed here
        Intent intent = getIntent();
        movieID = intent.getStringExtra("movieID");
        movieName = intent.getStringExtra("movieName");
        hour = intent.getIntExtra("runtimeHour",0);
        minute = intent.getIntExtra("runtimeMinute",0);
        endDateSeconds = intent.getLongExtra("endDateSeconds",0);

        reviewController = new ReviewController(this);
        ticketController = new TicketController(this);
        mFireStorage = FirebaseStorage.getInstance();
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);


        //get widgets/layouts
        reviewQtyTextView = findViewById(R.id.reviewQtyTextView);
        reviewListBackButton = findViewById(R.id.reviewListBackButton);
        addReviewRelativeLayout = findViewById(R.id.addReviewRelativeLayout);
        reviewsListRecyclerView = findViewById(R.id.reviewsListRecyclerView);
        reviewNestedScrollView = findViewById(R.id.reviewNestedScrollView);

        userOwnReviewLayout = findViewById(R.id.userOwnReviewLayout);
        //within the own review section
        userOwnReviewUpperLine = userOwnReviewLayout.findViewById(R.id.userOwnReviewUpperLine);

        nameTextView  = userOwnReviewLayout.findViewById(R.id.reviewerNameTextView);
        reviewScoreTextView = userOwnReviewLayout.findViewById(R.id.reviewScoreTextView);
        writtenReviewTextView = userOwnReviewLayout.findViewById(R.id.writtenReviewTextView);
        userReviewRatingBar = userOwnReviewLayout.findViewById(R.id.userReviewRatingBar);
        reviewerImageView = userOwnReviewLayout.findViewById(R.id.reviewerImageView);
        //top right corner of own review
        editAndDeleteLinearLayout = userOwnReviewLayout.findViewById(R.id.editAndDeleteLinearLayout);
        editReviewButton = editAndDeleteLinearLayout.findViewById(R.id.editReviewButton);
        deleteReviewButton = editAndDeleteLinearLayout.findViewById(R.id.deleteReviewButton);

        userOwnReviewBottomLine = userOwnReviewLayout.findViewById(R.id.userOwnReviewBottomLine);


        //set widgets/layouts settings
        editAndDeleteLinearLayout.setVisibility(View.VISIBLE);
        //set adapter for recycler view
        ratingOption = reviewController.queryAnonRatings(movieID);
        reviewAdapter = new ReviewAdapter(ratingOption,this);
        reviewsListRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        reviewsListRecyclerView.setAdapter(reviewAdapter);

        ////set button settings
        reviewListBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        //for review section, top right buttons
        //pass info of review, retrieved from the views of the review component
        editReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Float floatScore = userReviewRatingBar.getRating();
                Double ratingScore = Double.parseDouble(floatScore.toString());
                Intent intent = new Intent(getApplicationContext(),WriteReviewActivity.class);
                intent.putExtra("movieID",movieID);
                intent.putExtra("movieName",movieName);
                intent.putExtra("ratingScore",ratingScore);
                intent.putExtra("textReview",writtenReviewTextView.getText());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        //for review section, top right buttons
        deleteReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reviewController.deleteReview(movieID, new OnFirebaseCallback() {
                    @Override
                    public void firebaseCallBack(Object object) {
                        if(ratingOption.getSnapshots().size() > 1){
                            reviewQtyTextView.setText(ratingOption.getSnapshots().size()+" reviews");
                        }else{
                            reviewQtyTextView.setText(ratingOption.getSnapshots().size()+" review");
                        }

                        userOwnReviewLayout.setVisibility(View.GONE);
                        userOwnReviewBottomLine.setVisibility(View.GONE);
                        userOwnReviewUpperLine.setVisibility(View.GONE);
                        addReviewRelativeLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        addReviewRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WriteReviewActivity.class);
                intent.putExtra("movieID",movieID);
                intent.putExtra("movieName",movieName);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

    }//end oncreate


    private void updateOwnReview(Rating ownRating){
        if(ratingOption.getSnapshots().size() > 0) {
            reviewQtyTextView.setText((ratingOption.getSnapshots().size() + 1) + " reviews");
        }
        else{
            reviewQtyTextView.setText((ratingOption.getSnapshots().size() + 1) + " review");
        }

        if(ownRating.getAccountID() != null){
            addReviewRelativeLayout.setVisibility(View.GONE);

            userOwnReviewLayout.setVisibility(View.VISIBLE);
            userOwnReviewUpperLine.setVisibility(View.VISIBLE);
            userOwnReviewBottomLine.setVisibility(View.VISIBLE);

            userReviewRatingBar.setProgressTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.lineColor)));

            nameTextView.setText(ownRating.getUserName());
            userReviewRatingBar.setRating(ownRating.getRatingScore().floatValue());
            reviewScoreTextView.setText(ownRating.getRatingScore().toString());
            writtenReviewTextView.setText(ownRating.getTextReview());

            //set image
            StorageReference imageRef = mFireStorage.getReference(
                    app_preferences.getString("profileImgURL"," "));
            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String imageURL = uri.toString();
                    Glide.with(getApplicationContext()).load(imageURL)
                            .override(38,38)
                            .into(reviewerImageView);
                }
            });

        }
        else{
            //checks if user eligible for review or not
            //in callback as updateOwnReview method updates the visibility of add button
            getOwnReviews();

            if(ratingOption.getSnapshots().size() > 1){
                reviewQtyTextView.setText(ratingOption.getSnapshots().size()+" reviews");
            }else{
                reviewQtyTextView.setText(ratingOption.getSnapshots().size()+" review");
            }
            userOwnReviewLayout.setVisibility(View.GONE);
            userOwnReviewBottomLine.setVisibility(View.GONE);
            userOwnReviewUpperLine.setVisibility(View.GONE);
        }
    }//end updateOwnReview

    public void getOwnReviews(){
        //checks if user eligible for review or not
        ticketController.retrieveOwnTickets(movieID, new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {

                if(((ArrayList<Timestamp>)object).size() == 0){
                    //or if the movie have ended its run (known through "endDate")
                    //for users who didnt booked the movie ticket yet
                    if ((new Date().compareTo(new Date(endDateSeconds*1000)) > 0)){
                        addReviewRelativeLayout.setVisibility(View.VISIBLE);
                    }
                    else{
                        addReviewRelativeLayout.setVisibility(View.GONE);
                    }
                    return;
                }

                ArrayList<Timestamp> timestampList = (ArrayList<Timestamp>) object;
                //sort to get the earliest ticket first
                Collections.sort(timestampList);

                Log.d("Test","=> name"+movieName+" time "+timestampList.size());

                //retrieve the first ticket, and add on movie runtime
                Calendar cal = Calendar.getInstance();
                cal.setTime(timestampList.get(0).toDate());
                cal.add(Calendar.HOUR_OF_DAY,hour);
                cal.add(Calendar.MINUTE,minute);

                //if current day is after the showtime+movie duration
                //or if the movie have ended its run (known through "endDate")
                if((new Date().compareTo(cal.getTime()) > 0) ||
                        (new Date().compareTo(new Date(endDateSeconds*1000)) > 0)){
                    addReviewRelativeLayout.setVisibility(View.VISIBLE);
                }
                else{
                    addReviewRelativeLayout.setVisibility(View.GONE);
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onStart(){
        super.onStart();

        reviewAdapter.startListening();

        reviewController.queryOwnRating(movieID,new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                Rating ownRating = (Rating) object;
                updateOwnReview(ownRating);

            }
        });

    }

    @Override
    public void onStop(){
        super.onStop();
        reviewAdapter.stopListening();
    }
}
