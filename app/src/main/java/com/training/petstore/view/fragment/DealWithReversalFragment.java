package com.training.petstore.view.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowId;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.training.petstore.R;
import com.training.petstore.model.bean.Arbitration;
import com.training.petstore.util.constants.IntConstants;
import com.training.petstore.util.net.NetImplementation;
import com.training.petstore.view.adapter.ArbitrationAdapter;
import com.training.petstore.view.adapter.RecyclerViewSpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class DealWithReversalFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private View noItemView;
    private TextView noItemText;
    private ProgressDialog progressDialog;

    private List<Arbitration> arbitrationList = new ArrayList<>();
    private ArbitrationAdapter arbitrationAdapter;

    MyHandler myHandler;

    public DealWithReversalFragment() {
        // Required empty public constructor
    }

    public static DealWithReversalFragment newInstance(String param1, String param2) {
        return new DealWithReversalFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myHandler = new MyHandler(this);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deal_with_reversal, container, false);
        initView(view);
        return view;
    }

    public void initView(View view){
        swipeRefreshLayout = view.findViewById(R.id.fragment_deal_with_reversal_swipe_refresh);
        recyclerView = view.findViewById(R.id.fragment_deal_with_reversal_list);
        noItemView = view.findViewById(R.id.activity_no_item_bg);
        noItemText = view.findViewById(R.id.activity_no_item_bg_hint);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getArbitration();
            }
        });

        showArbitration();
    }

    public void showArbitration(){
        if(arbitrationAdapter == null){
            arbitrationAdapter = new ArbitrationAdapter(getActivity(), arbitrationList);
            arbitrationAdapter.setOnItemClickListener(new ArbitrationAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Arbitration current = arbitrationList.get(position);
                    if(current.getComp().equals("+1")){
                        Toast.makeText(getActivity(), "该仲裁已审核并通过", Toast.LENGTH_SHORT).show();
                    }else if(current.getComp().equals("-1")){
                        Toast.makeText(getActivity(), "该仲裁已审核并拒绝", Toast.LENGTH_SHORT).show();
                    }else{
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                .setTitle("审核仲裁")
                                .setIcon(R.drawable.ic_info)
                                .setMessage("如何处理该仲裁？")
                                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dealWithArbitration(current.getA_id(), IntConstants.REJECT_ARBITRATION);
                                    }
                                })
                                .setPositiveButton("通过", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dealWithArbitration(current.getA_id(), IntConstants.ACCEPT_ARBITRATION);
                                    }
                                })
                                .create();
                        alertDialog.show();
                    }
                }
            });
            recyclerView.setAdapter(arbitrationAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.addItemDecoration(new RecyclerViewSpacesItemDecoration(5, 5, 5, 5));
        }

        getArbitration();
    }

    public void getArbitration(){

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在获取仲裁列表...");
        progressDialog.show();

        NetImplementation.allArbitration(arbitrationList, myHandler, IntConstants.MANAGER_GET_ARBITRATION, null);
    }

    public void refreshArbitration(){
        if(arbitrationList.size() == 0){
            noItemView.setVisibility(View.VISIBLE);
            noItemText.setText("当前没有仲裁记录");
        }else{
            if(noItemView.getVisibility() == View.VISIBLE)
                noItemView.setVisibility(View.GONE);
        }
        arbitrationAdapter.notifyDataSetChanged();
    }

    public void dealWithArbitration(String a_id, int type){

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在处理仲裁...");
        progressDialog.show();

        NetImplementation.dealWithArbitration(a_id,
                type == IntConstants.REJECT_ARBITRATION ? 0 : 1, myHandler);
    }

    static class MyHandler extends Handler {

        DealWithReversalFragment dealWithReversalFragment;

        public MyHandler(DealWithReversalFragment dealWithReversalFragment){
            this.dealWithReversalFragment = dealWithReversalFragment;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == IntConstants.GOT_ARBITRATION){
                dealWithReversalFragment.show((String) msg.obj);
                dealWithReversalFragment.refreshArbitration();
            }else if(msg.what == IntConstants.CODE_QUERY_FAIL){
                dealWithReversalFragment.show((String) msg.obj);
            }
        }
    }

    private void show(String obj) {
        Toast.makeText(getContext(), obj, Toast.LENGTH_SHORT).show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

}