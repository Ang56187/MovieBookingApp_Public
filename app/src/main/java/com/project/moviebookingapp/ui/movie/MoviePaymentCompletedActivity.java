package com.project.moviebookingapp.ui.movie;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;
import com.project.moviebookingapp.MainActivity;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.ConcessionCheckoutItemAdapter;
import com.project.moviebookingapp.model.Concession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateTime;
import static com.project.moviebookingapp.custom.TextViewFormat.retrieveLocalCurrencyFormat;

public class MoviePaymentCompletedActivity extends AppCompatActivity {
    private String totalPriceString;
    private String movieID;
    private Long showtimeSeconds;
    private String hallID;
    private Double seatPrice;
    private String showtimeID;
    private String movieName;
    private String moviePosterURL;
    private String ticketID;
    private ArrayList<Concession> concessionList = new ArrayList<>();

    private FirebaseStorage mFireStorage = FirebaseStorage.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_payment_completed);

        //intent
        Intent intent = getIntent();
        ArrayList<String> seatList = intent.getStringArrayListExtra("seat_list");
        totalPriceString = intent.getStringExtra("totalPrice");
        hallID = intent.getStringExtra("hallID");
        movieName = intent.getStringExtra("movieName");
        moviePosterURL = intent.getStringExtra("moviePosterURL");
        seatList = intent.getStringArrayListExtra("seatList");
        showtimeSeconds = intent.getLongExtra("showtimeSeconds",0);
        seatPrice = intent.getDoubleExtra("seatPrice",0);
        ticketID = intent.getStringExtra("ticketID");
        concessionList = (ArrayList<Concession>)intent.getSerializableExtra("concessionList");

        //get widgets/views
        View component_ticket_detail = findViewById(R.id.component_ticket_detail);
        Button backToHomeButton = findViewById(R.id.backToHomeButton);
        ////get components inside include view (view-ception)
        TextView ticketMovieTitleTextView = component_ticket_detail.findViewById(R.id.ticketMovieTitleTextView);
        TextView ticketQRTextView = component_ticket_detail.findViewById(R.id.ticketQRTextView);
        TextView ticketDateTextView = component_ticket_detail.findViewById(R.id.ticketDateTextView);
        TextView ticketSeatsTextView = component_ticket_detail.findViewById(R.id.ticketSeatsTextView);
        TextView ticketHallTextView = component_ticket_detail.findViewById(R.id.ticketHallTextView);
        TextView ticketPriceTextView = component_ticket_detail.findViewById(R.id.ticketPriceTextView);

        ImageView ticketMoviePosterImageView = component_ticket_detail.findViewById(R.id.ticketMoviePosterImageView);
        ImageView ticketQRImage = component_ticket_detail.findViewById(R.id.ticketQRImage);

        //show concessions booked
        View concessionListLayout = findViewById(R.id.concessionListLayout);
        ////components within included layout
        ////// concessionListLayout
        TextView totalConcessionPriceTextView = concessionListLayout.findViewById(R.id.totalConcessionPriceTextView);
        RecyclerView concessionCheckoutItemRecyclerView = concessionListLayout.findViewById(R.id.concessionCheckoutItemRecyclerView);

        //set widgets/layout settings
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
        ticketPriceTextView.setText(totalPriceString);

        //set QR code image
        QRGEncoder qrgEncoder = new QRGEncoder(ticketID, null, QRGContents.Type.TEXT,220);
        // Getting QR-Code as Bitmap
        Bitmap bitmap = qrgEncoder.getBitmap();
        // Setting Bitmap to ImageView
        ticketQRImage.setImageBitmap(bitmap);

        ////set recycler view
        if(!concessionList.isEmpty()){
            concessionListLayout.setVisibility(View.VISIBLE);
        }
        concessionCheckoutItemRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        concessionCheckoutItemRecyclerView.setAdapter(new ConcessionCheckoutItemAdapter(this,concessionList));
        //set total price text
        Double totalConcessionPrice = 0.0;
        for(Concession c : concessionList){
            totalConcessionPrice += c.getQuantity()*c.getConcessionPrice();
        }
        //its actually the overall price (seats+ concessions)
        totalPriceString = retrieveLocalCurrencyFormat(totalConcessionPrice);
        totalConcessionPriceTextView.setText(totalPriceString);

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
                Toast.makeText(getApplicationContext(), "Error retrieving images.", Toast.LENGTH_SHORT).show();
                Log.d("Error", "Firebase storage error: " + exception);
            }
        });


        //set buttons
        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });


    }

    //disable back button so user does not go back to checkout page
    @Override
    public void onBackPressed() {
    }
}
