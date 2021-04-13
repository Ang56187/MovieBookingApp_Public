package com.project.moviebookingapp.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.controller.account.ProfileController;

public class AdminHomeActivity extends AppCompatActivity {
    private ImageButton qrScannerImageButton;
    private RelativeLayout logOutRelativeLayout;
    private Button goToRevenueButton;

    private ProfileController profileController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        //set controller
        profileController = new ProfileController(this);

        //get widgets/components
        qrScannerImageButton = findViewById(R.id.qrScannerImageButton);
        logOutRelativeLayout = findViewById(R.id.logOutRelativeLayout);
        goToRevenueButton = findViewById(R.id.goToRevenueButton);

        //logging out
        logOutRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileController.signOut();
            }
        });

        qrScannerImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),QRScannerActivity.class);
                startActivity(intent);
            }
        });

        goToRevenueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),AdminRevenueActivity.class);
                startActivity(intent);
            }
        });

    }
}
