package com.doubiweather.android;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.doubiweather.android.gson.Weather;
import com.doubiweather.android.util.Utility;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private TextView aqiText;

    private TextView pm25Text;

    private LinearLayout forecastLayout;

    private TextView comfort;

    private TextView carWashText;

    private TextView sportText;



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
        }else {
            String weatherId = getIntent().getExtras().getString("weather_id");
            weatherLayout.setVisibility(ScrollView.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    /**
     * 根据天气id请求天气
     * @param weatherId
     */
    private void requestWeather(String weatherId) {

    }

    private void showWeatherInfo(Weather weather) {
    }

    private void initUI(){
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_text);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        comfort = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.comfort_text);
        sportText = (TextView) findViewById(R.id.sport_text);
    }

}
