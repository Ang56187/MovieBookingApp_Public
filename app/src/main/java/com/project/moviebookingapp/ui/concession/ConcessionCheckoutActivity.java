package com.project.moviebookingapp.ui.concession;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.ConcessionCheckoutItemAdapter;
import com.project.moviebookingapp.controller.concession.ConcessionController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Concession;
import com.project.moviebookingapp.ui.movie.MoviePaymentCompletedActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import static com.project.moviebookingapp.custom.CustomAnimations.getRippleBackgroundDrawable;
import static com.project.moviebookingapp.custom.CustomAnimations.transparentClickAnim;
import static com.project.moviebookingapp.custom.EditTextFormat.concatString;
import static com.project.moviebookingapp.custom.EditTextFormat.getDigitArray;
import static com.project.moviebookingapp.custom.EditTextFormat.isInputCorrect;
import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateTime;
import static com.project.moviebookingapp.custom.TextViewFormat.retrieveLocalCurrencyFormat;

public class ConcessionCheckoutActivity extends AppCompatActivity {
    private ArrayList<Concession> concessionList = new ArrayList<>();
    private String ticketID;
    private String movieName;
    private long showtimeSeconds;
    private String totalConcessionPriceString;

    private boolean isSlash = false;
    private boolean isDelete = false;
    //for expiry date checking
    private Date todayDate = new Date();
    private Date expiryDate = new Date();

    private double totalConcessionPrice = 0.0;

    //to check card number
    private final int cardNoTotalLength = 19; // size of pattern 0000-0000-0000-0000
    private final int cardNoTotalDigits = 16; // max numbers of digits in pattern: 0000 x 4
    private final int cardNoDivideModulo = 5; // means divider position is every 5th symbol beginning with 1
    private final int cardNoDividerPosition = cardNoDivideModulo - 1; // means divider position is every 4th symbol beginning with 0
    private final char cardNoDivider = ' ';

    private ConcessionController concessionController;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        Intent intent = getIntent();//get intent from previous activity(MovieDetailActivity)
        concessionList = (ArrayList<Concession>) intent.getSerializableExtra("concessionList");
        ticketID = intent.getStringExtra("ticketID");
        movieName = intent.getStringExtra("movieName");
        showtimeSeconds = intent.getLongExtra("showtime",0);

        concessionController = new ConcessionController(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concession_checkout);


        //get widgets/layouts
        ////text views
        View concessionListLayout = findViewById(R.id.concessionListLayout);
        TextView movieTitleTextView = findViewById(R.id.movieTitleTextView);
        TextView movieDateTimeTextView = findViewById(R.id.movieDateTimeTextView);

        LinearLayout paymentOptionLinearLayout = findViewById(R.id.paymentOptionLayout);
        RelativeLayout concessionCheckoutBottomRelativeLayout = findViewById(R.id.concessionCheckoutBottomRelativeLayout);
        ImageButton concessionCheckoutForwardButton = findViewById(R.id.concessionCheckoutForwardButton);
        ImageButton concessionCheckoutBackButton = findViewById(R.id.concessionCheckoutBackButton);

        ////components within included layout
        ////// concessionListLayout
        TextView totalConcessionPriceTextView = concessionListLayout.findViewById(R.id.totalConcessionPriceTextView);
        RecyclerView concessionCheckoutItemRecyclerView = concessionListLayout.findViewById(R.id.concessionCheckoutItemRecyclerView);

        //////paymentOptionLinearLayout
        RadioButton walletRadioButton = paymentOptionLinearLayout.findViewById(R.id.walletRadioButton);
        RadioButton creditCardRadioButton = paymentOptionLinearLayout.findViewById(R.id.creditCardRadioButton);
        RelativeLayout cardInputRelativeLayout = paymentOptionLinearLayout.findViewById(R.id.cardInputRelativeLayout);
        ////errors
        TextView cardNoErrorTextView = paymentOptionLinearLayout.findViewById(R.id.cardNoErrorTextView);
        TextView expiryDateErrorTextView = paymentOptionLinearLayout.findViewById(R.id.expiryDateErrorTextView);
        TextView cvvErrorTextView = paymentOptionLinearLayout.findViewById(R.id.cvvErrorTextView);

        EditText expiryDateEditText = paymentOptionLinearLayout.findViewById(R.id.expiryDateEditText);
        EditText cvvEditText = paymentOptionLinearLayout.findViewById(R.id.cvvEditText);
        EditText cardNoEditText = paymentOptionLinearLayout.findViewById(R.id.cardNoEditText);

        //set settings for widgets/layouts
        //set text
        movieTitleTextView.setText(movieName);
        movieDateTimeTextView.setText(getLocalDateTime().format(new Date(showtimeSeconds*1000)));

        concessionCheckoutItemRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        concessionCheckoutItemRecyclerView.setAdapter(new ConcessionCheckoutItemAdapter(this,concessionList));

        for(Concession c: concessionList){
            totalConcessionPrice += (c.getConcessionPrice()*c.getQuantity());
        }
        totalConcessionPriceString = retrieveLocalCurrencyFormat(totalConcessionPrice);
        totalConcessionPriceTextView.setText(totalConcessionPriceString);

        ////set for payment options
        ////set for edit texts
        cardNoEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                switch(i){
                    case KeyEvent.KEYCODE_DEL:
                        isDelete = true;
                        break;
                    default:
                        isDelete= false;
                        break;
                }
                return false;
            }
        });

        cardNoEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!isInputCorrect(s, cardNoTotalLength, cardNoDivideModulo, cardNoDivider)) {
                            s.replace(0, s.length(), concatString(getDigitArray(s, cardNoTotalDigits), cardNoDividerPosition, cardNoDivider));
                        }
                        checkEditTextLength(cardNoEditText,expiryDateEditText,cvvEditText,concessionCheckoutBottomRelativeLayout,concessionCheckoutForwardButton);
                    }
                }
        );


        expiryDateEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int removed, int added) { }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String mLastInput = "";

                        SimpleDateFormat formatter = new SimpleDateFormat("MM/yy", Locale.ENGLISH);
                        Calendar expiryDateDate = Calendar.getInstance();

                        try {
                            expiryDateDate.setTime(formatter.parse(editable.toString()));
                        } catch (java.text.ParseException e) {
                            //before slash
                            if (editable.length() == 2 && !mLastInput.endsWith("/") && isSlash) {
                                isSlash = false;
                                int month = Integer.parseInt(editable.toString());
                                if (month <= 12) {
                                    expiryDateEditText.setText(expiryDateEditText.getText().toString().substring(0, 1));
                                    expiryDateEditText.setSelection(expiryDateEditText.getText().toString().length());
                                } else {
                                    editable.clear();
                                    expiryDateEditText.setText("");
                                    expiryDateEditText.setSelection(expiryDateEditText.getText().toString().length());
                                }
                            }
                            //after slash
                            else if (editable.length() == 2 && !mLastInput.endsWith("/") && !isSlash) {
                                isSlash = true;
                                int month = Integer.parseInt(editable.toString());
                                if (month <= 12) {
                                    expiryDateEditText.setText(expiryDateEditText.getText().toString() + "/");
                                    expiryDateEditText.setSelection(expiryDateEditText.getText().toString().length());
                                } else if (month > 12) {
                                    expiryDateEditText.setText("");
                                    expiryDateEditText.setSelection(expiryDateEditText.getText().toString().length());
                                    editable.clear();
                                }
                            } else if (editable.length() == 1) {
                                int month = Integer.parseInt(editable.toString());
                                if (month > 1 && month < 12) {
                                    isSlash = true;
                                    expiryDateEditText.setText("0" + expiryDateEditText.getText().toString() + "/");
                                    expiryDateEditText.setSelection(expiryDateEditText.getText().toString().length());
                                }
                            }
                            mLastInput = expiryDateEditText.getText().toString();
                            return;
                        }//end catch
                        checkEditTextLength(cardNoEditText,expiryDateEditText,cvvEditText,concessionCheckoutBottomRelativeLayout,concessionCheckoutForwardButton);
                    }//end after text changed
                }//end textwatcher
        );//end listener

        cvvEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                checkEditTextLength(cardNoEditText,expiryDateEditText,cvvEditText,concessionCheckoutBottomRelativeLayout,concessionCheckoutForwardButton);
            }
        });

        if(walletRadioButton.isChecked()){
            concessionCheckoutBottomRelativeLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.available_stroke));
            concessionCheckoutForwardButton.setBackgroundColor(getResources().getColor(R.color.availableColor));
        }

        ////set for buttons
        walletRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                creditCardRadioButton.setChecked(false);
                cardNoEditText.setText("");
                expiryDateEditText.setText("");
                cvvEditText.setText("");
                cardInputRelativeLayout.setVisibility(View.GONE);

                concessionCheckoutBottomRelativeLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.unavailable_stroke));
                concessionCheckoutForwardButton.setBackgroundColor(getResources().getColor(R.color.unavailableColor));

                concessionCheckoutBottomRelativeLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.available_stroke));
                concessionCheckoutForwardButton.setBackgroundColor(getResources().getColor(R.color.availableColor));
            }
        });

        creditCardRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                walletRadioButton.setChecked(false);
                cardInputRelativeLayout.setVisibility(View.VISIBLE);

                concessionCheckoutBottomRelativeLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.unavailable_stroke));
                concessionCheckoutForwardButton.setBackgroundColor(getResources().getColor(R.color.unavailableColor));
                checkEditTextLength(cardNoEditText,expiryDateEditText,cvvEditText,concessionCheckoutBottomRelativeLayout,concessionCheckoutForwardButton);
            }
        });

        concessionCheckoutBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                view.startAnimation(transparentClickAnim);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        concessionCheckoutForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean isPassed = true;

                if (walletRadioButton.isChecked()) {

                } else if (creditCardRadioButton.isChecked()) {
                    //check card number
                    String stripSpace = cardNoEditText.getText().toString().replace(" ", "");
                    System.out.println(stripSpace);
                    if (stripSpace.length() != 16) {
                        cardNoErrorTextView.setVisibility(View.VISIBLE);
                        cardNoErrorTextView.setText("Invalid card number");
                        cardNoEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                                PorterDuff.Mode.SRC_ATOP);
                        isPassed = false;
                    } else {
                        cardNoErrorTextView.setVisibility(View.GONE);
                        cardNoErrorTextView.setText("");
                        cardNoEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorFreeColor),
                                PorterDuff.Mode.SRC_ATOP);
                    }

                    //check expiry date
                    SimpleDateFormat sdformat = new SimpleDateFormat("MM/yy");
                    try {
                        expiryDate = sdformat.parse(expiryDateEditText.getText().toString());
                    } catch (ParseException dtpe) {
                        Toast.makeText(getApplicationContext(), "Invalid date entered", Toast.LENGTH_SHORT);
                    }

                    if (expiryDateEditText.getText().length() < 5 || !expiryDateEditText.getText().toString().contains("/") ||
                            !expiryDateEditText.getText().toString().matches("(?:0[1-9]|1[0-2])/[0-9]{2}")) {
                        expiryDateErrorTextView.setVisibility(View.VISIBLE);
                        expiryDateErrorTextView.setText("Invalid expiry date");
                        expiryDateEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                                PorterDuff.Mode.SRC_ATOP);
                        isPassed = false;
                    } else if (todayDate.after(expiryDate)) {
                        expiryDateErrorTextView.setVisibility(View.VISIBLE);
                        expiryDateErrorTextView.setText("Must be before today");
                        expiryDateEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                                PorterDuff.Mode.SRC_ATOP);
                        isPassed = false;
                    } else {
                        expiryDateErrorTextView.setVisibility(View.GONE);
                        expiryDateErrorTextView.setText("");
                        expiryDateEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorFreeColor),
                                PorterDuff.Mode.SRC_ATOP);
                    }


                    //check cvv
                    if (cvvEditText.getText().length() < 3) {
                        cvvErrorTextView.setVisibility(View.VISIBLE);
                        cvvErrorTextView.setText("Invalid CVV");
                        cvvEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorColor),
                                PorterDuff.Mode.SRC_ATOP);
                        isPassed = false;
                    } else {
                        cvvErrorTextView.setVisibility(View.GONE);
                        cvvErrorTextView.setText("");
                        cvvEditText.getBackground().setColorFilter(getResources().getColor(R.color.errorFreeColor),
                                PorterDuff.Mode.SRC_ATOP);
                    }
                }

                if (isPassed) {
                    //ripple effect
                    Drawable background = getResources().getDrawable( R.color.availableColor );
                    concessionCheckoutForwardButton.setBackground(getRippleBackgroundDrawable(R.color.availableColor, background));

                    Intent intent = new Intent(getApplicationContext(), ConcessionPaymentCompletedActivity.class);
                    intent.putExtra("movieName", movieName);
                    intent.putExtra("ticketID",ticketID);
                    intent.putExtra("showtime",showtimeSeconds);
                    intent.putExtra("concessionList",concessionList);
                    intent.putExtra("totalPrice",totalConcessionPriceString);

                    concessionController.addBookedConcessions(concessionList, ticketID,
                            new OnFirebaseCallback() {
                                @Override
                                public void firebaseCallBack(Object object) {
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            });
                }

            }//end onclick
        });
    }//end oncreate


    private void checkEditTextLength(EditText cardNoEditText, EditText expiryDateEditText, EditText cvvEditText,
                                     RelativeLayout movieCheckoutBottomRelativeLayout, ImageButton checkoutForwardButton){
        if(cardNoEditText.getText().length()==19 && expiryDateEditText.getText().length()==5 && cvvEditText.getText().length()==3 ){
            movieCheckoutBottomRelativeLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.available_stroke));
            checkoutForwardButton.setBackgroundColor(getResources().getColor(R.color.availableColor));
        }
        else{
            movieCheckoutBottomRelativeLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.unavailable_stroke));
            checkoutForwardButton.setBackgroundColor(getResources().getColor(R.color.unavailableColor));
        }
    }

    //animation when default back button tapped
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


}
