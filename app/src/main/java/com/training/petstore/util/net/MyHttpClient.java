package com.training.petstore.util.net;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MyHttpClient{

    public static OkHttpClient myHttpClient;

    public static OkHttpClient getInstance(){
        if(myHttpClient == null){
            synchronized (MyHttpClient.class){
                if(myHttpClient == null){
                    myHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(5, TimeUnit.SECONDS)
                            .callTimeout(5, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(false)
                            .build();
                }
            }
        }
        return myHttpClient;
    }

}
