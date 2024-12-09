package com.example.alarm.retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AlarmApi {
    @POST("start")
    Call<ResponseEntityMessageAnswer> setAlarm(@Body AlarmRequestEntity alarmRequestEntity);

    @GET("alarms")
    Call<AlarmsResponseEntity> getAlarms();
}
