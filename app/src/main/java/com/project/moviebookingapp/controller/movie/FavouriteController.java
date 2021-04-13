package com.project.moviebookingapp.controller.movie;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Favourite;
import com.project.moviebookingapp.model.Showtime;

public class FavouriteController {
    private FirebaseFirestore mFirestore;
    private Context context;
    private Boolean isFavourite ;
    private SharedPreferences app_preferences;

    public FavouriteController(Context context){
        this.context = context;
        mFirestore = FirebaseFirestore.getInstance();
        app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }//constructor

    public void checkIsFavourited(String movieID, OnFirebaseCallback callback){
        mFirestore.collection("favourites")
                .whereEqualTo("accountID",app_preferences.getString("accountID"," "))
                .whereEqualTo("movieID",movieID)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    //here it only checks if results were returned or not
                    //it does not care if result returned are actually from users
                    // as the query is intended to return only user favourite status on that movie
                    //if theres something else, the problem lies in the query
                    if(task.getResult().size() >0){
                        callback.firebaseCallBack(true);
                    }
                    else{
                        callback.firebaseCallBack(false);
                    }
                }
                else{
                    Log.d("Doc ", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void tapOnFavourite(String movieID,Boolean isFavourite,OnFirebaseCallback callback) {

        if(isFavourite) {
            //for delete, if movie selected
            //first query, find favourite id with user
            mFirestore.collection("favourites")
                    .whereEqualTo("accountID", app_preferences.getString("accountID", " "))
                    .whereEqualTo("movieID", movieID)
                    .get(Source.CACHE).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful() && task.getResult().size()>0) {

                        //only get the first result
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        //seconf query, delete the document
                        mFirestore.collection("favourites").document(doc.getId())
                                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                callback.firebaseCallBack(!isFavourite);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context,
                                        "Error removing favourite:"+e.toString(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Log.d("Doc ", "Error getting documents: ", task.getException());
                    }
                }
            });
        }//end delete
        else{
            Favourite favourite
                    = new Favourite(app_preferences.getString("accountID", " "), movieID);
            mFirestore.collection("favourites").add(favourite)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            callback.firebaseCallBack(!isFavourite);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context,
                            "Error adding favourite:"+e.toString(),
                            Toast.LENGTH_LONG).show();
                    Log.d("Doc ", "Error adding documents: "+ e.toString());

                }
            });
        }
    }
    }
