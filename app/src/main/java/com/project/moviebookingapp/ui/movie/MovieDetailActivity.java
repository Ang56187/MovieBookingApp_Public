package com.project.moviebookingapp.ui.movie;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.MovieDateAdapter;
import com.project.moviebookingapp.adapter.MovieRecyclerAdapter;
import com.project.moviebookingapp.adapter.MovieTimeAdapter;
import com.project.moviebookingapp.controller.TicketController;
import com.project.moviebookingapp.controller.movie.FavouriteController;
import com.project.moviebookingapp.controller.movie.MovieController;
import com.project.moviebookingapp.controller.movie.ReviewController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.listener.OnSelectMovieDateListener;
import com.project.moviebookingapp.model.Movie;
import com.project.moviebookingapp.model.Rating;
import com.project.moviebookingapp.model.Showtime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.project.moviebookingapp.custom.CustomAnimations.transparentClickAnim;
import static com.project.moviebookingapp.custom.TextViewFormat.df;
import static com.project.moviebookingapp.custom.TextViewFormat.getCompareDateTime;
import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateTime;
import static com.project.moviebookingapp.custom.TextViewFormat.ratingCountFormat;

public class MovieDetailActivity extends AppCompatActivity implements OnSelectMovieDateListener {
    //get the selected date and time by user
    private int selectedMovieDatePos;
    private int selectedMovieTimePos;

    //get components
    ////top bar
    private ImageButton movieDetailBackButton;
    private ImageButton movieDetailFavouriteButton;

    ////video player button
    private ImageView movieDetailImageView;
    private ImageButton movieDetailPlayButton;

    ////body section
    private TextView movieTitleTextView;
    private TextView movieActorTextView;
    private TextView movieGenreTextView;
    private TextView movieDurationTextView;

    private TextView ratingScoreTextView;
    private RatingBar movieRatingBar;
    private TextView ratingNumTextView;

    ////synopsis section
    private LinearLayout synopsisLinearLayout;
    private TextView synopsisTextView;
    private ImageView synopsisArrowButton;

    ////showtime section
    private RecyclerView movieDateRecyclerView;
    private RecyclerView movieTimeRecyclerView;
    private TextView noShowtimesTextView;

    ////reviews section
    private TextView allReviewsTextView;
    private RelativeLayout addReviewRelativeLayout;
    private TextView noUserReviewText;
    private ViewGroup userReviewLayout;
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


    //// related movies section
    private RecyclerView relatedMovieRecyclerView;
    
    ////set adapter
    //// for movie date section(to compare dates)
    private ArrayList<String> dateList;
    //// for movie time section
    //1=available 2=selling fast 3=sold out
    private ArrayList<Integer> testTime = new ArrayList<Integer>();
    ////for related movie section
    private ArrayList<String> movieList = new ArrayList<String>();
    private MovieDateAdapter movieDateAdapter;
    private MovieTimeAdapter movieTimeAdapter;
    private MovieRecyclerAdapter movieRecyclerAdapter;

    private MovieController controller;
    private ReviewController reviewController;
    private TicketController ticketController;
    private FavouriteController favouriteController;
    private SharedPreferences app_preferences;
    private FirestoreRecyclerOptions<Movie> movieOption;
    private FirestoreRecyclerOptions<Showtime> dateOption;
    private  FirestoreRecyclerOptions<Showtime> timeOption;

    //to make intent values available to listeners too
    private String movieID;
    private String movieName;
    private String moviePosterURL;
    private String trailerURL;
    private String synopsis;
    private Integer runtime;
    private ArrayList<String> genres;
    private ArrayList<String> actors;
    private ArrayList<Double> ratingScoreList;
    private Long startDateSeconds;
    private Long endDateSeconds;

    private int hour;
    private int minute;

