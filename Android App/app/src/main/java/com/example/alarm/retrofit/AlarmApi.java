package com.example.alarm.retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AlarmApi {
    @POST("create_alarm")
    Call<ResponseEntityMessageAnswer> setAlarm(@Body AlarmRequestEntity alarmRequestEntity);

    @GET("get_alarms")
    Call<AlarmsResponseEntity> getAlarms();

    @DELETE("delete_alarm/{alarm_id}")
    Call<ResponseEntityMessageAnswer> deleteAlarm(@Path("alarm_id") int alarm_id);

    @PUT("update_alarm")
    Call<ResponseEntityMessageAnswer> updateAlarm(@Body AlarmRequestEntity alarmRequestEntity);
}
