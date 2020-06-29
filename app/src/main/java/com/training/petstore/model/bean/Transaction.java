package com.training.petstore.model.bean;

public class Transaction {
    String transaction_id;
    String transaction_time;
    int price;
    String seller_id;
    String buyer_id;
    String pet_id;
    boolean hasArbitration = false;

    public Transaction(String transaction_id, String transaction_time, int price,
                       String seller_id, String buyer_id, String pet_id) {
        this.transaction_id = transaction_id;
        this.transaction_time = transaction_time;
        this.price = price;
        this.seller_id = seller_id;
        this.buyer_id = buyer_id;
        this.pet_id = pet_id;
    }

    public Transaction(String transaction_id, String transaction_time, int price,
                       String seller_id, String buyer_id, String pet_id, boolean hasArbitration) {
        this.transaction_id = transaction_id;
        this.transaction_time = transaction_time;
        this.price = price;
        this.seller_id = seller_id;
        this.buyer_id = buyer_id;
        this.pet_id = pet_id;
        this.hasArbitration = hasArbitration;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getTransaction_time() {
        return transaction_time;
    }

    public void setTransaction_time(String transaction_time) {
        this.transaction_time = transaction_time;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }

    public String getBuyer_id() {
        return buyer_id;
    }

    public void setBuyer_id(String buyer_id) {
        this.buyer_id = buyer_id;
    }

    public String getPet_id() {
        return pet_id;
    }

    public void setPet_id(String pet_id) {
        this.pet_id = pet_id;
    }

    public boolean isHasArbitration() {
        return hasArbitration;
    }

    public void setHasArbitration(boolean hasArbitration) {
        this.hasArbitration = hasArbitration;
    }
}
