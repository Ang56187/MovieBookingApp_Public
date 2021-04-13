package com.project.moviebookingapp.ui.movie;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.GenrePreferenceAdapter;
import com.project.moviebookingapp.adapter.MovieRecyclerAdapter;
import com.project.moviebookingapp.adapter.NonFirebaseMovieAdapter;
import com.project.moviebookingapp.controller.GenreController;
import com.project.moviebookingapp.controller.movie.MovieController;
import com.project.moviebookingapp.enumeration.BtnType;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.listener.OnFragmentInteractionListener;
import com.project.moviebookingapp.listener.OnGenreToggleListener;
import com.project.moviebookingapp.model.Movie;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class MovieSearchFragment  extends Fragment {
    private ArrayList<Movie> movieList = new ArrayList<>();
    private ArrayList<String> genreList = new ArrayList<String>();
    private ArrayList<String> selectedGenreList = new ArrayList<>();

    private Calendar filterCalendar = Calendar.getInstance();

    //by default "showing now" is selected
    private BtnType btnType = BtnType.SHOWINGNOW;

    //size of button animations
    private int lowestSize = 14;
    private int midSize = 16;
    private int largeSize = 18;

    //components
    private EditText searchMovieEditText;
    private ImageButton searchMovieButton;
    private ImageButton filterMovieButton;
    private Button showingNowButton;
    private Button comingSoonButton;
    private Button notShowingButton;
    private RecyclerView movieSearchRecyclerView;
    private ScrollView movieSearchScrollView;
    //for pop up
    private PopupWindow mpopup;
    private View popUpView;
    private RelativeLayout backgroundRelativeLayout;
    private RelativeLayout searchFilterRelativeLayout;
    private ImageButton closeButton;
    private TextView genreTextView, dateTextView;
    private HorizontalScrollView horizontalScrollView;
    private RecyclerView genreRecyclerView;
    private DatePicker filterDatePicker;
    private Button saveFilterButton;

    private FirestoreRecyclerOptions<Movie> movieOptions;

    private NonFirebaseMovieAdapter movieSearchAdapter;
    private GenrePreferenceAdapter genrePreferenceAdapter;

    private MovieController controller;
    private GenreController genreController;

    private OnFragmentInteractionListener fragmentListener;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_moviesearch, container, false);

        controller = new MovieController(getContext());
        genreController = new GenreController(getContext());

        //get components
        searchMovieEditText = root.findViewById(R.id.searchMovieEditText);
        searchMovieButton = root.findViewById(R.id.searchMovieButton);
        filterMovieButton = root.findViewById(R.id.filterMovieButton);
        showingNowButton = (Button)root.findViewById(R.id.showingNowButton);
        comingSoonButton = (Button)root.findViewById(R.id.comingSoonButton);
        notShowingButton = root.findViewById(R.id.notShowingButton);
        movieSearchRecyclerView = root.findViewById(R.id.movieSearchRecyclerView);
        movieSearchScrollView = root.findViewById(R.id.movieSearchScrollView);
        //popups
        popUpView = getLayoutInflater().inflate(R.layout.popup_searchfilter, null);
        closeButton  = popUpView.findViewById(R.id.closePopUpButton);
        backgroundRelativeLayout = popUpView.findViewById(R.id.backgroundRelativeLayout);
        searchFilterRelativeLayout = popUpView.findViewById(R.id.searchFilterRelativeLayout);
        genreTextView = popUpView.findViewById(R.id.genreTextView);
        dateTextView = popUpView.findViewById(R.id.dateTextView);
        horizontalScrollView = popUpView.findViewById(R.id.horizontalScrollView);
        genreRecyclerView = popUpView.findViewById(R.id.genreRecyclerView);
        filterDatePicker = popUpView.findViewById(R.id.filterDatePicker);
        saveFilterButton = popUpView.findViewById(R.id.saveFilterButton);

        ///set firebase
        movieOptions = controller.queryMovieDocuments();

        //set components settings
        //set settings for recyclerview
        controller.queryMovieDocuments(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                movieList.clear();
                movieList.addAll((ArrayList<Movie>)object);
                movieSearchAdapter = new NonFirebaseMovieAdapter(getContext(),movieList
                        ,R.layout.component_movieposter_grid);
                movieSearchRecyclerView.setAdapter(movieSearchAdapter);
            }
        });
        GridLayoutManager gridLayout = new GridLayoutManager(getContext(), 3);
        movieSearchAdapter = new NonFirebaseMovieAdapter(getContext(),movieList
                ,R.layout.component_movieposter_grid);
        movieSearchRecyclerView.setLayoutManager(gridLayout);
        movieSearchRecyclerView.setAdapter(movieSearchAdapter);


        //set settings for buttons
        showingNowButton.setTypeface(ResourcesCompat.getFont(getActivity(),R.font.roboto_bold));
        showingNowButton.setTextSize(lowestSize);
        //by default will show "showing now"
        switchButtonSearching(btnType);

        //set recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        genreRecyclerView.setLayoutManager(linearLayoutManager);
        genreController.retrieveGenres(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                genreList = (ArrayList<String>) object;
                genrePreferenceAdapter =new GenrePreferenceAdapter(getContext(),genreList,
                        selectedGenreList,2);
                genreRecyclerView.setAdapter(genrePreferenceAdapter);
            }
        });

        //set clickables
        searchMovieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //(search only starts from the prefix)
                //since search is case sensitive, and i cant put the movie titles as lower case
                //i need to make the first letter upper case as i assume all movies will start upper
                startSearching();
            }
        });

        filterMovieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mpopup = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, true); // Creation of popup
                mpopup.setAnimationStyle(android.R.style.Animation_Dialog);
                mpopup.showAtLocation(popUpView, Gravity.CENTER, 0, 0); // Displaying popup

                //set date
                if(btnType.equals(BtnType.SHOWINGNOW)){
                    filterDatePicker.setVisibility(View.VISIBLE);
                    dateTextView.setVisibility(View.VISIBLE);
                }
                else{
                    filterDatePicker.setVisibility(View.GONE);
                    dateTextView.setVisibility(View.GONE);
                }
            }
        });

        //both buttons and relative layout can close the popup
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mpopup.dismiss();
                resetDatePicker();
            }
        });
        backgroundRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mpopup.dismiss();
                resetDatePicker();
            }
        });
        searchFilterRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { }
        });
        saveFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mpopup.dismiss();
                startSearchingFilter();
            }
        });

        //for pop up scrolls
        genreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                horizontalScrollView.fullScroll(View.FOCUS_LEFT);
            }
        });
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                horizontalScrollView.fullScroll(View.FOCUS_RIGHT);
            }
        });

        showingNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!btnType.equals(BtnType.SHOWINGNOW)){
                    Button shrinkBtn = notShowingButton;
                    switch(btnType){
                        case NOTSHOWING:
                            shrinkBtn = notShowingButton;
                            break;
                        case COMINGSOON:
                            shrinkBtn = comingSoonButton;
                        default:
                            break;
                    }
                    animateButtonExpand(showingNowButton,shrinkBtn,gridLayout);
                }
                btnType = BtnType.SHOWINGNOW;
                switchButtonSearching(btnType);
            }
        });

        comingSoonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!btnType.equals(BtnType.COMINGSOON)) {
                    Button shrinkBtn = notShowingButton;
                    switch(btnType){
                        case NOTSHOWING:
                            shrinkBtn = notShowingButton;
                            break;
                        case SHOWINGNOW:
                            shrinkBtn = showingNowButton;
                        default:
                            break;
                    }
                    animateButtonExpand(comingSoonButton,shrinkBtn,gridLayout);
                }
                btnType = BtnType.COMINGSOON;
                switchButtonSearching(btnType);
            }
        });

        notShowingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!btnType.equals(BtnType.NOTSHOWING)) {
                    Button shrinkBtn = notShowingButton;
                    switch(btnType){
                        case SHOWINGNOW:
                            shrinkBtn = showingNowButton;
                            break;
                        case COMINGSOON:
                            shrinkBtn = comingSoonButton;
                        default:
                            break;
                    }
                    animateButtonExpand(notShowingButton,shrinkBtn,gridLayout);
                }
                btnType = BtnType.NOTSHOWING;
                switchButtonSearching(btnType);
            }
        });

        return root;
    }//end oncreate

    //used when switching between "showing now","coming soon" and "not showing"
    private void switchButtonSearching(BtnType btnType){
        String searchInput = searchMovieEditText.getText().toString().toLowerCase();
        controller.querySearchMovieDocuments(searchInput, btnType,selectedGenreList,
                new OnFirebaseCallback() {
                    @Override
                    public void firebaseCallBack(Object object) {
                        movieList.clear();
                        movieList.addAll((ArrayList<Movie>)object);
                        movieSearchAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void startSearching(){
        String searchInput = searchMovieEditText.getText().toString().toLowerCase();
        switchButtonSearching(btnType);
    }

    private void startSearchingFilter(){
        String searchInput = searchMovieEditText.getText().toString().toLowerCase();

        filterCalendar.set(Calendar.DAY_OF_MONTH,filterDatePicker.getDayOfMonth());
        filterCalendar.set(Calendar.MONTH,filterDatePicker.getMonth());
        filterCalendar.set(Calendar.YEAR,filterDatePicker.getYear());

        if(btnType.equals(BtnType.SHOWINGNOW)){
            controller.querySearchMovieDocuments(searchInput, selectedGenreList,
                    filterCalendar.getTime(), new OnFirebaseCallback() {
                        @Override
                        public void firebaseCallBack(Object object) {
                            movieList.clear();
                            movieList.addAll((ArrayList<Movie>)object);
                            movieSearchAdapter.notifyDataSetChanged();
                        }
                    });
        }
        else{
            startSearching();
        }

    }

    private void animateButtonExpand(Button expandBtn, Button shrinkBtn,
                                     GridLayoutManager gridLayout){
        ObjectAnimator fontSizeAnimExpand = ObjectAnimator.ofFloat(expandBtn,
                "textSize", lowestSize, largeSize, midSize);
        ObjectAnimator fontSizeAnimShrink = ObjectAnimator.ofFloat(shrinkBtn,
                "textSize", midSize, lowestSize);

        shrinkBtn.setTypeface(ResourcesCompat.getFont(getActivity(),R.font.roboto_light));
        expandBtn.setTypeface(ResourcesCompat.getFont(getActivity(),R.font.roboto_bold));

        fontSizeAnimExpand.setDuration(300);
        fontSizeAnimShrink.setDuration(300);

        fontSizeAnimExpand.start();
        fontSizeAnimShrink.start();

        gridLayout.smoothScrollToPosition(movieSearchRecyclerView,null,0);
    }


    public void checkSelectedGenreList(String genreName,boolean isSelected){
        if(isSelected){
            selectedGenreList.add(genreName);
        }else{
            selectedGenreList.remove(genreName);
        }
    }

    private void resetDatePicker(){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        filterDatePicker.init(year,month,day,null);
    }


    @Override
    public void onStart() {
        super.onStart();
        //by default will show "showing now"
        switchButtonSearching(btnType);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
