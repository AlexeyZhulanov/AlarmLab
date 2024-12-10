package com.example.alarm.retrofit.source.base;


import com.example.alarm.model.exceptions.AppException;
import com.example.alarm.model.exceptions.BackendException;
import com.example.alarm.model.exceptions.ConnectionException;
import com.example.alarm.model.exceptions.ParseBackendResponseException;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonEncodingException;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;

import retrofit2.HttpException;
import retrofit2.Retrofit;

public class BaseRetrofitSource {

    protected final Retrofit retrofit;
    private final Moshi moshi;
    private final JsonAdapter<ErrorResponseBody> errorAdapter;

    public BaseRetrofitSource(RetrofitConfig config) {
        this.retrofit = config.retrofit();
        this.moshi = config.moshi();
        this.errorAdapter = moshi.adapter(ErrorResponseBody.class);
    }

    public <T> T wrapRetrofitExceptions(Callable<T> block) throws AppException {
        try {
            return block.call();
        } catch (AppException e) {
            throw e;
        } catch (JsonDataException | JsonEncodingException e) {
            throw new ParseBackendResponseException(e);
        } catch (HttpException e) {
            throw createBackendException(e);
        } catch (IOException e) {
            throw new ConnectionException(e);
        } catch (Exception e) {
            throw new RuntimeException(e); // fallback
        }
    }

    private BackendException createBackendException(HttpException e) {
        try {
            String errorBodyJson = Objects.requireNonNull(Objects.requireNonNull(e.response()).errorBody()).string();
            ErrorResponseBody errorBody = errorAdapter.fromJson(errorBodyJson);
            return new BackendException(e.code(), Objects.requireNonNull(errorBody).getError());
        } catch (Exception ex) {
            throw new ParseBackendResponseException(ex);
        }
    }

    public static class ErrorResponseBody {
        private String error;

        public String getError() {
            return error;
        }
    }
}
