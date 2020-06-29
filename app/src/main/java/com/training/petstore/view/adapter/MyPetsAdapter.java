package com.training.petstore.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.training.petstore.R;
import com.training.petstore.model.bean.Pet;

import java.util.List;

public class MyPetsAdapter extends RecyclerView.Adapter<MyPetsAdapter.MyPetsViewHolder> {

    Context context;
    List<Pet> myPets;

    private OnItemClickListener onItemClickListener;

    public MyPetsAdapter(Context context, List<Pet> petList){
        this.context = context;
        myPets = petList;
    }

    @NonNull
    @Override
    public MyPetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_pet, parent, false);
        return new MyPetsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPetsViewHolder holder, int position) {
        Pet current = myPets.get(position);

        Glide.with(context)
                .load(current.getPhoto_url())
                .into(holder.petImage);
        holder.petName.setText(current.getName());
        String price = "¥" + current.getPrice();
        holder.petPrice.setText(price);
        holder.petState.setText(current.getOn_sell().equals("1") ? "已上架" : "未上架");

        holder.llt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myPets.size();
    }

    static class MyPetsViewHolder extends RecyclerView.ViewHolder {

        private View llt;
        private ImageView petImage;
        private TextView petName;
        private TextView petPrice;
        private TextView petState;

        public MyPetsViewHolder(@NonNull View itemView) {
            super(itemView);

            llt = itemView.findViewById(R.id.item_my_pet_lyt_parent);
            petImage = itemView.findViewById(R.id.item_my_pet_image);
            petName = itemView.findViewById(R.id.item_my_pet_name);
            petPrice = itemView.findViewById(R.id.item_my_pet_price);
            petState = itemView.findViewById(R.id.item_my_pet_state);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{

        void onClick(int position);
    }



}
