package com.doubiweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.support.annotation.Nullable;

import com.doubiweather.android.gson.BackgroundImage;
import com.doubiweather.android.gson.Weather;
import com.doubiweather.android.util.HttpUtil;
import com.doubiweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/2/7.
 */

public class AutoUpdateService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 1000 * 60 * 60 * 2;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent intent1 = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent1, 0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME,triggerAtTime,pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 自动更新天气功能
     */
    private void updateWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);

        if (weatherString != null) {
            final Weather weather = Utility.handleWeatherResponse(weatherString);
            final String weatherId = weather.basic.weatherId;

            String weatherUrl = "https://free-api.heweather.com/x3/weather?cityid=" + weatherId + "&key=dba73d823836445490ac0700a8bef25b";

            HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String weatherString = response.body().string();
                    Weather weather1 = Utility.handleWeatherResponse(weatherString);
                    if (weather1 != null && "ok".equals(weather1.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", weatherString);
                        editor.apply();
                    }
                }
            });
        }
    }

    private void updateBingPic() {
        final String bingRequestUrl = "http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";

        HttpUtil.sendOKHttpRequest(bingRequestUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingResponseString = response.body().string();
                BackgroundImage backgroundImage = Utility.handleBingResponse(bingResponseString);
                String bingPicUrl = "http://cn.bing.com" + backgroundImage.url;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                preferences.edit().putString("bing_pic", bingPicUrl).apply();
            }
        });
    }
}
