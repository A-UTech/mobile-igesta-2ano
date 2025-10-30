package com.example.igestamobile.ui.notifications;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.igestamobile.R;
import com.example.igestamobile.adapter.CondenaUnidadeAdapter;
import com.example.igestamobile.data.api.CondenaApi;
import com.example.igestamobile.data.api.CondenaUnidadeApi;
import com.example.igestamobile.data.api.EmpresaApi;
import com.example.igestamobile.data.api.MongoRetrofitClient;
import com.example.igestamobile.data.api.RegistroApi;
import com.example.igestamobile.data.api.SqlRetrofitClient;
import com.example.igestamobile.data.api.TurnoApi;
import com.example.igestamobile.data.api.UnidadeApi;
import com.example.igestamobile.data.model.CondenaModel;
import com.example.igestamobile.data.model.CondenaUnidadeResponse;
import com.example.igestamobile.data.model.EmpresaModel;
import com.example.igestamobile.data.model.RegistroCondenaModel;
import com.example.igestamobile.data.model.RegistroModel;
import com.example.igestamobile.data.model.TurnoResponse;
import com.example.igestamobile.data.model.UnidadeModel;
import com.example.igestamobile.databinding.FragmentNotificationsBinding;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USUARIO_ID = "USUARIO_ID";
    private static final String KEY_UNIDADE_ID = "UNIDADE_ID";
    private static final String KEY_USUARIO_NOME = "USUARIO_NOME";
    private static final String KEY_UNIDADE_NOME = "UNIDADE_NOME";
    private static final String KEY_EMPRESA_ID = "EMPRESA_ID";
    private static final String KEY_EMPRESA_NOME = "EMPRESA_NOME";
    private static final String KEY_TURNO_ID = "TURNO_ID";

    private CondenaApi condenaApi;
    private CondenaUnidadeApi condenaUnidadeApi;
    private UnidadeApi unidadeApi;
    private EmpresaApi empresaApi;
    private TurnoApi turnoApi;
    private FragmentNotificationsBinding binding;
    private CondenaUnidadeAdapter adapter;
    private Dialog dialog_enviar_contagens, dialog_enviar_condenas;
    private List<Condena> pendingCondenasList = null;
    private Button bt_filtrar_total;
    private Button bt_filtrar_parcial;
    private String currentFilterType = null;

    private ActivityResultLauncher<String> createDocumentLauncher;

    private Integer getClienteIdSalvo() {
        Integer clienteId = -1;

        if (getContext() != null) {
            SharedPreferences sharedPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            clienteId = sharedPrefs.getInt(KEY_UNIDADE_ID, -1);
        }
        return clienteId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        condenaApi = SqlRetrofitClient.getClient(requireContext()).create(CondenaApi.class);
        condenaUnidadeApi = SqlRetrofitClient.getClient(requireContext()).create(CondenaUnidadeApi.class);
        unidadeApi = SqlRetrofitClient.getClient(requireContext()).create(UnidadeApi.class);
        empresaApi = SqlRetrofitClient.getClient(requireContext()).create(EmpresaApi.class);
        turnoApi = SqlRetrofitClient.getClient(requireContext()).create(TurnoApi.class);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CondenaUnidadeAdapter(new ArrayList<>(), requireContext(), condenaApi);
        binding.recyclerView.setAdapter(adapter);

        carregarCondenasDeUnidade(getClienteIdSalvo());

        TextView bt_enviar_contagens = root.findViewById(R.id.bt_enviar_contagens);


        bt_filtrar_total = root.findViewById(R.id.btn_total);
        bt_filtrar_parcial = root.findViewById(R.id.btn_parcial);

        dialog_enviar_contagens = new Dialog(requireContext());
        dialog_enviar_condenas = new Dialog(requireContext());

        dialog_enviar_contagens.setContentView(R.layout.dialog_enviar);
        dialog_enviar_condenas.setContentView(R.layout.dialog_enviar_condenas);

        dialog_enviar_contagens.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_enviar_condenas.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_enviar_contagens.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_enviar_condenas.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        Button bt_n_enviar_contagens = dialog_enviar_contagens.findViewById(R.id.bt_n_remover);
        Button bt_enviar_contagens_dialog = dialog_enviar_contagens.findViewById(R.id.bt_cadastrar_func_dialog);
        Button bt_enviar_condenas_dialog = dialog_enviar_condenas.findViewById(R.id.bt_enviar_condenas_dialog);
        TextInputEditText dialogInputInicio = dialog_enviar_condenas.findViewById(R.id.input_horario_inicio);
        TextInputEditText dialogInputTermino = dialog_enviar_condenas.findViewById(R.id.input_horario_termino);

        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                uri -> {
                    if (uri != null && pendingCondenasList != null) {
                        gerarPlanilha(pendingCondenasList, uri);
                        pendingCondenasList = null;
                    } else if (uri == null) {
                        Toast.makeText(requireContext(), "Criação de arquivo cancelada pelo usuário.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        bt_enviar_contagens.setOnClickListener(v -> {
            dialog_enviar_contagens.show();
        });

        bt_n_enviar_contagens.setOnClickListener(v -> {
            dialog_enviar_contagens.dismiss();
        });

        bt_enviar_contagens_dialog.setOnClickListener(v -> {
            dialog_enviar_contagens.dismiss();
            dialog_enviar_condenas.show();
        });


        bt_enviar_condenas_dialog.setOnClickListener(v -> {
            dialog_enviar_condenas.dismiss();

            List<CondenaUnidadeResponse> contagensFinais = adapter.getContagensFinais();

            List<Condena> listaParaPlanilha = new ArrayList<>();
            for (CondenaUnidadeResponse item : contagensFinais) {
                listaParaPlanilha.add(new Condena(item.getNome(), item.getQuantidade(), item.getTipo()));
            }

            List<RegistroCondenaModel> condenasParaRegistro = adapter.getContagensFinaisMongo();
            SharedPreferences sharedPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String gestor = sharedPrefs.getString(KEY_USUARIO_NOME, "");
            Integer unidadeId = sharedPrefs.getInt(KEY_UNIDADE_ID, -1);

            unidadeApi.selecionarUnidadePorId(unidadeId).enqueue(new Callback<UnidadeModel>() {
                @Override
                public void onResponse(Call<UnidadeModel> call, Response<UnidadeModel> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putString(KEY_UNIDADE_NOME, response.body().getNome());
                        Integer empresaId = response.body().getIdEmpresa();
                        editor.putInt(KEY_EMPRESA_ID, empresaId);
                        editor.apply();

                        empresaApi.selecionarEmpresaPorId(empresaId).enqueue(new Callback<EmpresaModel>() {
                            @Override
                            public void onResponse(Call<EmpresaModel> call, Response<EmpresaModel> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    sharedPrefs.edit().putString(KEY_EMPRESA_NOME, response.body().getNome()).apply();

                                    Integer finalUnidadeId = sharedPrefs.getInt(KEY_UNIDADE_ID, -1);
                                    String inicio = dialogInputInicio.getText().toString();
                                    String termino = dialogInputTermino.getText().toString();

                                    turnoApi.selecionarPorUnidadeEPeriodo(finalUnidadeId, inicio, termino).enqueue(new Callback<TurnoResponse>() {
                                        @Override
                                        public void onResponse(Call<TurnoResponse> call, Response<TurnoResponse> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                sharedPrefs.edit().putInt(KEY_TURNO_ID, response.body().getId()).apply();

                                                String gestorFinal = sharedPrefs.getString(KEY_USUARIO_NOME, "Daniel Freitas");
                                                String unidadeFinal = sharedPrefs.getString(KEY_UNIDADE_NOME, "");
                                                String empresaFinal = sharedPrefs.getString(KEY_EMPRESA_NOME, "");
                                                Integer idTurnoFinal = sharedPrefs.getInt(KEY_TURNO_ID, -1);

                                                String lote = "L1234";
                                                Date dataAtual = new Date();

                                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.US);

                                                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                                                String dataFormatada = sdf.format(dataAtual);

                                                RegistroModel request = new RegistroModel(
                                                        gestorFinal,
                                                        empresaFinal,
                                                        idTurnoFinal,
                                                        dataFormatada,
                                                        lote,
                                                        unidadeFinal,
                                                        condenasParaRegistro
                                                );

                                                MongoRetrofitClient.getClient().create(RegistroApi.class).inserirRegistro(request).enqueue(new Callback<RegistroModel>() {
                                                    @Override
                                                    public void onResponse(Call<RegistroModel> call, Response<RegistroModel> response) {
                                                        if (response.isSuccessful()) {
                                                            Toast.makeText(requireContext(), "Condenas enviadas com sucesso.", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(requireContext(), "Erro ao enviar condenas.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<RegistroModel> call, Throwable t) {
                                                        Toast.makeText(requireContext(), "Falha ao enviar condenas.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                                NotificationsFragment.this.pendingCondenasList = listaParaPlanilha;
                                                createDocumentLauncher.launch("condenas.xlsx");

                                            } else {
                                                Toast.makeText(requireContext(), "Falha ao carregar turno.", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<TurnoResponse> call, Throwable t) {
                                            if (isAdded() && getActivity() != null) {
                                                Toast.makeText(requireContext(), "Erro de rede ao buscar turno!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else {
                                    Toast.makeText(requireContext(), "Falha ao carregar empresa.", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<EmpresaModel> call, Throwable t) {
                                if (isAdded() && getActivity() != null) {
                                    Toast.makeText(requireContext(), "Erro de rede ao buscar empresa!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(requireContext(), "Falha ao carregar unidade.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<UnidadeModel> call, Throwable t) {
                    if (isAdded() && getActivity() != null) {
                        Toast.makeText(requireContext(), "Erro de rede ao buscar unidade!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });




        View.OnClickListener filtroClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFilterType = null;


                if (v.getId() == R.id.btn_total) {
                    newFilterType = "Total";
                } else if (v.getId() == R.id.btn_parcial) {
                    newFilterType = "Parcial";
                }


                if (newFilterType != null && newFilterType.equals(currentFilterType)) {
                    currentFilterType = null;
                } else {
                    currentFilterType = newFilterType;
                }


                adapter.aplicarFiltroVisual(currentFilterType);


                updateFiltroButtons(currentFilterType);
            }
        };

        if (bt_filtrar_total != null) {
            bt_filtrar_total.setOnClickListener(filtroClickListener);
        }

        if (bt_filtrar_parcial != null) {
            bt_filtrar_parcial.setOnClickListener(filtroClickListener);
        }


        updateFiltroButtons(currentFilterType);



        return root;
    }

    private void updateFiltroButtons(String activeType) {
        if (getContext() == null) return;
        try {
            int activeColor = requireContext().getResources().getColor(R.color.cinza_claro, null);
            int inactiveColor = requireContext().getResources().getColor(R.color.verde_escuro, null);
            int textColorActive = requireContext().getResources().getColor(R.color.branco, null);
            int textColorInactive = requireContext().getResources().getColor(R.color.branco, null);

            if (bt_filtrar_total != null) {
                if ("Total".equals(activeType)) {
                    bt_filtrar_total.setBackgroundColor(activeColor);
                    bt_filtrar_total.setTextColor(textColorActive);
                } else {
                    bt_filtrar_total.setBackgroundColor(inactiveColor);
                    bt_filtrar_total.setTextColor(textColorInactive);
                }
            }

            if (bt_filtrar_parcial != null) {
                if ("Parcial".equals(activeType)) {
                    bt_filtrar_parcial.setBackgroundColor(activeColor);
                    bt_filtrar_parcial.setTextColor(textColorActive);
                } else {
                    bt_filtrar_parcial.setBackgroundColor(inactiveColor);
                    bt_filtrar_parcial.setTextColor(textColorInactive);
                }
            }
        } catch (Exception e) {


        }
    }

    private void carregarCondenasDeUnidade(Integer unidadeId) {
        condenaUnidadeApi.selecionarCondenasUnidade(unidadeId).enqueue(new Callback<List<CondenaUnidadeResponse>>() {
            @Override
            public void onResponse(Call<List<CondenaUnidadeResponse>> call, Response<List<CondenaUnidadeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setLista(response.body());
                } else {
                    Toast.makeText(requireContext(), "Falha ao carregar IDs de associação.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<CondenaUnidadeResponse>> call, Throwable t) {
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(requireContext(), "Erro de rede!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void gerarPlanilha(List<Condena> condenas, Uri uri) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Condenas");


        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Condena");
        header.createCell(1).setCellValue("Tipo");
        header.createCell(2).setCellValue("Quantidade");
        header.createCell(3).setCellValue("Porcentagem");


        int total = 0;
        for (Condena c : condenas) total += c.getQuantidade();

        int rowIndex = 1;
        for (Condena c : condenas) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(c.getNome());
            row.createCell(1).setCellValue(c.getTipo());
            row.createCell(2).setCellValue(c.getQuantidade());


            double porcentagem = (total > 0) ? ((double) c.getQuantidade() / total) * 100 : 0;
            row.createCell(3).setCellValue(String.format("%.2f%%", porcentagem));
        }

        try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
            if (os != null) {
                workbook.write(os);
                workbook.close();
                Toast.makeText(requireContext(), "Planilha salva com sucesso!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), "Não foi possível abrir o fluxo de saída.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Erro ao salvar planilha: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static class Condena {
        private String nome;
        private int quantidade;
        private String tipo;

        public Condena(String nome, int quantidade, String tipo) {
            this.nome = nome;
            this.quantidade = quantidade;
            this.tipo = tipo;
        }

        public String getNome() { return nome; }
        public int getQuantidade() { return quantidade; }
        public String getTipo() { return tipo; }
    }
}