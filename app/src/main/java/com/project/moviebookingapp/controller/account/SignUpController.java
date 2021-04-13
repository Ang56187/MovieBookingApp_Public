package com.project.moviebookingapp.controller.account;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import androidx.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.moviebookingapp.MainActivity;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.model.Account;
import com.project.moviebookingapp.model.RecommendedMovie;
import com.project.moviebookingapp.ui.account.SignUpActivity;

import java.util.Date;

import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateFormat;

public class SignUpController {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseFirestore mFirestore;
    private Context context;
    private Activity activity;
    private SignUpActivity signUpActivity;
    private SharedPreferences app_preferences;
    private Boolean isLoggedIn;

    //seperate the activity from firebase
    //more on business logic (manipulating models and server stuff)
    public SignUpController(Context context) {
        //firebase
        this.context = context;
        this.activity = (Activity) context;
        signUpActivity = (SignUpActivity) this.activity;

        mFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

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

    public void handleFirebaseSignUp(String name, String pass, String email,
                                     String gender, String phone, Date birthDate) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    Account account = new Account(user, name, email, gender, phone,
                            getLocalDateFormat().format(birthDate), new Timestamp(new Date()),
                            "user", "-");

                    mFirestore.collection("accounts").add(account)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    SharedPreferences.Editor editor = app_preferences.edit();
                                    editor.putString("role","user");
                                    editor.putString("email",email);
                                    editor.putString("accountID",user.getUid());
                                    editor.putString("userName",name);
                                    editor.putBoolean("isLoggedIn",true);
                                    editor.putString("docID",documentReference.getId());
                                    editor.commit();

                                    Toast.makeText(context, "Account created.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(context, MainActivity.class);
                                    intent.putExtra("user_name", name);
                                    context.startActivity(intent);
                                    activity.finish(); // once this is completed, there's no going back to this page from home page
                                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
                else {
                    try {
                        Toast.makeText(context, "Error creating account, please retry.", Toast.LENGTH_SHORT).show();
                        throw task.getException();
                    }
                    //if password too weak
                    catch (FirebaseAuthWeakPasswordException weakPassword) {
                        signUpActivity.passwordCheck();
                    }//conflicting emails
                    catch (FirebaseAuthUserCollisionException existEmail) {
                        signUpActivity.emailCheck();
                    }catch (FirebaseNetworkException e) {
                        Toast.makeText(context, "Network connection interrupted", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }//end else

            }//oncomplete
        });//complete sign in

    }//end handleFirebaseSignUp
}


//mFirestore.collection("recommendedMovies")
//            .document(user.getUid())
//            .set(new RecommendedMovie(user.getUid()))
//            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//
//                }
//            });
