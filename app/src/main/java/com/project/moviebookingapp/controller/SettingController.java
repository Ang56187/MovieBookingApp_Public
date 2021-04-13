package com.project.moviebookingapp.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.ui.account.SettingsFragment;

import java.util.ArrayList;
import java.util.List;

public class SettingController {
    private SharedPreferences app_preferences;
    private FirebaseFirestore mFirestore;
    private Context context;
    private FirebaseUser account;

    public SettingController(Context context){
        this.context = context;
        mFirestore = FirebaseFirestore.getInstance();
        this.app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.account = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void retrieveAllowNotShowing(OnFirebaseCallback callback){
        mFirestore.collection("accounts")
                .document(app_preferences.getString("docID","")).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        callback.firebaseCallBack(documentSnapshot.getBoolean("allowNotShowing"));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Error",e.toString());
                        Toast.makeText(context,e.toString(),Toast.LENGTH_LONG);
                    }
                });
    }

    public void retrieveUserGenreList(OnFirebaseCallback callback){
        mFirestore.collection("accounts")
                .document(app_preferences.getString("docID","")).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot != null){
                            List<String> genreList = (List<String>)documentSnapshot.get("genreList");
                            callback.firebaseCallBack(genreList);
                        }
                        else{
                            callback.firebaseCallBack(null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Error",e.toString());
                        Toast.makeText(context,e.toString(),Toast.LENGTH_LONG);
                    }
                });

    }


    public void updateAllowNotShowing(Boolean bool){
        mFirestore.collection("accounts")
                .document(app_preferences.getString("docID",""))
                .update("allowNotShowing",bool)
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"Fail to update..",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateGenreList(List<String> selectedGenreList){
        mFirestore.collection("accounts")
                .document(app_preferences.getString("docID",""))
                .update("genreList",selectedGenreList)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"Fail to update..",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
