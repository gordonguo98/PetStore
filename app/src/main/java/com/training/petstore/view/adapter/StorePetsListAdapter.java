package com.training.petstore.view.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.training.petstore.R;
import com.training.petstore.model.bean.Pet;

import java.util.List;

public class StorePetsListAdapter extends RecyclerView.Adapter<StorePetsListAdapter.StorePetsViewHolder> {

    private static final String TAG = StorePetsListAdapter.class.getSimpleName();
    private final LayoutInflater mInflater;
    List<Pet> petList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public StorePetsListAdapter(Context context, List<Pet> petList) {
        this.context = context;
        this.petList = petList;
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public StorePetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_store_pet, parent, false);
        return new StorePetsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StorePetsViewHolder holder, int position) {
        final Pet current = petList.get(position);
        holder.lyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onClick(position);
            }
        });
        holder.title.setText(current.getName());
        String price = "¥ " + current.getPrice();
        holder.price.setText(price);
        Glide.with(context)
                .load(current.getPhoto_url())
                .into(holder.image);
        Log.d(TAG, "onBindViewHolder");
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    static class StorePetsViewHolder extends RecyclerView.ViewHolder{

        private View lyt;
        private ImageView image;
        private TextView title;
        private ImageButton more;
        private TextView price;

        public StorePetsViewHolder(@NonNull View itemView) {
            super(itemView);

            lyt = itemView.findViewById(R.id.lyt_parent);
            image = itemView.findViewById(R.id.item_shop_product_card_image);
            title = itemView.findViewById(R.id.item_shop_product_card_title);
            more = itemView.findViewById(R.id.item_shop_product_card_more);
            price = itemView.findViewById(R.id.item_shop_product_card_price);
        }

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{

        void onClick(int position);
    }

}
