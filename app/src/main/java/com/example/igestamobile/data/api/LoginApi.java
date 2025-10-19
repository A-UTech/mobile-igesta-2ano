package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.LoginModelRequest;
import com.example.igestamobile.data.model.LoginModelResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginApi {

    @POST("api/tipo-usuario/login")
    Call<LoginModelResponse> login(@Body LoginModelRequest request);
}