package com.project.moviebookingapp.ui.movie;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.ConcessionCheckoutItemAdapter;
import com.project.moviebookingapp.controller.CheckoutController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Concession;
import com.project.moviebookingapp.model.Ticket;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.project.moviebookingapp.custom.CustomAnimations.getRippleBackgroundDrawable;
import static com.project.moviebookingapp.custom.EditTextFormat.concatString;
import static com.project.moviebookingapp.custom.EditTextFormat.getDigitArray;
import static com.project.moviebookingapp.custom.EditTextFormat.isInputCorrect;
import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateTime;
import static com.project.moviebookingapp.custom.TextViewFormat.retrieveLocalCurrencyFormat;

public class MovieCheckoutActivity extends AppCompatActivity {
    private boolean isSlash = false;
    private boolean isDelete = false;
    //for expiry date checking
    private Date todayDate = new Date();
    private Date expiryDate = new Date();

    //to check card number
    private final int cardNoTotalLength = 19; // size of pattern 0000-0000-0000-0000
    private final int cardNoTotalDigits = 16; // max numbers of digits in pattern: 0000 x 4
    private final int cardNoDivideModulo = 5; // means divider position is every 5th symbol beginning with 1
    private final int cardNoDividerPosition = cardNoDivideModulo - 1; // means divider position is every 4th symbol beginning with 0
    private final char cardNoDivider = ' ';

    //firebase
    private FirebaseStorage mFireStorage;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    private Double totalPrice = 0.0;
    private String totalPriceString;

    //pass/get intents
    private String movieID;
    private Long showtimeSeconds;
    private String hallID;
    private Double seatPrice;
    private String showtimeID;
    private String movieName;
    private String moviePosterURL;
    private ArrayList<String> seatList = new ArrayList<String>();
    private ArrayList<Concession> concessionList = new ArrayList<>();

    private Timestamp movieTimestamp;

    //check if user meet the requirement conditions to checkout
    private Boolean isPassed;

