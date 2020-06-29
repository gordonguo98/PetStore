package com.training.petstore.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.training.petstore.R;
import com.training.petstore.model.bean.Arbitration;

import java.util.List;

public class ArbitrationAdapter extends RecyclerView.Adapter<ArbitrationAdapter.ArbitrationViewHolder> {

    Context context;
    List<Arbitration> arbitration;

    ArbitrationAdapter.OnItemClickListener onItemClickListener;

    public ArbitrationAdapter(Context context, List<Arbitration> arbitration){
        this.context = context;
        this.arbitration = arbitration;
    }

    @NonNull
    @Override
    public ArbitrationAdapter.ArbitrationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_arbitration, parent, false);
        return new ArbitrationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArbitrationAdapter.ArbitrationViewHolder holder, int position) {
        Arbitration current = arbitration.get(position);
        String a_id = "仲裁ID: " + current.getA_id();
        holder.arbitrationId.setText(a_id);
        String t_id = "交易ID: " + current.getT_id();
        holder.transactionId.setText(t_id);
        String desc = "申请原因: " + current.getDesc();
        holder.description.setText(desc);
        holder.time.setText(current.getTime());

        if(current.getComp().equals("0")){
            holder.status.setText("待审核");
            holder.status.setTextColor(context.getResources().getColor(R.color.green_200));
        }else if(current.getComp().equals("+1")){
            holder.status.setText("已通过");
            holder.status.setTextColor(context.getResources().getColor(R.color.colorAccent));
        }else{
            holder.status.setText("已拒绝");
            holder.status.setTextColor(context.getResources().getColor(R.color.grey_500));
        }

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null)
                    onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arbitration.size();
    }

    static class ArbitrationViewHolder extends RecyclerView.ViewHolder{

        private View rootView;
        private TextView status;
        private TextView arbitrationId;
        private TextView transactionId;
        private TextView description;
        private TextView time;

        public ArbitrationViewHolder(@NonNull View itemView) {
            super(itemView);

            rootView = itemView.findViewById(R.id.item_arbitration_root);
            status = itemView.findViewById(R.id.item_arbitration_status);
            arbitrationId = itemView.findViewById(R.id.item_arbitration_id);
            transactionId = itemView.findViewById(R.id.item_arbitration_transaction);
            description = itemView.findViewById(R.id.item_arbitration_description);
            time = itemView.findViewById(R.id.item_arbitration_date);
        }
    }

    public void setOnItemClickListener(ArbitrationAdapter.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

}
