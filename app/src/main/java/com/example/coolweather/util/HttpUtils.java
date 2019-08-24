package com.example.coolweather.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtils {
    public static void SendOkhttpRequst(String url, Callback callback){
        OkHttpClient okHttpClient=new OkHttpClient();
        Request request=new Request.Builder().get().url(url).build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}
