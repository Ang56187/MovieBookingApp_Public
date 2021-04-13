package com.project.moviebookingapp.ui.account;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.controller.account.LoginController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.project.moviebookingapp.custom.EditTextFormat.isValidEmail;

public class LoginActivity extends AppCompatActivity {
    private Button goToSignUpButton;
    private Button signInButton;
    private EditText signInPassEditText;
    private EditText signInEmailEditText;
    private TextView emailErrorTextView;
    private TextView passErrorTextView;
    private LoginController controller;
    private TextView forgotPassTextView;

    private FirebaseFunctions mFunctions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //get widgets/layouts
        goToSignUpButton = findViewById(R.id.goToSignUpButton);
        signInButton = findViewById(R.id.signInButton);
        signInEmailEditText = findViewById(R.id.signInEmailEditText);
        signInPassEditText = findViewById(R.id.signInPassEditText);
        emailErrorTextView = findViewById(R.id.emailErrorTextView);
        passErrorTextView = findViewById(R.id.passErrorTextView);
        forgotPassTextView = findViewById(R.id.forgotPassTextView);

        controller = new LoginController(this);

        //set widgets/layouts settings
        goToSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = signInEmailEditText.getText().toString();
                String pass = signInPassEditText.getText().toString();
                Boolean isPassed=  true;

                //email
                if( !isValidEmail(email)){
                    isPassed = false;
                    emailErrorTextView.setText("Invalid email.");
                    emailErrorTextView.setVisibility(View.VISIBLE);
                    signInEmailEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                            PorterDuff.Mode.SRC_ATOP);
                }
                else{
                    emailErrorTextView.setText("");
                    emailErrorTextView.setVisibility(View.GONE);
                    signInEmailEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorFreeColor),
                            PorterDuff.Mode.SRC_ATOP);
                }

                //password
                if(pass.isEmpty() || pass.contains(" ") || pass.length() < 6){
                    isPassed = false;
                    passErrorTextView.setVisibility(View.VISIBLE);
                    signInPassEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                            PorterDuff.Mode.SRC_ATOP);
                    if (pass.isEmpty()){
                        passErrorTextView.setText("Password field cannot be empty");
                    }
                    else if (pass.contains(" ")){
                        passErrorTextView.setText("Spaces are not allowed");
                    }
                    else if (pass.length() < 6){
                        passErrorTextView.setText("Password should be at least 6 characters");
                    }
                }
                else{
                    signInPassEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorFreeColor),
                            PorterDuff.Mode.SRC_ATOP);
                    passErrorTextView.setVisibility(View.GONE);
                    passErrorTextView.setText("");
                }

                if(isPassed){
                    controller.handleFirebaseSignIn(email,pass);
                }


            }//end onclick
        });


        forgotPassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"It doesnt work, i didnt include the functionality",Toast.LENGTH_LONG).show();
            }
        });

    }//end oncreate

    public void emailCheck(){
        emailErrorTextView.setText("Invalid email.");
        emailErrorTextView.setVisibility(View.VISIBLE);
        signInEmailEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                PorterDuff.Mode.SRC_ATOP);
    }

    public void passwordCheck(){
        passErrorTextView.setText("Invalid password.");
        passErrorTextView.setVisibility(View.VISIBLE);
        signInPassEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                PorterDuff.Mode.SRC_ATOP);
    }

    private void testHTTP() {

        // Instantiate the RequestQueue.
//        RequestQueue queue = Volley.newRequestQueue(this);
//        String url ="https://asia-southeast2-moviebookingapp-4414b.cloudfunctions.net/python_recommendation";
//        JsonObjectRequest request = new JsonObjectRequest(url, null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        String s = null;
//                        try {
//                            s = response.getString("data");
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
//                            Log.d("Test","Success=>"+s);
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_LONG).show();
//                Log.d("Test","Error=>"+error.toString());
//                error.printStackTrace();
//
//            }
//        });
//        queue.add(request);
    }
    //    try {
    //        //handle your response
    //    } catch (JSONException e) {
    //        e.printStackTrace();
    //    }
}
