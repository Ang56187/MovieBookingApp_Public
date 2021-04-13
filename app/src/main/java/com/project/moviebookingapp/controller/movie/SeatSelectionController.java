package com.project.moviebookingapp.controller.movie;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.moviebookingapp.adapter.MovieSeatAdapter;
import com.project.moviebookingapp.adapter.MovieTimeAdapter;
import com.project.moviebookingapp.adapter.MovieTimeToggleAdapter;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Showtime;
import com.project.moviebookingapp.ui.movie.MovieSeatSelectionActivity;

import java.util.ArrayList;
import java.util.List;

public class SeatSelectionController {
    private FirebaseFirestore mFirestore;
    private Context context;
    //its to store list of seats by its ticket
    //to ensure seats booked from two tickets cant be mixed up
    private List<List<String>> oldSeatList = new ArrayList<>();

    public SeatSelectionController(Context context){
        this.context = context;
        mFirestore = FirebaseFirestore.getInstance();
    }

    //overlapped
    public void retrieveBookedSeats(ArrayList<String> bookedSeatList, String showtimeID,
                                    OnFirebaseCallback callback){
        mFirestore.collection("tickets").whereEqualTo("showtimeID",showtimeID)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                bookedSeatList.clear();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        List<String> seatList = (List<String>)document.get("seat");
                        bookedSeatList.addAll(seatList);
                    }
                    callback.firebaseCallBack(bookedSeatList);
                } else {
                    Toast.makeText(context,"Error getting seats of that showtime",
                            Toast.LENGTH_LONG).show();
                    Log.d("Error", "Error getting documents: ", task.getException());
                }
            }
        });
    }
    //overlapped
    public void retrieveBookedSeats(ArrayList<String> bookedSeatList, String showtimeID,
                                    MovieSeatAdapter adapter){
        Query query = mFirestore.collection("tickets").whereEqualTo("showtimeID",showtimeID);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    oldSeatList.clear();
                    bookedSeatList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        List<String> seatList = (List<String>)document.get("seat");
                        bookedSeatList.addAll(seatList);
                        oldSeatList.add(seatList);
                    }
                    //better to notify here in listener, to be in sync
                    // if outside, it will be out of order, which will create bugs
                    // that the time slot will not get the booked seats on time
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d("Error", "Error getting documents: ", task.getException());
                }
            }
        });
    }//end method

    //for MovieSeatSelectionActivity only
    public ListenerRegistration updateSeatListener(Query query, ArrayList<String> bookedSeatList,
                                                   ArrayList<String> selectedSeatList,
                                                   String showtimeID, MovieSeatAdapter adapter,
                                                   MovieTimeToggleAdapter timeAdapter){
        MovieSeatSelectionActivity activity = (MovieSeatSelectionActivity) context;
        ListenerRegistration listener =query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Error", "listen:error", e);
                    return;
                }
                //bookedSeatList.clear();
                for (DocumentChange dc : value.getDocumentChanges()) {
                    if (dc.getDocument().getString("showtimeID").equals((showtimeID))) {
                        switch (dc.getType()) {
                            case ADDED:
                                bookedSeatList.addAll((List<String>) dc.getDocument().get("seat"));
                                break;
                            case MODIFIED:
                                //prevent changes at other timeslots
                                List<String> seatList = (List<String>) dc.getDocument().get("seat");
                                bookedSeatList.removeAll(oldSeatList.get(dc.getNewIndex()));
                                bookedSeatList.addAll(seatList);

                                oldSeatList.get(dc.getNewIndex()).clear();
                                oldSeatList.get(dc.getNewIndex()).addAll(seatList);
                                break;
                            case REMOVED:
                                bookedSeatList.removeAll((List<String>) dc.getDocument().get("seat"));
                                break;
                        }

                        for(String booked: bookedSeatList){
                            if(selectedSeatList.contains(booked)){
                                selectedSeatList.remove(booked);
                            }
                        }
                        activity.updateBottomLayout();
                    }//end if
                }//end for
                adapter.notifyDataSetChanged();
            }
        });
        return listener;
    }//end method



}
