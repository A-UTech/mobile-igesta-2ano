package com.example.igestamobile.data.api.Registro;

import com.example.igestamobile.data.model.Condena.CondenaTopModel;
import com.example.igestamobile.data.model.Registro.RegistroModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RegistroApi {
    @GET("igesta/registros/selecionar")
    Call<List<RegistroModel>> selecionarRegistros();
    @GET("igesta/registros/periodo/{inicio}/{fim}/{unidade}")
    Call<List<RegistroModel>> buscarRegistrosPorPeriodoEUnidade(
            @Path("inicio") String dataInicioISO,
            @Path("fim") String dataFimISO,
            @Path("unidade") String unidade
    );
    @POST("igesta/registros/inserir")
    Call<RegistroModel> inserirRegistro(@Body RegistroModel registro);

    @GET("igesta/registros/total-condenas-registradas/{unidade}")
    Call<Integer> buscarTotalCondenasRegistradas(@Path("unidade") String unidade);

    @GET("igesta/registros/tipo-total-por-unidade/{unidade}")
    Call<Integer> buscarTipoTotalPorUnidade(@Path("unidade") String unidade);

    @GET("igesta/registros/tipo-parcial-por-unidade/{unidade}")
    Call<Integer> buscarTipoParcialPorUnidade(@Path("unidade") String unidade);

    @GET("igesta/registros/comparar-mes-passado/{unidade}")
    Call<Double> buscarComparacaoMesPassado(@Path("unidade") String unidade);

    @GET("igesta/registros/condenas-totais-mais-registradas/{unidade}")
    Call<List<CondenaTopModel>> buscarCondenasTotaisMaisRegistradas(@Path("unidade") String unidade);

    @GET("igesta/registros/condenas-parciais-mais-registradas/{unidade}")
    Call<List<CondenaTopModel>> buscarCondenasParciaisMaisRegistradas(@Path("unidade") String unidade);
}
