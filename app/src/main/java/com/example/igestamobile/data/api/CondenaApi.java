package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.CondenaModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CondenaApi {
    @GET("igesta/user/condenas/selecionar")
    Call<List<CondenaModel>> selecionarCondenas();

    @GET("igesta/user/condenas/selecionarPorId/{id}")
    Call<CondenaModel> selecionarCondenaPorId(@Path("id") Long id);
}
