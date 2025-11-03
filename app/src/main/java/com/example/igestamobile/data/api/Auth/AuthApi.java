package com.example.igestamobile.data.api.Auth;

import com.example.igestamobile.data.model.Auth.AuthRequest;
import com.example.igestamobile.data.model.Auth.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("igesta/auth/login")
    Call<AuthResponse> login(@Body AuthRequest authModel);
}
