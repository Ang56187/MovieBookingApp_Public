package com.project.moviebookingapp.ui.movie;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.project.moviebookingapp.R;
import com.project.moviebookingapp.controller.movie.ReviewController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Rating;

public class WriteReviewActivity extends AppCompatActivity {
    private ReviewController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        controller = new ReviewController(this);

        //Intents passed here
        Intent intent = getIntent();
        String movieID = intent.getStringExtra("movieID");
        String movieName = intent.getStringExtra("movieName");
        //passed from edit button, might be null so needs checking
        Double ratingScore = intent.getDoubleExtra("ratingScore",11);
        //i put 11 because it is unachievable by user,and a unique value to detect score that failed to pass
        String textReview = intent.getStringExtra("textReview");


        //get widgets/layouts
        RelativeLayout writeReviewRelativeLayout = findViewById(R.id.writeReviewRelativeLayout);
        ImageButton writeReviewBackButton = findViewById(R.id.writeReviewBackButton);
        TextView ratingScoreTextView = findViewById(R.id.ratingScoreTextView);
        TextView movieTitleTextView = findViewById(R.id.movieTitleTextView);
        RatingBar movieRatingBar = findViewById(R.id.movieRatingBar);
        EditText writeReviewEditText = findViewById(R.id.writeReviewEditText);
        Button postReviewButton = findViewById(R.id.postReviewButton);


        //set widgets/layouts buttons
        ////if edits were requested, set values to views
        if(ratingScore != 11 && textReview != null){
            movieRatingBar.setRating(ratingScore.floatValue());
            ratingScoreTextView.setText(ratingScore.toString());
            writeReviewEditText.setText(textReview);
        }

        ////set texts
        movieTitleTextView.setText(movieName);

        ////check if layout changed
        writeReviewRelativeLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(bottom>oldBottom){
                    // Keyboard Close
                    postReviewButton.setVisibility(View.VISIBLE);

                }else if(bottom<oldBottom){
                    // Keyboard Open
                    postReviewButton.setVisibility(View.GONE);
                }
            }
        });

        movieRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                ratingScoreTextView.setText(String.valueOf(v));
            }
        });

        movieRatingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ratingScoreTextView.setText(String.valueOf(movieRatingBar.getRating()));
                return movieRatingBar.onTouchEvent(motionEvent);
            }
        });

        writeReviewBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        postReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Float floatScore = movieRatingBar.getRating();
                Double ratingScore = Double.parseDouble(floatScore.toString());

                if(ratingScore != 11 && textReview != null) {
                    controller.editReview(movieID, ratingScore, writeReviewEditText.getText().toString(),
                            new OnFirebaseCallback() {
                                @Override
                                public void firebaseCallBack(Object object) {
                                    onBackPressed();
                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                    finish();
                                }
                            });
                }
                else {
                    controller.addReview(movieID, ratingScore, writeReviewEditText.getText().toString()
                            , new OnFirebaseCallback() {
                                @Override
                                public void firebaseCallBack(Object object) {
                                    onBackPressed();
                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                    finish();
                                }
                            });
                }
            }
        });


    }//end oncreate

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
