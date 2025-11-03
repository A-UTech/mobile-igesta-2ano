package com.example.igestamobile.data.api.CondenaUnidade;

import com.example.igestamobile.data.model.CondenaUnidade.CondenaUnidadeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CondenaUnidadeApi {
    @GET("igesta/user/condena-unidade/unidade/{id}")
    Call<List<CondenaUnidadeResponse>> selecionarCondenasUnidade(@Path("id") Integer id);

    @POST("igesta/user/condena-unidade/associar/{unidadeId}/{condenaId}")
    Call<Void> associarCondenaUnidade(@Path("unidadeId") Integer unidadeId, @Path("condenaId") Integer condenaId);

    @DELETE("igesta/user/condena-unidade/desassociar/{unidadeId}/{condenaId}")
    Call<Void> desassociarCondenaUnidade(@Path("unidadeId") Integer unidadeId, @Path("condenaId") Integer condenaId);
}