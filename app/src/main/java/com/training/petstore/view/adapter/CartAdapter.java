package com.training.petstore.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.training.petstore.R;
import com.training.petstore.model.bean.Pet;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    Context context;
    List<Pet> pets;

    OnCheckListener onCheckListener;
    OnItemClickListener onItemClickListener;
    OnItemLongClickListener onItemLongClickListener;

    public CartAdapter(Context context, List<Pet> pets){
        this.context = context;
        this.pets = pets;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Pet current = pets.get(position);
        Glide.with(context)
                .load(current.getPhoto_url())
                .into(holder.image);
        holder.name.setText(current.getName());
        holder.description.setText(current.getDescription());
        String price = "¥ " + current.getPrice();
        holder.price.setText(price);
        if(!current.isChecked())
            holder.checkBox.setChecked(false);
        else
            holder.checkBox.setChecked(true);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(onCheckListener != null)
                        onCheckListener.onCheck(position);
                }else{
                    if(onCheckListener != null)
                        onCheckListener.onUnCheck(position);
                }
            }
        });
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null)
                    onItemClickListener.onItemClick(position);
            }
        });
        holder.root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(onItemLongClickListener != null)
                    onItemLongClickListener.onItemLongClick(position);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        private View root;
        private ImageView image;
        private TextView name;
        private TextView description;
        private TextView price;
        private CheckBox checkBox;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.item_cart_root);
            image = itemView.findViewById(R.id.item_cart_image);
            name = itemView.findViewById(R.id.item_cart_name);
            description = itemView.findViewById(R.id.item_cart_description);
            price = itemView.findViewById(R.id.item_cart_price);
            checkBox = itemView.findViewById(R.id.item_cart_check);
        }
    }

    public void setOnCheckListener(OnCheckListener onCheckListener){
        this.onCheckListener = onCheckListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener){
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public interface OnCheckListener{
        void onCheck(int position);
        void onUnCheck(int position);
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(int position);
    }

}
