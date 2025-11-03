package com.example.igestamobile.data.api.Gestor;

import com.example.igestamobile.data.model.Gestor.GestorModel;
import com.example.igestamobile.data.model.Usuario.UsuarioRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GestorApi {
    @GET("igesta/user/gestores/selecionar")
    Call<List<GestorModel>> selecionarAllGestores();
    @GET("igesta/user/gestores/selecionarPorId/{id}")
    Call<GestorModel> selecionarGestores(@Path("id") Integer id);
    @POST("igesta/user/gestores/inserir")
    Call<GestorModel> cadastrarGestor(@Body GestorModel gestor);

    @DELETE("igesta/user/gestores/excluir/{id}")
    Call<GestorModel> excluirGestor(@Path("id") Integer id);

    @PATCH("igesta/user/gestores/atualizarParcial/{id}")
    Call<Void> atualizarGestorParcial(@Path("id") int id, @Body UsuarioRequest body);
}
