package com.project.moviebookingapp.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.listener.OnConcessionListener;
import com.project.moviebookingapp.listener.OnFragmentInteractionListener;
import com.project.moviebookingapp.listener.OnGenreToggleListener;
import com.project.moviebookingapp.model.Concession;
import com.project.moviebookingapp.ui.account.SettingsFragment;
import com.project.moviebookingapp.ui.concession.ConcessionFragment;
import com.project.moviebookingapp.ui.movie.MovieSearchFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements OnConcessionListener,
        OnGenreToggleListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_moviesearch, R.id.navigation_settings,
                R.id.navigation_ticketlist, R.id.navigation_concession)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

    }



    //both are for listeners in their respective fragments
    //as fragments relies on the activity listener to work, it relies on activity to get the callback
    //and fragments uses its methods to receive the callback results
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void concessionCallback(ArrayList<Concession> arr) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        ((ConcessionFragment)fragment.getChildFragmentManager().getFragments().get(0))
                .updateCocessionFragment(arr);
    }

    @Override
    public void genreToggleCallback(int type, String genreName, boolean isSelected){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (type == 1){
            ((SettingsFragment)fragment.getChildFragmentManager().getFragments().get(0))
                    .checkSelectedGenreList(genreName,isSelected);
        }
        else if (type == 2){
            ((MovieSearchFragment)fragment.getChildFragmentManager().getFragments().get(0))
                    .checkSelectedGenreList(genreName,isSelected);
        }

    }

}
