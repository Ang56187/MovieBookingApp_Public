package com.project.moviebookingapp.controller;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.ui.movie.MovieCheckoutActivity;

import java.util.ArrayList;

public class GenreController {
    private FirebaseFirestore mFirestore;
    private Context context;
    private Activity activity;

    public GenreController(Context context){
        this.context = context;
        this.activity = (Activity) context;
        mFirestore = FirebaseFirestore.getInstance();
    }

    public void retrieveGenres(OnFirebaseCallback callback){
        mFirestore.collection("genres").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<String> genreList = new ArrayList<>();
                        for(QueryDocumentSnapshot doc: task.getResult()){
                            genreList.add(doc.getString("genre"));
                        }
                        callback.firebaseCallBack(genreList);
                    }
                });
    }
}
