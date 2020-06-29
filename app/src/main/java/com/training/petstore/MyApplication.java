package com.training.petstore;

import android.app.Application;
import android.content.Context;

import com.training.petstore.model.bean.User;

public class MyApplication extends Application {

    public static User user;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static void newUser(String userId, String password, String photo_url, String isAdmin,
                               String nickName, int asset){
        user = new User(userId, password, photo_url, isAdmin, nickName, asset);
    }

    public static User getUser(){
        return user;
    }

    public static Context getContext(){
        return context;
    }
}
