package com.example.alarm.retrofit.source.base;

import com.example.alarm.retrofit.source.AlarmSource;
import com.example.alarm.retrofit.source.RetrofitAlarmSource;

public class RetrofitSourcesProvider implements SourcesProvider {

    private final RetrofitConfig config;

    public RetrofitSourcesProvider(RetrofitConfig config) {
        this.config = config;
    }

    @Override
    public AlarmSource getAlarmSource() {
        return new RetrofitAlarmSource(config);
    }
}
