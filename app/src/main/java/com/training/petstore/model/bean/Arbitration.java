package com.training.petstore.model.bean;

public class Arbitration {
    String a_id;
    String t_id;
    String time;
    String desc;
    String u_id;
    String comp;

    public Arbitration(String a_id, String t_id, String time, String desc, String u_id) {
        this.a_id = a_id;
        this.t_id = t_id;
        this.time = time;
        this.desc = desc;
        this.u_id = u_id;
        this.comp = "0";
    }

    public Arbitration(String a_id, String t_id, String time, String desc, String u_id, String comp) {
        this.a_id = a_id;
        this.t_id = t_id;
        this.time = time;
        this.desc = desc;
        this.u_id = u_id;
        this.comp = comp;
    }

    public String getA_id() {
        return a_id;
    }

    public void setA_id(String a_id) {
        this.a_id = a_id;
    }

    public String getT_id() {
        return t_id;
    }

    public void setT_id(String t_id) {
        this.t_id = t_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getU_id() {
        return u_id;
    }

    public void setU_id(String u_id) {
        this.u_id = u_id;
    }

    public String getComp() {
        return comp;
    }

    public void setComp(String comp) {
        this.comp = comp;
    }
}
