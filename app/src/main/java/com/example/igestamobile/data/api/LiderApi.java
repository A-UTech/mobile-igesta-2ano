package com.example.igestamobile.data.api;

import com.example.igestamobile.data.model.GestorModel;
import com.example.igestamobile.data.model.LiderModel;
import com.example.igestamobile.data.model.UsuarioRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface LiderApi {
    @GET("igesta/user/lideres/selecionar")
    Call<List<LiderModel>> selecionarAllLideres();
    @GET("igesta/user/lideres/selecionarPorId/{id}")
    Call<LiderModel> selecionarLideres(@Path("id") Integer id);
    @POST("igesta/user/lideres/inserir")
    Call<LiderModel> cadastrarLider(@Body LiderModel lider);
    @DELETE("igesta/user/lideres/excluir/{id}")
    Call<LiderModel> excluirLider(@Path("id") Integer id);

    @PUT("igesta/user/lideres/atualizarParcial/{id}")
    Call<Void> atualizarLiderParcial(@Path("id") int id, @Body UsuarioRequest body);
}