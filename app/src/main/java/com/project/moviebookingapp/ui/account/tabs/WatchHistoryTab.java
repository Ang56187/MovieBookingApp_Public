package com.project.moviebookingapp.ui.account.tabs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.MovieRecyclerAdapter;
import com.project.moviebookingapp.adapter.NonFirebaseMovieAdapter;
import com.project.moviebookingapp.controller.movie.MovieController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Movie;

import java.util.ArrayList;

public class WatchHistoryTab extends Fragment {
    private ArrayList<Movie> lastSeenList = new ArrayList<>();
    private ArrayList<Movie> currentlyBookedList = new ArrayList<>();

    private MovieController controller;
    private NonFirebaseMovieAdapter currentlyBookedAdapter;
    private NonFirebaseMovieAdapter lastSeenAdapter;


    public WatchHistoryTab(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.tab_watchhistory, container, false);

        controller = new MovieController(getContext());

        ///set firebase
        //get widgets/layouts settings
        RecyclerView currentlyBookedRecyclerView = root.findViewById(R.id.currentlyBookedRecyclerView);
        RecyclerView lastSeenRecyclerView = root.findViewById(R.id.lastSeenRecyclerView);

        //set widgets/layouts settings
        currentlyBookedAdapter = new NonFirebaseMovieAdapter(getActivity(),
                currentlyBookedList,R.layout.component_movieposter_grid);
        lastSeenAdapter = new NonFirebaseMovieAdapter(getActivity(),
                lastSeenList,R.layout.component_movieposter_grid);

        controller.queryMoviesByStatus(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                ArrayList<Movie> movieList = (ArrayList<Movie>)object;

                lastSeenList.clear();
                currentlyBookedList.clear();
                int count = 0;
                for(Movie m: movieList){
                    if(m.isUsed()) {
                        lastSeenList.add(m);
                    }
                    else{
                        currentlyBookedList.add(m);
                    }
                }

                lastSeenAdapter.notifyDataSetChanged();
                currentlyBookedAdapter.notifyDataSetChanged();
            }
        });


        currentlyBookedRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        currentlyBookedRecyclerView.setAdapter(currentlyBookedAdapter);

        lastSeenRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        lastSeenRecyclerView.setAdapter(lastSeenAdapter);

        return root;
    }

    private Boolean allowRefresh = false;

    @Override
    public void onResume() {
        super.onResume();
        //Initialize();
        if(allowRefresh){
            allowRefresh=false;

            controller.queryMoviesByStatus(new OnFirebaseCallback() {
                @Override
                public void firebaseCallBack(Object object) {
                    ArrayList<Movie> movieList = (ArrayList<Movie>)object;

                    lastSeenList.clear();
                    currentlyBookedList.clear();
                    for(Movie m: movieList){
                        if(m.isUsed()) {
                            lastSeenList.add(m);
                        }
                        else{
                            currentlyBookedList.add(m);
                        }
                    }

                    lastSeenAdapter.notifyDataSetChanged();
                    currentlyBookedAdapter.notifyDataSetChanged();
                }
            });

        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (!allowRefresh)
            allowRefresh = true;
    }


}
