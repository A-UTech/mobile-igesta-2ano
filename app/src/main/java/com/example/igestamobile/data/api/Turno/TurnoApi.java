package com.example.igestamobile.data.api.Turno;

import com.example.igestamobile.data.model.Turno.TurnoRequest;
import com.example.igestamobile.data.model.Turno.TurnoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TurnoApi {
    @GET("igesta/user/turnos/selecionarPorId/{id}")
    Call<TurnoResponse> selecionarPorId(@Path("id") Integer id);
    @GET("igesta/user/turnos/selecionarPorUnidadeEPeriodo/{idUnidade}/{inicio}/{fim}")
    Call<TurnoResponse> selecionarPorUnidadeEPeriodo(@Path("idUnidade") Integer idUnidade, @Path("inicio") String inicio, @Path("fim") String fim);

    @POST("igesta/user/turnos/inserir")
    Call<TurnoResponse> inserirTurno(@Body TurnoRequest body);
}
