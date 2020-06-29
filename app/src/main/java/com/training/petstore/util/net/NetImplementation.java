package com.training.petstore.util.net;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.training.petstore.MyApplication;
import com.training.petstore.model.bean.Arbitration;
import com.training.petstore.model.bean.Pet;
import com.training.petstore.model.bean.ResponseBean;
import com.training.petstore.model.bean.Transaction;
import com.training.petstore.model.bean.User;
import com.training.petstore.util.common.CommonUtil;
import com.training.petstore.util.constants.IntConstants;
import com.training.petstore.util.constants.StringConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetImplementation {

    private static final String TAG = "调试" + NetImplementation.class.getSimpleName();

    public static void accountLogin(String user_id, String password, boolean isAdmin, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient myHttpClient = MyHttpClient.getInstance();
                Request request = new Request.Builder()
                        .url(StringConstants.URL_ACCOUNT_SELECT + "?user_id=" + user_id)
                        .get()
                        .build();
                try {
                    Message message = new Message();
                    message.what = IntConstants.CODE_LOGIN_FAIL;

                    if(isNotWebConnect()){
                        message.obj = "网络无连接";
                        handler.sendMessage(message);
                        return;
                    }

                    Response response = myHttpClient.newCall(request).execute();
                    if(response.code() != 200){
                        message.obj = "响应出错，登录失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，登录失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    String accountSelectJson = responseBody.string();
                    ResponseBean.AccountSelect accountSelect = JSON.parseObject(accountSelectJson, ResponseBean.AccountSelect.class);
                    if(accountSelect == null){
                        message.obj = "解析失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    if(accountSelect.getRet_code() == -1){
                        message.obj = "账号不存在，登录失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    if(isAdmin && !"1".equals(accountSelect.getAdministrator())){
                        message.obj = "非管理员，登录失败";
                        handler.sendMessage(message);
                        return;
                    }
                    if(!password.equals(accountSelect.getPassword())){
                        message.obj = "密码错误，登录失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    // 账号存在且密码正确，成功登录
                    MyApplication.newUser(user_id, accountSelect.getPassword(), accountSelect.getPhoto_url(),
                            accountSelect.getAdministrator(), accountSelect.getName(), accountSelect.getBalance());
                    message.what = IntConstants.CODE_LOGIN_SUCCESS;
                    message.obj = "登录成功";
                    handler.sendMessage(message);
                }catch(IOException e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public static void accountRegister(User user, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.CODE_REGISTER_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                CodeAndMsg codeAndMsg = accountNotExisted(user.getUserId());
                if(codeAndMsg.code == IntConstants.CODE_ERROR || codeAndMsg.code == IntConstants.CODE_EXISTED){
                    message.obj = codeAndMsg.msg;
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();

                RequestBody requestBody = new FormBody.Builder()
                        .add("user_id", user.getUserId())
                        .add("password", user.getPassword())
                        .add("name", user.getNickName())
                        .add("balance", String.valueOf(user.getAsset()))
                        .add("photo_url", user.getPhoto_url())
                        .add("administrator", user.getIsAdmin())
                        .build();

                Request request = new Request.Builder()
                        .url(StringConstants.URL_ACCOUNT_REGIST)
                        .post(requestBody)
                        .build();
                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，注册失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，注册失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                            responseBody.string(), ResponseBean.GeneralResponse.class);
                    if(generalResponse == null){
                        message.obj = "解析失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    if(generalResponse.getResult().size() > 0
                            && generalResponse.getResult().get(0).getData() == 0){
                        message.what = IntConstants.CODE_REGISTER_SUCCESS;
                        message.obj = "注册成功";
                        handler.sendMessage(message);
                    }
                    else {
                        message.obj = "注册失败，请重试";
                        handler.sendMessage(message);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void accountModify(User user, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.CODE_MODIFY_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                CodeAndMsg codeAndMsg = accountNotExisted(user.getUserId());
                if(codeAndMsg.code == IntConstants.CODE_ERROR || codeAndMsg.code == IntConstants.CODE_NOT_EXISTED){
                    message.obj = codeAndMsg.msg;
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();

                RequestBody requestBody = new FormBody.Builder()
                        .add("user_id", user.getUserId())
                        .add("password", user.getPassword())
                        .add("name", user.getNickName())
                        .add("balance", String.valueOf(user.getAsset()))
                        .add("photo_url", user.getPhoto_url())
                        .add("administrator", user.getIsAdmin())
                        .build();

                Request request = new Request.Builder()
                        .url(StringConstants.URL_ACCOUNT_UPDATE)
                        .post(requestBody)
                        .build();
                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，修改失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，修改失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                            responseBody.string(), ResponseBean.GeneralResponse.class);
                    if(generalResponse == null){
                        message.obj = "解析失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    if(generalResponse.getResult().size() > 0
                            && generalResponse.getResult().get(0).getData() == 0){
                        message.what = IntConstants.CODE_MODIFY_SUCCESS;
                        message.obj = "修改成功";
                    }
                    else{
                        message.obj = "修改失败，请重试";
                    }
                    handler.sendMessage(message);

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void accountDelete(String user_id, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.CODE_DELETE_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                CodeAndMsg codeAndMsg = accountNotExisted(user_id);
                if(codeAndMsg.code == IntConstants.CODE_ERROR || codeAndMsg.code == IntConstants.CODE_NOT_EXISTED){
                    message.obj = codeAndMsg.msg;
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();

                RequestBody requestBody = new FormBody.Builder()
                        .add("user_id", user_id)
                        .build();

                Request request = new Request.Builder()
                        .url(StringConstants.URL_ACCOUNT_REMOVE)
                        .post(requestBody)
                        .build();
                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，删除失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，删除失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                            responseBody.string(), ResponseBean.GeneralResponse.class);
                    if(generalResponse == null){
                        message.obj = "解析失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    if(generalResponse.getResult().size() > 0
                            && generalResponse.getResult().get(0).getData() == 0){
                        message.what = IntConstants.CODE_DELETE_SUCCESS;
                        message.obj = "删除成功";
                    }
                    else {
                        message.obj = "删除失败，请重试";
                    }
                    handler.sendMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void queryAccount(User user, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.QUERY_ACCOUNT_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();
                Request request = new Request.Builder()
                        .url(StringConstants.URL_ACCOUNT_SELECT + "?user_id=" + user.getUserId())
                        .get()
                        .build();
                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，更新账户余额失败";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，更新账户余额失败";
                        handler.sendMessage(message);
                        return;
                    }
                    String accountSelectJson = responseBody.string();
                    ResponseBean.AccountSelect accountSelect = JSON.parseObject(accountSelectJson, ResponseBean.AccountSelect.class);
                    if(accountSelect == null){
                        message.obj = "解析失败，无法更新账户余额";
                        handler.sendMessage(message);
                        return;
                    }
                    if(accountSelect.getRet_code() == -1){
                        message.obj = "更新账户余额失败";
                        handler.sendMessage(message);
                        return;
                    }
                    message.what = IntConstants.FINISH_QUERY_ACCOUNT;
                    message.obj = "更新账余额成功";
                    user.setAsset(accountSelect.getBalance());
                    handler.sendMessage(message);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 私有方法，仅用于验证id是否存在，在子线程中调用
    private static CodeAndMsg accountNotExisted(String user_id){
        OkHttpClient myHttpClient = MyHttpClient.getInstance();

        RequestBody requestBody = new FormBody.Builder()
                .add("user_id", user_id)
                .build();

        Request request = new Request.Builder()
                .url(StringConstants.URL_ACCOUNT_UNIQUEID)
                .post(requestBody)
                .build();
        try {
            Response response = myHttpClient.newCall(request).execute();

            if(response.code() != 200)
                return new CodeAndMsg(IntConstants.CODE_ERROR, "响应出错");

            ResponseBody responseBody;
            if((responseBody = response.body()) == null)
                return new CodeAndMsg(IntConstants.CODE_ERROR, "响应为空，验证ID失败，请重试");

            ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                    responseBody.string(), ResponseBean.GeneralResponse.class);
            if(generalResponse == null)
                return new CodeAndMsg(IntConstants.CODE_ERROR, "解析失败，验证ID失败，请重试");

            if(generalResponse.getResult().size() > 0
                    && generalResponse.getResult().get(0).getData() == 0)
                return new CodeAndMsg(IntConstants.CODE_NOT_EXISTED, "ID不存在");
            else
                return new CodeAndMsg(IntConstants.CODE_EXISTED, "ID已存在");
        }catch (IOException e){
            e.printStackTrace();
        }
        return new CodeAndMsg(IntConstants.CODE_ERROR, "未知错误");
    }

    public static void petRegister(Pet pet, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.CODE_REGISTER_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                String pet_id;
                CodeAndMsg codeAndMsg;
                do{
                    pet_id = String.valueOf((long) (Math.random() * 1e12));
                    codeAndMsg = petNotExisted(pet_id);
                    if(codeAndMsg.code == IntConstants.CODE_ERROR){
                        message.obj = codeAndMsg.msg;
                        handler.sendMessage(message);
                        return;
                    }
                }while(codeAndMsg.code == IntConstants.CODE_EXISTED);

                pet.setPet_id(pet_id);

                OkHttpClient myHttpClient = MyHttpClient.getInstance();

                RequestBody requestBody = new FormBody.Builder()
                        .add("pet_id", pet.getPet_id())
                        .add("name", pet.getName())
                        .add("birth", pet.getBirth())
                        .add("variety", pet.getVariety())
                        .add("description", pet.getDescription())
                        .add("photo_url", pet.getPhoto_url())
                        .add("on_sell", pet.getOn_sell())
                        .add("price", String.valueOf(pet.getPrice()))
                        .add("user_id", pet.getUser_id())
                        .build();

                Request request = new Request.Builder()
                        .url(StringConstants.URL_MARKET_REGISTPET)
                        .post(requestBody)
                        .build();
                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，注册失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，注册失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                            responseBody.string(), ResponseBean.GeneralResponse.class);
                    if(generalResponse == null){
                        message.obj = "解析失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    if(generalResponse.getResult().size() > 0
                            && generalResponse.getResult().get(0).getData() == 0){
                        message.what = IntConstants.CODE_REGISTER_SUCCESS;
                        message.obj = "注册成功";
                    }
                    else
                        message.obj = "注册失败，请重试";
                    handler.sendMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void petModify(Pet pet, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.CODE_MODIFY_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();

                RequestBody requestBody = new FormBody.Builder()
                        .add("pet_id", pet.getPet_id())
                        .add("name", pet.getName())
                        .add("birth", pet.getBirth())
                        .add("variety", pet.getVariety())
                        .add("description", pet.getDescription())
                        .add("photo_url", pet.getPhoto_url())
                        .add("on_sell", pet.getOn_sell())
                        .add("price", String.valueOf(pet.getPrice()))
                        .add("user_id", pet.getUser_id())
                        .build();

                Request request = new Request.Builder()
                        .url(StringConstants.URL_MARKET_UPDATEPET)
                        .post(requestBody)
                        .build();
                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，修改失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，修改失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                            responseBody.string(), ResponseBean.GeneralResponse.class);
                    if(generalResponse == null){
                        message.obj = "解析失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    if(generalResponse.getResult().size() > 0
                            && generalResponse.getResult().get(0).getData() == 0){
                        message.what = IntConstants.CODE_MODIFY_SUCCESS;
                        message.obj = "修改成功";
                    }
                    else{
                        message.obj = "修改失败，请重试";
                    }
                    handler.sendMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void petDelete(List<Pet> pets, Pet pet, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message =  new Message();
                message.what = IntConstants.CODE_DELETE_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();

                RequestBody requestBody = new FormBody.Builder()
                        .add("pet_id", pet.getPet_id())
                        .build();

                Request request = new Request.Builder()
                        .url(StringConstants.URL_MARKET_REMOVEPET)
                        .post(requestBody)
                        .build();
                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，删除失败，请重试";
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，删除失败，请重试";
                        return;
                    }
                    ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                            responseBody.string(), ResponseBean.GeneralResponse.class);
                    if(generalResponse == null){
                        message.obj = "解析失败，请重试";
                        return;
                    }

                    if(generalResponse.getResult().size() > 0
                            && generalResponse.getResult().get(0).getData() == 0){
                        message.what = IntConstants.CODE_DELETE_SUCCESS;
                        message.obj = "删除成功";
                        pets.remove(pet);
                    }
                    else
                        message.obj = "删除失败，请重试";
                    handler.sendMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
/// TODO 2020-06-26 23:24
    public static void selectByUserId(String user_id, List<Pet> pets, Handler handler, int type){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.CODE_QUERY_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();
                String url;
                if(type == IntConstants.GET_MY_PET)
                    url = StringConstants.URL_MARKET_SELECTBYUSERID + "?user_id=" + user_id;
                else
                    url = StringConstants.URL_MARKET_SELECTONSELL;
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                try {
                    Response response = myHttpClient.newCall(request).execute();
                    if(response.code() != 200){
                        message.obj = "响应出错，获取宠物列表失败";
                        handler.sendMessage(message);
                        return;
                    }
                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，获取宠物列表失败";
                        handler.sendMessage(message);
                        return;
                    }

                    List<ResponseBean.ListResultForString> returnList = new ArrayList<>();
                    ResponseBean.ListResultForInt price_list = null;

                    String petListJson = responseBody.string();
                    JSONObject jsonObject = JSON.parseObject(petListJson);
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    for(int i = 0; i < jsonArray.size(); i++){
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        if(jsonObject1.getString("type").equals("int256[]"))
                            price_list = jsonObject1.toJavaObject(ResponseBean.ListResultForInt.class);
                        else returnList.add(jsonObject1.toJavaObject(ResponseBean.ListResultForString.class));
                    }

                    if(returnList.size() != 8 || price_list == null){
                        message.obj = "解析失败";
                        handler.sendMessage(message);
                        return;
                    }
                    pets.clear();
                    for(int i = 0; i < price_list.getData().size(); i++){
                        pets.add(new Pet(returnList.get(0).getData().get(i),
                                returnList.get(1).getData().get(i),
                                returnList.get(2).getData().get(i),
                                returnList.get(3).getData().get(i),
                                returnList.get(4).getData().get(i),
                                returnList.get(5).getData().get(i),
                                returnList.get(6).getData().get(i),
                                price_list.getData().get(i),
                                returnList.get(7).getData().get(i)));
                    }
                    if(type == IntConstants.GET_MY_PET)
                        message.what = IntConstants.GOT_MY_PETS;
                    else
                        message.what = IntConstants.GOT_MARKET_PETS;
                    message.obj = "成功获取宠物列表";
                    handler.sendMessage(message);
                }catch(IOException e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public static void selectByPetId(String pet_id){

    }

    // 用于验证id是否存在
    private static CodeAndMsg petNotExisted(String pet_id){

        OkHttpClient myHttpClient = MyHttpClient.getInstance();

        RequestBody requestBody = new FormBody.Builder()
                .add("pet_id", pet_id)
                .build();

        Request request = new Request.Builder()
                .url(StringConstants.URL_MARKET_UNIQUEID)
                .post(requestBody)
                .build();
        try {
            Response response = myHttpClient.newCall(request).execute();

            if(response.code() != 200)
                return new CodeAndMsg(IntConstants.CODE_ERROR, "响应出错，验证ID失败");

            ResponseBody responseBody;
            if((responseBody = response.body()) == null){
                return new CodeAndMsg(IntConstants.CODE_ERROR, "响应为空，验证ID失败");
            }

            ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                    responseBody.string(), ResponseBean.GeneralResponse.class);
            if(generalResponse == null){
                return new CodeAndMsg(IntConstants.CODE_ERROR, "解析失败，验证ID失败");
            }
            if(generalResponse.getResult().size() > 0
                    && generalResponse.getResult().get(0).getData() == 0)
                return new CodeAndMsg(IntConstants.CODE_NOT_EXISTED, "ID不存在");
            else
                return new CodeAndMsg(IntConstants.CODE_EXISTED, "ID已存在");
        }catch (IOException e){
            e.printStackTrace();
        }
        return new CodeAndMsg(IntConstants.CODE_ERROR, "未知错误");
    }

    public static void allTransactions(List<Transaction> transactions, List<String> arbitratedTransactions, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.CODE_QUERY_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();
                Request request = new Request.Builder()
                        .url(StringConstants.URL_TRANSACTION_ALLTRANSACTIONS)
                        .build();
                try{
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，获取交易列表失败";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，获取交易列表失败";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBean.ListResultForTransaction transactionResult = JSON.parseObject(
                            responseBody.string(), ResponseBean.ListResultForTransaction.class);

                    if(transactionResult == null){
                        message.obj = "解析失败";
                        handler.sendMessage(message);
                        return;
                    }
                    transactions.clear();
                    for(int i = 0; i < transactionResult.getTransaction_id().size(); i++){
                        if(transactionResult.getBuyer_id().get(i).equals(MyApplication.getUser().getUserId())) {
                            transactions.add(new Transaction(transactionResult.getTransaction_id().get(i),
                                    transactionResult.getTransaction_time().get(i),
                                    transactionResult.getPrice().get(i),
                                    transactionResult.getSeller_id().get(i),
                                    transactionResult.getBuyer_id().get(i),
                                    transactionResult.getPet_id().get(i),
                                    arbitratedTransactions.contains(transactionResult.getTransaction_id().get(i))));
                        }
                    }
                    message.what = IntConstants.GOT_TRANSACTION;
                    message.obj = "获取交易记录成功";
                    handler.sendMessage(message);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void sendTransaction(Transaction transaction, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.SEND_TRANSACTION_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                String transaction_id;
                CodeAndMsg codeAndMsg;
                do{
                    transaction_id = String.valueOf((long) (Math.random() * 1e12));
                    codeAndMsg = transactionNotExisted(transaction_id);
                    if(codeAndMsg.code == IntConstants.CODE_ERROR){
                        message.obj = codeAndMsg.msg;
                        handler.sendMessage(message);
                        return;
                    }
                }while(codeAndMsg.code == IntConstants.CODE_EXISTED);

                transaction.setTransaction_id(transaction_id);

                OkHttpClient myHttpClient = MyHttpClient.getInstance();

                RequestBody requestBody = new FormBody.Builder()
                        .add("transaction_id", transaction.getTransaction_id())
                        .add("transaction_time", transaction.getTransaction_time())
                        .add("price", String.valueOf(transaction.getPrice()))
                        .add("seller_id", transaction.getSeller_id())
                        .add("buyer_id", transaction.getBuyer_id())
                        .add("pet_id", transaction.getPet_id())
                        .build();

                Request request = new Request.Builder()
                        .url(StringConstants.URL_TRANSACTION_TRANSFER)
                        .post(requestBody)
                        .build();
                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，交易失败";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，交易失败";
                        handler.sendMessage(message);
                        return;
                    }
                    ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                            responseBody.string(), ResponseBean.GeneralResponse.class);
                    if(generalResponse == null){
                        message.obj = "解析失败";
                        handler.sendMessage(message);
                        return;
                    }
                    if(generalResponse.getResult().size() > 0
                            && generalResponse.getResult().get(0).getData() == 0){
                        message.what = IntConstants.FINISH_TRANSACTION;
                        message.obj = "交易成功";

                        requestBody = new FormBody.Builder()
                                .add("user_id", transaction.getBuyer_id())
                                .add("pet_id", transaction.getPet_id())
                                .build();

                        request = new Request.Builder()
                                .url(StringConstants.URL_CART_REMOVE)
                                .post(requestBody)
                                .build();
                        response = myHttpClient.newCall(request).execute();

                        if(response.code() != 200){
                            message.obj = "交易成功，但购物车删除失败（响应错误）";
                            handler.sendMessage(message);
                            return;
                        }

                        if((responseBody = response.body()) == null){
                            message.obj = "交易成功，但购物车删除失败（响应为空）";
                            handler.sendMessage(message);
                            return;
                        }
                        generalResponse = JSON.parseObject(
                                responseBody.string(), ResponseBean.GeneralResponse.class);
                        if(generalResponse == null){
                            message.obj = "交易成功，但购物车删除失败（解析失败）";
                            handler.sendMessage(message);
                            return;
                        }
                        if(generalResponse.getResult().size() > 0
                                && generalResponse.getResult().get(0).getData() == 0){
                            message.what = IntConstants.FINISH_TRANSACTION;
                            message.obj = "交易成功";
                        }
                        else {
                            message.obj = "交易成功，但购物车删除失败";
                        }
                    }
                    else
                        message.obj = "交易失败";
                    handler.sendMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 用于验证id是否存在
    private static CodeAndMsg transactionNotExisted(String transaction_id){

        OkHttpClient myHttpClient = MyHttpClient.getInstance();

        RequestBody requestBody = new FormBody.Builder()
                .add("name", transaction_id)
                .build();

        Request request = new Request.Builder()
                .url(StringConstants.URL_TRANSACTION_SELECT)
                .post(requestBody)
                .build();
        try {
            Response response = myHttpClient.newCall(request).execute();

            if(response.code() != 200)
                return new CodeAndMsg(IntConstants.CODE_ERROR, "响应出错");

            ResponseBody responseBody;
            if((responseBody = response.body()) == null)
                return new CodeAndMsg(IntConstants.CODE_ERROR, "响应为空");

            ResponseBean.ListResultForTransaction transactionResult = JSON.parseObject(
                    responseBody.string(), ResponseBean.ListResultForTransaction.class);
            if(transactionResult == null)
                return new CodeAndMsg(IntConstants.CODE_ERROR, "解析失败");

            if(transactionResult.getTransaction_id() != null && transactionResult.getTransaction_id().size() == 0)
                return new CodeAndMsg(IntConstants.CODE_NOT_EXISTED, "ID不存在");
            else
                return new CodeAndMsg(IntConstants.CODE_EXISTED, "ID已存在");
        }catch (IOException e){
            e.printStackTrace();
        }
        return new CodeAndMsg(IntConstants.CODE_ERROR, "未知错误");
    }

    public static void requestArbitration(int position, Arbitration arbitration, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {

                Message message = new Message();
                message.arg1 = position;
                message.what = IntConstants.CODE_QUERY_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                String a_id;
                CodeAndMsg codeAndMsg;
                int count = 0;
                do{
                    a_id = String.valueOf((long) (Math.random() * 1e12));
                    codeAndMsg = arbitrationNotExisted(a_id);
                    if(codeAndMsg.code == IntConstants.CODE_ERROR){
                        message.obj = codeAndMsg.msg;
                        handler.sendMessage(message);
                        return;
                    }
                    Log.d(TAG, "验证仲裁ID请求次数: " + count);
                }while(codeAndMsg.code == IntConstants.CODE_EXISTED);

                arbitration.setA_id(a_id);

                OkHttpClient myHttpClient = MyHttpClient.getInstance();

                RequestBody requestBody = new FormBody.Builder()
                        .add("a_id", arbitration.getA_id())
                        .add("t_id", arbitration.getT_id())
                        .add("time", arbitration.getTime())
                        .add("desc", arbitration.getDesc())
                        .add("u_id", arbitration.getU_id())
                        .build();

                Request request = new Request.Builder()
                        .url(StringConstants.URL_ARBITRATION_REQUEST)
                        .post(requestBody)
                        .build();

                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，申请失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，申请失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                            responseBody.string(), ResponseBean.GeneralResponse.class);
                    if(generalResponse == null){
                        message.obj = "解析失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    if(generalResponse.getResult().size() > 0
                            && generalResponse.getResult().get(0).getData() == 0){
                        message.what = IntConstants.FINISH_ARBITRATION_REQUEST;
                        message.obj = "申请成功, 请等待审核";
                    }
                    else
                        message.obj = "申请失败，请重试";
                    handler.sendMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void allArbitration(List<Arbitration> arbitration, Handler handler, int action, List<String> arbitratedTransactions){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.CODE_QUERY_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();
                Request request = new Request.Builder()
                        .url(StringConstants.URL_ARBITRATION_ALLARBITRATIONS)
                        .build();
                try{
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，获取仲裁列表失败";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，获取仲裁列表失败";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBean.ListResultForArbitration arbitrationResult = JSON.parseObject(
                            responseBody.string(), ResponseBean.ListResultForArbitration.class);

                    if(arbitrationResult == null){
                        message.obj = "解析失败";
                        handler.sendMessage(message);
                        return;
                    }
                    if(action == IntConstants.MANAGER_GET_ARBITRATION) {
                        arbitration.clear();
                        for (int i = 0; i < arbitrationResult.getA_id().size(); i++) {
                            arbitration.add(new Arbitration(arbitrationResult.getA_id().get(i),
                                    arbitrationResult.getT_id().get(i),
                                    arbitrationResult.getTime().get(i),
                                    arbitrationResult.getDesc().get(i),
                                    arbitrationResult.getU_id().get(i),
                                    arbitrationResult.getComp().get(i)));
                        }
                    }else if(action == IntConstants.USER_GET_ARBITRATION){
                        arbitratedTransactions.clear();
                        String user_id = MyApplication.getUser().getUserId();
                        for (int i = 0; i < arbitrationResult.getA_id().size(); i++) {
                            if(arbitrationResult.getU_id().get(i).equals(user_id))
                                arbitratedTransactions.add(arbitrationResult.getT_id().get(i));
                        }
                    }
                    message.what = IntConstants.GOT_ARBITRATION;
                    message.obj = "获取仲裁列表成功";
                    handler.sendMessage(message);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void dealWithArbitration(String a_id, int flag, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.CODE_QUERY_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();

                RequestBody requestBody = new FormBody.Builder()
                        .add("a_id", a_id)
                        .add("flag", String.valueOf(flag))
                        .build();

                Request request = new Request.Builder()
                        .url(StringConstants.URL_ARBITRATION_VERIFY)
                        .post(requestBody)
                        .build();
                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，处理仲裁失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，处理仲裁失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                            responseBody.string(), ResponseBean.GeneralResponse.class);
                    if(generalResponse == null){
                        message.obj = "解析失败，处理仲裁失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    if(generalResponse.getResult() != null
                            && generalResponse.getResult().get(0).getData() == 0){
                        message.what = IntConstants.GOT_ARBITRATION;
                        message.obj = "处理成功";
                    }else
                        message.obj = "处理失败";
                    handler.sendMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 用于验证id是否存在
    private static CodeAndMsg arbitrationNotExisted(String a_id){

        OkHttpClient myHttpClient = MyHttpClient.getInstance();

        RequestBody requestBody = new FormBody.Builder()
                .add("id", a_id)
                .build();

        Request request = new Request.Builder()
                .url(StringConstants.URL_ARBITRATION_SELECT)
                .post(requestBody)
                .build();
        try {
            Response response = myHttpClient.newCall(request).execute();

            if(response.code() != 200)
                return new CodeAndMsg(IntConstants.CODE_ERROR, "响应出错");

            ResponseBody responseBody;
            if((responseBody = response.body()) == null)
                return new CodeAndMsg(IntConstants.CODE_ERROR, "响应为空");

            ResponseBean.ListResultForArbitration arbitrationResult = JSON.parseObject(
                    responseBody.string(), ResponseBean.ListResultForArbitration.class);
            if(arbitrationResult == null)
                return new CodeAndMsg(IntConstants.CODE_ERROR, "解析失败");

            if(arbitrationResult.getA_id() != null
                    && arbitrationResult.getA_id().size() == 0)
                return new CodeAndMsg(IntConstants.CODE_NOT_EXISTED, "ID不存在");
            else {
                if(arbitrationResult.getA_id() == null)
                    Log.d(TAG, "getA_id为null");
                else
                    Log.d(TAG, "getA_id的size为" + arbitrationResult.getA_id().size());

                return new CodeAndMsg(IntConstants.CODE_EXISTED, "ID已存在");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return new CodeAndMsg(IntConstants.CODE_ERROR, "未知错误");
    }

    public static void addToCart(String user_id, String pet_id, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.ADD_TO_CART;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                CodeAndMsg codeAndMsg = isCartExisted(user_id, pet_id);
                if(codeAndMsg.code == IntConstants.CODE_ERROR){
                    message.obj = codeAndMsg.msg;
                    handler.sendMessage(message);
                    return;
                }
                if(codeAndMsg.code == IntConstants.CODE_EXISTED){
                    message.obj = "购物车已存在该宠物";
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();

                RequestBody requestBody = new FormBody.Builder()
                        .add("user_id", user_id)
                        .add("pet_id", pet_id)
                        .build();

                Request request = new Request.Builder()
                        .url(StringConstants.URL_CART_INSERT)
                        .post(requestBody)
                        .build();
                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，加入购物车失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，加入购物车失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                            responseBody.string(), ResponseBean.GeneralResponse.class);
                    if(generalResponse == null){
                        message.obj = "解析失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    if(generalResponse.getResult().size() > 0
                            && generalResponse.getResult().get(0).getData() == 0){
                        message.obj = "加入购物车成功";
                    }
                    else
                        message.obj = "加入购物车失败，请重试";
                    handler.sendMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void getCartPets(List<Pet> pets, String user_id, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = IntConstants.CODE_QUERY_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();
                Request request = new Request.Builder()
                        .url(StringConstants.URL_CART_SELECT + "?user_id=" + user_id)
                        .build();
                try{
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，查询购物车失败";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，查询购物车失败";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBean.GeneralResponseForString generalResponse = JSON.parseObject(
                            responseBody.string(), ResponseBean.GeneralResponseForString.class);

                    if(generalResponse == null){
                        message.obj = "解析失败";
                        handler.sendMessage(message);
                        return;
                    }
                    List<String> petIds = generalResponse.getResult().get(0).getData();

                    List<Pet> newPets = new ArrayList<>();

                    for(int i = 0; i < petIds.size(); i++){
                        request = new Request.Builder()
                                .url(StringConstants.URL_MARKET_SELECTBYPETID + "?pet_id=" + petIds.get(i))
                                .build();
                        response = myHttpClient.newCall(request).execute();

                        if(response.code() != 200 || (responseBody = response.body()) == null){
                            message.obj = "响应错误，查询购物车失败";
                            handler.sendMessage(message);
                            return;
                        }

                        List<ResponseBean.ResultForString> returnList = new ArrayList<>();
                        ResponseBean.Result price = null;

                        String petListJson = responseBody.string();
                        JSONObject jsonObject = JSON.parseObject(petListJson);
                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        for(int j = 0; j < jsonArray.size(); j++){
                            JSONObject jsonObject1 = jsonArray.getJSONObject(j);
                            if(jsonObject1.getString("type").equals("int256"))
                                price = jsonObject1.toJavaObject(ResponseBean.Result.class);
                            else returnList.add(jsonObject1.toJavaObject(ResponseBean.ResultForString.class));
                        }

                        if(returnList.size() != 8 || price == null){
                            message.obj = "解析失败";
                            handler.sendMessage(message);
                            return;
                        }

                        newPets.add(new Pet(
                                returnList.get(0).getData(),
                                returnList.get(1).getData(),
                                returnList.get(2).getData(),
                                returnList.get(3).getData(),
                                returnList.get(4).getData(),
                                returnList.get(5).getData(),
                                returnList.get(6).getData(),
                                price.getData(),
                                returnList.get(7).getData()
                        ));
                    }

                    pets.clear();
                    pets.addAll(newPets);
                    message.what = IntConstants.CODE_QUERY_SUCCESS;
                    message.obj = "查询购物车成功";
                    handler.sendMessage(message);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void cartDelete(String user_id, String pet_id, Handler handler, int position){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.arg1 = position;
                message.what = IntConstants.CODE_DELETE_FAIL;

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();

                RequestBody requestBody = new FormBody.Builder()
                        .add("user_id", user_id)
                        .add("pet_id", pet_id)
                        .build();

                Request request = new Request.Builder()
                        .url(StringConstants.URL_CART_REMOVE)
                        .post(requestBody)
                        .build();
                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        message.obj = "响应出错，删除失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }

                    ResponseBody responseBody;
                    if((responseBody = response.body()) == null){
                        message.obj = "响应为空，删除失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                            responseBody.string(), ResponseBean.GeneralResponse.class);
                    if(generalResponse == null){
                        message.obj = "解析失败，请重试";
                        handler.sendMessage(message);
                        return;
                    }
                    if(generalResponse.getResult().size() > 0
                            && generalResponse.getResult().get(0).getData() == 0){
                        message.what = IntConstants.CODE_DELETE_SUCCESS;
                        message.obj = "删除成功";
                    }
                    else {
                        message.obj = "删除失败，请重试";
                    }
                    handler.sendMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static CodeAndMsg isCartExisted(String user_id, String pet_id){

        OkHttpClient myHttpClient = MyHttpClient.getInstance();

        RequestBody requestBody = new FormBody.Builder()
                .add("user_id", user_id)
                .add("pet_id", pet_id)
                .build();

        Request request = new Request.Builder()
                .url(StringConstants.URL_CART_UNIQUE)
                .post(requestBody)
                .build();
        try {
            Response response = myHttpClient.newCall(request).execute();

            if(response.code() != 200)
                return new CodeAndMsg(IntConstants.CODE_ERROR, "响应出错，验证ID失败");

            ResponseBody responseBody;
            if((responseBody = response.body()) == null){
                return new CodeAndMsg(IntConstants.CODE_ERROR, "响应为空，验证ID失败");
            }

            ResponseBean.GeneralResponse generalResponse = JSON.parseObject(
                    responseBody.string(), ResponseBean.GeneralResponse.class);
            if(generalResponse == null){
                return new CodeAndMsg(IntConstants.CODE_ERROR, "解析失败，验证ID失败");
            }
            if(generalResponse.getResult().size() > 0
                    && generalResponse.getResult().get(0).getData() == 0)
                return new CodeAndMsg(IntConstants.CODE_NOT_EXISTED, "ID不存在");
            else
                return new CodeAndMsg(IntConstants.CODE_EXISTED, "ID已存在");
        }catch (IOException e){
            e.printStackTrace();
        }
        return new CodeAndMsg(IntConstants.CODE_ERROR, "未知错误");

    }

    public static void uploadImage(Activity activity, String path, Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();

                File file = new File(path);
                if(!file.exists()){
                    message.what = IntConstants.UPLOAD_IMAGE_FAILED;
                    handler.sendMessage(message);
                    return;
                }

                if(isNotWebConnect()){
                    message.obj = "网络无连接";
                    message.what = IntConstants.UPLOAD_IMAGE_FAILED;
                    handler.sendMessage(message);
                    return;
                }

                OkHttpClient myHttpClient = MyHttpClient.getInstance();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("blob", file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file))
                        .build();

                Request request = new Request.Builder()
                        .url(StringConstants.URL_UPLOAD_IMAGE)
                        .post(requestBody)
                        .build();

                try {
                    Response response = myHttpClient.newCall(request).execute();

                    if(response.code() != 200){
                        CommonUtil.MainThreadToast(activity, "响应出错，获取图片url失败");
                        return;
                    }

                    ResponseBody responseBody = response.body();
                    if(responseBody == null) {
                        message.what = IntConstants.UPLOAD_IMAGE_FAILED;
                        handler.sendMessage(message);
                        return;
                    }
                    String html = responseBody.string();
                    String regEx1 = "<h1>MD5: (.*?)</h1>";
                    Pattern p;
                    Matcher m;
                    p = Pattern.compile(regEx1);
                    m = p.matcher(html);
                    String md5 = null;
                    if(m.find()){
                        md5 = m.group(1);
                    }
                    if(md5 == null){
                        message.what = IntConstants.UPLOAD_IMAGE_FAILED;
                        handler.sendMessage(message);
                        return;
                    }
                    String photo_url = StringConstants.URL_UPLOAD_IMAGE_BASE + md5;
                    message.what = IntConstants.UPLOAD_IMAGE_SUCCEEDED;
                    message.obj = photo_url;
                    handler.sendMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public static class CodeAndMsg{
        int code;
        String msg;

        public CodeAndMsg(int code, String msg){
            this.code = code;
            this.msg = msg;
        }
    }

    public static boolean isNotWebConnect(){
        ConnectivityManager manager = (ConnectivityManager) MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(manager == null)
            return true;
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return !networkInfo.isConnected();
        }
        return true;
    }

}
