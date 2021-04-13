package com.project.moviebookingapp.ui.account;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.GenrePreferenceAdapter;
import com.project.moviebookingapp.controller.GenreController;
import com.project.moviebookingapp.controller.SettingController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.listener.OnGenreToggleListener;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {
    private List<String> selectedGenreList = new ArrayList<>();

    private ArrayList<String> genreList = new ArrayList<String>();

    private GenreController genreController;
    private SettingController settingController;

    private GenrePreferenceAdapter genrePreferenceAdapter;

    private RadioButton allowRadioButton;
    private RadioButton disallowRadioButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        genreController = new GenreController(getActivity());
        settingController = new SettingController(getActivity());

        //get components
        RelativeLayout notification_toggle = root.findViewById(R.id.notification_toggle);
        allowRadioButton = root.findViewById(R.id.allowRadioButton);
        disallowRadioButton = root.findViewById(R.id.disallowRadioButton);
        RecyclerView genrePreferenceRecyclerView = root.findViewById(R.id.genrePreferenceRecyclerView);

        //get text of toggles
        TextView notification_toggle_text = notification_toggle.findViewById(R.id.settingToggleTextview);


        //set components settings
        settingController.retrieveUserGenreList(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                if(object != null) {
                    selectedGenreList = (List<String>) object;
                    Log.d("Test","=>"+selectedGenreList);
                }
                genrePreferenceAdapter = new GenrePreferenceAdapter(getContext(), genreList
                        , (ArrayList<String>) selectedGenreList, 1);
                genrePreferenceAdapter.notifyDataSetChanged();
                genrePreferenceRecyclerView.setAdapter(genrePreferenceAdapter);
            }
        });

        notification_toggle_text.setText("Be notified 30 minutes before the movie starts");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);

        settingController.retrieveAllowNotShowing(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                Boolean allowNotShowing = (Boolean) object;
                if(allowNotShowing){
                    allowRadioButton.setChecked(true);
                }
                else{
                    disallowRadioButton.setChecked(true);
                }
            }
        });

        allowRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingController.updateAllowNotShowing(true);
            }
        });

        disallowRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingController.updateAllowNotShowing(false);
            }
        });

        genrePreferenceRecyclerView.setLayoutManager(linearLayoutManager);
        genreController.retrieveGenres(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                genreList = (ArrayList<String>) object;
                genrePreferenceAdapter =new GenrePreferenceAdapter(getContext(),genreList
                        , (ArrayList<String>) selectedGenreList,1);
                genrePreferenceRecyclerView.setAdapter(genrePreferenceAdapter);
            }
        });

        genrePreferenceRecyclerView.setLayoutManager(linearLayoutManager);
        genrePreferenceRecyclerView.setAdapter(new GenrePreferenceAdapter(getContext(),genreList));

        return root;
    }

    public void checkSelectedGenreList(String genreName,boolean isSelected){
        if(isSelected){
            selectedGenreList.add(genreName);
        }else{
            selectedGenreList.remove(genreName);
        }
        settingController.updateGenreList(selectedGenreList);
    }

}
