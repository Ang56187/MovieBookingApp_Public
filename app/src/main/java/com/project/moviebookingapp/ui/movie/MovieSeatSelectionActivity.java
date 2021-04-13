package com.project.moviebookingapp.ui.movie;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.icu.text.NumberFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.MovieDateAdapter;
import com.project.moviebookingapp.adapter.MovieSeatAdapter;
import com.project.moviebookingapp.adapter.MovieTimeToggleAdapter;
import com.project.moviebookingapp.controller.movie.MovieController;
import com.project.moviebookingapp.controller.movie.SeatSelectionController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.listener.OnSelectMovieDateListener;
import com.project.moviebookingapp.listener.OnSelectSeatListener;
import com.project.moviebookingapp.model.Showtime;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.project.moviebookingapp.custom.CustomAnimations.getRippleBackgroundDrawable;
import static com.project.moviebookingapp.custom.CustomAnimations.transparentClickAnim;
import static com.project.moviebookingapp.custom.TextViewFormat.retrieveLocalCurrencyFormat;


@RequiresApi(api = Build.VERSION_CODES.N)
public class MovieSeatSelectionActivity extends AppCompatActivity implements OnSelectMovieDateListener,OnSelectSeatListener {
    //for rows and columns of seat
    private int maxCol = 12;
    private char maxRow = 'L';

    ////set components variables here first
    //for back button
    private ImageButton seatSelectionBackButton;
    //for top bar text
    private TextView seatMovieTitleTextView;
    //for recycler views
    private RecyclerView movieDateRecyclerView;
    private RecyclerView movieTimeRecyclerView;
    //for text shown about seats numbers selected, seats qty, and total price
    private TextView seatQuantityTextView;
    private TextView totalSeatPriceTextView;
    //layout and button to change color as seat selected
    private RelativeLayout seatPriceRelativeLayout;
    private ImageButton seatForwardButton;
    //to create adapters for recycler views (for time,date and seats layout)
    private MovieDateAdapter movieDateAdapter;
    private MovieTimeToggleAdapter movieTimeAdapter;
    private MovieSeatAdapter movieSeatAdapter;

    //lists to set seat layout
    private ArrayList<String> seatList = new ArrayList<String>();
    //list to store selected seats
    private ArrayList<String> selectedSeatList = new ArrayList<String>();
    //list to store booked seats
    private ArrayList<String> bookedSeatList = new ArrayList<String>();
    //// for movie date section
    private ArrayList<String> dateList = new ArrayList<String>();

    private MovieController movieController;
    private SeatSelectionController seatSelectController;

    //default text or color if no seats selected
    //convert price to malaysian currency
    private String totalPrice = NumberFormat.getCurrencyInstance(new Locale("ms", "MY")).format(0);

    private FirestoreRecyclerOptions<Showtime> dateOption;
    private  FirestoreRecyclerOptions<Showtime> timeOption;

    private String seatQty = "x0";
    private Drawable defaultDrawable;
    private int defaultColor;

    //set price
    private double seatPrice = 10.40;

    //selected date time from movie detail page
    //get through intents
    private String movieID;
    private String showtimeID;
    private int selectedMovieDatePos;
    private int selectedMovieTimePos;
    private String movieName;
    private String moviePosterURL;
    //pass the intents
    private Showtime showtime;


