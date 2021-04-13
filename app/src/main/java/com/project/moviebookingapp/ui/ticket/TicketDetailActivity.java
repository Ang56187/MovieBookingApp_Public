package com.project.moviebookingapp.ui.ticket;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.ConcessionCheckoutItemAdapter;
import com.project.moviebookingapp.controller.TicketController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Concession;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static com.project.moviebookingapp.custom.CustomAnimations.transparentClickAnim;
import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateTime;
import static com.project.moviebookingapp.custom.TextViewFormat.retrieveLocalCurrencyFormat;

public class TicketDetailActivity  extends AppCompatActivity {
    private ArrayList<Concession> concessionList = new ArrayList<>();
    private ArrayList<String> seatList = new ArrayList<>();
    private Long showtimeSeconds;
    private String showtimeID;
    private String hallID;
    private String movieName;
    private String moviePosterURL;
    private String ticketID;
    private Boolean isUsed;
    private Double seatPrice;

    private FirebaseStorage mFireStorage;
    private FirebaseFirestore mFirestore;

    private CardView ticketStatusCardView;
    private TextView ticketStatusTextView;

    private Double totalPrice = 0.0;

    //controller
    private TicketController controller;

    private DocumentReference docRef;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticketdetail);

        controller = new TicketController(this);
        mFireStorage = FirebaseStorage.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        //get intents
        hallID = intent.getStringExtra("hallID");
        showtimeID = intent.getStringExtra("showtimeID");
        movieName = intent.getStringExtra("movieName");
        moviePosterURL = intent.getStringExtra("moviePosterURL");
        seatList = intent.getStringArrayListExtra("seatList");
        showtimeSeconds = intent.getLongExtra("showtimeSeconds",0);
        ticketID = intent.getStringExtra("ticketID");
        isUsed = intent.getBooleanExtra("isUsed",true);
        seatPrice = intent.getDoubleExtra("seatPrice",0);
