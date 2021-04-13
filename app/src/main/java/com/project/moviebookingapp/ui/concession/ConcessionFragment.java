package com.project.moviebookingapp.ui.concession;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.adapter.ConcessionAdapter;
import com.project.moviebookingapp.controller.concession.ConcessionController;
import com.project.moviebookingapp.listener.OnConcessionListener;
import com.project.moviebookingapp.model.Concession;
import com.project.moviebookingapp.ui.movie.MovieCheckoutActivity;

import java.util.ArrayList;

import static com.project.moviebookingapp.custom.CustomAnimations.getRippleBackgroundDrawable;
import static com.project.moviebookingapp.custom.TextViewFormat.retrieveLocalCurrencyFormat;

public class ConcessionFragment extends Fragment implements OnConcessionListener{
    private LinearLayout concessionLinearLayout;
    private ImageButton concessionForwardButton;
    private RecyclerView concessionRecyclerView;
    private TextView totalConcessionPriceTextView;
    private RelativeLayout bottomRelativeLayout;
    private ImageButton movieConcessionBackButton;

    private ConcessionController controller;

    private FirestoreRecyclerOptions<Concession> concessionOption;
    private ConcessionAdapter concessionAdapter;

    //list to store selected items
    private ArrayList<Concession> addedConcessionList = new ArrayList<>();

    public ConcessionFragment(){}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_concession,container,false);
        Context context = container.getContext();

        controller = new ConcessionController(context);

        //get widgets/layout
        concessionForwardButton = root.findViewById(R.id.concessionForwardButton);
        concessionRecyclerView = root.findViewById(R.id.concessionRecyclerView);
        totalConcessionPriceTextView = root.findViewById(R.id.totalConcessionPriceTextView);
        bottomRelativeLayout = root.findViewById(R.id.bottomRelativeLayout);
        movieConcessionBackButton = root.findViewById(R.id.movieConcessionBackButton);

        //set settings
        concessionOption = controller.queryConcessionsDocuments();
        concessionAdapter = new ConcessionAdapter(concessionOption,context,addedConcessionList);
        concessionRecyclerView.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false));
        concessionRecyclerView.setAdapter(concessionAdapter);

        concessionForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable background = getResources().getDrawable( R.color.availableColor );
                concessionForwardButton.setBackground(getRippleBackgroundDrawable(R.color.availableColor, background));

                if(addedConcessionList.size()>0) {
                    //go to checkout page and pass the intents
                    Intent intent = new Intent(getActivity(), TicketSelectionActivity.class);
                    intent.putExtra("concessionList",addedConcessionList);

                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void concessionCallback(ArrayList<Concession> arr){
        updateCocessionFragment(arr);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateCocessionFragment(ArrayList<Concession> arr){
        //checks and remove items with 0 quantity
        ArrayList<Concession> cloneList = (ArrayList<Concession>)arr.clone();
        for(Concession s: cloneList){
            if (s.getQuantity() == 0){
                addedConcessionList.remove(s);
            }
        }

        //then checks size again after 0 qty items are removed
        if (arr.size()>0) {
            Double totalprice = 0.0;
            for (Concession c : arr) {
                totalprice += c.getQuantity() * c.getConcessionPrice();
            }
            totalConcessionPriceTextView.setText(retrieveLocalCurrencyFormat(totalprice));
            bottomRelativeLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.available_stroke));
            concessionForwardButton.setBackgroundColor(getResources().getColor(R.color.availableColor));
        }
        else{
            bottomRelativeLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.unavailable_stroke));
            concessionForwardButton.setBackgroundColor(getResources().getColor(R.color.unavailableColor));
        }
    }//end oncreate

    @Override
    public void onStart(){
        super.onStart();
        concessionAdapter.startListening();
    }

    @Override
    public void onStop(){
        super.onStop();
        concessionAdapter.stopListening();
    }



}
