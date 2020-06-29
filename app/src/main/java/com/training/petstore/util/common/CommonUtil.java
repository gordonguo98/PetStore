package com.training.petstore.util.common;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.training.petstore.R;
import com.training.petstore.view.activity.ManagePetsActivity;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;

public class CommonUtil {

    public static final int CHECK_ACCOUNT = 500;
    public static final int CHECK_PASSWORD = 501;
    public static final int CHECK_NICKNAME = 502;
    public static final int CHECK_ASSET = 503;

    public static String dateToString(Date date){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINA);
        return simpleDateFormat.format(date);

    }

    public static boolean isMailBox(String s){
        return s.matches("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
    }

    public static boolean isAccountInputOK(String user_id, String password, String nickname, String asset){
        return isMailBox(user_id) && password.length() >= 6
                && nickname.length() != 0 && asset.matches("^(\\d+)$");
    }

    public static class MyTextWatcher implements TextWatcher {

        TextInputEditText textInputEditText;
        String error;
        int type;

        public MyTextWatcher(TextInputEditText inputEditText, String error, int type){
            this.textInputEditText = inputEditText;
            this.error = error;
            this.type = type;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch(type) {
                case CHECK_ACCOUNT:
                    if(!s.toString().matches("^([0-9]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"))
                        textInputEditText.setError(error);
                    break;
                case CHECK_PASSWORD:
                    if(s.toString().length() < 6)
                        textInputEditText.setError(error);
                    break;
                case CHECK_NICKNAME:
                    if(s.toString().length() == 0)
                        textInputEditText.setError(error);
                    break;
                case CHECK_ASSET:
                    if(!s.toString().matches("^(\\d+)$"))
                        textInputEditText.setError(error);
                    break;
                default:
                    break;
            }
        }
    }

    public static void MainThreadToast(Activity activity, String info){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, info, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void pickImage(Activity activity, int REQUEST_CODE_CHOOSE){

        Matisse.from(activity)
                .choose(MimeType.ofImage())
                .theme(R.style.Theme_Image_Selector)
                .countable(false)
                .maxSelectable(1)
                .gridExpectedSize(activity.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.5f)
                .imageEngine(new GlideEngine())
                .showPreview(true)
                .forResult(REQUEST_CODE_CHOOSE);
    }

}
