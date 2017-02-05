package com.doubiweather.android.gson;

/**
 * Created by Administrator on 2017/2/4.
 */

public class AQI {
    public AQICity city; //可能报错


    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
