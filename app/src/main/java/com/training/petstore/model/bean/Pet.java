package com.training.petstore.model.bean;

import java.io.Serializable;

public class Pet implements Serializable {

    private String pet_id;
    private String name;
    private String birth;
    private String variety;
    private String description;
    private String photo_url;
    private String on_sell;
    private int price;
    private String user_id;
    private boolean checked = false;

    public String getPet_id() {
        return pet_id;
    }

    public Pet(){}

    public Pet(String pet_id, String name, String birth, String variety, String description,
               String photo_url, String on_sell, int price, String user_id) {
        this.pet_id = pet_id;
        this.name = name;
        this.birth = birth;
        this.variety = variety;
        this.description = description;
        this.photo_url = photo_url;
        this.on_sell = on_sell;
        this.price = price;
        this.user_id = user_id;
    }

    public void setPet_id(String pet_id) {
        this.pet_id = pet_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getOn_sell() {
        return on_sell;
    }

    public void setOn_sell(String on_sell) {
        this.on_sell = on_sell;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
