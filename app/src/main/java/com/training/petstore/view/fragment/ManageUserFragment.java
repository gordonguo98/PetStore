package com.training.petstore.view.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.training.petstore.R;
import com.training.petstore.model.bean.User;
import com.training.petstore.util.common.CommonUtil;
import com.training.petstore.util.constants.IntConstants;
import com.training.petstore.util.net.NetImplementation;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ManageUserFragment extends Fragment implements PickiTCallbacks {

    private final String ACTION_REGISTER = "注册";
    private final String ACTION_MODIFY = "修改";

    private final int REQUEST_CODE_PICK_IMAGE = 600;

    PickiT pickiT;
    List<Uri> mSelected;
    CircleImageView photo;
    String imagePath;
    String action;

    MyHandler myHandler;

    private TextInputEditText userId;
    private TextInputEditText password;
    private TextInputEditText nickName;
    private TextInputEditText asset;
    private Switch isAdminSwitch;
    private ProgressDialog progressDialog;
    private Dialog dialog;

    public ManageUserFragment() {
        // Required empty public constructor
    }

    public static ManageUserFragment newInstance() {
        return new ManageUserFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickiT = new PickiT(getContext(), this, getActivity());
        myHandler = new MyHandler(this);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_user, container, false);
        initView(view);
        return view;
    }

    public void initView(final View view){

        View registerBtn = view.findViewById(R.id.fragment_manage_user_register);
        View modifyBtn = view.findViewById(R.id.fragment_manage_user_modify);
        View deleteBtn = view.findViewById(R.id.fragment_manage_user_delete);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action = ACTION_REGISTER;
                showFullScreenDialog();
            }
        });

        modifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action = ACTION_MODIFY;
                showFullScreenDialog();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWrapContentDialog(view);
            }
        });

    }

    public void showFullScreenDialog(){

        Context context = getContext();
        if(context != null) {

            dialog = new Dialog(context, R.style.FullScreenDialogStyle);
            final View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_full_screen, null);

            userId = contentView.findViewById(R.id.dialog_full_screen_account);
            password = contentView.findViewById(R.id.dialog_full_screen_password);
            nickName = contentView.findViewById(R.id.dialog_full_screen_name);
            asset = contentView.findViewById(R.id.dialog_full_screen_asset);
            isAdminSwitch = contentView.findViewById(R.id.dialog_full_screen_switch);

            userId.addTextChangedListener(new CommonUtil.MyTextWatcher(userId, "请输入邮箱", CommonUtil.CHECK_ACCOUNT));
            password.addTextChangedListener(new CommonUtil.MyTextWatcher(password, "密码长度至少6位", CommonUtil.CHECK_PASSWORD));
            nickName.addTextChangedListener(new CommonUtil.MyTextWatcher(nickName, "名字不能为空", CommonUtil.CHECK_NICKNAME));
            asset.addTextChangedListener(new CommonUtil.MyTextWatcher(asset, "资产必须为整数", CommonUtil.CHECK_ASSET));

            photo = contentView.findViewById(R.id.dialog_full_screen_photo);
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtil.pickImage(getActivity(), REQUEST_CODE_PICK_IMAGE);
                }
            });

            Button button = contentView.findViewById(R.id.dialog_full_screen_btn);
            button.setText(action);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(imagePath == null || !new File(imagePath).exists()){
                        Toast.makeText(getActivity(), "检测不到图片，请重新选择", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(!CommonUtil.isAccountInputOK(userId.getText().toString(), password.getText().toString(),
                            nickName.getText().toString(), asset.getText().toString())){
                        Toast.makeText(context, "输入有误，请检查输入", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    getPhotoUrl();
                }
            });
            ImageButton imageButton = contentView.findViewById(R.id.dialog_full_screen_bt_close);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.setContentView(contentView);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);

            dialog.show();
        }

    }

    public void getPhotoUrl(){
        NetImplementation.uploadImage(getActivity(), imagePath, myHandler);
    }

    public void showWrapContentDialog(final View view){

        Context context = getContext();
        if(context != null){
            final Dialog dialog = new Dialog(context);
            View contentView  = LayoutInflater.from(context).inflate(R.layout.dialog_wrap_content, null);
            TextInputEditText editText = (TextInputEditText) contentView.findViewById(R.id.dialog_wrap_content_account_edit_text);
            editText.addTextChangedListener(new CommonUtil.MyTextWatcher(editText, "请输入邮箱", CommonUtil.CHECK_ACCOUNT));
            AppCompatButton appCompatButton = contentView.findViewById(R.id.dialog_wrap_content_bt_delete);
            appCompatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete(editText.getText().toString());
                }
            });
            dialog.setContentView(contentView);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.show();
        }

    }

    public void register(User user){

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在注册...");
        progressDialog.show();

        NetImplementation.accountRegister(user, myHandler);
    }

    public void modify(User user){

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在修改...");
        progressDialog.show();

        NetImplementation.accountModify(user, myHandler);
    }

    public void delete(String userId){

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在删除...");
        progressDialog.show();

        NetImplementation.accountDelete(userId, myHandler);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("调试", requestCode + (data == null ? " data is null" : " data is not null"));
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            mSelected = Matisse.obtainResult(data);
            //imageBtn.setImageURI(mSelected.get(0));
            Glide.with(this)
                    .load(mSelected.get(0))
                    .into(photo);
            pickiT.getPath(mSelected.get(0), Build.VERSION.SDK_INT);
            Log.d("Matisse", "mSelected: " + mSelected);
        }
    }

    @Override
    public void PickiTonUriReturned() {

    }

    @Override
    public void PickiTonStartListener() {

    }

    @Override
    public void PickiTonProgressUpdate(int progress) {

    }

    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {
        imagePath = path;
    }

    public static class MyHandler extends Handler {

        ManageUserFragment manageUserFragment;

        public MyHandler(ManageUserFragment manageUserFragment){
            this.manageUserFragment = manageUserFragment;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == IntConstants.UPLOAD_IMAGE_FAILED){
                manageUserFragment.onUploadImageFailed();
            }else if(msg.what == IntConstants.UPLOAD_IMAGE_SUCCEEDED){
                manageUserFragment.onUploadImageSucceeded();
                String photo_url = (String) msg.obj;
                manageUserFragment.registerOrModifyUser(photo_url);
            }else if(msg.what == IntConstants.CODE_REGISTER_FAIL){
                manageUserFragment.onFail((String) msg.obj);
            }else if(msg.what == IntConstants.CODE_REGISTER_SUCCESS){
                manageUserFragment.onSuccess((String) msg.obj);
            }else if(msg.what == IntConstants.CODE_MODIFY_FAIL){
                manageUserFragment.onFail((String) msg.obj);
            }else if(msg.what == IntConstants.CODE_MODIFY_SUCCESS){
                manageUserFragment.onSuccess((String) msg.obj);
            }else if(msg.what == IntConstants.CODE_DELETE_FAIL){
                manageUserFragment.onFail((String) msg.obj);
            }else if(msg.what == IntConstants.CODE_DELETE_SUCCESS){
                manageUserFragment.onSuccess((String) msg.obj);
            }

        }
    }

    private void onSuccess(String obj) {
        Toast.makeText(getContext(), obj, Toast.LENGTH_SHORT).show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        if(dialog.isShowing())
            dialog.dismiss();
    }

    private void onFail(String obj) {
        Toast.makeText(getContext(), obj, Toast.LENGTH_SHORT).show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void registerOrModifyUser(String photo_url) {
        String isAdmin = isAdminSwitch.isChecked() ? "1" : "0";
        User user = new User(userId.getText().toString(), password.getText().toString(), photo_url,
                isAdmin, nickName.getText().toString(), Integer.parseInt(asset.getText().toString()));
        if(action.equals(ACTION_REGISTER)){



            register(user);
        }else if(action.equals(ACTION_MODIFY)){
            modify(user);
        }
    }

    private void onUploadImageSucceeded() {
        Toast.makeText(getActivity(), "上传图片成功", Toast.LENGTH_SHORT).show();
    }

    private void onUploadImageFailed() {
        Toast.makeText(getActivity(), "上传图片失败，请重试", Toast.LENGTH_SHORT).show();
    }

}