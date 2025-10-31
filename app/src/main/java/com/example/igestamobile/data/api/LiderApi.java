package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.GestorModel;
import com.example.igestamobile.data.model.LiderModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface LiderApi {
    @GET("igesta/user/lideres/selecionar")
    Call<List<LiderModel>> selecionarAllLideres();
    @GET("igesta/user/lideres/selecionarPorId/{id}")
    Call<LiderModel> selecionarLideres(@Path("id") Integer id);
}