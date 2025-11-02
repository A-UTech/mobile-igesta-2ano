package com.example.igestamobile.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.igestamobile.R;
import com.example.igestamobile.data.api.ChatBot.ApiClient;
import com.example.igestamobile.data.api.GestorApi;
import com.example.igestamobile.data.api.LiderApi;
import com.example.igestamobile.data.api.SqlRetrofitClient;
import com.example.igestamobile.data.api.UnidadeApi;
import com.example.igestamobile.data.model.UnidadeRequest;
import com.example.igestamobile.data.model.UsuarioRequest;

public class Configuracao extends Fragment {

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USUARIO_CREDENCIAL = "USUARIO_CREDENCIAL";
    private static final String KEY_USUARIO_NOME = "USUARIO_NOME";
    private static final String KEY_USUARIO_SENHA = "USUARIO_SENHA";
    private static final String KEY_TIPO_USUARIO = "TIPO_USUARIO";

    private static final String KEY_USUARIO_ID = "USUARIO_ID";
    private static final String KEY_UNIDADE_ID = "UNIDADE_ID";

    private TextView txtUsuarioCredencial;
    private TextView txtUsuarioNome;
    private TextView txtUsuarioSenha;

    public Configuracao() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracao, container, false);

        View btVoltar = view.findViewById(R.id.bt_voltar);
        if (btVoltar != null) {
            btVoltar.setOnClickListener(v ->
                    Navigation.findNavController(v).popBackStack()
            );
        }

        txtUsuarioCredencial = view.findViewById(R.id.tvCredencialUsuario);
        txtUsuarioNome = view.findViewById(R.id.tvNomeUsuario);
        txtUsuarioSenha = view.findViewById(R.id.tvSenhaUsuario);


        SharedPreferences sharedPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String usuarioCredencial = sharedPrefs.getString(KEY_USUARIO_CREDENCIAL, null);
        String usuarioNome = sharedPrefs.getString(KEY_USUARIO_NOME, null);
        String usuarioSenha = sharedPrefs.getString(KEY_USUARIO_SENHA, null);

        txtUsuarioCredencial.setText(usuarioCredencial);
        txtUsuarioNome.setText(usuarioNome);

        String usuarioSenhaCriptografada = "•".repeat(usuarioSenha.length());

        txtUsuarioSenha.setText(usuarioSenhaCriptografada);

        Button btnEditarInfos = view.findViewById(R.id.btn_editarInfos);
        btnEditarInfos.setOnClickListener(v -> abrirDialogSenha());

        return view;
    }

    private void abrirDialogSenha() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_senha, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        android.widget.EditText etSenha = dialogView.findViewById(R.id.input_senha);
        android.widget.Button btnConfirmar = dialogView.findViewById(R.id.bt_confirmar);
        android.widget.Button btnCancelar = dialogView.findViewById(R.id.bt_cancelar);

        btnConfirmar.setOnClickListener(v -> {
            String senhaDigitada = etSenha.getText().toString();

            SharedPreferences sharedPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String senhaAtual = sharedPrefs.getString(KEY_USUARIO_SENHA, "");

            if (senhaDigitada.equals(senhaAtual)) {
                dialog.dismiss(); // Fecha o dialog de senha
                abrirDialogEdicao(senhaAtual); // Abre dialog de edição
            } else {
                android.widget.Toast.makeText(getContext(), "Senha incorreta", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
    }

    private void abrirDialogEdicao(String senhaAtual) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edicao, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        android.widget.EditText etNome = dialogView.findViewById(R.id.input_nome);
        android.widget.EditText etEmail = dialogView.findViewById(R.id.input_credencial);
        android.widget.EditText etSenha = dialogView.findViewById(R.id.input_senha);

        android.widget.Button btnConfirmar = dialogView.findViewById(R.id.bt_confirmar);
        android.widget.Button btnCancelar = dialogView.findViewById(R.id.bt_cancelar);

        // Preenche os campos com os dados atuais
        etNome.setText(txtUsuarioNome.getText().toString());
        etEmail.setText(txtUsuarioCredencial.getText().toString());
        etSenha.setText(senhaAtual);

        btnConfirmar.setOnClickListener(v -> {
            Integer id;
            SharedPreferences sharedPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String tipoUsuario = sharedPrefs.getString(KEY_TIPO_USUARIO, "");

            if (tipoUsuario.equals("unidade")) {
                id = sharedPrefs.getInt(KEY_UNIDADE_ID, -1);
            } else if (tipoUsuario.equals("lider")) {
                id = sharedPrefs.getInt(KEY_USUARIO_ID, -1);
            } else if (tipoUsuario.equals("gestor")) {
                id = sharedPrefs.getInt(KEY_USUARIO_ID, -1);
            } else {
                android.widget.Toast.makeText(getContext(), "Tipo de usuário inválido", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            String novoNome = etNome.getText().toString();
            String novoEmail = etEmail.getText().toString();
            String novaSenha = etSenha.getText().toString();


            atualizarUsuarioApi(novoNome, novoEmail, novaSenha, id);

            txtUsuarioNome.setText(novoNome);
            txtUsuarioCredencial.setText(novoEmail);
            txtUsuarioSenha.setText("•".repeat(novaSenha.length()));

            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
    }

    private void atualizarUsuarioApi(String nome, String credencial, String senha, Integer id) {
        SharedPreferences sharedPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String tipoUsuario = sharedPrefs.getString(KEY_TIPO_USUARIO, "");

        UsuarioRequest bodyUsuario = new UsuarioRequest(nome, credencial, senha);
        UnidadeRequest bodyUnidade = new UnidadeRequest(nome, credencial, senha);


        retrofit2.Call<Void> call = null;

        switch (tipoUsuario) {
            case "unidade":
                UnidadeApi unidadeApi = SqlRetrofitClient.getClient(requireContext()).create(UnidadeApi.class);
                call = unidadeApi.atualizarUnidadeParcial(id, bodyUnidade);
                break;
            case "líder":
                LiderApi liderApi = SqlRetrofitClient.getClient(requireContext()).create(LiderApi.class);
                call = liderApi.atualizarLiderParcial(id, bodyUsuario);
                break;
            case "gestor":
                GestorApi gestorApi = SqlRetrofitClient.getClient(requireContext()).create(GestorApi.class);
                call = gestorApi.atualizarGestorParcial(id, bodyUsuario);
                break;
            default:
                android.widget.Toast.makeText(getContext(), "Tipo de usuário inválido", android.widget.Toast.LENGTH_SHORT).show();
                return;
        }

        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    android.widget.Toast.makeText(getContext(), "Dados atualizados com sucesso!", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    android.widget.Toast.makeText(getContext(), "Erro ao atualizar: " + response.code(), android.widget.Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                android.widget.Toast.makeText(getContext(), "Falha na conexão: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });


        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(KEY_USUARIO_NOME, nome);
        editor.putString(KEY_USUARIO_CREDENCIAL, credencial);
        editor.putString(KEY_USUARIO_SENHA, senha);
        editor.apply();
    }
}
