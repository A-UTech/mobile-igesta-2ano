package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.AuthRequest;
import com.example.igestamobile.data.model.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("igesta/auth/login")
    Call<AuthResponse> login(@Body AuthRequest authModel);
}
