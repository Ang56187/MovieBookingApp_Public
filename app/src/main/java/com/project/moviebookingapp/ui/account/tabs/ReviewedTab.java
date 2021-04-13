package com.project.moviebookingapp.ui.account.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.NonFirebaseMovieAdapter;
import com.project.moviebookingapp.controller.movie.MovieController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Movie;

import java.util.ArrayList;

public class ReviewedTab extends Fragment {
    private ArrayList<Movie> reviewedMovieList = new ArrayList<>();
    private MovieController controller;
    private NonFirebaseMovieAdapter adapter;

    public ReviewedTab(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.tab_reviewed, container, false);

        controller = new MovieController(getContext());

        ///set firebase
        FirestoreRecyclerOptions<Movie> movieAdapter = controller.queryMovieDocuments();

        //get widgets/layouts
        RecyclerView reviewedRecyclerView = root.findViewById(R.id.reviewedRecyclerView);

        //set widgets/layouts settings

        adapter = new NonFirebaseMovieAdapter(getActivity(),reviewedMovieList,
                R.layout.component_movieposter_grid);

        controller.queryReviewedMovies(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                reviewedMovieList.clear();
                reviewedMovieList.addAll((ArrayList<Movie>) object);
                adapter.notifyDataSetChanged();
            }
        });

        reviewedRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        reviewedRecyclerView.setAdapter(adapter);

        return root;
    }

    private Boolean allowRefresh = false;

    @Override
    public void onResume() {
        super.onResume();
        //Initialize();
        if(allowRefresh){
            allowRefresh=false;
            controller.queryReviewedMovies(new OnFirebaseCallback() {
                @Override
                public void firebaseCallBack(Object object) {
                    reviewedMovieList.clear();
                    reviewedMovieList.addAll((ArrayList<Movie>) object);
                    adapter.notifyDataSetChanged();
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
