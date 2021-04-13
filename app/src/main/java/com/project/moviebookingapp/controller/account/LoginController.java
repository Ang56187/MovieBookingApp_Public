package com.project.moviebookingapp.controller.account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.moviebookingapp.ui.account.LoginActivity;
import com.project.moviebookingapp.ui.account.SignUpActivity;
import com.project.moviebookingapp.ui.admin.AdminHomeActivity;
import com.project.moviebookingapp.ui.home.HomeActivity;

public class LoginController {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseFirestore mFirestore;
    private Context context;
    private Activity activity;
    private SignUpActivity signUpActivity;
    private SharedPreferences app_preferences;
    private LoginActivity loginActivity;

    public LoginController(Context context){
        this.context = context;
        this.activity = (LoginActivity)context;
        this.loginActivity = (LoginActivity) activity;

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
    }//end constructor

    public void handleFirebaseSignIn(String email,String pass){
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> outerTask) {
                if (outerTask.isSuccessful()) {

                    mFirestore.collection("accounts")
                            .whereEqualTo("accountId",outerTask.getResult().getUser().getUid())
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                                DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                                SharedPreferences.Editor editor = app_preferences.edit();
                                editor.putString("role",doc.getString("role"));
                                editor.putString("email",email);
                                editor.putBoolean("isLoggedIn",true);
                                editor.putString("accountID",outerTask.getResult().getUser().getUid());
                                editor.putString("userName",doc.getString("userName"));
                                editor.putString("profileImgURL", doc.getString("profileImgURL"));
                                editor.putString("docID",doc.getId());

                                editor.commit();

                                Toast.makeText(context, "Signing in", Toast.LENGTH_LONG).show();
                                if(doc.getString("role").equals("user")) {
                                    context.startActivity(new Intent(context,HomeActivity.class));
                                }
                                else if(doc.getString("role").equals("admin")){
                                    context.startActivity(new Intent(context,AdminHomeActivity.class));

                                }
                                //context.startActivity(new Intent(context,HomeActivity.class));
                                activity.finish();
                            }
                        }
                    });
                }
                else{
                    try {
                        throw outerTask.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        loginActivity.emailCheck();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        loginActivity.passwordCheck();
                    } catch (FirebaseNetworkException e) {
                        Toast.makeText(context, "Network connection interrupted.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
