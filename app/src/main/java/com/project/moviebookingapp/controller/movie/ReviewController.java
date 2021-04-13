package com.project.moviebookingapp.controller.movie;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Rating;
import com.project.moviebookingapp.model.Showtime;

import java.util.ArrayList;

public class ReviewController {
    private FirebaseFirestore mFirestore;
    private Context context;
    private FirebaseUser account;
    private SharedPreferences app_preferences;

    public ReviewController(Context context){
        this.context = context;
        mFirestore = FirebaseFirestore.getInstance();
        this.account = FirebaseAuth.getInstance().getCurrentUser();
        app_preferences = PreferenceManager.getDefaultSharedPreferences(context);

    }

    public FirestoreRecyclerOptions<Rating> queryAnonRatings(String movieID){
        Query query = mFirestore.collection("ratings")
                .whereEqualTo("movieID",movieID)
                .whereNotEqualTo("accountID",account.getUid());

        FirestoreRecyclerOptions<Rating> options
                =  new FirestoreRecyclerOptions.Builder<Rating>().setQuery(query,
                Rating.class).build();

        return options;
    }

    public void queryOwnRating(String movieID,OnFirebaseCallback callback){
        mFirestore.collection("ratings")
                .whereEqualTo("accountID",account.getUid())
                .whereEqualTo("movieID",movieID)
                .limit(1).get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            Rating rating = new Rating(doc.getString("accountID"),
                                    doc.getString("movieID"), doc.getDouble("ratingScore"),
                                    doc.getString("textReview"),
                                    doc.getString("userName"), doc.getString("profileImgURL"));
                            callback.firebaseCallBack(rating);
                        }
                        else{
                         Rating rating = new Rating();
                         callback.firebaseCallBack(rating);
                        }
                    }
                });
    }

    public void addReview(String movieID,Double ratingScore,String textReview ,
                          OnFirebaseCallback callback){
        String userName = app_preferences.getString("userName"," ");
        String profileImgURL = app_preferences.getString("profileImgURL"," ");
        String accountID = account.getUid();

        Rating rating = new Rating(accountID,movieID,ratingScore,textReview,userName, profileImgURL);

        mFirestore.collection("ratings").add(rating).addOnSuccessListener(
                new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference docRef) {
                        //passed arg does nothing
                        callback.firebaseCallBack(docRef);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"Error adding document: "+e.toString(),Toast.LENGTH_LONG);
                        Log.d("Error","Error adding document: "+e.toString());
                    }
        });
    }

    public void editReview(String movieID,Double ratingScore, String textReview,
                           OnFirebaseCallback callback){
        //first query, find doc id with that movie id and account id
        mFirestore.collection("ratings").whereEqualTo("movieID",movieID)
                .whereEqualTo("accountID",account.getUid()).get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult().size() > 0) {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            //second query, use doc id to update the specified review
                            mFirestore.collection("ratings").document(doc.getId())
                                    .update("textReview",textReview,
                                            "ratingScore",ratingScore)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //passed arg does nothing
                                            callback.firebaseCallBack(true);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context,"Error adding document: "
                                                    +e.toString(),Toast.LENGTH_LONG).show();
                                            Log.d("Error","Error adding document: "+e.toString());
                                        }
                                    });
                        }
                        else{
                            Toast.makeText(context,"Error",Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    public void deleteReview(String movieID,OnFirebaseCallback callback){
        //first query, find doc id with that movie id and account id
        mFirestore.collection("ratings").whereEqualTo("movieID",movieID)
                .whereEqualTo("accountID",account.getUid()).get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult().size() > 0) {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            //second query, use doc id to delete the specified review
                            mFirestore.collection("ratings").document(doc.getId()).delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //passed arg does nothing
                                            callback.firebaseCallBack(true);
                                        }
                                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context,"Error adding document: "
                                            +e.toString(),Toast.LENGTH_LONG).show();
                                    Log.d("Error","Error adding document: "+e.toString());
                                }
                            });
                        }
                    }
                }
        );
    }

    public void retrieveReviews(String movieID, OnFirebaseCallback callback){
        mFirestore.collection("ratings").whereEqualTo("movieID",movieID)
                .get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().size() == 0 ){
                            callback.firebaseCallBack(null);
                        }
                        else{
                            ArrayList<Rating> ratingList = new ArrayList<>();
                            for(QueryDocumentSnapshot doc: task.getResult()){
                                Rating rating = new Rating(doc.getString("accountID"),
                                        doc.getString("movieID"),
                                        doc.getDouble("ratingScore"),
                                        doc.getString("textReview"),
                                        doc.getString("userName"),
                                        doc.getString("profileImgURL"));
                                ratingList.add(rating);
                            }
                            callback.firebaseCallBack(ratingList);
                        }
                    }
                });
    }

}
