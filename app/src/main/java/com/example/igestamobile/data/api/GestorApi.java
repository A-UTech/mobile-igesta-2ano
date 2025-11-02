package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.GestorModel;
import com.example.igestamobile.data.model.LiderModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GestorApi {
    @GET("igesta/user/gestores/selecionar")
    Call<List<GestorModel>> selecionarAllGestores();
    @GET("igesta/user/gestores/selecionarPorId/{id}")
    Call<GestorModel> selecionarGestores(@Path("id") Integer id);
    @POST("igesta/user/gestores/inserir")
    Call<GestorModel> cadastrarGestor(@Body GestorModel gestor);
}
