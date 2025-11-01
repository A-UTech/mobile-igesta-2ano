package com.example.igestamobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.example.igestamobile.adapter.OnTurnoClickListener;
import com.example.igestamobile.adapter.TurnosAdapter;
import com.example.igestamobile.data.api.RegistroApi;
import com.example.igestamobile.data.api.TurnoApi;
import com.example.igestamobile.data.api.MongoRetrofitClient;
import com.example.igestamobile.data.api.SqlRetrofitClient;
import com.example.igestamobile.data.api.UnidadeApi;
import com.example.igestamobile.data.model.RegistroModel;
import com.example.igestamobile.data.model.TurnoResponse;
import com.example.igestamobile.data.model.UnidadeModel;
import com.example.igestamobile.data.model.RegistroCondenaModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

public class Historico extends Fragment implements OnTurnoClickListener {
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_UNIDADE_ID = "UNIDADE_ID";
    private CalendarView calendarView;
    private RecyclerView recyclerViewHistorico;
    private TurnosAdapter turnosAdapter;
    private List<RegistroModel> ultimosRegistrosDoDia = new ArrayList<>();

    public Historico() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_historico, container, false);
        calendarView = view.findViewById(R.id.calendarView2);
        recyclerViewHistorico = view.findViewById(R.id.rv_turnos);

        recyclerViewHistorico.setLayoutManager(new LinearLayoutManager(getContext()));
        turnosAdapter = new TurnosAdapter(new ArrayList<>(), ultimosRegistrosDoDia, requireContext(), this);
        recyclerViewHistorico.setAdapter(turnosAdapter);

        if (calendarView != null) {
            calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                    carregarDadosDoMongo(year, month + 1, dayOfMonth);
                }
            });

            long hoje = calendarView.getDate();
            Calendar calHoje = Calendar.getInstance();
            calHoje.setTimeInMillis(hoje);

            carregarDadosDoMongo(calHoje.get(Calendar.YEAR), calHoje.get(Calendar.MONTH) + 1, calHoje.get(Calendar.DAY_OF_MONTH));
        }

        View btVoltar = view.findViewById(R.id.bt_voltar_historico);
        if (btVoltar != null) {
            btVoltar.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        }

        return view;
    }

    private void carregarDadosDoMongo(int year, int month, int dayOfMonth) {

        SimpleDateFormat sdfMongo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdfMongo.setTimeZone(TimeZone.getTimeZone("UTC"));

        Calendar calendar = Calendar.getInstance();

        calendar.set(year, month - 1, dayOfMonth, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        String dataInicioISO = sdfMongo.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        String dataFimISO = sdfMongo.format(calendar.getTime());

        Log.d("MongoQuery", "Início: " + dataInicioISO + " | Fim: " + dataFimISO);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Integer unidadeId = prefs.getInt(KEY_UNIDADE_ID, -1);

        UnidadeApi unidadeApi = SqlRetrofitClient.getClient(requireContext()).create(UnidadeApi.class);
        unidadeApi.selecionarUnidadePorId(unidadeId).enqueue(new Callback<UnidadeModel>() {
            @Override
            public void onResponse(@NonNull Call<UnidadeModel> call, @NonNull Response<UnidadeModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UnidadeModel unidadeResponse = response.body();
                    String unidadeNome = unidadeResponse.getNome();
                    RegistroApi registroApi = MongoRetrofitClient.getClient().create(RegistroApi.class);
                    registroApi.buscarRegistrosPorPeriodoEUnidade(dataInicioISO, dataFimISO, unidadeNome)
                            .enqueue(new Callback<List<RegistroModel>>() {
                                @Override
                                public void onResponse(@NonNull Call<List<RegistroModel>> call,
                                                       @NonNull Response<List<RegistroModel>> response) {

                                    if (response.isSuccessful() && response.body() != null) {
                                        List<RegistroModel> registros = response.body();
                                        Log.i("MongoAPI", "Registros carregados: " + registros.size());
                                        fetchTurnosDetails(registros);
                                    } else {
                                        Log.e("MongoAPI", "Erro ao carregar dados. Código: " + response.code());
                                        atualizarRecyclerView(new ArrayList<>(), new ArrayList<>());
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<List<RegistroModel>> call,
                                                      @NonNull Throwable t) {
                                    Log.e("MongoAPI", "Falha na comunicação com a API: " + t.getMessage());
                                    atualizarRecyclerView(new ArrayList<>(), new ArrayList<>());
                                }
                            });
                } else {
                    Log.e("UnidadeAPI", "Erro ao carregar dados. Código: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UnidadeModel> call, @NonNull Throwable t) {
                Log.e("UnidadeAPI", "Falha na comunicação com a API: " + t.getMessage());
            }
        });
    }

    private void fetchTurnosDetails(List<RegistroModel> registros) {
        this.ultimosRegistrosDoDia = registros;

        Set<Integer> turnosIds = new HashSet<>();
        for (RegistroModel registro : registros) {
            if (registro.getIdTurno() != null) {
                turnosIds.add(registro.getIdTurno());
            }
        }

        if (turnosIds.isEmpty()) {
            atualizarRecyclerView(new ArrayList<>(), new ArrayList<>());
            return;
        }

        TurnoApi turnoApi = SqlRetrofitClient.getClient(requireContext()).create(TurnoApi.class);
        List<TurnoResponse> listaTurnos = new ArrayList<>();

        AtomicInteger responsesCount = new AtomicInteger(0);
        int totalRequests = turnosIds.size();

        for (Integer id : turnosIds) {
            turnoApi.selecionarPorId(id).enqueue(new Callback<TurnoResponse>() {
                @Override
                public void onResponse(@NonNull Call<TurnoResponse> call, @NonNull Response<TurnoResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        listaTurnos.add(response.body());
                    } else {
                        Log.w("TurnoAPI", "Turno ID " + id + " não encontrado ou erro " + response.code());
                    }

                    if (responsesCount.incrementAndGet() == totalRequests) {
                        atualizarRecyclerView(listaTurnos, ultimosRegistrosDoDia);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<TurnoResponse> call, @NonNull Throwable t) {
                    Log.e("TurnoAPI", "Falha ao buscar Turno ID " + id + ": " + t.getMessage());

                    if (responsesCount.incrementAndGet() == totalRequests) {
                        atualizarRecyclerView(listaTurnos, ultimosRegistrosDoDia);
                    }
                }
            });
        }
    }

    private void atualizarRecyclerView(List<TurnoResponse> turnos, List<RegistroModel> registrosDoDia) {
        if (turnosAdapter != null) {
            turnosAdapter.updateList(turnos, registrosDoDia);
        } else {
            Log.e("Historico", "TurnosAdapter não inicializado.");
        }
    }

    @Override
    public void onTurnoClick(TurnoResponse turnoClicado, List<RegistroModel> registrosDoDia) {
        Log.d("TurnoClick", "Turno clicado: " + turnoClicado.getNome());

        List<RegistroModel> registrosDoTurno = registrosDoDia.stream()
                .filter(r -> turnoClicado.getId().equals(r.getIdTurno()))
                .collect(Collectors.toList());

        List<RegistroCondenaModel> condenasConsolidadas = consolidarCondenas(registrosDoTurno);

        navegarParaDetalhesCondenas(turnoClicado.getNome(), condenasConsolidadas);
    }

    private List<RegistroCondenaModel> consolidarCondenas(List<RegistroModel> registros) {
        Map<String, RegistroCondenaModel> mapaConsolidado = new HashMap<>();

        for (RegistroModel registro : registros) {
            if (registro.getCondenas() != null) {
                for (RegistroCondenaModel condena : registro.getCondenas()) {
                    String chave = condena.getNome() + "_" + condena.getTipo();

                    if (mapaConsolidado.containsKey(chave)) {
                        RegistroCondenaModel existente = mapaConsolidado.get(chave);
                        existente.setQuantidade(existente.getQuantidade() + condena.getQuantidade());
                        mapaConsolidado.put(chave, existente);
                    } else {
                        mapaConsolidado.put(chave, new RegistroCondenaModel(
                                condena.getNome(),
                                condena.getTipo(),
                                condena.getQuantidade()
                        ));
                    }
                }
            }
        }
        return new ArrayList<>(mapaConsolidado.values());
    }

    private void navegarParaDetalhesCondenas(String nomeTurno, List<RegistroCondenaModel> condenas) {
        Bundle args = new Bundle();
        args.putString("nomeTurno", nomeTurno);

        args.putParcelableArrayList("condenas", new ArrayList<>(condenas));

        try {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_historico_to_navigation_turnos, args);
        } catch (IllegalArgumentException e) {
            Log.e("Navigation", "Erro ao navegar para Turnos: " + e.getMessage());
        }
    }
}