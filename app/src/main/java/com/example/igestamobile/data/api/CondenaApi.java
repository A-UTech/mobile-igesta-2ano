package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.CondenaModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CondenaApi {
    @GET("condenas/selecionar")
    Call<List<CondenaModel>> selecionarCondenas();
}
