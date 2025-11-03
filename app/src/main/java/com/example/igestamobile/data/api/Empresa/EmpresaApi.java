package com.example.igestamobile.data.api.Empresa;

import com.example.igestamobile.data.model.Empresa.EmpresaModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface EmpresaApi {
    @GET("igesta/user/empresas/selecionarPorId/{id}")
    Call<EmpresaModel> selecionarEmpresaPorId(@Path("id") Integer id);
}