    private Query query;
    private ListenerRegistration updateListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        Intent intent = getIntent();//get intent from previous activity(MovieDetailActivity)
        movieID = intent.getStringExtra("movieID");
        movieName = intent.getStringExtra("movieName");
        moviePosterURL = intent.getStringExtra("moviePosterURL");
        selectedMovieDatePos = intent.getIntExtra("movie_date",0);
        selectedMovieTimePos = intent.getIntExtra("movie_time",0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        movieController = new MovieController(this);
        seatSelectController = new SeatSelectionController(this);

        dateList = new ArrayList<String>();

        //default text or color if no seats selected(initialize variables)
        defaultDrawable = ContextCompat.getDrawable(this, R.drawable.unavailable_stroke);
        defaultColor = getResources().getColor(R.color.unavailableColor);

        //use array to determine type of seat or space between them
        generateSeatLayoutArray(maxRow,maxCol,seatList);


        //get component
        ////whole layout
        ViewGroup seatSelectionRelativeLayout = (ViewGroup)findViewById(R.id.seatSelectionRelativeLayout);
        ////top bar views
        ImageView seatSelectionBackButton = findViewById(R.id.seatSelectionBackButton);
        seatMovieTitleTextView = findViewById(R.id.seatMovieTitleTextView);

        ////recycler views
        RecyclerView movieSeatRecyclerView = findViewById(R.id.movieSeatRecyclerView);
        movieDateRecyclerView = findViewById(R.id.movieDateRecyclerView);
        movieTimeRecyclerView = findViewById(R.id.movieTimeRecyclerView);

        ////texts for seats details
        seatQuantityTextView = findViewById(R.id.seatQuantityTextView);
        totalSeatPriceTextView = findViewById(R.id.totalSeatPriceTextView);

        ////layout for color changes if seats selected were filled/empty
        seatPriceRelativeLayout = findViewById(R.id.seatPriceRelativeLayout);
        seatForwardButton = findViewById(R.id.seatForwardButton);

        ////bottom bar views
        ImageButton seatForwardButton = findViewById(R.id.seatForwardButton);



        //set widgets/components settings
        seatMovieTitleTextView.setText(movieName);

        ////set layout resizing anim
        LayoutTransition lt = seatSelectionRelativeLayout.getLayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);


        //set firebase recyclerview
        //option->adapter->recyclerview
        dateOption = movieController.queryShowDateDocuments(movieID,dateList);
        timeOption = movieController.queryShowTimeDocuments(movieID,new Timestamp(new Date()));

        movieDateAdapter = new MovieDateAdapter(dateOption,this,this
                ,selectedMovieDatePos);
        movieTimeAdapter = new MovieTimeToggleAdapter(timeOption,this,selectedMovieTimePos);
        movieSeatAdapter = new MovieSeatAdapter(this,seatList,selectedSeatList,bookedSeatList
                ,this);

        ////for movie date section
        movieDateRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        movieDateRecyclerView.setAdapter(movieDateAdapter);
        //scroll to selected date position in recycler view
        movieDateRecyclerView.getLayoutManager().smoothScrollToPosition(movieDateRecyclerView,null,
                selectedMovieDatePos);

        ////for movie time section
        movieTimeRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        movieTimeRecyclerView.setAdapter(movieTimeAdapter);
        //scroll to selected time position in recycler view
        movieTimeRecyclerView.getLayoutManager().smoothScrollToPosition(movieTimeRecyclerView,null,
                selectedMovieTimePos);

        ////for seats section
        movieSeatRecyclerView.setLayoutManager(new GridLayoutManager(this,16));
        movieSeatRecyclerView.setAdapter(movieSeatAdapter);

