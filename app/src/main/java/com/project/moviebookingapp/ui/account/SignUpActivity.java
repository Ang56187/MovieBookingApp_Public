package com.project.moviebookingapp.ui.account;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.moviebookingapp.MainActivity;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.controller.account.SignUpController;
import com.project.moviebookingapp.model.Account;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.project.moviebookingapp.custom.EditTextFormat.isValidEmail;
import static com.project.moviebookingapp.custom.EditTextFormat.isValidPhone;


public class SignUpActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private int defaultYear = 2000;
    private int defaultMonth = 1;
    private int defaultDay = 1;
    //edit texts
    private EditText nameEditText,emailEditText,phoneNoEditText,passEditText,confirmPassEditText;
    //error texts
    private TextView nameErrorTextView, emailErrorTextView, phoneErrorTextView, passErrorTextView ,
            confirmPassErrorTextView,dateErrorTextView;
    private Button signUpButton,birthDatePickerButton;
    private RadioGroup genderRadioGroup;
    private Date currentDate = new Date();
    private Date birthDate = new Date();

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseFirestore mFirestore;

    private SignUpController controller;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //set up controller
        controller = new SignUpController(this);

        //firebase
        auth = FirebaseAuth.getInstance();

        //check firebase status
        authListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                //signed in
                if(firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(SignUpActivity.this, "Internet connection unavailable.", Toast.LENGTH_LONG).show();
                }
            }
        };

        mFirestore = FirebaseFirestore.getInstance();

        //get components
        Toolbar toolbar = (Toolbar) findViewById(R.id.signUpToolbar);
        ////for edit text section
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneNoEditText = findViewById(R.id.phoneNoEditText);
        passEditText = findViewById(R.id.passEditText);
        confirmPassEditText = findViewById(R.id.confirmPassEditText);
        ////for error text
        nameErrorTextView = findViewById(R.id.nameErrorTextView);
        emailErrorTextView = findViewById(R.id.emailErrorTextView);
        phoneErrorTextView = findViewById(R.id.phoneErrorTextView);
        passErrorTextView = findViewById(R.id.passErrorTextView);
        confirmPassErrorTextView = findViewById(R.id.confirmPassErrorTextView);
        dateErrorTextView = findViewById(R.id.dateErrorTextView);

        birthDatePickerButton = findViewById(R.id.birthDatePickerButton);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        signUpButton = findViewById(R.id.signUpButton);

        RadioButton femaleRadioButton = findViewById(R.id.femaleRadioButton);

        //set settings to widgets layouts
        ////set for female radio button as true
        femaleRadioButton.setChecked(true);

        ////set date as string
        StringBuilder date =  new StringBuilder()
                .append(defaultDay<10? "0"+defaultDay :defaultDay).append("/")
                .append(defaultMonth+1<10 ? "0"+(defaultMonth+1): defaultMonth+1).append("/")
                .append(defaultYear);
        birthDatePickerButton.setText(date.toString());

        //set changes to toolbar
        toolbar.setNavigationIcon(R.drawable.img_back_30);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        birthDatePickerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDatePicker();
            }
        });

        //set UI/presentation logic here
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton selectedGenderRadioButton = findViewById(genderRadioGroup.getCheckedRadioButtonId());

                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String phone = phoneNoEditText.getText().toString();
                String pass = passEditText.getText().toString();
                String confirmPass = confirmPassEditText.getText().toString();
                String gender = selectedGenderRadioButton.getText().toString();

                boolean isPassed = true;

                //name
                if(name.isEmpty() || name.equals(" ") || name.length()>21){
                    isPassed = false;
                    if(name.length()>21){
                        nameErrorTextView.setText("Must be at most 20 characters.");
                    }
                    else{
                        nameErrorTextView.setText("Please enter name.");
                    }
                    nameErrorTextView.setVisibility(View.VISIBLE);
                    nameEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                            PorterDuff.Mode.SRC_ATOP);
                }else{
                    nameErrorTextView.setText("");
                    nameErrorTextView.setVisibility(View.GONE);
                    nameEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorFreeColor),
                            PorterDuff.Mode.SRC_ATOP);
                }

                //birth date
                SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    birthDate = sdformat.parse(birthDatePickerButton.getText().toString());
                    if(birthDate.after(currentDate)){
                        isPassed = false;
                        dateErrorTextView.setText("You cant be born in the future.");
                        dateErrorTextView.setVisibility(View.VISIBLE);
                    }
                    else{
                        dateErrorTextView.setText("");
                        dateErrorTextView.setVisibility(View.GONE);
                    }
                } catch (ParseException dtpe) {
                    Toast.makeText(getApplicationContext(), "Invalid date entered", Toast.LENGTH_SHORT).show();
                }

                //email
                if( !isValidEmail(email)){
                    isPassed = false;
                    emailErrorTextView.setText("Invalid email.");
                    emailErrorTextView.setVisibility(View.VISIBLE);
                    emailEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                            PorterDuff.Mode.SRC_ATOP);
                }
                else{
                    emailErrorTextView.setText("");
                    emailErrorTextView.setVisibility(View.GONE);
                    emailEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorFreeColor),
                            PorterDuff.Mode.SRC_ATOP);
                }

                //phone number
                if(phone.isEmpty() || isValidPhone(phone)){
                    isPassed = false;
                    phoneErrorTextView.setText("Invalid phone number.");
                    phoneErrorTextView.setVisibility(View.VISIBLE);
                    phoneNoEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                            PorterDuff.Mode.SRC_ATOP);
                }
                else{
                    phoneErrorTextView.setText("");
                    phoneErrorTextView.setVisibility(View.GONE);
                    phoneNoEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorFreeColor),
                            PorterDuff.Mode.SRC_ATOP);
                }

                //password
                if(pass.isEmpty() || pass.contains(" ") || pass.length() < 6){
                    isPassed = false;
                    passErrorTextView.setVisibility(View.VISIBLE);
                    passEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
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
                    passEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorFreeColor),
                            PorterDuff.Mode.SRC_ATOP);
                    passErrorTextView.setVisibility(View.GONE);
                    passErrorTextView.setText("");
                }

                //confirm password
                if(confirmPass.isEmpty() || !confirmPass.equals(pass)){
                    isPassed = false;
                    confirmPassErrorTextView.setVisibility(View.VISIBLE);
                    confirmPassErrorTextView.setText("Please enter the correct password.");
                    confirmPassEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                            PorterDuff.Mode.SRC_ATOP);
                }
                else{
                    confirmPassErrorTextView.setVisibility(View.GONE);
                    confirmPassErrorTextView.setText("");
                    confirmPassEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorFreeColor),
                            PorterDuff.Mode.SRC_ATOP);
                }

                //send to firebase
                if(isPassed){
                    controller.handleFirebaseSignUp(name,pass,email,gender,phone,birthDate);
                }//end if

            }
        });//end listener

    }//end oncreate

    //functions as way for controller to tell view what to update
    public void passwordCheck(){
        passErrorTextView.setVisibility(View.VISIBLE);
        passErrorTextView.setText("Password is too weak");
        passEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                PorterDuff.Mode.SRC_ATOP);
    }
    public void emailCheck(){
        emailErrorTextView.setVisibility(View.VISIBLE);
        emailErrorTextView.setText("Email have been used");
        emailEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                PorterDuff.Mode.SRC_ATOP);
    }


    private void showDatePicker(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,R.style.datepicker,this,
                defaultYear,
                defaultMonth,
                defaultDay
                );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker v, int year, int month, int day){
        defaultYear = year;
        defaultMonth = month;
        defaultDay = day;

        StringBuilder date =  new StringBuilder()
                .append(defaultDay<10? "0"+defaultDay :defaultDay).append("/")
                .append(defaultMonth+1<10 ? "0"+(defaultMonth+1):defaultMonth+1).append("/")
                .append(defaultYear);
        birthDatePickerButton.setText(date.toString());
    }



    //animation when default back button tapped
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}
