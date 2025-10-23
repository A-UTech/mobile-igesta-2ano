package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.CondenaUnidadeRequest;
import com.example.igestamobile.data.model.CondenaUnidadeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CondenaUnidadeApi {
    @GET("igesta/condena-unidade/unidade/{id}")
    Call<List<CondenaUnidadeResponse>> selecionarCondenasUnidade(@Path("id") Integer id);

    @POST("igesta/condena-unidade/associar")
    Call<CondenaUnidadeResponse> associarCondenaUnidade(@Body CondenaUnidadeRequest request);

    @DELETE("igesta/condena-unidade/desassociar")
    Call<Void> desassociarCondenaUnidade(@Body CondenaUnidadeRequest request);
}
