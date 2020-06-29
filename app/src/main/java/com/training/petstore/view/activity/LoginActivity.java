package com.training.petstore.view.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.jaeger.library.StatusBarUtil;
import com.training.petstore.R;
import com.training.petstore.util.common.CommonUtil;
import com.training.petstore.util.constants.IntConstants;
import com.training.petstore.util.net.NetImplementation;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "调试" + LoginActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 100;

    private TextInputEditText accountEditText;
    private TextInputEditText passwordEditText;
    private FloatingActionButton loginButton;
    private TextView adminTextView;
    private View rootView;

    private ProgressDialog progressDialog;

    MyHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myHandler = new MyHandler(this);

        askForPermissions();

        initView();

    }

    public void askForPermissions(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_STORAGE);
        }
    }

    public void initView(){

        accountEditText = findViewById(R.id.activity_login_account);
        passwordEditText = findViewById(R.id.activity_login_password);
        loginButton = findViewById(R.id.activity_login_login_fab);
        adminTextView = findViewById(R.id.activity_login_administrator_text_view);
        rootView = findViewById(R.id.activity_login_mask_view);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        StatusBarUtil.setTranslucentForImageView(this, 0, rootView);

        loginButton.setOnClickListener(v -> {
            login();
        });

        adminTextView.setOnClickListener(v -> goToManagerLoginActivity());

        accountEditText.addTextChangedListener(new CommonUtil.MyTextWatcher(accountEditText, "请输入邮箱", CommonUtil.CHECK_ACCOUNT));
        passwordEditText.addTextChangedListener(new CommonUtil.MyTextWatcher(passwordEditText, "密码长度至少6位", CommonUtil.CHECK_PASSWORD));

    }

    public void login() {

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

        NetImplementation.accountLogin(user_id, password, false, myHandler);
    }

    public void goToManagerLoginActivity(){
        Intent intent = new Intent(this, ManagerLoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_STORAGE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length <= 0
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "获取读写权限失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    static class MyHandler extends Handler {

        LoginActivity loginActivity;

        public MyHandler(LoginActivity loginActivity){
            this.loginActivity = loginActivity;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == IntConstants.CODE_LOGIN_FAIL){
                String info = (String) msg.obj;
                loginActivity.onFail(info);
            }else if(msg.what == IntConstants.CODE_LOGIN_SUCCESS){
                String info = (String) msg.obj;
                loginActivity.onSuccess(info);
            }
        }
    }

    public void onFail(String info){
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void onSuccess(String info) {
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        Intent intent = new Intent(this, StoreActivity.class);
        startActivity(intent);
    }

}