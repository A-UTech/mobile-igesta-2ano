package com.example.igestamobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igestamobile.adapter.ChatBot.MensagemAdapter;
import com.example.igestamobile.data.api.ChatBot.ApiClient;
import com.example.igestamobile.data.api.ChatBot.ChatApi;
import com.example.igestamobile.data.model.ChatBot.ChatRequest;
import com.example.igestamobile.data.model.ChatBot.ChatResponse;
import com.example.igestamobile.data.model.ChatBot.MensagemModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chatbot extends Fragment {

    private static final String BASE_URL = "https://chatbot-mobile-igesta.vercel.app/";
    private static final String UNIDADE = "Panatem Osasco";
    private RecyclerView recyclerView;
    private TextInputEditText mensagem_funcionario;
    private ImageButton btnEnviar;

    private MensagemAdapter adapter;
    private List<MensagemModel> mensagens = new ArrayList<>();

    private ChatApi chatApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatbot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        mensagem_funcionario = view.findViewById(R.id.mensagem_funcionario);
        btnEnviar = view.findViewById(R.id.btnEnviar);

        adapter = new MensagemAdapter(mensagens);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        chatApi = ApiClient.getClient(BASE_URL).create(ChatApi.class);

        btnEnviar.setOnClickListener(v -> {
            String mensagem = mensagem_funcionario.getText().toString().trim();
            if(TextUtils.isEmpty(mensagem)) return;
            enviarMensagemFuncionario(mensagem);
        });

        mensagem_funcionario.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_SEND){
                btnEnviar.performClick();
                return true;
            }
            return false;
        });
    }

    private void enviarMensagemFuncionario(String mensagem){
        mensagens.add(new MensagemModel(mensagem, true));
        adapter.notifyItemInserted(mensagens.size() - 1);
        recyclerView.smoothScrollToPosition(mensagens.size() - 1);
        mensagem_funcionario.setText("");

        ChatRequest chatRequest = new ChatRequest(mensagem, UNIDADE);

        chatApi.enviarMensagem(chatRequest).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    String respostaBot = response.body().getResposta();
                    if (respostaBot == null || respostaBot.isEmpty()){
                        respostaBot = "Resposta vazia do servidor.";
                        addMensagemBot(respostaBot);
                    }else {
                        addMensagemBot(respostaBot);
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                addMensagemBot("⚠️ Falha na conexão: " + t.getMessage());
            }
        });
    }

    private void addMensagemBot(String text) {
        mensagens.add(new MensagemModel(text, false));
        adapter.notifyItemInserted(mensagens.size() - 1);
        recyclerView.scrollToPosition(mensagens.size() - 1);
    }
}
