package com.doubiweather.android;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.doubiweather.android.gson.BackgroundImage;
import com.doubiweather.android.gson.Forecast;
import com.doubiweather.android.gson.Weather;
import com.doubiweather.android.util.HttpUtil;
import com.doubiweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public static final String TAG = "WeatherActivity";

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private TextView aqiText;

    private TextView pm25Text;

    private LinearLayout forecastLayout;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        initUI();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);
        if (weatherString != null) {
            //有缓存天气时，直接解析缓存天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            String weatherId = getIntent().getExtras().getString("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        String bingPic = preferences.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingImage);
        } else {
            loadBingPic();
        }
    }

    /**
     * 加载每日bing图
     */
    private void loadBingPic() {
        String address = "http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
        String bingImageUrl = handleBingImageUrl(address);
        HttpUtil.sendOKHttpRequest(bingImageUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //加载失败

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingImage);
                    }
                });

            }
        });


    }

    private String handleBingImageUrl(String address) {
        final String[] bingImageUrl = new String[1];
        HttpUtil.sendOKHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //Toast.makeText(WeatherActivity.this, "背景图加载失败！！！", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseContent = response.body().string();
                BackgroundImage backgroundImage = Utility.handleBingResponse(responseContent);
                bingImageUrl[0] = "cn.bing.com" + backgroundImage.url;
            }
        });

        return bingImageUrl[0];

    }

    /**
     * 根据天气id请求天气 http://guolin.tech/api/weather?cityid=CN101190401&key=dba73d823836445490ac0700a8bef25b
     * https://free-api.heweather.com/v5/weather?cityid=CN101190401&key=dba73d823836445490ac0700a8bef25b
     * https://free-api.heweather.com/x3/
     *
     * @param weatherId
     */
    private void requestWeather(String weatherId) {
        String address = "https://free-api.heweather.com/x3/weather?cityid=" + weatherId + "&key=dba73d823836445490ac0700a8bef25b";

        HttpUtil.sendOKHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);


                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                        }
                    }
                });


            }
        });


    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        Log.d(TAG, cityName);
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forcecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);

            forecastLayout.addView(view);

        }

        if (weather.aqi != null) {
            //Log.d(TAG, weather.aqi.city.aqi);
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        } else {
            Log.d(TAG, "weather.aqi is null");
        }

        String comfort = "舒适度：" + weather.suggestion.comfort;
        String carWash = "洗车指数：" + weather.suggestion.carWash;
        String sport = "运动建议：" + weather.suggestion.sport;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);
    }

    private void initUI() {
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bingImage = (ImageView) findViewById(R.id.bing_pic_image);
    }

}
