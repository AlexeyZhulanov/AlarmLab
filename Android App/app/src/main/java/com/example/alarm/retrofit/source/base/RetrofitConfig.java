package com.example.alarm.retrofit.source.base;

import com.squareup.moshi.Moshi;

import retrofit2.Retrofit;

public record RetrofitConfig(Retrofit retrofit, Moshi moshi) {}