    private CheckoutController checkoutController;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_checkout);

        //set controller
        checkoutController = new CheckoutController(this);

        //check firebase status
        mFireStorage = FirebaseStorage.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //signed in
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(getApplicationContext(), "Internet connection unavailable.",
                            Toast.LENGTH_LONG).show();
                }
            }
        };

        //get intents
        Intent intent = getIntent();
        movieID = intent.getStringExtra("movieID");
        hallID = intent.getStringExtra("hallID");
        movieName = intent.getStringExtra("movieName");
        moviePosterURL = intent.getStringExtra("moviePosterURL");
        showtimeID = intent.getStringExtra("showtimeID");
        seatList = intent.getStringArrayListExtra("seatList");
        showtimeSeconds = intent.getLongExtra("showtimeSeconds",0);
        seatPrice = intent.getDoubleExtra("seatPrice",0);
        concessionList = (ArrayList<Concession>)intent.getSerializableExtra("concessionList");

        movieTimestamp = new Timestamp(new Date(showtimeSeconds*1000));

        // total seat price will only be calculated here,
        // and be sent to payment completed page through intent
        String totalSeatPrice = retrieveLocalCurrencyFormat(seatList.size()*seatPrice);

        ImageView moviePosterImageView = findViewById(R.id.moviePosterImageView);

        //get components
        LinearLayout paymentOptionLinearLayout = findViewById(R.id.paymentOptionLayout);
        ImageButton movieCheckoutBackButton = findViewById(R.id.movieCheckoutBackButton);
        RelativeLayout movieCheckoutBottomRelativeLayout = findViewById(R.id.movieCheckoutBottomRelativeLayout);

        TextView movieTitleTextView = findViewById(R.id.movieTitleTextView);
        TextView movieDateTimeTextView = findViewById(R.id.movieDateTimeTextView);

        //show seats booked
        TextView seatQuantityTextView = findViewById(R.id.seatQuantityTextView);
        TextView seatNumbersTextView = findViewById(R.id.seatNumbersTextView);
        TextView totalSeatPriceTextView = findViewById(R.id.totalSeatPriceTextView);

        //show concessions booked
        View concessionListLayout = findViewById(R.id.concessionListLayout);
        ////components within included layout
        ////// concessionListLayout
        TextView totalConcessionPriceTextView = concessionListLayout.findViewById(R.id.totalConcessionPriceTextView);
        RecyclerView concessionCheckoutItemRecyclerView = concessionListLayout.findViewById(R.id.concessionCheckoutItemRecyclerView);

        ImageButton movieCheckoutForwardButton = findViewById(R.id.movieCheckoutForwardButton);

        ////components within included layout
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



        //get widgets/views
        ////set for text views
        ////// set for seat quantity
        seatQuantityTextView.setText(Integer.toString(seatList.size())+(((seatList.size()>1)?" seats":" seat")));

        ////set image
        StorageReference imageRef = mFireStorage.getReference(moviePosterURL);
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(getApplicationContext()).load(imageURL)
                        .override(105,157)
                        .into(moviePosterImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Error retrieving images.", Toast.LENGTH_SHORT).show();
                Log.d("Eror", "Firebase storage error: " + exception);
            }
        });

        ////set movie title
        movieTitleTextView.setText(movieName);
        movieDateTimeTextView.setText(getLocalDateTime().format(movieTimestamp.toDate()));

        ////// set seat numbers
        StringBuilder seatListStringBuilder = new StringBuilder();
        Collections.sort(seatList);
        for (String s:seatList){
            seatListStringBuilder.append(s);
            if(!s.equals(seatList.get(seatList.size()-1)))
            seatListStringBuilder.append(", ");
        }

        seatNumbersTextView.setText(seatListStringBuilder);
        //// set total price
        totalSeatPriceTextView.setText(totalSeatPrice);

        ////set recycler view
        if(!concessionList.isEmpty()){
            concessionListLayout.setVisibility(View.VISIBLE);
        }
        concessionCheckoutItemRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        concessionCheckoutItemRecyclerView.setAdapter(new ConcessionCheckoutItemAdapter(this,concessionList));

        //set total price text
        for(Concession c : concessionList){
            totalPrice += c.getQuantity()*c.getConcessionPrice();
        }
        totalPrice+=seatList.size()*seatPrice;
        totalPriceString = retrieveLocalCurrencyFormat(totalPrice);
        totalConcessionPriceTextView.setText(totalPriceString);

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
                        checkEditTextLength(cardNoEditText,expiryDateEditText,cvvEditText,movieCheckoutBottomRelativeLayout,movieCheckoutForwardButton);
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
                        checkEditTextLength(cardNoEditText,expiryDateEditText,cvvEditText,movieCheckoutBottomRelativeLayout,movieCheckoutForwardButton);
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
                checkEditTextLength(cardNoEditText,expiryDateEditText,cvvEditText,movieCheckoutBottomRelativeLayout,movieCheckoutForwardButton);
            }
        });

        if(walletRadioButton.isChecked()){
            movieCheckoutBottomRelativeLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.available_stroke));
            movieCheckoutForwardButton.setBackgroundColor(getResources().getColor(R.color.availableColor));
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

                movieCheckoutBottomRelativeLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.unavailable_stroke));
                movieCheckoutForwardButton.setBackgroundColor(getResources().getColor(R.color.unavailableColor));

                movieCheckoutBottomRelativeLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.available_stroke));
                movieCheckoutForwardButton.setBackgroundColor(getResources().getColor(R.color.availableColor));
            }
        });

        creditCardRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                walletRadioButton.setChecked(false);
                cardInputRelativeLayout.setVisibility(View.VISIBLE);

                movieCheckoutBottomRelativeLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.unavailable_stroke));
                movieCheckoutForwardButton.setBackgroundColor(getResources().getColor(R.color.unavailableColor));
                checkEditTextLength(cardNoEditText,expiryDateEditText,cvvEditText,movieCheckoutBottomRelativeLayout,movieCheckoutForwardButton);
            }
        });

        movieCheckoutBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        movieCheckoutForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPassed = true;

                if (walletRadioButton.isChecked()) {
                    isPassed = false;
                    Toast.makeText(getApplicationContext(),"Wallet not implemented, please use credit card.",
                            Toast.LENGTH_SHORT).show();
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
                        System.out.println("Not a valid expiry date: ");
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

                checkoutController.retrieveBookedSeats(showtimeID, new OnFirebaseCallback() {
                    @Override
                    public void firebaseCallBack(Object object) {
                        ArrayList<String> otherSeatList = (ArrayList<String>) object;

                        for (String seat:seatList){
                            if(otherSeatList.contains(seat)){
                                isPassed = false;
                                Toast.makeText(getApplicationContext(),
                                        "Seat have been booked by other users, please select " +
                                                "other seats.",Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }


                        if (isPassed) {
                            //ripple effect
                            Drawable background = getResources().getDrawable( R.color.availableColor );
                            movieCheckoutForwardButton.setBackground(getRippleBackgroundDrawable(
                                    R.color.availableColor, background));

                            FirebaseUser accountID = FirebaseAuth.getInstance().getCurrentUser();
                            Ticket ticket = new Ticket(accountID.getUid(),showtimeID,movieID,movieName,
                                    false, movieTimestamp,seatList,moviePosterURL,hallID,seatPrice);

                            checkoutController.addTicketToFirebase(ticket,concessionList);
                        }
                    }
                });

            }//end onclick
        });
    }//end onCreate

    // used in controller class at addTicketToFirebase
    public void setIntent(String ticketID){
        Intent intent = new Intent(getApplicationContext(), MoviePaymentCompletedActivity.class);
        intent.putExtra("seatList",seatList);
        intent.putExtra("totalPrice",totalPriceString);
        intent.putExtra("showtimeID",showtimeID);
        intent.putExtra("seatPrice",seatPrice);
        intent.putExtra("showtimeSeconds",showtimeSeconds);
        intent.putExtra("hallID",hallID);
        intent.putExtra("movieID",movieID);
        intent.putExtra("movieName",movieName);
        intent.putExtra("moviePosterURL",moviePosterURL);
        intent.putExtra("seatList",seatList);
        intent.putExtra("concessionList",concessionList);
        intent.putExtra("ticketID",ticketID);

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


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
