package com.training.petstore.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.jaeger.library.StatusBarUtil;
import com.training.petstore.R;
import com.training.petstore.model.bean.User;
import com.training.petstore.util.common.CommonUtil;
import com.training.petstore.util.constants.IntConstants;
import com.training.petstore.util.net.NetImplementation;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ManagerLoginActivity extends AppCompatActivity implements PickiTCallbacks {

    private final int REQUEST_CODE_PICK_IMAGE = 700;

    private TextInputEditText accountEditText;
    private TextInputEditText passwordEditText;
    private Button loginBtn;
    private TextView registerBtn;
    private ProgressDialog progressDialog;

    PickiT pickiT;
    List<Uri> mSelected;
    CircleImageView photo;
    String imagePath;

    private TextInputEditText userId;
    private TextInputEditText password;
    private TextInputEditText nickName;
    private TextInputEditText asset;

    MyHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_login);

        myHandler = new MyHandler(this);
        pickiT = new PickiT(this, this, this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        initView();

    }

    public void initView(){

        StatusBarUtil.setTransparent(this);

        accountEditText = findViewById(R.id.activity_manager_login_account_edit_text);
        passwordEditText = findViewById(R.id.activity_manager_login_password_edit_text);
        loginBtn = findViewById(R.id.activity_manager_login_login_button);
        registerBtn = findViewById(R.id.activity_manager_login_sign_in);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        accountEditText.addTextChangedListener(new CommonUtil.MyTextWatcher(accountEditText, "请输入邮箱", CommonUtil.CHECK_ACCOUNT));
        passwordEditText.addTextChangedListener(new CommonUtil.MyTextWatcher(passwordEditText, "密码长度至少6位", CommonUtil.CHECK_PASSWORD));

    }

    public void login(){

        if(accountEditText.getText() == null || passwordEditText.getText() == null){
            Toast.makeText(this, "登录失败，请重试", Toast.LENGTH_SHORT).show();
            return;
        }
        String user_id = accountEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if(!CommonUtil.isMailBox(user_id)){
            Toast.makeText(this, "请输入邮箱", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length() < 6){
            Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("提示");
        progressDialog.setMessage("登录中...");
        progressDialog.show();

        NetImplementation.accountLogin(user_id, password, true, myHandler);
    }

    public void register(){

        final Dialog dialog = new Dialog(this, R.style.FullScreenDialogStyle);
        final View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_full_screen, null);
        Switch isAdminSwitch = contentView.findViewById(R.id.dialog_full_screen_switch);
        isAdminSwitch.setChecked(true);
        isAdminSwitch.setEnabled(false);

        userId = contentView.findViewById(R.id.dialog_full_screen_account);
        password = contentView.findViewById(R.id.dialog_full_screen_password);
        nickName = contentView.findViewById(R.id.dialog_full_screen_name);
        asset = contentView.findViewById(R.id.dialog_full_screen_asset);

        userId.addTextChangedListener(new CommonUtil.MyTextWatcher(userId, "请输入邮箱", CommonUtil.CHECK_ACCOUNT));
        password.addTextChangedListener(new CommonUtil.MyTextWatcher(password, "密码长度至少6位", CommonUtil.CHECK_PASSWORD));
        nickName.addTextChangedListener(new CommonUtil.MyTextWatcher(nickName, "名字不能为空", CommonUtil.CHECK_NICKNAME));
        asset.addTextChangedListener(new CommonUtil.MyTextWatcher(asset, "资产必须为整数", CommonUtil.CHECK_ASSET));

        photo = contentView.findViewById(R.id.dialog_full_screen_photo);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.pickImage(ManagerLoginActivity.this, REQUEST_CODE_PICK_IMAGE);
            }
        });

        Button button = contentView.findViewById(R.id.dialog_full_screen_btn);
        button.setText("注册");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(imagePath == null || !new File(imagePath).exists()){
                    Toast.makeText(ManagerLoginActivity.this, "检测不到图片，请重新选择", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!CommonUtil.isAccountInputOK(userId.getText().toString(), password.getText().toString(),
                        nickName.getText().toString(), asset.getText().toString())){
                    Toast.makeText(ManagerLoginActivity.this, "输入有误，请检查输入", Toast.LENGTH_SHORT).show();
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

    public void getPhotoUrl(){
        NetImplementation.uploadImage(this, imagePath, myHandler);
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

    static class MyHandler extends Handler {

        ManagerLoginActivity managerLoginActivity;

        public MyHandler(ManagerLoginActivity managerLoginActivity){
            this.managerLoginActivity = managerLoginActivity;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == IntConstants.UPLOAD_IMAGE_FAILED){
                managerLoginActivity.onUploadImageFailed();
            }else if(msg.what == IntConstants.UPLOAD_IMAGE_SUCCEEDED){
                managerLoginActivity.onUploadImageSucceeded();
                String photo_url = (String) msg.obj;
                managerLoginActivity.register(photo_url);
            }else if(msg.what == IntConstants.CODE_LOGIN_FAIL){
                managerLoginActivity.onFail((String) msg.obj);
            }else if(msg.what == IntConstants.CODE_LOGIN_SUCCESS){
                managerLoginActivity.onSuccess((String) msg.obj);
            }else if(msg.what == IntConstants.CODE_REGISTER_FAIL){
                managerLoginActivity.onFail((String) msg.obj);
            }else if(msg.what == IntConstants.CODE_REGISTER_SUCCESS){
                managerLoginActivity.onSuccess((String) msg.obj);
            }
        }
    }

    private void onSuccess(String obj) {
        Toast.makeText(this, obj, Toast.LENGTH_SHORT).show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        Intent intent = new Intent(this, ManagingActivity.class);
        startActivity(intent);
    }

    private void onFail(String obj) {
        Toast.makeText(this, obj, Toast.LENGTH_SHORT).show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void register(String photo_url) {
        User user = new User(userId.getText().toString(), password.getText().toString(), photo_url,
                "1", nickName.getText().toString(), Integer.parseInt(asset.getText().toString()));

        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在注册...");
        progressDialog.show();

        NetImplementation.accountRegister(user, myHandler);
    }

    private void onUploadImageSucceeded() {
        Toast.makeText(this, "上传图片成功", Toast.LENGTH_SHORT).show();
    }

    private void onUploadImageFailed() {
        Toast.makeText(this, "上传图片失败，请重试", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);
            //imageBtn.setImageURI(mSelected.get(0));
            Glide.with(this)
                    .load(mSelected.get(0))
                    .into(photo);
            pickiT.getPath(mSelected.get(0), Build.VERSION.SDK_INT);
            Log.d("Matisse", "mSelected: " + mSelected);
        }
    }

}