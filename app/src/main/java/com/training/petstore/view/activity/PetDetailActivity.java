package com.training.petstore.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.training.petstore.MyApplication;
import com.training.petstore.R;
import com.training.petstore.model.bean.Pet;
import com.training.petstore.model.bean.Transaction;
import com.training.petstore.model.bean.User;
import com.training.petstore.util.common.CommonUtil;
import com.training.petstore.util.constants.IntConstants;
import com.training.petstore.util.net.NetImplementation;

import java.util.Date;

public class PetDetailActivity extends AppCompatActivity {

    Pet pet = null;
    User user = null;
    int position = -1;

    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    MyHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_detail);

        user = MyApplication.getUser();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        Intent intent = getIntent();
        Bundle bundle;
        if(intent != null && (bundle = intent.getBundleExtra("pet")) != null) {
            pet = (Pet) bundle.getSerializable("pet");
            position = bundle.getInt("position", -1);
        }

        initView();

    }

    public void initView(){

        ImageView petImage = findViewById(R.id.activity_pet_detail_image);
        TextView petName = findViewById(R.id.activity_pet_detail_pet_name);
        TextView petPrice = findViewById(R.id.activity_pet_detail_price);
        TextView petDescription = findViewById(R.id.activity_pet_detail_pet_description);
        TextView petBirth = findViewById(R.id.activity_pet_detail_pet_birth);
        TextView petVariety = findViewById(R.id.activity_pet_detail_pet_variety);
        TextView petSeller = findViewById(R.id.activity_pet_detail_pet_seller);
        FloatingActionButton buyPetBtn = findViewById(R.id.activity_pet_detail_fab);

        buyPetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pet.getUser_id().equals(user.getUserId()))
                    Toast.makeText(PetDetailActivity.this, "不能购买自己的宠物", Toast.LENGTH_SHORT).show();
                else if(pet.getPrice() > user.getAsset())
                    Toast.makeText(PetDetailActivity.this, "账户余额不足", Toast.LENGTH_SHORT).show();
                else
                    showPurchaseDialog();
            }
        });

        Glide.with(this)
             .load(pet.getPhoto_url())
             .into(petImage);

        petName.setText(pet.getName());
        String price = "¥" + pet.getPrice();
        petPrice.setText(price);
        petDescription.setText(pet.getDescription());
        petBirth.setText(pet.getBirth());
        petVariety.setText(pet.getVariety());
        petSeller.setText(pet.getUser_id());

    }

    public void showPurchaseDialog(){
        alertDialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_info)
                .setTitle("添加到购物车")
                .setCancelable(true)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addToCart();
                    }
                })
                .create();
        alertDialog.show();
        myHandler = new MyHandler(this, alertDialog);
    }

    public void addToCart(){

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在添加到购物车...");
        progressDialog.show();

        NetImplementation.addToCart(user.getUserId(), pet.getPet_id(), myHandler);

    }

    public void finishTransaction(){
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        setResult(RESULT_OK);
        finish();
    }

    static class MyHandler extends Handler{

        PetDetailActivity petDetailActivity;
        AlertDialog alertDialog;

        public MyHandler(PetDetailActivity petDetailActivity, AlertDialog alertDialog){
            this.petDetailActivity = petDetailActivity;
            this.alertDialog = alertDialog;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == IntConstants.FINISH_TRANSACTION) {
                petDetailActivity.show((String) msg.obj);
                petDetailActivity.finishTransaction();
            }else if(msg.what == IntConstants.CODE_QUERY_FAIL || msg.what == IntConstants.ADD_TO_CART){
                petDetailActivity.show((String) msg.obj);
            }
        }
    }

    private void show(String obj) {
        Toast.makeText(this, obj, Toast.LENGTH_SHORT).show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

}