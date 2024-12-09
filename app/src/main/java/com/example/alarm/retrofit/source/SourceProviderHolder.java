package com.example.alarm.retrofit.source;

import com.example.alarm.retrofit.source.base.RetrofitConfig;
import com.example.alarm.retrofit.source.base.RetrofitSourcesProvider;
import com.example.alarm.retrofit.source.base.SourcesProvider;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;

import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

@Singleton
public class SourceProviderHolder {
    private static final String BASE_URL = "https://amessenger.ru";

    private SourcesProvider sourcesProvider;

    public SourcesProvider getSourcesProvider() {
        if (sourcesProvider == null) {
            Moshi moshi = new Moshi.Builder()
                    .add(new KotlinJsonAdapterFactory())
                    .build();

            RetrofitConfig config = new RetrofitConfig(
                    createRetrofit(moshi),
                    moshi
            );
            sourcesProvider = new RetrofitSourcesProvider(config);
        }
        return sourcesProvider;
    }

    private Retrofit createRetrofit(Moshi moshi) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(createOkHttpClient())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build();
    }

    private OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(createLoggingInterceptor())
                .build();
    }

    private Interceptor createLoggingInterceptor() {
        return new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);
    }
}
