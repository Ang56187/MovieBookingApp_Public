package com.project.moviebookingapp.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.BookedConcession;
import com.project.moviebookingapp.model.Concession;
import com.project.moviebookingapp.model.Showtime;
import com.project.moviebookingapp.model.Ticket;
import com.project.moviebookingapp.ui.movie.MovieCheckoutActivity;

import java.util.ArrayList;
import java.util.List;

//checkout at movie booking or concession booking
public class CheckoutController {
    private FirebaseFirestore mFirestore;
    private Context context;
    private MovieCheckoutActivity activity;

    public CheckoutController(Context context){
        this.context = context;
        this.activity = (MovieCheckoutActivity) context;
        mFirestore = FirebaseFirestore.getInstance();
    }


    boolean isConcessionAdded =  true;
    public void addTicketToFirebase(Ticket ticket, List<Concession> concessionList){
        //checks if user brought any concessions too
        if(concessionList.size() > 0){
            ticket.setConcessionBooked(true);
        }
        else{ticket.setConcessionBooked(false);
        }

        //first query
        mFirestore.collection("tickets").add(ticket)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference docRef) {
//                        Toast.makeText(context,"Ticket created", Toast.LENGTH_SHORT).show();
                        for(Concession c :concessionList){
                            BookedConcession newConcession = new BookedConcession(
                                    c.getConcessionID(),docRef.getId(), c.getQuantity());
                            //second query
                            mFirestore.collection("bookedConcessions")
                                    .add(newConcession).addOnFailureListener(
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    isConcessionAdded = false;
                                                    Toast.makeText(context,e.toString(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                        }
                        if(isConcessionAdded) {
                            activity.setIntent(docRef.getId());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    //used to check seats
    public void retrieveBookedSeats(String showtimeID, OnFirebaseCallback callback){
        mFirestore.collection("tickets").whereEqualTo("showtimeID",showtimeID)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<String> seatList = new ArrayList<>();
                if(!queryDocumentSnapshots.getDocuments().isEmpty()) {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        seatList.addAll((List<String>) doc.get("seat"));
                    }
                }
                callback.firebaseCallBack(seatList);
            }
        });
    }


}
