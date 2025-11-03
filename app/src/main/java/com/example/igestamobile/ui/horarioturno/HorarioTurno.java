package com.example.igestamobile.ui.horarioturno;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.igestamobile.R;
import com.example.igestamobile.data.api.Client.SqlRetrofitClient;
import com.example.igestamobile.data.api.Turno.TurnoApi;
import com.example.igestamobile.data.model.Turno.TurnoRequest;
import com.example.igestamobile.data.model.Turno.TurnoResponse;
import com.example.igestamobile.ui.main.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorarioTurno extends AppCompatActivity {
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_UNIDADE_ID = "UNIDADE_ID";
    private static final String TAG = "HorarioTurno";

    private TextInputEditText inputInicioManha, inputTerminoManha;
    private TextInputEditText inputInicioTarde, inputTerminoTarde;
    private TextInputEditText inputInicioNoite, inputTerminoNoite;
    private AppCompatButton btConcluido;

    private TurnoApi turnoApi = SqlRetrofitClient.getClient(this).create(TurnoApi.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_horario_turno);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupTimePickers();

        btConcluido.setOnClickListener(v -> insertTurnos());
    }

    private void initializeViews() {
        inputInicioManha = findViewById(R.id.input_horario_inicio_manha);
        inputTerminoManha = findViewById(R.id.input_horario_termino_manha);
        inputInicioTarde = findViewById(R.id.input_horario_inicio_tarde);
        inputTerminoTarde = findViewById(R.id.input_horario_termino_tarde);
        inputInicioNoite = findViewById(R.id.input_horario_inicio_noite);
        inputTerminoNoite = findViewById(R.id.input_horario_termino_noite);
        btConcluido = findViewById(R.id.bt_selecionar);
    }

    private void setupTimePickers() {
        List<TextInputEditText> timeInputs = new ArrayList<>();
        timeInputs.add(inputInicioManha);
        timeInputs.add(inputTerminoManha);
        timeInputs.add(inputInicioTarde);
        timeInputs.add(inputTerminoTarde);
        timeInputs.add(inputInicioNoite);
        timeInputs.add(inputTerminoNoite);

        for (TextInputEditText editText : timeInputs) {
            editText.setFocusable(false);
            editText.setOnClickListener(v -> showTimePickerDialog(editText));
        }
    }

    private void showTimePickerDialog(final TextInputEditText editText) {
        new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String selectedTime = String.format("%02d:%02d:00", hourOfDay, minute);
            editText.setText(selectedTime);
        }, 8, 0, true).show();
    }

    private int getUnidadeId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_UNIDADE_ID, -1);
    }

    private void insertTurnos() {
        int unidadeId = getUnidadeId();
        if (unidadeId == -1) {
            Toast.makeText(this, "ID da Unidade não encontrado. Tente novamente.", Toast.LENGTH_LONG).show();
            return;
        }

        List<TurnoRequest> turnosToInsert = new ArrayList<>();

        addIfValid(turnosToInsert, "Manhã", unidadeId,
                inputInicioManha.getText().toString(), inputTerminoManha.getText().toString());
        addIfValid(turnosToInsert, "Tarde", unidadeId,
                inputInicioTarde.getText().toString(), inputTerminoTarde.getText().toString());
        addIfValid(turnosToInsert, "Noite", unidadeId,
                inputInicioNoite.getText().toString(), inputTerminoNoite.getText().toString());


        if (turnosToInsert.isEmpty()) {
            Toast.makeText(this, "Nenhum horário de turno foi preenchido.", Toast.LENGTH_SHORT).show();
            return;
        }

        final int totalCalls = turnosToInsert.size();
        final int[] completedCalls = {0};
        final int[] successfulCalls = {0};

        for (TurnoRequest request : turnosToInsert) {
            Call<TurnoResponse> call = turnoApi.inserirTurno(request);
            Log.d(TAG, "Enviando: " + new Gson().toJson(request));

            call.enqueue(new Callback<TurnoResponse>() {
                @Override
                public void onResponse(Call<TurnoResponse> call, Response<TurnoResponse> response) {
                    completedCalls[0]++;
                    if (response.isSuccessful()) {
                        successfulCalls[0]++;
                        Log.d(TAG, "Sucesso ao inserir turno: " + request.getNome());
                    } else {
                        Log.e(TAG, "Falha na resposta para turno: " + request.getNome() + ". Code: " + response.code());
                    }
                    checkCompletion(totalCalls, completedCalls[0], successfulCalls[0]);
                }

                @Override
                public void onFailure(Call<TurnoResponse> call, Throwable t) {
                    completedCalls[0]++;
                    Log.e(TAG, "Erro de API para turno: " + request.getNome(), t);
                    checkCompletion(totalCalls, completedCalls[0], successfulCalls[0]);
                }
            });
        }
    }

    private void addIfValid(List<TurnoRequest> list, String nome, int unidadeId, String inicio, String fim) {
        if (!inicio.trim().isEmpty() && !fim.trim().isEmpty()) {
            list.add(new TurnoRequest(unidadeId, nome, inicio.trim(), fim.trim()));
        }
    }

    private void checkCompletion(int total, int completed, int successful) {
        if (completed == total) {
            String message;
            if (successful == total) {
                message = "Todos os " + total + " turnos inseridos com sucesso!";

                Toast.makeText(HorarioTurno.this, message, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(HorarioTurno.this, MainActivity.class);
                startActivity(intent);
                finish();

            } else if (successful > 0) {
                message = successful + " de " + total + " turnos foram inseridos. Verifique os erros no log.";
                Toast.makeText(HorarioTurno.this, message, Toast.LENGTH_LONG).show();
            } else {
                message = "Falha ao inserir todos os turnos. Verifique sua conexão ou a API.";
                Toast.makeText(HorarioTurno.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}