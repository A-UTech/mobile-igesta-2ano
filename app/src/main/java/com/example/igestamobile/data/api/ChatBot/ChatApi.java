package com.example.igestamobile.data.api.ChatBot;

import com.example.igestamobile.data.model.ChatBot.ChatRequest;
import com.example.igestamobile.data.model.ChatBot.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatApi {
    @POST("chat")
    Call<ChatResponse> enviarMensagem(@Body ChatRequest request);
}
