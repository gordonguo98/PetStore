package com.training.petstore.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.training.petstore.R;
import com.training.petstore.model.bean.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    Context context;
    List<Transaction> transactions;

    OnItemClickListener onItemClickListener;

    public TransactionAdapter(Context context, List<Transaction> transactions){
        this.context = context;
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction current = transactions.get(position);
        holder.transactionId.setText(current.getTransaction_id());
        holder.transactionTime.setText(current.getTransaction_time());
        holder.transactionSeller.setText(current.getSeller_id());
        holder.transactionBuyer.setText(current.getBuyer_id());
        holder.transactionPet.setText(current.getPet_id());
        holder.transactionPrice.setText(String.valueOf(current.getPrice()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null)
                    onItemClickListener.onItemClick(position);
            }
        });
        if(current.isHasArbitration()){
            if(holder.mask.getVisibility() == View.GONE)
                holder.mask.setVisibility(View.VISIBLE);
        }
        else{
            if(holder.mask.getVisibility() == View.VISIBLE)
                holder.mask.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder{

        private CardView cardView;
        private TextView transactionId;
        private TextView transactionTime;
        private TextView transactionSeller;
        private TextView transactionBuyer;
        private TextView transactionPet;
        private TextView transactionPrice;
        private View mask;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.item_transaction_card);
            transactionId = itemView.findViewById(R.id.item_transaction_id);
            transactionTime = itemView.findViewById(R.id.item_transaction_time);
            transactionSeller = itemView.findViewById(R.id.item_transaction_seller);
            transactionBuyer = itemView.findViewById(R.id.item_transaction_buyer);
            transactionPet = itemView.findViewById(R.id.item_transaction_pet);
            transactionPrice = itemView.findViewById(R.id.item_transaction_price);
            mask = itemView.findViewById(R.id.item_transaction_mask);

        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

}