//        concessionList = (ArrayList<Concession>)intent.getSerializableExtra("concessionList");

        docRef = mFirestore.collection("tickets").document(ticketID);


        //get intent and set info onto the ticket detail
        String movieTitle = getIntent().getStringExtra("movie_title");

        //get componenents
        Toolbar toolbar = findViewById(R.id.ticketDetailToolbar);
        ImageButton ticketDetailBackButton = findViewById(R.id.ticketDetailBackButton);
        View componentTicketDetail = findViewById(R.id.componentTicketDetail);
        View concessionListLayout = findViewById(R.id.concessionListLayout);
        TextView noConcessionTextView = findViewById(R.id.noConcessionTextView);

        ////get components inside include view (view-ception)
        //////componentTicketDetail
        TextView ticketMovieTitleTextView = componentTicketDetail.findViewById(R.id.ticketMovieTitleTextView);
        TextView ticketQRTextView = componentTicketDetail.findViewById(R.id.ticketQRTextView);
        TextView ticketDateTextView = componentTicketDetail.findViewById(R.id.ticketDateTextView);
        TextView ticketSeatsTextView = componentTicketDetail.findViewById(R.id.ticketSeatsTextView);
        TextView ticketHallTextView = componentTicketDetail.findViewById(R.id.ticketHallTextView);
        TextView ticketPriceTextView = componentTicketDetail.findViewById(R.id.ticketPriceTextView);

        ImageView ticketMoviePosterImageView = componentTicketDetail.findViewById(R.id.ticketMoviePosterImageView);
        ImageView ticketQRImage = componentTicketDetail.findViewById(R.id.ticketQRImage);

        ticketStatusCardView = componentTicketDetail.findViewById(R.id.ticketStatusCardView);
        ticketStatusTextView = componentTicketDetail.findViewById(R.id.ticketStatusTextView);

        //////concessionListLayout
        TextView totalPriceTextView = concessionListLayout.findViewById(R.id.totalConcessionPriceTextView);
        RecyclerView concessionCheckoutItemRecyclerView = concessionListLayout.findViewById(R.id.concessionCheckoutItemRecyclerView);

        //set component settings

        ////setting for text views
        StringBuilder seatListStringBuilder = new StringBuilder();
        Collections.sort(seatList);
        for (String s:seatList){
            seatListStringBuilder.append(s);
            if(!s.equals(seatList.get(seatList.size()-1)))
                seatListStringBuilder.append(", ");
        }

        ticketQRTextView.setText(ticketID);
        ticketDateTextView.setText(getLocalDateTime().format(new Date(showtimeSeconds*1000)));
        ticketHallTextView.setText("Hall "+hallID);
        ticketMovieTitleTextView.setText(movieName);
        ticketSeatsTextView.setText(seatListStringBuilder);

        totalPrice = seatList.size()*seatPrice;
        ticketPriceTextView.setText(retrieveLocalCurrencyFormat(totalPrice));

        //for concessions
        controller.retrieveConcessionList(ticketID,new OnFirebaseCallback() {
            @Override
            public void firebaseCallBack(Object object) {
                concessionList = (ArrayList<Concession>)object;
                if(concessionList.size()>0){
                    noConcessionTextView.setVisibility(View.GONE);
                    concessionListLayout.setVisibility(View.VISIBLE);
                }
                else{
                    noConcessionTextView.setVisibility(View.VISIBLE);
                    concessionListLayout.setVisibility(View.GONE);
                }
                concessionCheckoutItemRecyclerView.setLayoutManager(new LinearLayoutManager(
                        getApplicationContext(),LinearLayoutManager.VERTICAL, false));
                concessionCheckoutItemRecyclerView.setAdapter(new ConcessionCheckoutItemAdapter(
                        getApplicationContext(),concessionList));
                for(Concession c: concessionList){
                    totalPrice += c.getConcessionPrice()*c.getQuantity();
                }
                totalPriceTextView.setText(retrieveLocalCurrencyFormat(totalPrice));
            }
        });

        //set QR code image
        //set QR code
        QRGEncoder qrgEncoder = new QRGEncoder(ticketID, null, QRGContents.Type.TEXT,220);
        // Getting QR-Code as Bitmap
        Bitmap bitmap = qrgEncoder.getBitmap();
        // Setting Bitmap to ImageView
        ticketQRImage.setImageBitmap(bitmap);

        //toolbar go back
        ticketDetailBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                view.startAnimation(transparentClickAnim);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        ////set image for poster
        StorageReference imageRef = mFireStorage.getReference(moviePosterURL);
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(getApplicationContext()).load(imageURL)
                        .override(105,157)
                        .into(ticketMoviePosterImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(),
                        "Error retrieving images.", Toast.LENGTH_SHORT).show();
                Log.d("Error", "Firebase storage error: " + exception);
            }
        });

        //// show ticket status
        if(isUsed){
            ticketStatusCardView.setBackgroundTintList(
                    ColorStateList.valueOf(
                            getResources().getColor(R.color.unavailableColor)));
            ticketStatusTextView.setText("Used");
        }else{
            ticketStatusCardView.setBackgroundTintList(
                    ColorStateList.valueOf(
                            getResources().getColor(R.color.availableColor)));
            ticketStatusTextView.setText("Active");
        }

        //listen to doc changes and updates the status when qr code scanned
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), "Listen failed.", Toast.LENGTH_SHORT);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    boolean isTicketUsed = snapshot.getBoolean("isUsed");
                    if(isTicketUsed){
                        ticketStatusCardView.setBackgroundTintList(
                                ColorStateList.valueOf(
                                        getResources().getColor(R.color.unavailableColor)));
                        ticketStatusTextView.setText("Used");
                    }else{
                        ticketStatusCardView.setBackgroundTintList(
                                ColorStateList.valueOf(
                                        getResources().getColor(R.color.availableColor)));
                        ticketStatusTextView.setText("Active");
                    }
                } else {
                    Log.d("Error:", "Current data: null");
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
