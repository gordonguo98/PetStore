package com.training.petstore.util.common;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class TestUtil {

    public static final String[] PET_VARIETIES = {
      "哈士奇", "秋田犬", "边牧", "柴犬", "金毛", "布偶猫", "英国短尾猫", "挪威森林猫", "金刚鹦鹉", "折衷鹦鹉"
    };

    public static final String[] PET_URLS = {
            "http://www.gudengpet.com/uploads/allimg/150204/2-1502040UKJJ.jpg",
            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=2853743919,1392445214&fm=26&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=2813408016,3422946770&fm=26&gp=0.jpg",
            "http://img1.imgtn.bdimg.com/it/u=2862225811,18225510&fm=26&gp=0.jpg",
            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=1418152659,3728354594&fm=26&gp=0.jpg",
            "https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=3312580166,3080476140&fm=26&gp=0.jpg",
            "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=22918048,2089546720&fm=26&gp=0.jpg",
            "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2058297120,4020196096&fm=26&gp=0.jpg",
            "https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=4261840620,1462186844&fm=26&gp=0.jpg",
            "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=1395337841,2238546766&fm=26&gp=0.jpg"
    };

    public static final String[] USER_IDS = {
            "12345678@qq.com",
            "ubdc68d_usb@qq.com",
            "73684883@163.com",
            "muycj_ucy@gmail.com",
            "2538gd364@qq.com"
    };

    public static String generatePetId(){

        return String.valueOf((long) (Math.random() * 1e12));

    }

    public static String generateDate() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return simpleDateFormat.format(new Date());

    }

}
