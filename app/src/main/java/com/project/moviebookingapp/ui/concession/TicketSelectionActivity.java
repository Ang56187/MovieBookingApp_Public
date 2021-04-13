package com.project.moviebookingapp.ui.concession;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.TicketSelectAdapter;
import com.project.moviebookingapp.controller.TicketController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.listener.OnSelectTicketListener;
import com.project.moviebookingapp.model.Concession;
import com.project.moviebookingapp.model.Ticket;
import com.project.moviebookingapp.ui.movie.MoviePaymentCompletedActivity;

import java.util.ArrayList;
import java.util.List;

import static com.project.moviebookingapp.custom.CustomAnimations.getRippleBackgroundDrawable;
import static com.project.moviebookingapp.custom.CustomAnimations.transparentClickAnim;

public class TicketSelectionActivity extends AppCompatActivity implements OnSelectTicketListener {
    private ArrayList<String> ticketList = new ArrayList<String>();
    private int selectedTicketPosition;

    private ArrayList<Concession> addedConcessionList = new ArrayList<>();

    private TicketController controller;

    private ArrayList<Ticket> ticketsWithConcessionsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Context context = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        controller = new TicketController(this);

        Intent intent = getIntent();//get intent from previous activity(MovieDetailActivity)
        addedConcessionList = (ArrayList<Concession>)intent.getSerializableExtra("concessionList");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_selection);

        //get widgets/layouts
        ImageButton ticketSelectBackButton = findViewById(R.id.ticketSelectBackButton);
        RecyclerView ticketSelectRecyclerView = findViewById(R.id.ticketSelectRecyclerView);
        RelativeLayout ticketSelectBottomRelativeLayout = findViewById(R.id.ticketSelectionBottomRelativeLayout);
        ImageButton ticketSelectForwardButton = findViewById(R.id.ticketSelectForwardButton);
        TextView noBookedTicketsTextView = findViewById(R.id.noBookedTicketsTextView);


        //set settings for widgets/layouts
        controller.retrieveTicketsWithConcessions(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                ArrayList<Ticket> ticketsWithConcessionsList = (ArrayList<Ticket>) object;
                TicketSelectAdapter ticketSelectAdapter = new TicketSelectAdapter(context,
                        ticketsWithConcessionsList);
                ticketSelectRecyclerView.setLayoutManager(new LinearLayoutManager(context
                        ,LinearLayoutManager.VERTICAL, false));
                ticketSelectRecyclerView.setAdapter(ticketSelectAdapter);

                if(ticketsWithConcessionsList.size()>0){
                    noBookedTicketsTextView.setVisibility(View.GONE);
                    ticketSelectBottomRelativeLayout.setBackground(ContextCompat.getDrawable(context,
                            R.drawable.available_stroke));
                    ticketSelectForwardButton.setBackgroundColor(getResources().getColor(R.color.availableColor));
                }
                else{
                    noBookedTicketsTextView.setVisibility(View.VISIBLE);
                    ticketSelectBottomRelativeLayout.setBackground(ContextCompat.getDrawable(context,
                            R.drawable.unavailable_stroke));
                    ticketSelectForwardButton.setBackgroundColor(getResources().getColor(R.color.unavailableColor));
                }

                ticketSelectForwardButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(ticketsWithConcessionsList.size()>0) {
                            //ripple effect
                            Drawable background = getResources().getDrawable(R.color.availableColor);
                            ticketSelectForwardButton.setBackground(getRippleBackgroundDrawable(
                                    R.color.availableColor, background));

                            Ticket selectedTicket = ticketsWithConcessionsList.get(selectedTicketPosition);
                            Intent intent = new Intent(getApplicationContext(),
                                    ConcessionCheckoutActivity.class);
                            intent.putExtra("movieName", selectedTicket.getMovieName());
                            intent.putExtra("ticketID",selectedTicket.ticketID());
                            intent.putExtra("showtime",selectedTicket.getShowtime().getSeconds());
                            intent.putExtra("concessionList",addedConcessionList);

                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    }
                });
            }
        });

        //// buttons set
        ticketSelectBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                view.startAnimation(transparentClickAnim);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

    }//end oncreate

    //triggers if one of tickets tapped and selected
    @Override
    public void onTicketCallBack(Integer i){
        selectedTicketPosition = i;
    }


    //animation when default back button tapped
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


}
