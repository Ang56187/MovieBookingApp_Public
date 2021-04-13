package com.project.moviebookingapp.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.project.moviebookingapp.R;
import com.project.moviebookingapp.model.Concession;

import java.util.ArrayList;

import static com.project.moviebookingapp.custom.TextViewFormat.retrieveLocalCurrencyFormat;

public class ConcessionCheckoutItemAdapter  extends RecyclerView.Adapter<ConcessionCheckoutItemAdapter.ConcessionCheckoutItemViewHolder> {
    private ArrayList<Concession> concessionDataset;
    private LayoutInflater mInflater;
    private Context context;

    // Constructor
    public ConcessionCheckoutItemAdapter(Context context, ArrayList<Concession> cDataset) {
        concessionDataset = cDataset;
        this.mInflater = LayoutInflater.from(context);
    }

    //get components from the view where recyclerview resides
    public static class ConcessionCheckoutItemViewHolder extends RecyclerView.ViewHolder{
        private TextView nameTextView;
        private TextView qtyTextView;
        private TextView priceTextView;

        public ConcessionCheckoutItemViewHolder(View v){
            super(v);
            nameTextView = v.findViewById(R.id.concessionNameTextView);
            qtyTextView = v.findViewById(R.id.concessionQtyTextView);
            priceTextView = v.findViewById(R.id.concessionPriceTextView);
        }
    }

    // obtain view where recyclerview resides in
    @Override
    public ConcessionCheckoutItemAdapter.ConcessionCheckoutItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.component_concession_checkoutitem, parent, false);
        return new ConcessionCheckoutItemAdapter.ConcessionCheckoutItemViewHolder(view);
    }

    // set values for contents of a view
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(ConcessionCheckoutItemAdapter.ConcessionCheckoutItemViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.nameTextView.setText(concessionDataset.get(position).getConcessionName());
        holder.qtyTextView.setText("x"+concessionDataset.get(position).getQuantity());
        holder.priceTextView.setText(retrieveLocalCurrencyFormat(
                concessionDataset.get(position).getConcessionPrice()*
                        concessionDataset.get(position).getQuantity()));

    }

    // Return the size of your dataset
    @Override
    public int getItemCount() {
        return concessionDataset.size();
    }

}
