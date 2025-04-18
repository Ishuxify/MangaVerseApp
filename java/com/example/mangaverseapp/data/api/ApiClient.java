package com.example.mangaverseapp.data.api;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://mangaverse-api.p.rapidapi.com/";
    private static final String API_KEY = "ad8ff55b51msh6c10e43fb5ecad8p1881d7jsn99d94729a044"; // Store in local.properties
    private static final String API_HOST = "mangaverse-api.p.rapidapi.com";
    private static volatile Retrofit retrofit = null;

    public static Retrofit getApiClient() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    // Setup OkHttpClient with headers and logging
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new Interceptor() {
                                @Override
                                public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
                                    Request request = chain.request().newBuilder()
                                            .addHeader("X-RapidAPI-Key", API_KEY)
                                            .addHeader("X-RapidAPI-Host", API_HOST)
                                            .build();
                                    return chain.proceed(request);
                                }
                            })
                            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build();

                    // Setup Retrofit
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }
}