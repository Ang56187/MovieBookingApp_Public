package com.project.moviebookingapp.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.project.moviebookingapp.adapter.MovieSeatAdapter;
import com.project.moviebookingapp.adapter.MovieTimeToggleAdapter;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Concession;
import com.project.moviebookingapp.model.Movie;
import com.project.moviebookingapp.model.Ticket;
import com.project.moviebookingapp.ui.movie.MovieSeatSelectionActivity;

import java.util.ArrayList;
import java.util.List;

import static com.project.moviebookingapp.custom.TextViewFormat.retrieveLocalCurrencyFormat;

public class TicketController {
    private SharedPreferences app_preferences;
    private FirebaseFirestore mFirestore;
    private Context context;
    private FirebaseUser account;
    private double totalPrice = 0.0;
    private double seatPrice = 0.0;

    public TicketController(Context context){
        this.context = context;
        this.mFirestore = FirebaseFirestore.getInstance();
        this.account = FirebaseAuth.getInstance().getCurrentUser();
        this.app_preferences = PreferenceManager.getDefaultSharedPreferences(context);

    }

    public FirestoreRecyclerOptions<Ticket> queryTickets(){
        Query query = mFirestore.collection("tickets")
                .whereEqualTo("accountID",account.getUid());

        FirestoreRecyclerOptions<Ticket> options =
                new FirestoreRecyclerOptions.Builder<Ticket>().setQuery(query,Ticket.class).build();

        return options;
    }

    //used in ticket detail page and review list page
    //to see if user eligible for review
    //conditons
    //1. passed the movie show time
    //2. ticket have been used(debatable)
    private int ticketCount = 0;
    public void retrieveOwnTickets(String movieID,OnFirebaseCallback callback){
        ArrayList<Timestamp> timestampList = new ArrayList<>();
        mFirestore.collection("tickets").whereEqualTo("accountID",
                app_preferences.getString("accountID"," "))
                .whereEqualTo("movieID",movieID).get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int size = task.getResult().size();
                        if(size == 0){
                            callback.firebaseCallBack(new ArrayList<Timestamp>());
                        }
                        for (QueryDocumentSnapshot doc: task.getResult()){
                            //second query
                            timestampList.add(doc.getTimestamp("showtime"));
                            if(ticketCount == size-1){
                                callback.firebaseCallBack(timestampList);
                            }
                            ticketCount ++;
                        }//end for
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    public void retrieveTicketsWithConcessions(OnFirebaseCallback callback){
        ArrayList<Ticket> list = new ArrayList<>();
        mFirestore.collection("tickets")
                .whereEqualTo("accountID",account.getUid())
                .whereEqualTo("isUsed",false)
                .whereEqualTo("isConcessionBooked",false).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot doc : task.getResult()){
                            Ticket ticket = new Ticket(doc.getString("accountID"),
                                    doc.getString("movieID"), doc.getString("showtimeID")
                                    ,doc.getString("movieName"), doc.getBoolean("isUsed"),
                                    doc.getTimestamp("showtime"),
                                    (List<String>)doc.get("seat"),doc.getString("moviePosterURL"),
                                    doc.getString("hallID"),doc.getDouble("seatPrice"));

                            ticket.setTicketID(doc.getId());
                            ticket.setConcessionBooked(doc.getBoolean("isConcessionBooked"));
                            list.add(ticket);
                        }
                        callback.firebaseCallBack(list);
                    }
                });
    }


    public void totalTicketPrice(String showtimeID, ArrayList<String> seatList,
                                 OnFirebaseCallback callback){
        totalPrice = 0.0;
        mFirestore.collection("showtimes")
                .document(showtimeID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        seatPrice = document.getDouble("price");
                        totalPrice = seatList.size()*seatPrice;
                        callback.firebaseCallBack(totalPrice);
                    }

                }else{
                    Log.d("Error", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private int concessionCount = 0 ;
    public void retrieveConcessionList(String ticketID,OnFirebaseCallback callback){
        //first query (find the id of concession booked)
        ArrayList<Concession> concessionList = new ArrayList<>();
        mFirestore.collection("bookedConcessions")
                .whereEqualTo("ticketID",ticketID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            concessionCount = 0;
                            int size = task.getResult().size();
                            for (QueryDocumentSnapshot doc : task.getResult()){
                                //second query (find name of concession by id)
                                mFirestore.collection("concessions")
                                        .document(doc.getString("concessionID")).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> innerTask) {
                                                if(innerTask.isSuccessful()){
                                                    DocumentSnapshot innerDoc = innerTask.getResult();
                                                    if(innerDoc.exists()){
                                                        Concession concession = new Concession(
                                                                innerDoc.getString("concessionName"),
                                                                innerDoc.getString("concessionImageURL"),
                                                                innerDoc.getDouble("concessionPrice"));
                                                        concession.setQuantity(
                                                                doc.getLong("quantity").intValue());
                                                        concessionList.add(concession);
                                                        //see if last item added to list
                                                        //then start calculating the total price
                                                        if(concessionCount == size-1) {
                                                            callback.firebaseCallBack(concessionList);
                                                        }
                                                        concessionCount++;
                                                    }
                                                }
                                                else{
                                                    Log.d("Error",
                                                            "Error getting documents: ",
                                                            task.getException());
                                                }
                                            }
                                        });
                            }//end for loop
                        }
                        else{
                            Log.d("Error", "Error getting documents: ",
                                    task.getException());
                        }
                    }
                });
    }//end

    //below methods are created for debugging
    //easier to test later
    public void deleteTicket(String ticketID){
        mFirestore.collection("tickets").document(ticketID)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context,"Deleted ticket (for debugging)",
                        Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"Failed to delete",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void updateTicketStatus(String ticketID,Boolean isUsed){
        mFirestore.collection("tickets").document(ticketID)
                .update("isUsed",!isUsed).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context,"Updated ticket status (for debugging)",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"Failed to update",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
