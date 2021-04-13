package com.project.moviebookingapp.ui.account;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
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
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.controller.account.ProfileController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Account;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.project.moviebookingapp.custom.EditTextFormat.isValidEmail;
import static com.project.moviebookingapp.custom.EditTextFormat.isValidPhone;
import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateFormat;
import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateTime;

public class EditProfileActivity  extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private int defaultYear = 2000;
    private int defaultMonth = 1;
    private int defaultDay = 1;
    //edit texts
    private EditText nameEditText,emailEditText,phoneNoEditText,passEditText,confirmPassEditText;
    //error texts
    private TextView nameErrorTextView, emailErrorTextView, phoneErrorTextView, passErrorTextView ,
            confirmPassErrorTextView,dateErrorTextView;
    //header
    private TextView signUpTextView,emailSignUpTextView;
    private Button saveChangesButton,birthDatePickerButton;
    private RadioGroup genderRadioGroup;
    private Date currentDate = new Date();
    private Date birthDate = new Date();

    public ProfileController profileController;
    private SharedPreferences app_preferences;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        profileController = new ProfileController(this);
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //firebase
        auth = FirebaseAuth.getInstance();

        //check firebase status
        authListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                //signed in
                if(firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(EditProfileActivity.this, "Internet connection unavailable.", Toast.LENGTH_LONG).show();
                }
            }
        };

        mFirestore = FirebaseFirestore.getInstance();


        mFirestore = FirebaseFirestore.getInstance();

        //get widgets/layouts
        Toolbar toolbar = (Toolbar) findViewById(R.id.signUpToolbar);
        emailSignUpTextView = findViewById(R.id.emailSignUpTextView);
        signUpTextView = findViewById(R.id.signUpTextView);
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
        saveChangesButton = findViewById(R.id.signUpButton);

        //for both gender radio buttons
        RadioButton femaleRadioButton = findViewById(R.id.femaleRadioButton);
        RadioButton maleRadioButton = findViewById(R.id.maleRadioButton);

        //set widgets/layouts settings
        //change headers
        signUpTextView.setText("Edit profile");
        saveChangesButton.setText("Save changes");
        //hide some stuff
        emailEditText.setVisibility(View.GONE);
        emailSignUpTextView.setVisibility(View.GONE);

        profileController.retrieveUserInfo(new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                Account account = (Account) object;
                int defaultDay = Integer.parseInt(account.getBirthDate().substring(8,10));
                int defaultMonth = Integer.parseInt(account.getBirthDate().substring(5,7));
                int defaultYear = Integer.parseInt(account.getBirthDate().substring(0,4));

                //set for birthdate
                ////set date as string
                StringBuilder date =  new StringBuilder()
                        .append(defaultDay<10? "0"+defaultDay :defaultDay).append("/")
                        .append(defaultMonth<10 ? "0"+(defaultMonth): defaultMonth).append("/")
                        .append(defaultYear);
                birthDatePickerButton.setText(date.toString());

                birthDatePickerButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        showDatePicker(defaultYear,defaultMonth-1,defaultDay);
                    }
                });

                //set for gender
                switch(account.getGender()){
                    case "male":
                        maleRadioButton.setChecked(true);
                        break;
                    case "female":
                        femaleRadioButton.setChecked(true);
                        break;
                    default:
                        femaleRadioButton.setChecked(true);
                        break;
                }

                phoneNoEditText.setText(account.getPhoneNo());
                nameEditText.setText(account.getUserName());

                saveChangesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RadioButton selectedGenderRadioButton = findViewById(
                                genderRadioGroup.getCheckedRadioButtonId());

                        String name = nameEditText.getText().toString();
                        String phone = phoneNoEditText.getText().toString();
                        String oldPass = passEditText.getText().toString();
                        String newPass = confirmPassEditText.getText().toString();
                        String gender = selectedGenderRadioButton.getText().toString();

                        checkSaveChanges(name,gender,phone,oldPass,newPass);
                    }
                });//end update click

            }
        });

        //set changes to toolbar
        toolbar.setNavigationIcon(R.drawable.img_back_30);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

    }//end on create

    //functions as way for controller to tell view what to update
    public void passwordCheck(){
        passErrorTextView.setVisibility(View.VISIBLE);
        passErrorTextView.setText("Password is too weak");
        passEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                PorterDuff.Mode.SRC_ATOP);
    }

    private void showDatePicker(int year, int month, int day){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,R.style.datepicker,this,
                year,
                month,
                day
        );
        datePickerDialog.show();
    }

    //check submissions detail before saving it to firebase
    private void checkSaveChanges(String name,String gender,String phone,String oldPass,
                                  String newPass){
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

        //old password
        passEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorFreeColor),
                PorterDuff.Mode.SRC_ATOP);
        passErrorTextView.setVisibility(View.GONE);
        passErrorTextView.setText("");


        //change new password
        if(!oldPass.isEmpty() && (newPass.isEmpty() || newPass.contains(" ") || newPass.length() < 6)){
            isPassed = false;
            confirmPassErrorTextView.setVisibility(View.VISIBLE);
            confirmPassEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                    PorterDuff.Mode.SRC_ATOP);
            if (newPass.isEmpty()){
                confirmPassErrorTextView.setText("Password field cannot be empty");
            }
            else if (newPass.contains(" ")){
                confirmPassErrorTextView.setText("Spaces are not allowed");
            }
            else if (newPass.length() < 6){
                confirmPassErrorTextView.setText("Password should be at least 6 characters");
            }
        }
        else{
            confirmPassErrorTextView.setVisibility(View.GONE);
            confirmPassErrorTextView.setText("");
            confirmPassEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorFreeColor),
                    PorterDuff.Mode.SRC_ATOP);
        }

        //send to firebase
        if(isPassed){
            Account account = new Account(name,gender,phone,getLocalDateFormat().format(birthDate));
            profileController.updateUserProfile(account,oldPass,newPass);
        }//end if

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




}