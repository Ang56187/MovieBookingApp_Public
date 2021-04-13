package com.project.moviebookingapp.controller.concession;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.BookedConcession;
import com.project.moviebookingapp.model.Concession;
import com.project.moviebookingapp.model.Movie;
import com.project.moviebookingapp.model.Showtime;

import java.util.ArrayList;
import java.util.List;

public class ConcessionController {
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mFireStorage;
    private Context context;

    public ConcessionController(Context context){
        this.context = context;
        mFirestore = FirebaseFirestore.getInstance();
        mFireStorage = FirebaseStorage.getInstance();
    }//constructor

    public FirestoreRecyclerOptions<Concession> queryConcessionsDocuments() {
        Query query = mFirestore.collection("concessions");

        FirestoreRecyclerOptions<Concession> options =
                new FirestoreRecyclerOptions.Builder<Concession>().setQuery(query, Concession.class).build();
        return options;
    }//end method

    public void addBookedConcessions(ArrayList<Concession> concessionList, String ticketID,
                                     OnFirebaseCallback callback){
        for(Concession c: concessionList){
            BookedConcession bookedConcession =
                    new BookedConcession(c.getConcessionID(),ticketID,c.getQuantity());
            mFirestore.collection("bookedConcessions").add(bookedConcession);
        }

        mFirestore.collection("tickets").document(ticketID)
                .update("isConcessionBooked",true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.firebaseCallBack(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,e.toString(),Toast.LENGTH_LONG);
                    }
                });

    }





}
