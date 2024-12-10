package com.example.alarm.retrofit.source;

import com.example.alarm.model.Alarm;
import com.example.alarm.model.exceptions.AppException;
import com.example.alarm.retrofit.AlarmApi;
import com.example.alarm.retrofit.AlarmRequestEntity;
import com.example.alarm.retrofit.AlarmsResponseEntity;
import com.example.alarm.retrofit.ResponseEntityMessageAnswer;
import com.example.alarm.retrofit.source.base.BaseRetrofitSource;
import com.example.alarm.retrofit.source.base.RetrofitConfig;

import java.util.List;
import java.util.Objects;

import retrofit2.HttpException;
import retrofit2.Response;

public class RetrofitAlarmSource extends BaseRetrofitSource implements AlarmSource {
    private final AlarmApi alarmApi;

    public RetrofitAlarmSource(RetrofitConfig config) {
        super(config);
        this.alarmApi = retrofit.create(AlarmApi.class);
    }

    @Override
    public String setAlarm(Alarm alarm) throws AppException {
        return wrapRetrofitExceptions(() -> {
            AlarmRequestEntity requestEntity = new AlarmRequestEntity((int)alarm.getId(), alarm.getTimeHours(), alarm.getTimeMinutes(), alarm.getName());
            Response<ResponseEntityMessageAnswer> response = alarmApi.setAlarm(requestEntity).execute();
            if (!response.isSuccessful()) {
                throw new HttpException(response);
            }
            return Objects.requireNonNull(response.body()).getMessage();
        });
    }

    @Override
    public List<Integer> getAlarms() throws AppException {
        return wrapRetrofitExceptions(() -> {
            Response<AlarmsResponseEntity> response = alarmApi.getAlarms().execute();
            if (!response.isSuccessful()) {
                throw new HttpException(response);
            }
            return Objects.requireNonNull(response.body()).getAlarms();
        });
    }

    @Override
    public String deleteAlarm(int alarm_id) throws AppException {
        return wrapRetrofitExceptions(() -> {
            Response<ResponseEntityMessageAnswer> response = alarmApi.deleteAlarm(alarm_id).execute();
            if (!response.isSuccessful()) {
                throw new HttpException(response);
            }
            return Objects.requireNonNull(response.body()).getMessage();
        });
    }

    @Override
    public String updateAlarm(Alarm alarm) throws AppException {
        return wrapRetrofitExceptions(() -> {
            AlarmRequestEntity requestEntity = new AlarmRequestEntity((int)alarm.getId(), alarm.getTimeHours(), alarm.getTimeMinutes(), alarm.getName());
            Response<ResponseEntityMessageAnswer> response = alarmApi.updateAlarm(requestEntity).execute();
            if (!response.isSuccessful()) {
                throw new HttpException(response);
            }
            return Objects.requireNonNull(response.body()).getMessage();
        });
    }
}
