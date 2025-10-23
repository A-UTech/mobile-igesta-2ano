package com.example.igestamobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igestamobile.R;
import com.example.igestamobile.adapter.CondenaAdapter;
import com.example.igestamobile.data.api.CondenaApi;
import com.example.igestamobile.data.api.RetrofitClient;
import com.example.igestamobile.data.model.CondenaModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SelecionarCondenas extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CondenaAdapter adapter;
    private CondenaApi condenaApi;

    private Button btSelecionar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_condenas);

        recyclerView = findViewById(R.id.condena_recyclerView);

        condenaApi = RetrofitClient.getClient().create(CondenaApi.class);

        adapter = new CondenaAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        fetchCondenas();

        btSelecionar = findViewById(R.id.bt_selecionar);

        btSelecionar.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            List<CondenaModel> condenasSelecionadas = adapter.getCondenasSelecionadas();
        });
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

}
