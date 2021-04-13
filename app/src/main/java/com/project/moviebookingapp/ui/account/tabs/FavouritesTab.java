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

public class FavouritesTab extends Fragment {
    private ArrayList<Movie> favMovieList = new ArrayList<>();
    private MovieController controller;
    private NonFirebaseMovieAdapter adapter;

    public FavouritesTab(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.tab_favourites, container, false);

        controller = new MovieController(getContext());

        ///set firebase
        FirestoreRecyclerOptions<Movie> movieAdapter = controller.queryMovieDocuments();

        //get widgets/layouts
        RecyclerView favouritesRecyclerView = root.findViewById(R.id.favouritesRecyclerView);

        //set widgets/layouts settings

        adapter = new NonFirebaseMovieAdapter(getActivity(),favMovieList,true,
                R.layout.component_movieposter_grid);

        controller.queryFavouriteMovies(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                favMovieList.clear();
                favMovieList.addAll((ArrayList<Movie>) object);
                adapter.notifyDataSetChanged();
            }
        });

        favouritesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        favouritesRecyclerView.setAdapter(adapter);

        return root;
    }


    private Boolean allowRefresh = false;

    @Override
    public void onResume() {
        super.onResume();
        //Initialize();
        if(allowRefresh){
            allowRefresh=false;
            controller.queryFavouriteMovies(new OnFirebaseCallback() {
                @Override
                public void firebaseCallBack(Object object) {
                    favMovieList.clear();
                    favMovieList.addAll((ArrayList<Movie>) object);
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
