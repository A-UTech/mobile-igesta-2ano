package com.example.igestamobile.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.igestamobile.R;
import com.example.igestamobile.data.api.AuthApi;
import com.example.igestamobile.data.api.CondenaUnidadeApi;
import com.example.igestamobile.data.api.GestorApi;
import com.example.igestamobile.data.api.LiderApi;
import com.example.igestamobile.data.api.LoginApi;
import com.example.igestamobile.data.api.SqlRetrofitClient;
import com.example.igestamobile.data.api.TokenManager;
import com.example.igestamobile.data.model.AuthRequest;
import com.example.igestamobile.data.model.AuthResponse;
import com.example.igestamobile.data.model.CondenaUnidadeResponse;
import com.example.igestamobile.data.model.GestorModel;
import com.example.igestamobile.data.model.LiderModel;
import com.example.igestamobile.data.model.LoginModelRequest;
import com.example.igestamobile.data.model.LoginModelResponse;
import com.example.igestamobile.utils.MaskUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_CLIENTE_ID = "CLIENTE_ID";
    private EditText etEmailCnpj;
    private EditText etSenha;
    private AppCompatButton btnLogin;
    private TextView cadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmailCnpj = findViewById(R.id.login_email_input);
        etSenha = findViewById(R.id.login_senha_input);
        btnLogin = findViewById(R.id.logar_bt);
        cadastro = findViewById(R.id.fazer_cadastro_txt);

        MaskUtil.aplicarMascara(etEmailCnpj);

        btnLogin.setOnClickListener(view -> {
            String emailCnpj = etEmailCnpj.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (!emailCnpj.isEmpty() && !senha.isEmpty()) {
                pegarToken(emailCnpj, senha);
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            }
        });

        cadastro.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Cadastro.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void pegarToken(String emailCnpj, String senha) {
        AuthRequest authRequest = new AuthRequest(emailCnpj, senha);
        SqlRetrofitClient.getClient(this).create(AuthApi.class).login(authRequest)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        btnLogin.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse authResponse = response.body();
                            TokenManager tokenManager = new TokenManager(Login.this);
                            tokenManager.saveToken(authResponse.getToken());
                            Toast.makeText(Login.this, authResponse.getToken(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Login.this, "Erro no servidor: Código " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnLogin.setEnabled(true);
                        Toast.makeText(Login.this, "Falha na conexão de rede ao buscar token.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void performLogin(String emailCnpj, String senha) {
        LoginModelRequest request = new LoginModelRequest(emailCnpj, senha);
        btnLogin.setEnabled(false);

        SqlRetrofitClient.getClient(this).create(LoginApi.class).login(request)
                .enqueue(new Callback<LoginModelResponse>() {
                    @Override
                    public void onResponse(Call<LoginModelResponse> call, Response<LoginModelResponse> response) {
                        btnLogin.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            LoginModelResponse user = response.body();

                            switch (user.getTipoUsuario()) {
                                case "unidade":
                                    handleLoginUnidade(user);
                                    break;

                                case "lider":
                                    handleLoginLider(user);
                                    break;

                                case "gestor":
                                    handleLoginGestor(user);
                                    break;

                                default:
                                    Toast.makeText(Login.this, "Tipo de usuário inválido.", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else if (response.code() == 401) {
                            etEmailCnpj.setError("E-mail ou CNPJ inválido.");
                            etSenha.setError("Senha inválida.");
                            etEmailCnpj.setBackground(ContextCompat.getDrawable(Login.this, R.drawable.borda_edittext_error));
                            etSenha.setBackground(ContextCompat.getDrawable(Login.this, R.drawable.borda_edittext_error));
                        } else {
                            Toast.makeText(Login.this, "Erro no servidor: Código " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginModelResponse> call, Throwable t) {
                        btnLogin.setEnabled(true);
                        Toast.makeText(Login.this, "Falha na conexão de rede. Verifique o servidor.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleLoginUnidade(LoginModelResponse user) {
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPrefs.edit().putInt(KEY_CLIENTE_ID, user.getId()).apply();

        CondenaUnidadeApi condenaUnidadeApi = SqlRetrofitClient.getClient(this).create(CondenaUnidadeApi.class);
        condenaUnidadeApi.selecionarCondenasUnidade(user.getId()).enqueue(new Callback<List<CondenaUnidadeResponse>>() {
            @Override
            public void onResponse(Call<List<CondenaUnidadeResponse>> call, Response<List<CondenaUnidadeResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    irParaTela(MainActivity.class);
                } else {
                    irParaTela(SelecionarCondenas.class);
                }
            }

            @Override
            public void onFailure(Call<List<CondenaUnidadeResponse>> call, Throwable t) {
                Toast.makeText(Login.this, "Falha na conexão de rede ao buscar condenas.", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void handleLoginLider(LoginModelResponse user) {
        LiderApi liderApi = SqlRetrofitClient.getClient(this).create(LiderApi.class);
        liderApi.selecionarLideres(user.getId()).enqueue(new Callback<LiderModel>() {
            @Override
            public void onResponse(Call<LiderModel> call, Response<LiderModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Integer unidadeId = response.body().getIdUnidade();

                    SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    sharedPrefs.edit().putInt(KEY_CLIENTE_ID, unidadeId).apply();

                    irParaTela(MainActivity.class);
                } else {
                    Toast.makeText(Login.this, "Não foi possível obter a unidade do líder.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LiderModel> call, Throwable t) {
                Toast.makeText(Login.this, "Falha na conexão de rede ao buscar líder.", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void handleLoginGestor(LoginModelResponse user) {
        GestorApi gestorApi = SqlRetrofitClient.getClient(this).create(GestorApi.class);
        gestorApi.selecionarGestores(user.getId()).enqueue(new Callback<GestorModel>() {
            @Override
            public void onResponse(Call<GestorModel> call, Response<GestorModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Integer unidadeId = response.body().getIdUnidade();

                    SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    sharedPrefs.edit().putInt(KEY_CLIENTE_ID, unidadeId).apply();

                    irParaTela(MainActivity.class);
                } else {
                    Toast.makeText(Login.this, "Não foi possível obter a unidade do gestor.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GestorModel> call, Throwable t) {
                Toast.makeText(Login.this, "Falha na conexão de rede ao buscar gestor.", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void irParaTela(Class<?> destino) {
        Intent rota = new Intent(Login.this, destino);
        startActivity(rota);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}