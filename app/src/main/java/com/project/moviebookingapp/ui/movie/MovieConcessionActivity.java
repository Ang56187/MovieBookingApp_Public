package com.project.moviebookingapp.ui.movie;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.ConcessionAdapter;
import com.project.moviebookingapp.controller.concession.ConcessionController;
import com.project.moviebookingapp.listener.OnConcessionListener;
import com.project.moviebookingapp.model.Concession;
import com.project.moviebookingapp.model.Showtime;

import java.util.ArrayList;
import java.util.Date;

import static com.project.moviebookingapp.custom.CustomAnimations.getRippleBackgroundDrawable;
import static com.project.moviebookingapp.custom.TextViewFormat.getCompareDateTime;
import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateFormat;
import static com.project.moviebookingapp.custom.TextViewFormat.retrieveLocalCurrencyFormat;

public class MovieConcessionActivity extends AppCompatActivity implements OnConcessionListener {
    private LinearLayout concessionLinearLayout;
    private ImageButton concessionForwardButton;
    private RecyclerView concessionRecyclerView;
    private TextView totalConcessionPriceTextView;
    private RelativeLayout bottomRelativeLayout;
    private ImageButton movieConcessionBackButton;

    private ConcessionController controller;

    private FirestoreRecyclerOptions<Concession> concessionOption;
    private ConcessionAdapter concessionAdapter;

    //list to store selected items
    private ArrayList<Concession> addedConcessionList = new ArrayList<>();

    //pass/retrieve intents
    private String movieID;
    private Long showtimeSeconds;
    private Double seatPrice;
    private String showtimeID;
    private String hallID;
    private String movieName;
    private String moviePosterURL;
    private ArrayList<String> seatList = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_concession);

        controller=new ConcessionController(this);

        //get intents
        Intent intent = getIntent();//get intent from previous activity(MovieDetailActivity)
        movieID = intent.getStringExtra("movieID");
        movieName = intent.getStringExtra("movieName");
        hallID = intent.getStringExtra("hallID");
        moviePosterURL = intent.getStringExtra("moviePosterURL");
        showtimeID = intent.getStringExtra("showtimeID");
        seatList = intent.getStringArrayListExtra("seatList");
        showtimeSeconds = intent.getLongExtra("showtimeSeconds",0);
        seatPrice = intent.getDoubleExtra("seatPrice",0);

        //For testing
//        Date date = new Date(showtimeSeconds*1000);
//        Log.d("Test","Showtime date "+ getCompareDateTime().format(date)+ " price:"+seatPrice);
//        Log.d("Test"," seat list "+ seatList);

        //get widgets/layouts
        concessionLinearLayout = findViewById(R.id.concessionLinearLayout);
        concessionForwardButton = concessionLinearLayout.findViewById(R.id.concessionForwardButton);
        concessionRecyclerView = concessionLinearLayout.findViewById(R.id.concessionRecyclerView);
        totalConcessionPriceTextView = concessionLinearLayout.findViewById(R.id.totalConcessionPriceTextView);
        bottomRelativeLayout = concessionLinearLayout.findViewById(R.id.bottomRelativeLayout);
        movieConcessionBackButton = concessionLinearLayout.findViewById(R.id.movieConcessionBackButton);

        //set widgets/layouts settings
        concessionOption = controller.queryConcessionsDocuments();
        concessionAdapter = new ConcessionAdapter(concessionOption,this,addedConcessionList);
        concessionRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        concessionRecyclerView.setAdapter(concessionAdapter);

        concessionLinearLayout.setPadding(0,0,0,0);

        bottomRelativeLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.available_stroke));
        concessionForwardButton.setBackgroundColor(getResources().getColor(R.color.availableColor));
        concessionForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable background = getResources().getDrawable( R.color.availableColor );
                concessionForwardButton.setBackground(getRippleBackgroundDrawable(R.color.availableColor, background));

                ArrayList<Concession> cloneList = (ArrayList<Concession>)addedConcessionList.clone();

                for(Concession s: cloneList){
                    if (s.getQuantity() == 0){
                        addedConcessionList.remove(s);
                    }
                }

                //go to checkout page and pass the intents
                Intent intent = new Intent(getApplicationContext(), MovieCheckoutActivity.class);
                intent.putExtra("showtimeID",showtimeID);
                intent.putExtra("seatPrice",seatPrice);
                intent.putExtra("showtimeSeconds",showtimeSeconds);
                intent.putExtra("hallID",hallID);
                intent.putExtra("movieID",movieID);
                intent.putExtra("movieName",movieName);
                intent.putExtra("moviePosterURL",moviePosterURL);
                intent.putExtra("seatList",seatList);
                intent.putExtra("concessionList",addedConcessionList);

                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        ////for top bar back button
        movieConcessionBackButton.setVisibility(View.VISIBLE);
        movieConcessionBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });
    }//end oncreate

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updatePriceTextView(ArrayList<Concession> arr){
        Double totalprice = 0.0;
        for(Concession c : arr){
            totalprice = c.getQuantity()*c.getConcessionPrice();
        }
        totalConcessionPriceTextView.setText(retrieveLocalCurrencyFormat(totalprice));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void concessionCallback(ArrayList<Concession> arr){
        Double totalprice = 0.0;
        for(Concession c : arr){
            totalprice += c.getQuantity()*c.getConcessionPrice();
        }
        totalConcessionPriceTextView.setText(retrieveLocalCurrencyFormat(totalprice));
    }

    //animation when default back button tapped
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    @Override
    public void onStart(){
        super.onStart();
        concessionAdapter.startListening();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        concessionAdapter.stopListening();
    }

}
