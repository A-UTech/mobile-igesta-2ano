package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.RegistroModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RegistroApi {
    @GET("igesta/registros/selecionar")
    Call<List<RegistroModel>> selecionarRegistros();
    @GET("igesta/registros/selecionarPorPeriodoEUnidade")
    Call<List<RegistroModel>> buscarRegistrosPorPeriodoEUnidade(
            @Query("dataInicio") String dataInicioISO,
            @Query("dataFim") String dataFimISO,
            @Query("unidade") String unidade
    );
    @POST("igesta/registros/inserir")
    Call<RegistroModel> inserirRegistro(@Body RegistroModel registro);
}
