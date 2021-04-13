package com.project.moviebookingapp.adapter;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.moviebookingapp.R;
import com.project.moviebookingapp.listener.OnGenreToggleListener;

import java.util.ArrayList;

public class GenrePreferenceAdapter extends RecyclerView.Adapter<GenrePreferenceAdapter.GenreViewHolder> {
    private ArrayList<String> genreDataset;
    private ArrayList<String> selectedGenreDataSet;
    private OnGenreToggleListener listener;
    private LayoutInflater mInflater;
    private int height;
    private int width;
    private Context context;
    private int type;


    // Constructor
    public GenrePreferenceAdapter(Context context, ArrayList<String> mDataset) {
        genreDataset = mDataset;
        this.mInflater = LayoutInflater.from(context);
    }
    //overlapped constructor
    public GenrePreferenceAdapter(Context context, ArrayList<String> mDataset,
                                  ArrayList<String> selectedGenreDataSet,int type) {
        genreDataset = mDataset;
        this.mInflater = LayoutInflater.from(context);
        this.selectedGenreDataSet = selectedGenreDataSet;
        this.listener = (OnGenreToggleListener)context;
        this.type = type;
    }

    public static class GenreViewHolder extends RecyclerView.ViewHolder{
        private TextView textView;
        private SwitchCompat settingToggleSwitch;

        public GenreViewHolder(View v){
            super(v);
            textView = v.findViewById(R.id.settingToggleTextview);
            settingToggleSwitch = v.findViewById(R.id.settingToggleSwitch);
        }
    }


    // Create new views
    @Override
    public GenrePreferenceAdapter.GenreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.component_settings_toggle, parent, false);
        return new GenreViewHolder(view);
    }

    // set values for contents of a view
    @Override
    public void onBindViewHolder(GenreViewHolder holder, int position) {
        holder.textView.setText(genreDataset.get(position));

        if(selectedGenreDataSet.contains(genreDataset.get(position))){
            holder.settingToggleSwitch.setChecked(true);
        }
        else{
            holder.settingToggleSwitch.setChecked(false);
        }

        holder.settingToggleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.genreToggleCallback(type,genreDataset.get(position),
                        holder.settingToggleSwitch.isChecked());
            }
        });

    }


    // Return the size of your dataset
    @Override
    public int getItemCount() {
        return genreDataset.size();
    }
}