        ////for top bar back button
        seatSelectionBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        ////seat forward button
        seatForwardButton.setOnClickListener(new View.OnClickListener() {//go to concession page
            @Override
            public void onClick(View view) {
                if(selectedSeatList.size()>0){
                    Drawable background = getResources().getDrawable( R.color.availableColor );
                    seatForwardButton.setBackground(getRippleBackgroundDrawable(R.color.availableColor, background));

                    Intent intent = new Intent(getApplicationContext(), MovieConcessionActivity.class);
                    intent.putExtra("showtimeID",showtimeID);
                    intent.putExtra("seatPrice",showtime.getPrice());
                    intent.putExtra("hallID",showtime.getHallID());
                    intent.putExtra("showtimeSeconds",showtime.getStartTime().getSeconds());
                    intent.putExtra("movieID",movieID);
                    intent.putExtra("movieName",movieName);
                    intent.putExtra("moviePosterURL",moviePosterURL);
                    intent.putExtra("seatList",selectedSeatList);

                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });
    }//end oncreate

    //animation when default back button tapped
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    ////callback methods
    @Override
    //to pass data from adapter to activity through observer pattern
    //trigger when date changed
    public void onDateCallBack(Timestamp time,int i){
        timeOption = movieController.queryShowTimeDocuments(movieID,time);
        movieTimeAdapter.updateOptions(timeOption);
        if(movieDateAdapter.getIsClicked()){
            movieTimeAdapter.resetTimePosition();
            movieDateAdapter.setIsClicked(false);
        }

        if(selectedSeatList.size()>0){
            resetBottomBarStatus();
        }
    }

    //trigger when time changed
    @Override
    public void onTimeCallBack(Timestamp time, int i){ }

    public void onTimeCallBack(int i,String showtimeID, Showtime showtime){
        seatPrice = showtime.getPrice();
        this.showtime = showtime;
        this.showtimeID = showtimeID;
        if(selectedSeatList.size()>0){
            selectedSeatList.clear();
            resetBottomBarStatus();
        }
        seatSelectController.retrieveBookedSeats(bookedSeatList,showtimeID,movieSeatAdapter);
        query = FirebaseFirestore.getInstance().collection("tickets")
                .whereEqualTo("showtimeID",showtimeID);
        updateListener = seatSelectController.updateSeatListener(query,bookedSeatList,selectedSeatList,
                showtimeID,movieSeatAdapter,movieTimeAdapter);
    }


    //triggers when select seat
    @Override
    public void onSeatCallBack(String s){
        updateBottomLayout();
    }

    @Override
    public void onStart() {
        super.onStart();
        dateList.clear();//clear when going back from seat select

        movieDateAdapter.startListening();
        movieTimeAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        movieDateAdapter.stopListening();
        movieTimeAdapter.stopListening();

        updateListener.remove();
    }


    public void updateBottomLayout(){
        //convert price to malaysian currency
        String totalPrice = retrieveLocalCurrencyFormat(selectedSeatList.size()*seatPrice);
        String seatQty = "x"+Integer.toString(selectedSeatList.size());

        //updates text to show quantity of selected seats
        seatQuantityTextView.setText(seatQty);
        //updates total price
        totalSeatPriceTextView.setText(totalPrice);
        //updates color of layouts

        if(selectedSeatList.size()>0){
            seatPriceRelativeLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.available_stroke));
            seatForwardButton.setBackgroundColor(getResources().getColor(R.color.availableColor));
        }
        else{
            seatPriceRelativeLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.unavailable_stroke));
            seatForwardButton.setBackgroundColor(getResources().getColor(R.color.unavailableColor));
        }
    }

    //to reset qty,price and color of bottom bar
    public void resetBottomBarStatus(){
        //updates text to show quantity of selected seats
        seatQuantityTextView.setText(seatQty);
        //updates total price
        totalSeatPriceTextView.setText(totalPrice);
        seatPriceRelativeLayout.setBackground(defaultDrawable);
        seatForwardButton.setBackgroundColor(defaultColor);
    }

    private void generateSeatLayoutArray(char maxRow, int maxCol, ArrayList<String> seatList){
        for(char row = 'A'; row<=maxRow;++row){
            for(int col = 1; col<=maxCol;col++){
                //add numbering at left side
                if(col==1 && row!='L'){
                    seatList.add(Character.toString(row));
                }
                else if (col==1 && row==maxRow){
                    seatList.add("-");
                }

                //add the seats
                if(row!='L'){
                    seatList.add(Character.toString(row) + Integer.toString(col));
                }
                //add numbering at bottom
                else{
                    seatList.add(Integer.toString(col));
                }

                //add numbering at right side
                if(col==maxCol && row!=maxRow){
                    seatList.add(Character.toString(row));
                }
                else if(col==maxCol && row==maxRow){
                    seatList.add("-");
                }

                //add spaces between seats (by adding invisible seats)
                if(col==2 || col == 10){
                    seatList.add("-");
                }

            }
        }
    }//end

}//end class

