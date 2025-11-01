package com.example.igestamobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.example.igestamobile.adapter.ChatBot.MensagemAdapter;
import com.example.igestamobile.data.api.ChatBot.ApiClient;
import com.example.igestamobile.data.api.ChatBot.ChatApi;
import com.example.igestamobile.data.api.UnidadeApi;
import com.example.igestamobile.data.model.ChatBot.ChatRequest;
import com.example.igestamobile.data.model.ChatBot.ChatResponse;
import com.example.igestamobile.data.model.ChatBot.MensagemModel;
import com.example.igestamobile.data.model.UnidadeModel;
import com.example.igestamobile.utils.MaskUtil;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chatbot extends Fragment {

    private static final String BASE_URL = "https://chatbot-mobile-ha2n.onrender.com/";
    private String UNIDADE;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_UNIDADE_ID = "UNIDADE_ID";
    private static final String KEY_USUARIO_CREDENCIAL = "USUARIO_CREDENCIAL";
    private RecyclerView recyclerView;
    private TextInputEditText mensagem_funcionario;
    private ImageButton btnEnviar;

    private MensagemAdapter adapter;
    private List<MensagemModel> mensagens = new ArrayList<>();

    private ChatApi chatApi;
    private UnidadeApi unidadeApi;

    ShapeableImageView shapeableImageView5;
    private FirebaseFirestore db;
    private String profileImageUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatbot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int unidadeId = sharedPrefs.getInt(KEY_UNIDADE_ID, -1);
        unidadeApi = ApiClient.getClient(BASE_URL).create(UnidadeApi.class);
        unidadeApi.selecionarUnidadePorId(unidadeId).enqueue(new Callback<UnidadeModel>() {
            @Override
            public void onResponse(Call<UnidadeModel> call, Response<UnidadeModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UNIDADE = response.body().getNome();
                }
            }

            @Override
            public void onFailure(Call<UnidadeModel> call, Throwable t) {
                UNIDADE = "Unidade não encontrada";
            }
        });

        recyclerView = view.findViewById(R.id.recyclerView);
        mensagem_funcionario = view.findViewById(R.id.mensagem_funcionario);
        btnEnviar = view.findViewById(R.id.btnEnviar);
        shapeableImageView5 = view.findViewById(R.id.shapeableImageView5);

        adapter = new MensagemAdapter(mensagens, requireContext(), profileImageUrl);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        chatApi = ApiClient.getClient(BASE_URL).create(ChatApi.class);

        addMensagemBot("Olá, sou o Igestinha. Como posso ajudar você hoje?");

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

        loadProfileImage();
    }

    private void enviarMensagemFuncionario(String mensagem){
        mensagens.add(new MensagemModel(mensagem, true));
        adapter.notifyItemInserted(mensagens.size() - 1);
        recyclerView.smoothScrollToPosition(mensagens.size() - 1);
        mensagem_funcionario.setText("");

        MensagemModel loadingMsg = new MensagemModel("...", false);
        loadingMsg.setLoading(true);
        mensagens.add(loadingMsg);
        adapter.notifyItemInserted(mensagens.size() - 1);
        recyclerView.smoothScrollToPosition(mensagens.size() - 1);

        ChatRequest chatRequest = new ChatRequest(mensagem, UNIDADE);

        chatApi.enviarMensagem(chatRequest).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                removerMensagemLoading();
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
                removerMensagemLoading();
                addMensagemBot("⚠️ Falha na conexão: " + t.getMessage());
            }
        });
    }

    private void removerMensagemLoading() {
        if (!mensagens.isEmpty()) {
            int lastIndex = mensagens.size() - 1;
            MensagemModel ultima = mensagens.get(lastIndex);
            if (ultima.isLoading()) {
                mensagens.remove(lastIndex);
                adapter.notifyItemRemoved(lastIndex);
            }
        }
    }
    private void addMensagemBot(String text) {
        mensagens.add(new MensagemModel(text, false));
        adapter.notifyItemInserted(mensagens.size() - 1);
        recyclerView.scrollToPosition(mensagens.size() - 1);
    }

    private void loadProfileImage() {
        String documentId = getUsuarioCredencial();

        if (documentId == null) {
            Log.e("Firebase", "Credencial não encontrada. Imagem não pode ser carregada.");
            return;
        }

        db.collection("usuarios").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("imagem");

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            this.profileImageUrl = profileImageUrl;
                            Glide.with(this)
                                    .load(profileImageUrl)
                                    .override(175, 175)
                                    .centerCrop()
                                    .placeholder(R.mipmap.fotoperfil)
                                    .error(R.mipmap.fotoperfil)
                                    .into(shapeableImageView5);

                            Log.i("Firebase", "Foto de perfil carregada do Firebase.");
                        } else {
                            Log.d("Firebase", "URL da foto de perfil (campo 'imagem') não encontrada no Firestore.");
                        }
                    } else {
                        Log.d("Firebase", "Documento do usuário não encontrado no Firestore (ID: " + documentId + ").");
                    }
                    initAdapter();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Erro ao buscar documento do usuário: " + e.getMessage());
                    initAdapter();
                });
    }

    private void initAdapter() {
        if (profileImageUrl != null) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .override(175, 175).centerCrop()
                    .placeholder(R.mipmap.fotoperfil)
                    .error(R.mipmap.fotoperfil)
                    .into(shapeableImageView5);
        }

        adapter = new MensagemAdapter(mensagens, requireContext(), profileImageUrl);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        if (!mensagens.isEmpty()) {
            adapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(mensagens.size() - 1);
        }
    }

    private String getUsuarioCredencial() {
        if (getActivity() == null) return null;

        String rawCredencial = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USUARIO_CREDENCIAL, null);

        if (rawCredencial != null) {
            if (rawCredencial.contains("@")) {
                return rawCredencial;
            } else {
                return MaskUtil.unmaskCnpj(rawCredencial);
            }
        }
        return null;
    }
}
