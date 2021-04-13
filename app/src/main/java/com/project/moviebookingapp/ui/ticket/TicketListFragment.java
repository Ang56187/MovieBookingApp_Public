package com.project.moviebookingapp.ui.ticket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.TicketListAdapter;
import com.project.moviebookingapp.controller.TicketController;
import com.project.moviebookingapp.model.Ticket;

import java.util.ArrayList;

public class TicketListFragment extends Fragment {
    private ArrayList<String> ticketList = new ArrayList<String>();
    private TicketController controller;
    private TicketListAdapter ticketListAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ticketlist,container,false);

        //set controller
        controller = new TicketController(getContext());

        //get component
        RecyclerView ticketListRecyclerView = root.findViewById(R.id.ticketListRecyclerView);

        //set components settings
        ticketListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));

        //set adapter for recycler view
        FirestoreRecyclerOptions<Ticket> ticketOptions = controller.queryTickets();
        ticketListAdapter = new TicketListAdapter(ticketOptions,
                getActivity(),getActivity());
        ticketListRecyclerView.setAdapter(ticketListAdapter);

        return root;
    }//end oncreate

    @Override
    public void onStart() {
        super.onStart();
        ticketListAdapter.startListening();
    }

    @Override
    public void onStop(){
        super.onStop();
        ticketListAdapter.stopListening();
    }


}
