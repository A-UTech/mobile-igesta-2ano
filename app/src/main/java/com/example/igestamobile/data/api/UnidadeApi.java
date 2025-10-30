package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.UnidadeModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UnidadeApi {
    @GET("igesta/user/unidades/selecionarPorId/{id}")
    Call<UnidadeModel> selecionarUnidadePorId(@Path("id") Integer id);
}
