package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.EmpresaModel;
import com.example.igestamobile.data.model.TurnoRequest;
import com.example.igestamobile.data.model.TurnoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface EmpresaApi {
    @GET("igesta/user/empresas/selecionarPorId/{id}")
    Call<EmpresaModel> selecionarEmpresaPorId(@Path("id") Integer id);
}
