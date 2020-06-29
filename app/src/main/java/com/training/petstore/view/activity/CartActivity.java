package com.training.petstore.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.training.petstore.MyApplication;
import com.training.petstore.R;
import com.training.petstore.model.bean.Pet;
import com.training.petstore.model.bean.Transaction;
import com.training.petstore.util.common.CommonUtil;
import com.training.petstore.util.constants.IntConstants;
import com.training.petstore.util.net.NetImplementation;
import com.training.petstore.view.adapter.CartAdapter;
import com.training.petstore.view.adapter.RecyclerViewSpacesItemDecoration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView totalPrice;
    private View submit;

    private List<Pet> pets = new ArrayList<>();
    private CartAdapter cartAdapter;
    private RecyclerView recyclerView;

    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View noItemView;
    private TextView noItemText;

    MyHandler myHandler;
    int count;
    int failTransaction;

    private int total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        myHandler = new MyHandler(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        initView();
    }

    private void initView() {

        submit = findViewById(R.id.activity_cart_submit);
        noItemView = findViewById(R.id.activity_no_item_bg);
        noItemText = findViewById(R.id.activity_no_item_bg_hint);
        swipeRefreshLayout = findViewById(R.id.activity_cart_swipe_refresh);
        toolbar = findViewById(R.id.toolbar);
        totalPrice = findViewById(R.id.activity_cart_total);

        toolbar.setTitle("购物车");
        String price = "¥ " + total;
        totalPrice.setText(price);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTransactions();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPets();
            }
        });

        cartAdapter = new CartAdapter(this, pets);

        cartAdapter.setOnItemClickListener(new CartAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(CartActivity.this, PetDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("pet", pets.get(position));
                intent.putExtra("pet", bundle);
                startActivity(intent);
            }
        });

        cartAdapter.setOnItemLongClickListener(new CartAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                alertDialog = new AlertDialog.Builder(CartActivity.this)
                        .setIcon(R.drawable.ic_info)
                        .setTitle("删除订单")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCart(position);
                            }
                        })
                        .create();
                alertDialog.show();
            }
        });

        cartAdapter.setOnCheckListener(new CartAdapter.OnCheckListener() {
            @Override
            public void onCheck(int position) {
                pets.get(position).setChecked(true);
                total += pets.get(position).getPrice();
                String price = "¥ " + total;
                totalPrice.setText(price);
            }

            @Override
            public void onUnCheck(int position) {
                pets.get(position).setChecked(false);
                total -= pets.get(position).getPrice();
                String price = "¥ " + total;
                totalPrice.setText(price);
            }
        });

        recyclerView = findViewById(R.id.activity_cart_cart_list);
        recyclerView.setAdapter(cartAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecyclerViewSpacesItemDecoration(2, 2, 2, 2));

        getPets();
    }

    private void sendTransactions() {

        count = pets.size() - 1;
        failTransaction = 0;

        if(count < 0)
            return;

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在交易...");
        progressDialog.show();

        send();
    }

    public void send(){

        Transaction transaction;

        if(pets.get(count).isChecked()) {
            transaction = new Transaction(null, CommonUtil.dateToString(new Date()),
                    pets.get(count).getPrice(), pets.get(count).getUser_id(),
                    MyApplication.getUser().getUserId(), pets.get(count).getPet_id());
            NetImplementation.sendTransaction(transaction, myHandler);
        }else{
            count--;
            if(count >= 0)
                send();
        }
    }

    public void deleteCart(int position){

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在删除...");
        progressDialog.show();

        NetImplementation.cartDelete(MyApplication.getUser().getUserId(), pets.get(position).getPet_id(), myHandler, position);

    }

    public void getPets(){

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在查询...");
        progressDialog.show();

        NetImplementation.getCartPets(pets, MyApplication.getUser().getUserId(), myHandler);
    }

    public void refreshPets(){
        if(pets.size() == 0 && noItemView.getVisibility() == View.GONE){
            noItemView.setVisibility(View.VISIBLE);
            noItemText.setText("当前购物车为空");
        }else{
            if(noItemView.getVisibility() == View.VISIBLE)
                noItemView.setVisibility(View.GONE);
        }
        cartAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            setResult(RESULT_OK);
            finish();
        }
        return true;
    }

    static class MyHandler extends Handler {

        CartActivity cartActivity;

        public MyHandler(CartActivity cartActivity){
            this.cartActivity = cartActivity;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case IntConstants.CODE_QUERY_SUCCESS:
                    cartActivity.show((String) msg.obj);
                    cartActivity.refreshPets();
                    break;
                case IntConstants.CODE_QUERY_FAIL:
                case IntConstants.CODE_DELETE_FAIL:
                    cartActivity.show((String) msg.obj);
                    break;
                case IntConstants.SEND_TRANSACTION_FAIL:
                    cartActivity.onTransactionFail();
                    break;
                case IntConstants.FINISH_TRANSACTION:
                    cartActivity.onTransactionSuccess();
                    break;
                case IntConstants.CODE_DELETE_SUCCESS:
                    cartActivity.onDeleteSuccess(msg);
                    break;
                default: break;
            }
        }
    }

    public void onDeleteSuccess(Message msg){
        show((String) msg.obj);
        Pet pet = pets.remove(msg.arg1);
        if(pet.isChecked()) {
            total -= pet.getPrice();
            String price = "¥ " + total;
            totalPrice.setText(price);
        }
        refreshPets();
    }

    public void onTransactionFail(){
        failTransaction++;
        continueTransaction();
    }

    public void onTransactionSuccess(){
        pets.remove(count);
        continueTransaction();
    }

    public void continueTransaction(){
        count--;
        if(count >= 0){
            send();
        }else{
            show("交易完成，共有" + failTransaction + "条交易执行失败");
            for(int i = 0; i < pets.size(); i++)
                pets.get(i).setChecked(false);
            total = 0;
            String price = "¥ " + total;
            totalPrice.setText(price);
            refreshPets();
        }
    }

    public void show(String obj){
        Toast.makeText(this, obj, Toast.LENGTH_SHORT).show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

}