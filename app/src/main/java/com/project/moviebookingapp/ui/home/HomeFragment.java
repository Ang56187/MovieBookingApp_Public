package com.project.moviebookingapp.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.MovieRecyclerAdapter;
import com.project.moviebookingapp.controller.account.ProfileController;
import com.project.moviebookingapp.controller.movie.MovieController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.listener.OnFragmentInteractionListener;
import com.project.moviebookingapp.model.Movie;
import com.project.moviebookingapp.ui.account.ProfileActivity;
import com.project.moviebookingapp.ui.movie.MovieSearchFragment;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment {
    private MovieRecyclerAdapter recommendedAdapter;
    private MovieRecyclerAdapter showingNowAdapter;
    private MovieRecyclerAdapter latestMoviesAdapter;

    private MovieController controller;
    private ProfileController profileController;
    private SharedPreferences app_preferences;
    private FirebaseStorage mFireStorage;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    private ImageView userProfileImageView;

    private FirestoreRecyclerOptions<Movie> recommendedOptions;

    private OnFragmentInteractionListener fragmentListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        controller = new MovieController(getContext());
        profileController = new ProfileController(getContext());
        app_preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mFireStorage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        //check firebase status
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //signed in
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(getContext(), "Internet connection unavailable.",
                            Toast.LENGTH_LONG).show();
                }
            }
        };

        //get widgets/layout
        RecyclerView recommendedMovieRecyclerView = (RecyclerView)
                root.findViewById(R.id.recommendedMovieRecyclerView);
        RecyclerView showingMovieRecyclerView = (RecyclerView)
                root.findViewById(R.id.showingMovieRecyclerView);
        TextView noRecommendationsText = root.findViewById(R.id.noRecommendationsText);
        TextView viewAllTextView = root.findViewById(R.id.viewAllTextView);
        ImageButton recommendationSettingsButton =
                root.findViewById(R.id.recommendationSettingsButton);
        ViewPager2 imageSliderViewPager = (ViewPager2) root.findViewById(R.id.imageSliderViewPager);
        TabLayout imageSliderTabLayout = (TabLayout) root.findViewById(R.id.imageSliderTabLayout);
        userProfileImageView = root.findViewById(R.id.userProfileImageView);
        ImageView homeNotificationImageView = root.findViewById(R.id.homeNotificationImageView);


        // set widgets/layout settings
        //onclick
        viewAllTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"Tap on movies icon at bottom nav bar to see all movies"
                ,Toast.LENGTH_SHORT).show();
            }
        });

        recommendationSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"Tap on settings icon at bottom nav bar to " +
                                "configure recommendations"
                        ,Toast.LENGTH_SHORT).show();
            }
        });

        //sliding animation at viewpager2
        imageSliderViewPager.setClipToPadding(false);
        imageSliderViewPager.setClipChildren(false);
        imageSliderViewPager.setOffscreenPageLimit(1);
        imageSliderViewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1-Math.abs(position);
                page.setScaleY(0.85f+r*0.15f);
            }
        });
        imageSliderViewPager.setPageTransformer(compositePageTransformer);

        //dynamically add images of movies using adapters
        // use a linear layout manager
        recommendedMovieRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        showingMovieRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        // specify an adapter for recycler views
        recommendedOptions = controller.queryNoMovieDocuments();
        //recommended
        controller.queryRecommendedMovies("accounts",
                app_preferences.getString("docID", ""), new OnFirebaseCallback() {
                    @Override
                    public void firebaseCallBack(Object object) {
                        if(object == null){
                            noRecommendationsText.setVisibility(View.VISIBLE);
                            recommendedMovieRecyclerView.setVisibility(View.GONE);
                        }else {
                            noRecommendationsText.setVisibility(View.GONE);
                            recommendedMovieRecyclerView.setVisibility(View.VISIBLE);
                            recommendedOptions = (FirestoreRecyclerOptions<Movie>) object;
                            recommendedAdapter.updateOptions(recommendedOptions);
                        }
                    }
                });
        recommendedAdapter = new MovieRecyclerAdapter(recommendedOptions,getActivity(),
                R.layout.component_movieposter);
        recommendedMovieRecyclerView.setAdapter(recommendedAdapter);

        //showing now
        FirestoreRecyclerOptions<Movie> showingNowOptions;
        FirestoreRecyclerOptions<Movie> noMovieOptions = controller.queryNoMovieDocuments();
        showingNowAdapter = new MovieRecyclerAdapter(noMovieOptions,getActivity(),
                R.layout.component_movieposter);
        controller.queryShowingNowMovies(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                FirestoreRecyclerOptions<Movie> showingNowOptions =
                        (FirestoreRecyclerOptions<Movie>) object;
                showingNowAdapter.updateOptions(showingNowOptions);
                showingMovieRecyclerView.setAdapter(showingNowAdapter);
            }
        });

        //latest
        latestMoviesAdapter = new MovieRecyclerAdapter(noMovieOptions,getActivity(),
                R.layout.component_movieposter_alt);
        imageSliderViewPager.setAdapter(latestMoviesAdapter);
        controller.queryLatestMovies(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                FirestoreRecyclerOptions<Movie> latestMovieOptions =
                        (FirestoreRecyclerOptions<Movie>) object;
                latestMoviesAdapter.updateOptions(latestMovieOptions);
                showingMovieRecyclerView.setAdapter(showingNowAdapter);
            }
        });

        //set user profile image
        StorageReference imageRef = mFireStorage.getReference(
                app_preferences.getString("profileImgURL"," "));
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(getContext()).load(imageURL)
                        .override(38,38)
                        .into(userProfileImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("Error", "Firebase storage error: " + exception);
            }
        });

        //set slider indicator
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(imageSliderTabLayout, imageSliderViewPager, true,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(TabLayout.Tab tab, int position) {
                        System.out.println(tab+" "+position);
                    }
                });
        tabLayoutMediator.attach();

        //set image to go to profile
        userProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        homeNotificationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), NotificationActivity.class);
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        return root;

    }

    @Override
    public void onStart() {
        super.onStart();
        recommendedAdapter.startListening();
        showingNowAdapter.startListening();
        latestMoviesAdapter.startListening();

        //reload image
        StorageReference imageRef = mFireStorage.getReference(
                app_preferences.getString("profileImgURL"," "));
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(getContext()).load(imageURL)
                        .override(38,38)
                        .into(userProfileImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("Error", "Firebase storage error: " + exception);
            }
        });
    }

    @Override
    public void onStop() {//TODO: check if there is any performance impact here if onDestroy
        super.onStop();
        recommendedAdapter.stopListening();
        showingNowAdapter.stopListening();
        latestMoviesAdapter.stopListening();
    }

}


//           FirebaseFirestore.getInstance().collection("movies")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//        @Override
//        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//            if (task.isSuccessful()) {
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    Log.d(TAG, document.getId() + " => " + document.getData());
//                }
//            } else {
//                Log.d(TAG, "Error getting documents: ", task.getException());
//            }
//        }
//    });

