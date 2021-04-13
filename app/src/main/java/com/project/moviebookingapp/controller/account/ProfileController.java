package com.project.moviebookingapp.controller.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Account;
import com.project.moviebookingapp.ui.account.EditProfileActivity;
import com.project.moviebookingapp.ui.account.LoginActivity;
import com.project.moviebookingapp.ui.account.ProfileActivity;

import java.util.Map;

public class ProfileController {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private Context context;
    private Activity activity;
    private ProfileActivity profileActivity;
    private SharedPreferences app_preferences;

    private StorageReference storageReference;
    private FirebaseStorage mFireStorage;
    private FirebaseFirestore mFirestore;
    private FirebaseUser user;

    public ProfileController(Context context){
        this.context = context;
        this.activity = (Activity) context;
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mFireStorage = FirebaseStorage.getInstance();
        storageReference = mFireStorage.getReference();
        mFirestore = FirebaseFirestore.getInstance();

        //set sharedpreference
        app_preferences = PreferenceManager.getDefaultSharedPreferences(context);

        //check firebase status
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //signed in
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(context, "Internet connection unavailable.", Toast.LENGTH_LONG).show();
                }
            }
        };

    }

    public void signOut(){
        auth.signOut();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O){
            ActivityCompat.finishAffinity(activity);
        }else{
            activity.finishAffinity();
        }
        activity.startActivity(intent);

        SharedPreferences.Editor editor = app_preferences.edit();
        editor.clear();
        editor.putBoolean("isLoggedIn",false);
        editor.commit();

        for(Map.Entry<String,?> entry : app_preferences.getAll().entrySet()){
            Log.d("map values",entry.getKey() + ": " + entry.getValue().toString());
        }
    }

    public void chooseImage(int requestInt) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"),requestInt);
    }

    // UploadImage method
    public void uploadImage(Uri filePath, OnFirebaseCallback callback)
    {
        if (filePath != null) {
            String accountId = app_preferences.getString("accountID"," ");

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref = storageReference.child("profiles/" + accountId);

            ref.putFile(filePath).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            progressDialog.dismiss();
                            Toast.makeText(context, "Image uploaded.",
                                    Toast.LENGTH_SHORT).show();
                            //update preferences
                            SharedPreferences.Editor editor = app_preferences.edit();
                            String imageURL = ref.getPath();
                            editor.putString("profileImgURL", imageURL);
                            editor.commit();

                            //update firebase
                            mFirestore.collection("accounts").document(
                                    app_preferences.getString("docID"," "))
                                    .update("profileImgURL",imageURL).addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFirestore.collection("ratings")
                                                    .whereEqualTo("accountID",accountId)
                                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if(task.isSuccessful()){
                                                                for(QueryDocumentSnapshot doc: task.getResult()){
                                                                    if(doc.getString("profileImgURL") == null
                                                                    || !doc.getString("profileImgURL")
                                                                            .contains("/profiles/")){
                                                                        mFirestore.collection("ratings")
                                                                                .document(doc.getId())
                                                                                .update("profileImgURL",imageURL);
                                                                    }

                                                                }//end loop
                                                                callback.firebaseCallBack(imageURL);
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Failed " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(
                    new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()
                                    / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int)progress + "%"); }
                    });
        }
    }//end uploadimage

    //edit profile by retrieving info on user first
    public void retrieveUserInfo(OnFirebaseCallback callback){
        mFirestore.collection("accounts").document(
                app_preferences.getString("docID"," ")).get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot doc = task.getResult();
                        Account account = new Account(doc.getString("userName"),
                                doc.getString("gender"),
                                doc.getString("phoneNo"),
                                doc.getString("birthDate"));
                        callback.firebaseCallBack(account);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"Error getting user documents:"+e.toString()
                        ,Toast.LENGTH_LONG).show();
            }
        });
    }

    public void updateUserProfile(Account account, String oldPass, String newPass){
        //update user info first
        mFirestore.collection("accounts").document(
                app_preferences.getString("docID","")).update(
                        "userName",account.getUserName(),
                "birthDate",account.getBirthDate(),
                "phoneNo",account.getPhoneNo(),
                "gender",account.getGender()
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //update shared prefs
                    SharedPreferences.Editor editor = app_preferences.edit();
                    editor.putString("userName",account.getUserName());
                    editor.commit();

                    if(!oldPass.isEmpty() && !newPass.isEmpty()) {
                        //then update the password
                        AuthCredential credential = EmailAuthProvider
                                .getCredential(app_preferences.getString("email", ""), oldPass);
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updatePassword(newPass).addOnCompleteListener(
                                            new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(context, "Password updated",
                                                    Toast.LENGTH_LONG).show();
                                            activity.onBackPressed();
                                            activity.overridePendingTransition(
                                                    R.anim.slide_in_left, R.anim.slide_out_right);
                                        }
                                    });
                                } else {
                                    Toast.makeText(context, "Authentication failed",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else{
                        activity.onBackPressed();
                        activity.overridePendingTransition(
                                R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                }
                else{
                    try {
                        Toast.makeText(context, "Error updating account, please retry.",
                                Toast.LENGTH_SHORT).show();
                        throw task.getException();
                    }
                    catch (FirebaseNetworkException e) {
                    Toast.makeText(context, "Network connection interrupted",
                            Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }//end else
            }
        });

    }

}
