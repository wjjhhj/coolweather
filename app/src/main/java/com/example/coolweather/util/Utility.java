package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.Country;
import com.example.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Utility {
    /**
     * 处理和服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvince=new JSONArray(response);
                for (int i=0;i<allProvince.length();i++){
                    JSONObject provinceObject=allProvince.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    /**
     * 处理和服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCity=new JSONArray(response);
                for (int i=0;i<allCity.length();i++){
                    JSONObject cityObject=allCity.getJSONObject(i);
                    City city=new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    /**
     * 处理和服务器返回的县级数据
     */
    public static boolean handleCountryResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCountry=new JSONArray(response);
                for (int i=0;i<allCountry.length();i++){
                    JSONObject countryObject=allCountry.getJSONObject(i);
                    Country country=new Country();
                    country.setCountryName(countryObject.getString("name"));
                    country.setCityId(cityId);
                    country.setWeatherId(countryObject.getString("weather_id"));
                    country.save();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
