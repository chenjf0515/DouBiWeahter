package com.doubiweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/2/4.
 */

public class Basic
{
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    private String weatherId;

    public Update update;


    private class Update {
        @SerializedName("loc")
        private String updateTime;
    }
}
