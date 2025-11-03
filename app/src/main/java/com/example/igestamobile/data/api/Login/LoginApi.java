package com.example.igestamobile.data.api.Login;

import com.example.igestamobile.data.model.Login.LoginModelRequest;
import com.example.igestamobile.data.model.Login.LoginModelResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginApi {
    @POST("igesta/user/tipo-usuario/login")
    Call<LoginModelResponse> login(@Body LoginModelRequest request);
}