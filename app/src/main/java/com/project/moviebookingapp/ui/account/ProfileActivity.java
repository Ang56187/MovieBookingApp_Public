package com.project.moviebookingapp.ui.account;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.ProfilePagerAdapter;
import com.project.moviebookingapp.controller.account.ProfileController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;

import java.io.IOException;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {
    //set request to upload image
    private final int PICK_IMAGE_REQUEST = 22;
    private Uri filePath;
    private SharedPreferences app_preferences;
    private ProfileController controller;

    private StorageReference storageReference;
    private FirebaseStorage mFireStorage;

    private ImageView userProfileImageView;
    private TextView editProfileTextView,topUpWalletTextView, userNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mFireStorage = FirebaseStorage.getInstance();
        storageReference = mFireStorage.getReference();

        controller = new ProfileController(this);
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //get widgets/layouts
        RelativeLayout logOutRelativeLayout = findViewById(R.id.logOutRelativeLayout);
        ViewPager profileViewPager = findViewById(R.id.profileViewPager);
        TabLayout profileTabLayout = findViewById(R.id.profileTabLayout);
        ImageButton profileBackButton = findViewById(R.id.profileBackButton);

        userProfileImageView = findViewById(R.id.userProfileImageView);//130*130
        ImageButton editUserProfileImageButton = findViewById(R.id.editUserProfileImageButton);

        userNameTextView = findViewById(R.id.userNameTextView);
        TextView walletTextView = findViewById(R.id.walletTextView);
        //clickable text views
        editProfileTextView = findViewById(R.id.editProfileTextView);
        topUpWalletTextView = findViewById(R.id.topUpWalletTextView);

        //set widgets/layouts settings
        final ProfilePagerAdapter profileAdapter = new ProfilePagerAdapter(getSupportFragmentManager(),
                profileTabLayout.getTabCount());
        profileViewPager.setAdapter(profileAdapter);
        profileViewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener
                (profileTabLayout));

        ////first tab set font to bold
        LinearLayout watchHistTabLayout = (LinearLayout)((ViewGroup) profileTabLayout.getChildAt(0)).getChildAt(0);
        TextView watchHistTextView = (TextView) watchHistTabLayout.getChildAt(1);
        watchHistTextView.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.roboto_bold));

        profileTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                LinearLayout tabLayout = (LinearLayout)((ViewGroup) profileTabLayout.getChildAt(0)).getChildAt(tab.getPosition());
                TextView tabTextView = (TextView) tabLayout.getChildAt(1);

                profileViewPager.setCurrentItem(tab.getPosition());
                tabTextView.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.roboto_bold));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                LinearLayout tabLayout = (LinearLayout)((ViewGroup) profileTabLayout.getChildAt(0)).getChildAt(tab.getPosition());
                TextView tabTextView = (TextView) tabLayout.getChildAt(1);
                tabTextView.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.roboto_light));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        //set texts
        userNameTextView.setText(app_preferences.getString("userName"," "));

        //set image through Glide
        //retrieve from firebase
        StorageReference imageRef = mFireStorage.getReference(
                app_preferences.getString("profileImgURL"," "));
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(getApplicationContext()).load(imageURL)
                        .override(120,120)
                        .into(userProfileImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("Error", "Firebase storage error: " + exception);
            }
        });

        ////set onclick
        editUserProfileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.chooseImage(PICK_IMAGE_REQUEST);
            }
        });

        logOutRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.signOut();
            }
        });

        editProfileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),EditProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        profileBackButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onStart(){
        super.onStart();
        userNameTextView.setText(app_preferences.getString("userName"," "));
    }

    // to upload images
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            // Get the Uri of data
            filePath = data.getData();
            controller.uploadImage(filePath, new OnFirebaseCallback() {
                @Override
                public void firebaseCallBack(Object object) {
                    String imageURL = (String) object;
                    //set image through Glide
                    //retrieve from firebase
                    StorageReference imageRef = mFireStorage.getReference(imageURL);
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageURL = uri.toString();
                            Glide.with(getApplicationContext()).load(imageURL)
                                    .override(120,120)
                                    .into(userProfileImageView);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d("Error", "Firebase storage error: " + exception);
                        }
                    });
                }
            });
        }
    }


}
