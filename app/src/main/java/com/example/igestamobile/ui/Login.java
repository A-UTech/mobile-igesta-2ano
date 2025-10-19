package com.example.igestamobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.example.igestamobile.R;
import com.example.igestamobile.data.api.LoginApiClient;
import com.example.igestamobile.data.model.LoginModelRequest;
import com.example.igestamobile.data.model.LoginModelResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    private EditText etEmailCnpj;
    private EditText etSenha;
    private AppCompatButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmailCnpj = findViewById(R.id.login_email_input);
        etSenha = findViewById(R.id.login_senha_input);
        btnLogin = findViewById(R.id.logar_bt);

        btnLogin.setOnClickListener(view -> {
            String emailCnpj = etEmailCnpj.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (!emailCnpj.isEmpty() && !senha.isEmpty()) {
                performLogin(emailCnpj, senha);
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogin(String emailCnpj, String senha) {

        LoginModelRequest request = new LoginModelRequest(emailCnpj, senha);

        btnLogin.setEnabled(false);

        LoginApiClient.getService().login(request)
                .enqueue(new Callback<LoginModelResponse>() {

                    @Override
                    public void onResponse(Call<LoginModelResponse> call, Response<LoginModelResponse> response) {
                        btnLogin.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            LoginModelResponse user = response.body();

                            if (user.getTipoUsuario().equals("unidade")) {
                                Intent rota = new Intent(Login.this, SelecionarCondenas.class);
                                startActivity(rota);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                            } else {
                                Intent rota = new Intent(Login.this, MainActivity.class);
                                startActivity(rota);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                            }
                        } else if (response.code() == 401) {
                            Toast.makeText(Login.this, "Credenciais inválidas. Tente novamente.", Toast.LENGTH_LONG).show();

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
}