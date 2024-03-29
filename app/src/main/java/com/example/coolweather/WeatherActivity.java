package com.example.coolweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpUtils;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
private ScrollView weatherLayout;
private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView bingPicImg;
    private LinearLayout forecastLayout;
    private SharedPreferences sharedPreferences;

    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;

    public DrawerLayout mDrawerLayout;
    private ImageView ivNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        initView();
        String weatherString=sharedPreferences.getString("weather",null);
        String bingPic=sharedPreferences.getString("bing_pic",null);
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }
        if (weatherString!=null){
            //有缓存直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            mWeatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            //无缓存时去服务器查询天气
            mWeatherId=getIntent().getStringExtra("weather_id");
            //String weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requstWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requstWeather(mWeatherId);
            }
        });
        ivNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        final String url="http://guolin.tech/api/bing_pic";
        HttpUtils.SendOkhttpRequst(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            final String bingPic=response.body().string();
             SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
             editor.putString("bing_pic",bingPic);
             editor.apply();
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     Glide.with(WeatherActivity.this).load(url).into(bingPicImg);
                 }
             });
            }
        });
    }

    /**
     * 根据Id请求城市天气信息
     * @param weatherId
     */
    public void requstWeather(String weatherId) {
            String url="http://guolin.tech/api/weather?cityid="+weatherId+"&key=bc0418b57b2d4918819d3974ac1285d9";
            HttpUtils.SendOkhttpRequst(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            runOnUiThread(new Runnable() {
    @Override
    public void run() {
        swipeRefresh.setRefreshing(false);
    }
});
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            final String responseText=response.body().string();
            final Weather weather=Utility.handleWeatherResponse(responseText);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
            if (weather!=null&&"ok".equals(weather.status)){
                SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("weather",responseText);
                editor.apply();
                mWeatherId=weather.basic.weatherId;
                showWeatherInfo(weather);
            }else {
                Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
            }
            swipeRefresh.setRefreshing(false);
                }
            });
            }
        });
        loadBingPic();
    }

    /**
     * 处理并且展示weather实体类的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Forecast forecast:weather.forecastList){
        View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
        TextView dataText=view.findViewById(R.id.date_text);
        TextView infoText=view.findViewById(R.id.info_text);
        TextView maxText=view.findViewById(R.id.max_text);
        TextView minText=view.findViewById(R.id.min_text);

        dataText.setText(forecast.date);
        infoText.setText(forecast.more.info);
        maxText.setText(forecast.temperature.max);
        minText.setText(forecast.temperature.min);
        forecastLayout.addView(view);
        }
        if (weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carWash.info;
        String sport="运动建议："+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @SuppressLint("ResourceAsColor")
    private void initView() {
        weatherLayout=findViewById(R.id.weather_scroll_view);
        titleCity=findViewById(R.id.title_city);
        titleUpdateTime=findViewById(R.id.title_update_time);
        degreeText=findViewById(R.id.degree_text);
        weatherInfoText=findViewById(R.id.weather_info_text);
        aqiText=findViewById(R.id.aqi_text);
        pm25Text=findViewById(R.id.pm25_text);
        comfortText=findViewById(R.id.comfort_text);
        carWashText=findViewById(R.id.car_wash_text);
        sportText=findViewById(R.id.sport_text);
        forecastLayout=findViewById(R.id.forecast_layout);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        bingPicImg=findViewById(R.id.bing_pic_img);
        swipeRefresh=findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(R.color.colorPrimary);

        mDrawerLayout=findViewById(R.id.drawer_layout);
        ivNav=findViewById(R.id.nav_image_view);

    }
}