    private Boolean isFavourited;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moviedetail);

        controller = new MovieController(this);
        reviewController = new ReviewController(this);
        ticketController = new TicketController(this);
        favouriteController = new FavouriteController(this);
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        dateList =  new ArrayList<String>();

        //get passed intents
        Intent intent = getIntent();
        movieID = intent.getStringExtra("movieID");
        movieName = intent.getStringExtra("movieName");
        moviePosterURL = intent.getStringExtra("moviePosterURL");
        trailerURL = intent.getStringExtra("trailerURL");
        synopsis = intent.getStringExtra("synopsis");
        runtime = intent.getIntExtra("runtime",0);
        genres = intent.getStringArrayListExtra("genres");
        actors = intent.getStringArrayListExtra("actors");
        ratingScoreList = (ArrayList<Double>) intent.getSerializableExtra("ratingScoreList");
        startDateSeconds = intent.getLongExtra("startDateSeconds",0);
        endDateSeconds = intent.getLongExtra("endDateSeconds",0);

        //set time
        hour = runtime/60;
        minute = runtime%60;

        //get components
        ////top bar
        movieDetailBackButton = findViewById(R.id.movieDetailBackButton);
        movieDetailFavouriteButton = findViewById(R.id.movieDetailFavouriteButton);

        //half body
        ViewGroup secondHalfBodyLinearLayout = (ViewGroup) findViewById(R.id.secondHalfBodyLinearLayout);

        ////video player
        movieDetailImageView = findViewById(R.id.movieDetailImageView);
        movieDetailPlayButton = findViewById(R.id.movieDetailPlayButton);

        //body section
        movieTitleTextView = findViewById(R.id.movieTitleTextView);
        movieActorTextView = findViewById(R.id.movieActorTextView);
        movieGenreTextView = findViewById(R.id.movieGenreTextView);
        movieDurationTextView = findViewById(R.id.movieDurationTextView);

        ////synopsis section
        synopsisLinearLayout = findViewById(R.id.synopsisLinearLayout);
        synopsisTextView = findViewById(R.id.synopsisTextView);
        synopsisArrowButton = findViewById(R.id.synopsisArrowButton);

        //movie rating section
        movieRatingBar = findViewById(R.id.movieRatingBar);
        ratingScoreTextView = findViewById(R.id.ratingScoreTextView);
        ratingNumTextView = findViewById(R.id.ratingNumTextView);

        ////showtime section
        movieDateRecyclerView = findViewById(R.id.movieDateRecyclerView);
        movieTimeRecyclerView = findViewById(R.id.movieTimeRecyclerView);
        noShowtimesTextView = findViewById(R.id.noShowtimesTextView);
        TextView showtimeTextView = findViewById(R.id.showtimeTextView);

        ////review section
        allReviewsTextView = findViewById(R.id.allReviewsTextView);
        //add own review button
        addReviewRelativeLayout = findViewById(R.id.addReviewRelativeLayout);
        //if no review
        noUserReviewText = findViewById(R.id.noUserReviewText);
        //own review layout
        userReviewLayout = findViewById(R.id.userReviewLayout);
        //views within the review layout
        userOwnReviewUpperLine = userReviewLayout.findViewById(R.id.userOwnReviewUpperLine);
        nameTextView  = userReviewLayout.findViewById(R.id.reviewerNameTextView);
        reviewScoreTextView = userReviewLayout.findViewById(R.id.reviewScoreTextView);
        writtenReviewTextView = userReviewLayout.findViewById(R.id.writtenReviewTextView);
        userReviewRatingBar = userReviewLayout.findViewById(R.id.userReviewRatingBar);
        reviewerImageView = userReviewLayout.findViewById(R.id.reviewerImageView);
        //top right corner of own review
        editAndDeleteLinearLayout = userReviewLayout.findViewById(R.id.editAndDeleteLinearLayout);
        editReviewButton = userReviewLayout.findViewById(R.id.editReviewButton);
        deleteReviewButton = userReviewLayout.findViewById(R.id.deleteReviewButton);
        userOwnReviewBottomLine = userReviewLayout.findViewById(R.id.userOwnReviewBottomLine);


        ////related movie section
        relatedMovieRecyclerView = findViewById(R.id.relatedMovieRecyclerView);

        //set component settings
        //set top bar
        //favourited
        favouriteController.checkIsFavourited(movieID, new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                isFavourited = (Boolean) object;
                if(isFavourited){
                    movieDetailFavouriteButton.setImageResource(R.drawable.ic_star);
                }
                else{
                    movieDetailFavouriteButton.setImageResource(R.drawable.ic_star_unfill);
                }
            }
        });

        movieDetailFavouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favouriteController.tapOnFavourite(movieID, isFavourited, new OnFirebaseCallback() {
                    @Override
                    public void firebaseCallBack(Object object) {
                        isFavourited = (Boolean)object;
                        if(isFavourited){
                            movieDetailFavouriteButton.setImageResource(R.drawable.ic_star);
                        }
                        else{
                            movieDetailFavouriteButton.setImageResource(R.drawable.ic_star_unfill);
                        }
                    }
                });
            }
        });

        ////set movie details
        movieTitleTextView.setText(movieName);
        synopsisTextView.setText(synopsis);


        StringBuilder genreStringBuilder = new StringBuilder();
        for (String s:genres){
            genreStringBuilder.append(s);
            if(!s.equals(genres.get(genres.size()-1)))
                genreStringBuilder.append(", ");
        }
        movieGenreTextView.setText(genreStringBuilder);


        StringBuilder actorStringBuilder = new StringBuilder();
        for (String s:actors){
            actorStringBuilder.append(s);
            if(!s.equals(actors.get(actors.size()-1)))
                actorStringBuilder.append(", ");
        }
        movieActorTextView.setText(actorStringBuilder);

        movieDurationTextView.setText(hour+" hr "+minute+" min");

        Double average = ratingScoreList.stream().mapToDouble(val -> val).average().orElse(0.0);
        if(ratingScoreList.size()>0){
            movieRatingBar.setRating(average.floatValue());
            ratingScoreTextView.setText(df.format(average));
            ratingNumTextView.setText(ratingCountFormat(ratingScoreList));
        }
        else{
            movieRatingBar.setRating(0);
            ratingScoreTextView.setText("0.0");
            ratingNumTextView.setText(ratingCountFormat(ratingScoreList));
        }

        ////set image for vid
        Glide.with(this)
                .load("https://img.youtube.com/vi/"+trailerURL+"/maxres1.jpg")
                .override(900, 350)//change aspect ratio to 3:2
                .into(movieDetailImageView);

        ////set layout resizing anim
        LayoutTransition lt = secondHalfBodyLinearLayout.getLayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);
        ////back button
        movieDetailBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                view.startAnimation(transparentClickAnim);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        ////play video
        movieDetailPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),VideoPlayerActivity.class);
                intent.putExtra("trailerURL",trailerURL);
                startActivity(intent);
            }
        });


        ////synopsis expand or shrink
        synopsisArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(synopsisTextView.getVisibility() == View.GONE){
                    synopsisTextView.setVisibility(View.VISIBLE);

                    ObjectAnimator rotateImage = ObjectAnimator.ofFloat(synopsisArrowButton , "rotation", 0f, 180f);
                    rotateImage.setDuration(300);
                    rotateImage.start();
                }
                else{
                    synopsisTextView.setVisibility(View.GONE);

                    ObjectAnimator rotateImage = ObjectAnimator.ofFloat(synopsisArrowButton , "rotation", 180f, 0f);
                    rotateImage.setDuration(300);
                    rotateImage.start();
                }
            }
        });

        //see all reviews
        allReviewsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ReviewsListActivity.class);
                intent.putExtra("movieID",movieID);
                intent.putExtra("movieName",movieName);
                intent.putExtra("runtimeHour",hour);
                intent.putExtra("runtimeMinute",minute);
                intent.putExtra("endDateSeconds",endDateSeconds);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        //add review
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

        movieOption = controller.queryNoMovieDocuments();

        ///set firebase
        controller.queryRecommendedMovies("movies",movieID, new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                if(object == null){

                }else{
                    movieOption = (FirestoreRecyclerOptions<Movie>) object;
                    movieRecyclerAdapter.updateOptions(movieOption);
                    relatedMovieRecyclerView.setAdapter(movieRecyclerAdapter);
                }
            }
        });

        dateOption = controller.queryShowDateDocuments(movieID, dateList);
        timeOption = controller.queryShowTimeDocuments(movieID, new Timestamp(new Date()));

        movieRecyclerAdapter = new MovieRecyclerAdapter(movieOption,this,R.layout.component_movieposter);
        relatedMovieRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        relatedMovieRecyclerView.setAdapter(movieRecyclerAdapter);

        movieDateAdapter = new MovieDateAdapter(dateOption,this,this);
        movieDateRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        movieDateRecyclerView.setAdapter(movieDateAdapter);

        //// for movie time section
        //1=available 2=selling fast 3=sold out
        movieTimeAdapter = new MovieTimeAdapter(timeOption,this,this);
        movieTimeRecyclerView.setLayoutManager(new GridLayoutManager(this,3));
        movieTimeRecyclerView.setAdapter(movieTimeAdapter);

        refreshReviews();

        //checks if theres any showtime in recycler view or not
        //placed in callback as data might be retrieved async
        //meaning CPU execution time != firebase retrieval time online
        controller.retrieveShowdates(movieID, new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                int size = (int)object;
                if(size > 0){
                    noShowtimesTextView.setVisibility(View.GONE);
                    movieDateRecyclerView.setVisibility(View.VISIBLE);
                    movieTimeRecyclerView.setVisibility(View.VISIBLE);
                }
                else{
                    noShowtimesTextView.setVisibility(View.VISIBLE);
                    movieDateRecyclerView.setVisibility(View.GONE);
                    movieTimeRecyclerView.setVisibility(View.GONE);
                }
            }
        });

    }//end oncreate

    @Override
    public void onStart() {
        super.onStart();
        dateList.clear();//clear when going back from seat select

        movieRecyclerAdapter.startListening();
        movieDateAdapter.startListening();
        movieTimeAdapter.startListening();

        refreshReviews();

    }//end onstart

    @Override
    public void onDestroy() {
        super.onDestroy();
        movieRecyclerAdapter.stopListening();
        movieDateAdapter.stopListening();
        movieTimeAdapter.stopListening();
    }


    //animation when default back button tapped
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    //to pass data from adapter to activity through observer pattern
    //get all times available on the sent date/timestamp
    public void onDateCallBack(Timestamp time, int i){
        selectedMovieDatePos = i;
        timeOption = controller.queryShowTimeDocuments(movieID,time);
        movieTimeAdapter.updateOptions(timeOption);
    }


    @Override
    //to pass data from adapter to activity through observer pattern
    // go to movie seat selection page
    public void onTimeCallBack(Timestamp time , int i){
        selectedMovieTimePos = i;
        Intent intent = new Intent(this, MovieSeatSelectionActivity.class);
        intent.putExtra("movieID",movieID);
        intent.putExtra("movie_date",selectedMovieDatePos);
        intent.putExtra("movie_time",selectedMovieTimePos);
        intent.putExtra("moviePosterURL",moviePosterURL);
        intent.putExtra("movieName",movieName);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    //only used at seat selection, only included as interface ask it to
    public void onTimeCallBack(int i, String showtimeID, Showtime showtime){}

    public OnSelectMovieDateListener getDateListener(){
        return this;
    }

    private void refreshReviews(){
        //refreshes reviews
        controller.retrieveRatings(movieID, new OnFirebaseCallback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void firebaseCallBack(Object object) {
                ArrayList<Double> list = (ArrayList<Double>) object;
                Double average = list.stream().mapToDouble(val -> val).average().orElse(0.0);
                if (list.size() > 0) {
                    movieRatingBar.setRating(average.floatValue());
                    ratingScoreTextView.setText(df.format(average));
                    ratingNumTextView.setText(ratingCountFormat(list));
                } else {
                    movieRatingBar.setRating(average.floatValue());
                    ratingScoreTextView.setText("0.0");
                    ratingNumTextView.setText("(0)");
                }

            }

        });

        //set that one review
        reviewController.retrieveReviews(movieID, new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                if(object == null){
                    noUserReviewText.setVisibility(View.VISIBLE);
                    userReviewLayout.setVisibility(View.GONE);
                    //checks if user eligible for review or not
                    getOwnReviews();
                }//end if no review

                else{
                    ArrayList<Rating> ratingList = (ArrayList<Rating>) object;
                    noUserReviewText.setVisibility(View.GONE);
                    userReviewLayout.setVisibility(View.VISIBLE);
                    int count = 0;
                    for (Rating r: ratingList){
                        //if user posted review, show that review instead
                        if(r.getAccountID().equals(app_preferences.getString("accountID"," "))){
                            addReviewRelativeLayout.setVisibility(View.GONE);
                            userReviewRatingBar.setProgressTintList(ColorStateList.valueOf(
                                    getResources().getColor(R.color.lineColor)));
                            nameTextView.setText(r.getUserName());
                            userReviewRatingBar.setRating(r.getRatingScore().floatValue());
                            reviewScoreTextView.setText(r.getRatingScore().toString());
                            writtenReviewTextView.setText(r.getTextReview());

                            //set image
                            StorageReference imageRef = FirebaseStorage.getInstance().getReference(
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

                            break;
                        }

                        //if user didnt write the review for that movie, show other users review
                        else if(count == (ratingList.size()-1)){
                            getOwnReviews();

                            nameTextView.setText(r.getUserName());
                            userReviewRatingBar.setProgressTintList(ColorStateList.valueOf
                                    (Color.WHITE));
                            userReviewRatingBar.setRating(r.getRatingScore().floatValue());
                            reviewScoreTextView.setText(r.getRatingScore().toString());
                            writtenReviewTextView.setText(r.getTextReview());

                            //set image
                            StorageReference imageRef = FirebaseStorage.getInstance().getReference(
                                    r.getProfileImgURL());
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageURL = uri.toString();
                                    Glide.with(getApplicationContext()).load(imageURL)
                                            .override(38,38)
                                            .into(reviewerImageView);
                                }
                            });

                            break;
                        }//end else
                        count++;

                    }//end loop
                }//end else
            }
        });
    }//end refresh review



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
    }//end

}
