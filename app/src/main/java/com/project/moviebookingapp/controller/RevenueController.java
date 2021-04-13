package com.project.moviebookingapp.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Movie;
import com.project.moviebookingapp.model.MovieRevenue;
import com.project.moviebookingapp.model.Showtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RevenueController {
    private FirebaseFirestore mFirestore;
    private Context context;
    private SharedPreferences app_preferences;

    public RevenueController(Context context) {
        this.context = context;
        mFirestore = FirebaseFirestore.getInstance();
        app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    private ArrayList<MovieRevenue> movieRevenueList = new ArrayList<>();
    //retrieves movies first
    public void retrieveMovies(OnFirebaseCallback callback) {
        //first query get movie
        mFirestore.collection("movies")
//                .whereIn("movieName", Arrays.asList("Baby driver","Dunkirk"))
                .whereGreaterThanOrEqualTo("endDate",new Timestamp(new Date()))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            MovieRevenue movieRevenue = new MovieRevenue(
                                    doc.getId(),
                                    doc.getString("movieName"),
                                    doc.getTimestamp("startDate").getSeconds(),
                                    doc.getTimestamp("endDate").getSeconds(),
                                    0,0
                            );
                            movieRevenueList.add(movieRevenue);
                        }//end for
                        callback.firebaseCallBack(movieRevenueList);
                    }
                });//end movie query
    }

    private ArrayList<MovieRevenue> showingNowRevenueList;
    //retrieves tickets for movie
    public void retrieveMovieTicketRevenue(OnFirebaseCallback callback) {
        retrieveMovies(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {

                showingNowRevenueList = (ArrayList<MovieRevenue>)object;

                //select movies showing now
                ArrayList<MovieRevenue> showingNowList = new ArrayList<>();
                for(MovieRevenue m:movieRevenueList){
                    Date startDate = new Date(m.getStartDateSeconds()*1000);
                    Date endDate = new Date(m.getEndDateSeconds()*1000);
                    if((startDate.compareTo(new Date()) <= 0 &&
                            endDate.compareTo(new Date()) >= 0)){
                        showingNowList.add(m);
                    }
                }
                showingNowRevenueList=showingNowList;

                mFirestore.collection("tickets").get().addOnSuccessListener(
                        new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (DocumentSnapshot doc: queryDocumentSnapshots.getDocuments()){
                                    for(MovieRevenue m :showingNowRevenueList) {
                                        if (doc.getString("movieID").equals(m.getMovieID())) {
                                            List<String> seatList = (List<String>)doc.get("seat");
                                            double seatPrice = doc.getDouble("seatPrice");

                                            m.setTotalMovieRevenue(m.getTotalMovieRevenue()+
                                                    (seatList.size()*seatPrice));
                                            m.setTicketCount(m.getTicketCount()+1);
                                            break;
                                        }
                                    }
                                }//end for
                                callback.firebaseCallBack(showingNowRevenueList);
                            }//end success
                        });
            }
        });
    }

}
