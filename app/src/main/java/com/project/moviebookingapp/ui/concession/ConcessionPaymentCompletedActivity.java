package com.project.moviebookingapp.ui.concession;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.moviebookingapp.MainActivity;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.ConcessionCheckoutItemAdapter;
import com.project.moviebookingapp.model.Concession;

import java.util.ArrayList;
import java.util.Date;

import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateTime;

public class ConcessionPaymentCompletedActivity extends AppCompatActivity {
    private ArrayList<Concession> concessionList = new ArrayList<>();
    private String ticketID;
    private String movieName;
    private long showtimeSeconds;
    private String totalConcessionPriceString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        Intent intent = getIntent();//get intent from previous activity(MovieDetailActivity)
        concessionList = (ArrayList<Concession>) intent.getSerializableExtra("concessionList");
        ticketID = intent.getStringExtra("ticketID");
        movieName = intent.getStringExtra("movieName");
        showtimeSeconds = intent.getLongExtra("showtime",0);
        totalConcessionPriceString = intent.getStringExtra("totalPrice");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concession_payment_completed);

        ////get widgets/layouts
        View concessionListLayout = findViewById(R.id.concessionListLayout);
        Button backToHomeButton = findViewById(R.id.backToHomeButton);

        ////// concessionListLayout
        TextView totalConcessionPriceTextView = concessionListLayout.findViewById(R.id.totalConcessionPriceTextView);
        RecyclerView concessionCheckoutItemRecyclerView = concessionListLayout.findViewById(R.id.concessionCheckoutItemRecyclerView);
        //set text
        TextView movieTitleTextView = findViewById(R.id.movieTitleTextView);
        TextView movieDateTimeTextView = findViewById(R.id.movieDateTimeTextView);

        //// set widgets/layout settings
        ////components within included layout
        movieTitleTextView.setText(movieName);
        movieDateTimeTextView.setText(getLocalDateTime().format(new Date(showtimeSeconds*1000)));
        totalConcessionPriceTextView.setText(totalConcessionPriceString);

        concessionCheckoutItemRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        concessionCheckoutItemRecyclerView.setAdapter(new ConcessionCheckoutItemAdapter(this,concessionList));

        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

    }

    ////disable back button here
    @Override
    public void onBackPressed() {
    }
}
