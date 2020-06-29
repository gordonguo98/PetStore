package com.training.petstore.model.bean;

import java.util.List;

public class ResponseBean {

    public static class Result{
        String name;
        String type;
        int data;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getData() {
            return data;
        }

        public void setData(int data) {
            this.data = data;
        }
    }

    public static class ResultForString{
        String name;
        String type;
        String data;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    public static class ListResultForTransaction{
        List<String> transaction_id;
        List<String> transaction_time;
        List<Integer> price;
        List<String> seller_id;
        List<String> buyer_id;
        List<String> pet_id;

        public List<String> getTransaction_id() {
            return transaction_id;
        }

        public void setTransaction_id(List<String> transaction_id) {
            this.transaction_id = transaction_id;
        }

        public List<String> getTransaction_time() {
            return transaction_time;
        }

        public void setTransaction_time(List<String> transaction_time) {
            this.transaction_time = transaction_time;
        }

        public List<Integer> getPrice() {
            return price;
        }

        public void setPrice(List<Integer> price) {
            this.price = price;
        }

        public List<String> getSeller_id() {
            return seller_id;
        }

        public void setSeller_id(List<String> seller_id) {
            this.seller_id = seller_id;
        }

        public List<String> getBuyer_id() {
            return buyer_id;
        }

        public void setBuyer_id(List<String> buyer_id) {
            this.buyer_id = buyer_id;
        }

        public List<String> getPet_id() {
            return pet_id;
        }

        public void setPet_id(List<String> pet_id) {
            this.pet_id = pet_id;
        }
    }

    public static class ListResultForArbitration{
        List<String> a_id;
        List<String> t_id;
        List<String> time;
        List<String> desc;
        List<String> comp;
        List<String> u_id;

        public List<String> getA_id() {
            return a_id;
        }

        public void setA_id(List<String> a_id) {
            this.a_id = a_id;
        }

        public List<String> getT_id() {
            return t_id;
        }

        public void setT_id(List<String> t_id) {
            this.t_id = t_id;
        }

        public List<String> getTime() {
            return time;
        }

        public void setTime(List<String> time) {
            this.time = time;
        }

        public List<String> getDesc() {
            return desc;
        }

        public void setDesc(List<String> desc) {
            this.desc = desc;
        }

        public List<String> getComp() {
            return comp;
        }

        public void setComp(List<String> comp) {
            this.comp = comp;
        }

        public List<String> getU_id() {
            return u_id;
        }

        public void setU_id(List<String> u_id) {
            this.u_id = u_id;
        }
    }

    public static class ListResultForString{
        String name;
        String type;
        List<String> data;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getData() {
            return data;
        }

        public void setData(List<String> data) {
            this.data = data;
        }
    }

    public static class ListResultForInt{
        String name;
        String type;
        List<Integer> data;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Integer> getData() {
            return data;
        }

        public void setData(List<Integer> data) {
            this.data = data;
        }
    }

    public static class AccountSelect{
        String password;
        String administrator;
        String name;
        int balance;
        String photo_url;
        int ret_code;

        public String getPhoto_url() {
            return photo_url;
        }

        public void setPhoto_url(String photo_url) {
            this.photo_url = photo_url;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getAdministrator() {
            return administrator;
        }

        public void setAdministrator(String administrator) {
            this.administrator = administrator;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getBalance() {
            return balance;
        }

        public void setBalance(int balance) {
            this.balance = balance;
        }

        public int getRet_code() {
            return ret_code;
        }

        public void setRet_code(int ret_code) {
            this.ret_code = ret_code;
        }
    }

    public static class GeneralResponse {
        String function;
        String methodID;
        List<Result> result;

        public String getFunction() {
            return function;
        }

        public void setFunction(String function) {
            this.function = function;
        }

        public String getMethodID() {
            return methodID;
        }

        public void setMethodID(String methodID) {
            this.methodID = methodID;
        }

        public List<Result> getResult() {
            return result;
        }

        public void setResult(List<Result> result) {
            this.result = result;
        }
    }

    public static class GeneralResponseForString {
        String function;
        String methodID;
        List<ListResultForString> result;

        public String getFunction() {
            return function;
        }

        public void setFunction(String function) {
            this.function = function;
        }

        public String getMethodID() {
            return methodID;
        }

        public void setMethodID(String methodID) {
            this.methodID = methodID;
        }

        public List<ListResultForString> getResult() {
            return result;
        }

        public void setResult(List<ListResultForString> result) {
            this.result = result;
        }
    }

}
