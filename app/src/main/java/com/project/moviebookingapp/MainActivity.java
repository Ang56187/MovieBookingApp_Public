package com.project.moviebookingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.project.moviebookingapp.ui.account.LoginActivity;
import com.project.moviebookingapp.ui.admin.AdminHomeActivity;
import com.project.moviebookingapp.ui.home.HomeActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private SharedPreferences app_preferences;
    private Boolean isLoggedIn;
    private String role;
    private Intent intent;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();


        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = app_preferences.edit();
//        editor.clear();
//        editor.commit();

        //save user info and direct user who already logged in to their respective activities
        //after user closed and reopened the app
        isLoggedIn = app_preferences.getBoolean("isLoggedIn", false);
        role = app_preferences.getString("role",null);
        if(!isLoggedIn){
            intent = new Intent(this, LoginActivity.class);
        }
        else if(isLoggedIn){
            if(role.equals("user")) {
                intent = new Intent(this, HomeActivity.class);
            }
            else if(role.equals("admin")){
                intent = new Intent(this, AdminHomeActivity.class);
            }
        }
        startActivity(intent);

    }



}