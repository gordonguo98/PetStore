package com.training.petstore.view.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.training.petstore.MyApplication;
import com.training.petstore.R;
import com.training.petstore.model.bean.Arbitration;
import com.training.petstore.model.bean.Pet;
import com.training.petstore.model.bean.Transaction;
import com.training.petstore.model.bean.User;
import com.training.petstore.util.common.CommonUtil;
import com.training.petstore.util.common.TestUtil;
import com.training.petstore.util.constants.IntConstants;
import com.training.petstore.util.constants.StringConstants;
import com.training.petstore.util.net.NetImplementation;
import com.training.petstore.view.adapter.MyPetsAdapter;
import com.training.petstore.view.adapter.StorePetsListAdapter;
import com.training.petstore.view.adapter.RecyclerViewSpacesItemDecoration;
import com.training.petstore.view.adapter.TransactionAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoreActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CREATE_PET = 200;
    private static final int REQUEST_CODE_MODIFY_PET = 201;
    private static final int REQUEST_CODE_PURCHASE_PET = 202;
    private static final int REQUEST_CODE_CART = 203;

    private static final String TAG = "调试" + StoreActivity.class.getSimpleName();

    User user = MyApplication.getUser();
    List<String> arbitratedTransactions = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView noItemHint;
    private View noItemView;
    private FloatingActionButton addPetBtn;

    private List<Pet> petList = new ArrayList<>();
    private List<Pet> myPets = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private Toolbar toolbar;
    private StorePetsListAdapter adapter;
    private MyPetsAdapter myPetsAdapter;
    private TransactionAdapter transactionAdapter;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;

    private Drawer drawer;
    private SecondaryDrawerItem item4;
    private AlertDialog alertDialog;

    private ProgressDialog progressDialog;

    MyHandler myHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        initImageLoader();

        initView();

    }

    public void initImageLoader(){
        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder, String tag) {
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.with(imageView.getContext()).clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }
                //we use the default one for
                //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()
                return super.placeholder(ctx, tag);
            }
        });
    }

    public void initView(){

        swipeRefreshLayout = findViewById(R.id.activity_store_swipe_refresh);
        toolbar = findViewById(R.id.toolbar);
        noItemView = findViewById(R.id.activity_no_item_bg);
        noItemHint = findViewById(R.id.activity_no_item_bg_hint);
        addPetBtn = findViewById(R.id.activity_shopping_product_add_btn);
        addPetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreActivity.this, ManagePetsActivity.class);
                intent.putExtra("action", StringConstants.ACTION_CREATE_PET);
                startActivityForResult(intent, REQUEST_CODE_CREATE_PET);
            }
        });

        toolbar.setTitle("宠物市场");
        setSupportActionBar(toolbar);

        initDrawerLayout();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String title = (String) toolbar.getTitle();
                switch(title){
                    case "宠物市场":
                        showMarketPets();
                        break;
                    case "我的宠物":
                        showMyPets();
                        break;
                    case "我的交易":
                        showMyTransactions();
                        break;
                    default: break;
                }
            }
        });

        recyclerView = findViewById(R.id.activity_store_recyclerView);
        recyclerView.addItemDecoration(new RecyclerViewSpacesItemDecoration(10,10,10,10));

        showMarketPets();
    }

    private void initTestData(List<Pet> pets){
        for(int i = 0; i < 10; i++) {
            pets.add(new Pet(TestUtil.generatePetId(), "pet_" + i, TestUtil.generateDate(),
                    TestUtil.PET_VARIETIES[i], "天青色等烟雨，而我咕咕嘎嘎噶，你好，恭喜发财，救命啊，开挖掘机啦", TestUtil.PET_URLS[i],
                    "是", (i + 1) * 100, TestUtil.USER_IDS[i % 5]));
        }
    }

    public void initDrawerLayout(){
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(user.getNickName())
                                .withEmail(user.getUserId())
                                .withIcon(user.getPhoto_url())
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .build();

        PrimaryDrawerItem item1 = new PrimaryDrawerItem()
                .withIcon(R.drawable.ic_store).withIdentifier(1).withName("宠物市场");
        PrimaryDrawerItem item2 = new PrimaryDrawerItem()
                .withIcon(R.drawable.ic_pets).withIdentifier(2).withName("我的宠物");
        PrimaryDrawerItem item3 = new PrimaryDrawerItem()
                .withIcon(R.drawable.ic_exit).withIdentifier(3).withName("退出登录");
        item4 = new SecondaryDrawerItem()
                .withIcon(R.drawable.ic_account_balance).withIdentifier(4)
                .withEnabled(false).withName("资产：¥ " + user.getAsset());
        PrimaryDrawerItem item5 = new PrimaryDrawerItem()
                .withIcon(R.drawable.ic_format_list).withIdentifier(5).withName("我的交易");

        DrawerBuilder drawerBuilder = new DrawerBuilder();
        drawer = drawerBuilder.withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new DividerDrawerItem(),
                        item5,
                        new DividerDrawerItem(),
                        item3,
                        new DividerDrawerItem(),
                        item4
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch((int) drawerItem.getIdentifier()){
                            case 1:
                                toolbar.setTitle("宠物市场");
                                showMarketPets();
                                break;
                            case 2:
                                toolbar.setTitle("我的宠物");
                                showMyPets();
                                break;
                            case 3:
                                exitLogin();
                                break;
                            case 5:
                                toolbar.setTitle("我的交易");
                                showMyTransactions();
                                break;
                            default: break;
                        }
                        return true;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        queryUserInfo();
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .build();
    }

    public void showMyPets(){
        addPetBtn.setVisibility(View.VISIBLE);

        if(myPetsAdapter == null){
            myPetsAdapter = new MyPetsAdapter(this, myPets);
            myPetsAdapter.setOnItemClickListener(new MyPetsAdapter.OnItemClickListener() {
                @Override
                public void onClick(int position) {
                    editOrDeletePet(myPets.get(position));
                }
            });
            linearLayoutManager = new LinearLayoutManager(this);
        }

        recyclerView.setAdapter(myPetsAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        getPets(myPets, IntConstants.GET_MY_PET);
    }

    public void refreshMyPets(){
        if(myPets.size() == 0){
            noItemView.setVisibility(View.VISIBLE);
            noItemHint.setText("你还没有宠物，点击下方按钮创建");
        }else{
            if(noItemView.getVisibility() == View.VISIBLE)
                noItemView.setVisibility(View.GONE);
        }
        myPetsAdapter.notifyDataSetChanged();
    }

    public void showMarketPets(){
        addPetBtn.setVisibility(View.GONE);

        if(adapter == null){
            // use test data
            //initTestData(petList);
            adapter = new StorePetsListAdapter(this, petList);
            adapter.setOnItemClickListener(new StorePetsListAdapter.OnItemClickListener() {
                @Override
                public void onClick(int position) {
                    Pet pet = petList.get(position);
                    goToPetDetailActivity(pet, position);
                }
            });
            gridLayoutManager = new GridLayoutManager(this, 2);
        }

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(gridLayoutManager);

        getPets(petList, IntConstants.GET_MARKET_PET);
    }

    public void refreshMarketPets(){
        if(petList.size() == 0){
            noItemView.setVisibility(View.VISIBLE);
            noItemHint.setText("当前市场还没有宠物，试试将宠物上架");
        }else{
            if(noItemView.getVisibility() == View.VISIBLE)
                noItemView.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    public void getPets(List<Pet> list, int type){

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在获取宠物列表...");
        progressDialog.show();

        NetImplementation.selectByUserId(user.getUserId(), list, myHandler, type);
    }

    public void exitLogin(){
        Toast.makeText(StoreActivity.this, "退出登录", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void showMyTransactions(){
        if(addPetBtn.getVisibility() == View.VISIBLE)
            addPetBtn.setVisibility(View.GONE);

        if(transactionAdapter == null){
            transactionAdapter = new TransactionAdapter(this, transactions);
            transactionAdapter.setOnItemClickListener(new TransactionAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {

                    if(transactions.get(position).isHasArbitration()){
                        Toast.makeText(StoreActivity.this, "该交易已申请仲裁", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    View view  = LayoutInflater.from(StoreActivity.this).inflate(R.layout.request_arbitration_dialog, null);
                    TextInputEditText editText = view.findViewById(R.id.request_arbitration_dialog_edit_text);

                    alertDialog = new AlertDialog.Builder(StoreActivity.this)
                            .setIcon(R.drawable.ic_info)
                            .setTitle("仲裁")
                            .setMessage("申请仲裁需要管理员审核，确认申请？")
                            .setView(view)
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(editText.getText() == null || editText.getText().toString().equals("")){
                                        Toast.makeText(StoreActivity.this, "请输入申请原因", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    requestArbitration(position, editText.getText().toString());
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            });
            if(linearLayoutManager == null)
                linearLayoutManager = new LinearLayoutManager(this);
        }

        recyclerView.setAdapter(transactionAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        //getTransactions(transactions);
        queryArbitration();

    }

    public void getTransactions(){
        NetImplementation.allTransactions(transactions, arbitratedTransactions, myHandler);
    }

    private void queryArbitration() {

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在获取交易记录...");
        progressDialog.show();

        NetImplementation.allArbitration(null, myHandler, IntConstants.USER_GET_ARBITRATION, arbitratedTransactions);
    }

    public void refreshTransactions(){
        if(transactions.size() == 0){
            Log.d(TAG, "refreshTransactions: " + "size=0 执行");
            noItemView.setVisibility(View.VISIBLE);
            noItemHint.setText("当前没有交易记录");
        }else{
            Log.d(TAG, "refreshTransactions: " + "执行");
            if(noItemView.getVisibility() == View.VISIBLE)
                noItemView.setVisibility(View.GONE);
        }
        transactionAdapter.notifyDataSetChanged();
    }

    public void requestArbitration(int position, String description){
        Transaction transaction = transactions.get(position);
        Arbitration arbitration = new Arbitration(null, transaction.getTransaction_id(),
                CommonUtil.dateToString(new Date()), description, user.getUserId());

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在申请...");
        progressDialog.show();

        NetImplementation.requestArbitration(position, arbitration, myHandler);
    }

    public void finishArbitrationRequest(int position){
        if(alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        transactions.get(position).setHasArbitration(true);
        transactionAdapter.notifyItemChanged(position);
    }

    public void goToPetDetailActivity(Pet pet, int position){
        Intent intent = new Intent(this, PetDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("pet", pet);
        bundle.putInt("position", position);
        intent.putExtra("pet", bundle);
        startActivityForResult(intent, REQUEST_CODE_PURCHASE_PET);
    }

    public void editOrDeletePet(Pet pet){
        AlertDialog alertDialog = new AlertDialog
                .Builder(this)
                .setIcon(R.drawable.ic_notification)
                .setTitle("编辑或删除宠物")
                .setCancelable(true)
                .setPositiveButton("编辑", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editPet(pet);
                    }
                })
                .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePet(pet);
                    }
                }).create();
        alertDialog.show();
        Toast.makeText(this, "点击", Toast.LENGTH_SHORT).show();
    }

    public void editPet(Pet pet){
        Intent intent = new Intent(this, ManagePetsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("pet", pet);
        intent.putExtra("pet", bundle);
        intent.putExtra("action", StringConstants.ACTION_MODIFY_PET);
        startActivityForResult(intent, REQUEST_CODE_MODIFY_PET);
    }

    public void deletePet(Pet pet){

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在删除...");
        progressDialog.show();

        NetImplementation.petDelete(myPets, pet, myHandler);
    }

    public void updateBalance(){
        item4.withName("资产：¥ " + user.getAsset());
        drawer.updateItem(item4);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_store_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.activity_store_menu_item_cart){
            Intent intent = new Intent(this, CartActivity.class);
            startActivityForResult(intent, REQUEST_CODE_CART);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_PURCHASE_PET && resultCode == RESULT_OK){
            getPets(petList, IntConstants.GET_MARKET_PET);
            queryUserInfo();
        }
        if((requestCode == REQUEST_CODE_CREATE_PET || requestCode == REQUEST_CODE_MODIFY_PET) && resultCode == RESULT_OK){
            getPets(myPets, IntConstants.GET_MY_PET);
        }
        if(requestCode == REQUEST_CODE_CART && resultCode == RESULT_OK){
            switch((String) toolbar.getTitle()){
                case "宠物市场":
                    showMarketPets();
                    break;
                case "我的宠物":
                    showMyPets();
                    break;
                case "我的交易":
                    showMyTransactions();
                    break;
                default: break;
            }
        }
    }

    public void queryUserInfo(){
/*
        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在查询余额...");
        progressDialog.show();
*/
        NetImplementation.queryAccount(user, myHandler);
    }

    static class MyHandler extends Handler{

        StoreActivity storeActivity;

        public MyHandler(StoreActivity storeActivity){
            this.storeActivity = storeActivity;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case IntConstants.GOT_MY_PETS:
                case IntConstants.CODE_DELETE_SUCCESS:
                    storeActivity.show((String) msg.obj);
                    storeActivity.refreshMyPets();
                    break;
                case IntConstants.GOT_MARKET_PETS:
                    storeActivity.show((String) msg.obj);
                    storeActivity.refreshMarketPets();
                    break;
                case IntConstants.GOT_TRANSACTION:
                    storeActivity.show((String) msg.obj);
                    storeActivity.refreshTransactions();
                    break;
                case IntConstants.CODE_QUERY_SUCCESS:
                    storeActivity.show((String) msg.obj);
                    storeActivity.updateBalance();
                    break;
                case IntConstants.CODE_QUERY_FAIL:
                case IntConstants.CODE_DELETE_FAIL:
                case IntConstants.QUERY_ACCOUNT_FAIL:
                    storeActivity.show((String) msg.obj);
                    break;
                case IntConstants.FINISH_ARBITRATION_REQUEST:
                    storeActivity.show((String) msg.obj);
                    storeActivity.finishArbitrationRequest(msg.arg1);
                    break;
                case IntConstants.GOT_ARBITRATION:
                    storeActivity.getTransactions();
                    break;
                default:
                    break;
            }
        }
    }

    private void show(String obj) {
        Toast.makeText(this, obj, Toast.LENGTH_SHORT).show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }
}