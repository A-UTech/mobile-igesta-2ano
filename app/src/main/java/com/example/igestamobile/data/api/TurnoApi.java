package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.AuthRequest;
import com.example.igestamobile.data.model.AuthResponse;
import com.example.igestamobile.data.model.TurnoRequest;
import com.example.igestamobile.data.model.TurnoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TurnoApi {
    @GET("igesta/user/turnos/selecionarPorUnidadeEPeriodo/{idUnidade}/{inicio}/{fim}")
    Call<TurnoResponse> selecionarPorUnidadeEPeriodo(@Path("idUnidade") Integer idUnidade, @Path("inicio") String inicio, @Path("fim") String fim);
}
