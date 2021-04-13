package com.project.moviebookingapp.ui.home;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.NotificationAdapter;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {
    private ArrayList<String> notificationList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        //get widgets/layouts
        RecyclerView notificationRecylerView = findViewById(R.id.notificationRecylerView);
        ImageButton notifcationBackButton = findViewById(R.id.notificationBackButton);

        //set widgets/layouts settings

        notificationList.add("3322222222222222\n22222222222222222\n23423234234\n22222222222223");
        notificationList.add("bbbbbbbbb");
        notificationList.add("aaaaaa");
        notificationList.add("bbbbbbbbb");
        notificationList.add("aaaaaa");
        notificationList.add("bbbbbbbbb");
        notificationList.add("aaaaaa");
        notificationList.add("bbbbbbbbb");
        notificationList.add("aaaaaa");
        notificationList.add("bbbbbbbbb");

        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(notificationRecylerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);

        notificationRecylerView.setAdapter(new NotificationAdapter(this,notificationList));
        notificationRecylerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        notificationRecylerView.addItemDecoration(horizontalDecoration);

        notifcationBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }//end oncreate

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
