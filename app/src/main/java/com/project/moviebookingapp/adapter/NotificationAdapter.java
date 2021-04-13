package com.project.moviebookingapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.project.moviebookingapp.R;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private ArrayList<String> notificationDataset;
    private LayoutInflater mInflater;
    private Context context;

    // Constructor
    public NotificationAdapter(Context context, ArrayList<String> nDataset) {
        notificationDataset = nDataset;
        this.mInflater = LayoutInflater.from(context);
    }

    //get components from the view where recyclerview resides
    public static class NotificationViewHolder extends RecyclerView.ViewHolder{
        private TextView notificationDescTextView;
        private TextView notificationTimeTextView;

        public NotificationViewHolder(View v){
            super(v);
            notificationDescTextView = v.findViewById(R.id.notificationDescTextView);
            notificationTimeTextView = v.findViewById(R.id.notificationTimeTextView);
        }
    }

    // obtain view where recyclerview resides in
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.component_notification_item, parent, false);
        return new NotificationAdapter.NotificationViewHolder(view);
    }

    // set values for contents of a view
    @Override
    public void onBindViewHolder(NotificationAdapter.NotificationViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.notificationDescTextView.setText(notificationDataset.get(position));

    }

    // Return the size of your dataset
    @Override
    public int getItemCount() {
        return notificationDataset.size();
    }

}
