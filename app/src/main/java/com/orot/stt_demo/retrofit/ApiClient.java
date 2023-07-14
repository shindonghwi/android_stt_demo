package com.orot.stt_demo.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String MAGO52_BASE_URL = "http://dev.mago52.com:8006/";
    private static final String STT_BASE_URL = "http://147.47.41.49:9003/";
    private static Retrofit retrofit;

    static OkHttpClient getOkhttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }

    public static Retrofit getMago52ApiClient() {
        Gson gson = new GsonBuilder().setLenient().create();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(MAGO52_BASE_URL).client(getOkhttpClient()).addConverterFactory(GsonConverterFactory.create(gson)).build();
        }

        return retrofit;
    }

    public static Retrofit getSttApiClient() {
        Gson gson = new GsonBuilder().setLenient().create();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(STT_BASE_URL).client(getOkhttpClient()).addConverterFactory(GsonConverterFactory.create(gson)).build();
        }

        return retrofit;
    }
}
