package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.RegistroModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RegistroApi {
    @GET("igesta/registros/selecionar")
    Call<List<RegistroModel>> selecionarRegistros();

    @POST("igesta/registros/inserir")
    Call<RegistroModel> inserirRegistro(@Body RegistroModel registro);
}
