package com.project.moviebookingapp.ui.admin;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.MovieRevenueAdapter;
import com.project.moviebookingapp.controller.RevenueController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.MovieRevenue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AdminRevenueActivity extends AppCompatActivity {
    private ImageButton adminRevenueBackButton;
    private RecyclerView movieRevenueRecyclerView;

    private RevenueController revenueController;

    private MovieRevenueAdapter movieRevenueAdapter;

    private ArrayList<MovieRevenue> movieRevenueList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_revenue);

        revenueController = new RevenueController(this);

        //get widgets
        adminRevenueBackButton = findViewById(R.id.adminRevenueBackButton);
        movieRevenueRecyclerView = findViewById(R.id.movieRevenueRecyclerView);

        //set widgets settings
        adminRevenueBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        movieRevenueAdapter = new MovieRevenueAdapter(this, movieRevenueList);
        movieRevenueRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        movieRevenueRecyclerView.setAdapter(movieRevenueAdapter);

        revenueController.retrieveMovieTicketRevenue(new OnFirebaseCallback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void firebaseCallBack(Object object) {
                movieRevenueList.clear();
                movieRevenueList.addAll((ArrayList<MovieRevenue>)object);
                movieRevenueList.sort(Comparator.comparing(MovieRevenue::getTotalMovieRevenue).reversed());
                movieRevenueAdapter.notifyDataSetChanged();
            }
        });

    }//end oncreate

    public ArrayList<MovieRevenue> getMovieRevenueList(){
        return movieRevenueList;
    }

}//end class
