package com.example.igestamobile.data.api.Unidade;

import com.example.igestamobile.data.model.Unidade.UnidadeModel;
import com.example.igestamobile.data.model.Unidade.UnidadeRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface UnidadeApi {
    @GET("igesta/user/unidades/selecionarPorId/{id}")
    Call<UnidadeModel> selecionarUnidadePorId(@Path("id") Integer id);

    @PATCH("igesta/user/unidades/atualizarParcial/{id}")
    Call<Void> atualizarUnidadeParcial(@Path("id") Integer id, @Body UnidadeRequest body);
}
