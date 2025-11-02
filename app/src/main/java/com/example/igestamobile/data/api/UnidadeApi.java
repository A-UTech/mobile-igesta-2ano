package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.UnidadeModel;
import com.example.igestamobile.data.model.UnidadeRequest;
import com.example.igestamobile.data.model.UsuarioRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UnidadeApi {
    @GET("igesta/user/unidades/selecionarPorId/{id}")
    Call<UnidadeModel> selecionarUnidadePorId(@Path("id") Integer id);

    @PUT("igesta/user/unidades/atualizarParcial/{id}")
    Call<Void> atualizarUnidadeParcial(@Path("id") Integer id, @Body UnidadeRequest body);
}
