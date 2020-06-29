package com.training.petstore.model.bean;

import java.io.Serializable;

public class User implements Serializable {

    private String userId;
    private String password;
    private String photo_url;
    private String isAdmin;
    private String nickName;
    private int asset;

    public User(String userId, String password, String photo_url, String isAdmin,
                String nickName, int asset){
        this.userId = userId;
        this.password = password;
        this.photo_url = photo_url;
        this.isAdmin = isAdmin;
        this.nickName = nickName;
        this.asset = asset;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(String isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getAsset() {
        return asset;
    }

    public void setAsset(int asset) {
        this.asset = asset;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }
}
