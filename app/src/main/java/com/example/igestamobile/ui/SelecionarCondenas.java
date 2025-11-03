package com.example.igestamobile.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igestamobile.HorarioTurno;
import com.example.igestamobile.R;
import com.example.igestamobile.adapter.CondenaAdapter;
import com.example.igestamobile.data.api.CondenaApi;
import com.example.igestamobile.data.api.CondenaUnidadeApi;
import com.example.igestamobile.data.api.SqlRetrofitClient;
import com.example.igestamobile.data.model.CondenaModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelecionarCondenas extends AppCompatActivity {
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_UNIDADE_ID = "UNIDADE_ID";
    private RecyclerView recyclerView;
    private CondenaAdapter adapter;
    private CondenaApi condenaApi;
    private CondenaUnidadeApi condenaUnidadeApi;
    private Button btSelecionar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_condenas);

        recyclerView = findViewById(R.id.condena_recyclerView);
        condenaApi = SqlRetrofitClient.getClient(this).create(CondenaApi.class);
        condenaUnidadeApi = SqlRetrofitClient.getClient(this).create(CondenaUnidadeApi.class);

        adapter = new CondenaAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        fetchCondenas();

        btSelecionar = findViewById(R.id.bt_selecionar);
        btSelecionar.setOnClickListener(v -> associaCondenasSelecionadas());
    }

    private void fetchCondenas() {
        condenaApi.selecionarCondenas().enqueue(new Callback<List<CondenaModel>>() {
            @Override
            public void onResponse(Call<List<CondenaModel>> call, Response<List<CondenaModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setCondenas(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<CondenaModel>> call, Throwable t) {
                Toast.makeText(SelecionarCondenas.this, "Não foi possível carregar as condenas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Integer getUnidadeId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_UNIDADE_ID, -1);
    }

    private void associaCondenasSelecionadas() {
        List<CondenaModel> condenasSelecionadas = adapter.getCondenasSelecionadas();
        Integer unidadeId = getUnidadeId();

        if (unidadeId == null || unidadeId <= 0) {
            Toast.makeText(this, "ID da Unidade inválido. Associação cancelada.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (condenasSelecionadas.isEmpty()) {
            Toast.makeText(this, "Nenhuma condena selecionada para associar.", Toast.LENGTH_SHORT).show();
            iniciarHorarioTurno();
            return;
        }

        final int totalCondenas = condenasSelecionadas.size();
        final int[] condenasProcessadas = {0};

        for (CondenaModel condena : condenasSelecionadas) {
            Integer condenaId = condena.getId() != null ? condena.getId().intValue() : null;

            if (condenaId != null) {
                condenaUnidadeApi.associarCondenaUnidade(unidadeId, condenaId).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        condenasProcessadas[0]++;
                        if (!response.isSuccessful()) {
                            Toast.makeText(SelecionarCondenas.this,
                                    "Falha ao associar condena ID: " + condenaId + ". Código: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        if (condenasProcessadas[0] == totalCondenas) {
                            Toast.makeText(SelecionarCondenas.this,
                                    "Associação concluída.",
                                    Toast.LENGTH_LONG).show();
                            iniciarHorarioTurno();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        condenasProcessadas[0]++;
                        Toast.makeText(SelecionarCondenas.this,
                                "Falha de rede ao associar condena. Erro: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();

                        if (condenasProcessadas[0] == totalCondenas) {
                            iniciarHorarioTurno();
                        }
                    }
                });
            }
        }
    }

    private void iniciarHorarioTurno() {
        Intent intent = new Intent(this, HorarioTurno.class);
        startActivity(intent);
        finish();
    }
}