package com.example.igestamobile.ui.notifications;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.igestamobile.NotificationHelper;
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
import com.example.igestamobile.ui.dialogs.AcessoNegadoDialog;
import com.example.igestamobile.utils.MaskUtil;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment implements CondenaUnidadeAdapter.OnCondenaSelectionListener {
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USUARIO_CREDENCIAL = "USUARIO_CREDENCIAL";
    private static final String KEY_UNIDADE_ID = "UNIDADE_ID";
    private static final String KEY_USUARIO_NOME = "USUARIO_NOME";
    private static final String KEY_UNIDADE_NOME = "UNIDADE_NOME";
    private static final String KEY_EMPRESA_ID = "EMPRESA_ID";
    private static final String KEY_EMPRESA_NOME = "EMPRESA_NOME";
    private static final String KEY_TURNO_ID = "TURNO_ID";
    private static final String KEY_TIPO_USUARIO = "TIPO_USUARIO";
    private CondenaApi condenaApi;
    private CondenaUnidadeApi condenaUnidadeApi;
    private UnidadeApi unidadeApi;
    private EmpresaApi empresaApi;
    private TurnoApi turnoApi;
    private FragmentNotificationsBinding binding;
    private CondenaUnidadeAdapter adapter;
    private Dialog dialog_enviar_contagens, dialog_enviar_condenas, dialog_condena_opcoes, dialog_apagar, dialog_concluir;
    private List<Condena> pendingCondenasList = null;
    private Button bt_filtrar_total;
    private Button bt_filtrar_parcial;
    private ShapeableImageView fotoPerfilCondenas;
    private FirebaseFirestore db;
    private String currentFilterType = null;
    private TextView txt_concluir_alteracoes, txt_descartar_alteracoes, bt_enviar_contagens, txt_ola_contar_condenas;
    private ImageButton btn_option;
    private TextInputEditText searchCondenas;
    private List<CondenaUnidadeResponse> listaOriginalCondenas = new ArrayList<>();

    private ActivityResultLauncher<String> createDocumentLauncher;

    private Integer getClienteIdSalvo() {
        Integer clienteId = -1;

        if (getContext() != null) {
            SharedPreferences sharedPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            clienteId = sharedPrefs.getInt(KEY_UNIDADE_ID, -1);
        }
        return clienteId;
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

    private boolean isUsuarioGestor() {
        if (getContext() == null) return false;

        SharedPreferences sharedPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String cargo = sharedPrefs.getString(KEY_TIPO_USUARIO, "");

        return "Gestor".equalsIgnoreCase(cargo);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (!isUsuarioGestor()) {
            AcessoNegadoDialog dialog = new AcessoNegadoDialog();
            dialog.show(getParentFragmentManager(), "AcessoNegadoDialogTag");

            return inflater.inflate(R.layout.fragment_notifications, container, false);
        }

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        condenaApi = SqlRetrofitClient.getClient(requireContext()).create(CondenaApi.class);
        condenaUnidadeApi = SqlRetrofitClient.getClient(requireContext()).create(CondenaUnidadeApi.class);
        unidadeApi = SqlRetrofitClient.getClient(requireContext()).create(UnidadeApi.class);
        empresaApi = SqlRetrofitClient.getClient(requireContext()).create(EmpresaApi.class);
        turnoApi = SqlRetrofitClient.getClient(requireContext()).create(TurnoApi.class);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CondenaUnidadeAdapter(new ArrayList<>(), requireContext(), condenaApi, this);
        binding.recyclerView.setAdapter(adapter);

        searchCondenas = root.findViewById(R.id.inputTextPesquisarCondenas);

        carregarCondenasDeUnidade(getClienteIdSalvo());
        setupSearchListener();

        txt_ola_contar_condenas = root.findViewById(R.id.txt_ola_contar_condenas);

        String nome = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USUARIO_NOME, null);
        txt_ola_contar_condenas.setText("Olá, " + nome + "!");

        bt_enviar_contagens = root.findViewById(R.id.bt_enviar_contagens);

        bt_filtrar_total = root.findViewById(R.id.btn_total);
        bt_filtrar_parcial = root.findViewById(R.id.btn_parcial);

        dialog_enviar_contagens = new Dialog(requireContext());
        dialog_enviar_condenas = new Dialog(requireContext());
        dialog_condena_opcoes = new Dialog(requireContext());
        dialog_apagar = new Dialog(requireContext());
        dialog_concluir = new Dialog(requireContext());

        dialog_enviar_contagens.setContentView(R.layout.dialog_enviar);
        dialog_enviar_condenas.setContentView(R.layout.dialog_enviar_condenas);
        dialog_condena_opcoes.setContentView(R.layout.dialog_condena_opcoes);
        dialog_apagar.setContentView(R.layout.dialog_apagar);
        dialog_concluir.setContentView(R.layout.dialog_concluir);

        dialog_enviar_contagens.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_enviar_condenas.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_enviar_contagens.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_enviar_condenas.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_apagar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_concluir.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_apagar.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_concluir.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        db = FirebaseFirestore.getInstance();

        fotoPerfilCondenas = root.findViewById(R.id.foto_perfil_condenas);
        btn_option = root.findViewById(R.id.btn_option);
        txt_descartar_alteracoes = root.findViewById(R.id.txt_descartar_alteracoes);
        txt_concluir_alteracoes = root.findViewById(R.id.txt_concluir_alteracoes);
        Button bt_n_enviar_contagens = dialog_enviar_contagens.findViewById(R.id.bt_n_remover);
        Button bt_enviar_contagens_dialog = dialog_enviar_contagens.findViewById(R.id.bt_cadastrar_func_dialog);
        Button bt_enviar_condenas_dialog = dialog_enviar_condenas.findViewById(R.id.bt_enviar_condenas_dialog);
        Button bt_n_remover = dialog_apagar.findViewById(R.id.bt_n_remover);
        Button bt_s_remover = dialog_apagar.findViewById(R.id.bt_s_remover);
        Button bt_n_concluir = dialog_concluir.findViewById(R.id.bt_n_concluir);
        Button bt_s_concluir = dialog_concluir.findViewById(R.id.bt_s_concluir);
        TextView bt_limpar_contagens = dialog_condena_opcoes.findViewById(R.id.bt_limpar_contagens);
        TextView bt_editar_condenas = dialog_condena_opcoes.findViewById(R.id.bt_editar_condenas);
        TextInputEditText dialogInputInicio = dialog_enviar_condenas.findViewById(R.id.input_horario_inicio);
        TextInputEditText dialogInputTermino = dialog_enviar_condenas.findViewById(R.id.input_horario_termino);
        TextInputEditText dialogInputLote = dialog_enviar_condenas.findViewById(R.id.input_lote);

        if (txt_concluir_alteracoes != null) txt_concluir_alteracoes.setVisibility(View.GONE);
        if (txt_descartar_alteracoes != null) txt_descartar_alteracoes.setVisibility(View.GONE);

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

        if (bt_enviar_contagens != null) {
            bt_enviar_contagens.setOnClickListener(v -> {
                dialog_enviar_contagens.show();
            });
        }

        if (bt_n_enviar_contagens != null) {
            bt_n_enviar_contagens.setOnClickListener(v -> {
                dialog_enviar_contagens.dismiss();
            });
        }

        if (bt_enviar_contagens_dialog != null) {
            bt_enviar_contagens_dialog.setOnClickListener(v -> {
                dialog_enviar_contagens.dismiss();
                dialog_enviar_condenas.show();
            });
        }

        if (btn_option != null) {
            btn_option.setOnClickListener(v -> {
                dialog_condena_opcoes.show();
            });
        }

        if (bt_limpar_contagens != null) {
            bt_limpar_contagens.setOnClickListener(v -> {
                dialog_condena_opcoes.dismiss();
                limparTodasAsContagens();
            });
        }

        if (bt_editar_condenas != null) {
            bt_editar_condenas.setOnClickListener(v -> {
                dialog_condena_opcoes.dismiss();
                carregarCondenasParaEdicao(getClienteIdSalvo());
                Toast.makeText(requireContext(), "Modo de Edição ativado. Selecione as condenas.", Toast.LENGTH_LONG).show();

                if (bt_enviar_contagens != null) bt_enviar_contagens.setVisibility(View.GONE);
                if (bt_filtrar_total != null) bt_filtrar_total.setVisibility(View.GONE);
                if (bt_filtrar_parcial != null) bt_filtrar_parcial.setVisibility(View.GONE);
                if (btn_option != null) btn_option.setVisibility(View.GONE);

                if (txt_concluir_alteracoes != null) txt_concluir_alteracoes.setVisibility(View.VISIBLE);
                if (txt_descartar_alteracoes != null) txt_descartar_alteracoes.setVisibility(View.VISIBLE);
            });
        }

        if (txt_descartar_alteracoes != null) {
            txt_descartar_alteracoes.setOnClickListener(v -> {
                dialog_apagar.show();
                bt_n_remover.setOnClickListener(v1 -> {
                    dialog_apagar.dismiss();
                });
                bt_s_remover.setOnClickListener(v1 -> {
                    dialog_apagar.dismiss();
                    carregarCondenasDeUnidade(getClienteIdSalvo());
                    Toast.makeText(requireContext(), "Alterações descartadas. Retornando ao Modo Contagem.", Toast.LENGTH_SHORT).show();

                    if (bt_enviar_contagens != null) bt_enviar_contagens.setVisibility(View.VISIBLE);
                    if (bt_filtrar_total != null) bt_filtrar_total.setVisibility(View.VISIBLE);
                    if (bt_filtrar_parcial != null) bt_filtrar_parcial.setVisibility(View.VISIBLE);
                    if (btn_option != null) btn_option.setVisibility(View.VISIBLE);

                    if (txt_concluir_alteracoes != null) txt_concluir_alteracoes.setVisibility(View.GONE);
                    if (txt_descartar_alteracoes != null) txt_descartar_alteracoes.setVisibility(View.GONE);
                });
            });
        }

        if (txt_concluir_alteracoes != null) {
            txt_concluir_alteracoes.setOnClickListener(v -> {
                if (adapter.getCondenasParaAdicionarIds().isEmpty() && adapter.getCondenasParaRemoverIds().isEmpty()) {
                    Toast.makeText(requireContext(), "Nenhuma alteração pendente para salvar.", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog_concluir.show();
                bt_n_concluir.setOnClickListener(v1 -> {
                    dialog_concluir.dismiss();
                });
                bt_s_concluir.setOnClickListener(v1 -> {
                    dialog_concluir.dismiss();
                    enviarAlteracoesDeCondenas();
                });
            });
        }

        loadProfileImage();

        if (bt_enviar_condenas_dialog != null) {
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
                                        String lote = dialogInputLote.getText().toString();

                                        turnoApi.selecionarPorUnidadeEPeriodo(finalUnidadeId, inicio, termino).enqueue(new Callback<TurnoResponse>() {
                                            @Override
                                            public void onResponse(Call<TurnoResponse> call, Response<TurnoResponse> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    sharedPrefs.edit().putInt(KEY_TURNO_ID, response.body().getId()).apply();

                                                    String gestorFinal = sharedPrefs.getString(KEY_USUARIO_NOME, "Daniel Freitas");
                                                    String unidadeFinal = sharedPrefs.getString(KEY_UNIDADE_NOME, "");
                                                    String empresaFinal = sharedPrefs.getString(KEY_EMPRESA_NOME, "");
                                                    Integer idTurnoFinal = sharedPrefs.getInt(KEY_TURNO_ID, -1);

                                                    String loteFinal = lote;
                                                    Date dataAtual = new Date();

                                                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.US);

                                                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                                                    String dataFormatada = sdf.format(dataAtual);

                                                    RegistroModel request = new RegistroModel(
                                                            gestorFinal,
                                                            empresaFinal,
                                                            idTurnoFinal,
                                                            dataFormatada,
                                                            loteFinal,
                                                            unidadeFinal,
                                                            condenasParaRegistro
                                                    );

                                                    MongoRetrofitClient.getClient().create(RegistroApi.class).inserirRegistro(request).enqueue(new Callback<RegistroModel>() {
                                                        @Override
                                                        public void onResponse(Call<RegistroModel> call, Response<RegistroModel> response) {
                                                            if (response.isSuccessful()) {
                                                                Toast.makeText(requireContext(), "Condenas enviadas com sucesso.", Toast.LENGTH_SHORT).show();

                                                                limparTodasAsContagens();

                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                                    if (requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                                                                            != PackageManager.PERMISSION_GRANTED) {
                                                                        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                                                                    }
                                                                }

                                                                NotificationHelper.showNotification(
                                                                        requireContext(),
                                                                        "Suas contagens foram enviadas!",
                                                                        "Volte mais tarde para inserir as contagens do próximo turno."
                                                                );
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
        }

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

    private void setupSearchListener() {
        if (searchCondenas == null) return;

        searchCondenas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        if (adapter == null) return;

        String query = text.toLowerCase(Locale.getDefault()).trim();

        if (query.isEmpty()) {
            if (adapter.getModo() == CondenaUnidadeAdapter.MODO_CONTAGEM) {
                adapter.setLista(new ArrayList<>(listaOriginalCondenas));
            } else {
                carregarCondenasParaEdicao(getClienteIdSalvo());
            }
            return;
        }

        List<CondenaUnidadeResponse> listaBase = adapter.getListaAtual();
        List<CondenaUnidadeResponse> listaFiltrada = new ArrayList<>();

        for (CondenaUnidadeResponse condena : listaBase) {
            if (condena.getNome() != null && condena.getNome().toLowerCase(Locale.getDefault()).contains(query)) {
                listaFiltrada.add(condena);
            }
        }

        adapter.setLista(listaFiltrada);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCondenaSelectionChanged(boolean hasPendingChanges) {
        if (txt_concluir_alteracoes != null) {
            txt_concluir_alteracoes.setEnabled(hasPendingChanges);
        }
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

                    listaOriginalCondenas.clear();
                    listaOriginalCondenas.addAll(response.body());

                    adapter.setLista(new ArrayList<>(listaOriginalCondenas));
                    adapter.setModo(CondenaUnidadeAdapter.MODO_CONTAGEM);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Falha ao carregar condenas de contagem.", Toast.LENGTH_LONG).show();
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

    private void carregarCondenasParaEdicao(Integer unidadeId) {
        condenaUnidadeApi.selecionarCondenasUnidade(unidadeId).enqueue(new Callback<List<CondenaUnidadeResponse>>() {
            @Override
            public void onResponse(Call<List<CondenaUnidadeResponse>> call, Response<List<CondenaUnidadeResponse>> responseExistentes) {
                if (responseExistentes.isSuccessful() && responseExistentes.body() != null) {

                    adapter.setCondenasExistentesIds(responseExistentes.body());

                    condenaApi.selecionarCondenas().enqueue(new Callback<List<CondenaModel>>() {
                        @Override
                        public void onResponse(Call<List<CondenaModel>> call, Response<List<CondenaModel>> responseTodas) {
                            if (responseTodas.isSuccessful() && responseTodas.body() != null) {

                                List<CondenaUnidadeResponse> condenasConvertidas = responseTodas.body().stream()
                                        .map(condenaModel -> new CondenaUnidadeResponse(
                                                condenaModel.getId()
                                        )).collect(Collectors.toList());

                                adapter.setLista(condenasConvertidas);
                                adapter.setModo(CondenaUnidadeAdapter.MODO_EDICAO);
                                adapter.notifyDataSetChanged();
                                if (adapter.getCondenasParaAdicionarIds().isEmpty() && adapter.getCondenasParaRemoverIds().isEmpty()) {
                                    onCondenaSelectionChanged(false);
                                } else {
                                    onCondenaSelectionChanged(true);
                                }


                            } else {
                                Toast.makeText(requireContext(), "Falha ao carregar lista de todas as condenas.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<CondenaModel>> call, Throwable t) {
                            Toast.makeText(requireContext(), "Não foi possível carregar as condenas gerais", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(requireContext(), "Falha ao carregar condenas existentes da unidade.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<CondenaUnidadeResponse>> call, Throwable t) {
                Toast.makeText(requireContext(), "Erro de rede ao buscar condenas existentes!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarAlteracoesDeCondenas() {
        if (getContext() == null) return;

        Integer unidadeId = getClienteIdSalvo();
        List<Integer> idsParaAdicionar = adapter.getCondenasParaAdicionarIds();
        List<Integer> idsParaRemover = adapter.getCondenasParaRemoverIds();

        AtomicInteger totalCalls = new AtomicInteger(idsParaAdicionar.size() + idsParaRemover.size());
        AtomicInteger successfulCalls = new AtomicInteger(0);

        if (totalCalls.get() == 0) {
            Toast.makeText(requireContext(), "Nenhuma alteração para salvar.", Toast.LENGTH_SHORT).show();
            handleCompletion(0);
            return;
        }

        for (Integer condenaId : idsParaAdicionar) {
            condenaUnidadeApi.associarCondenaUnidade(unidadeId, condenaId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        successfulCalls.incrementAndGet();
                    } else {
                        Log.e("API_CALL", "Falha ao associar Condena ID: " + condenaId + ". Código: " + response.code());
                    }
                    checkCompletion();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("API_CALL", "Erro de rede ao associar Condena ID: " + condenaId, t);
                    checkCompletion();
                }

                private void checkCompletion() {
                    if (totalCalls.decrementAndGet() == 0) {
                        handleCompletion(successfulCalls.get());
                    }
                }
            });
        }

        for (Integer condenaId : idsParaRemover) {
            condenaUnidadeApi.desassociarCondenaUnidade(unidadeId, condenaId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        successfulCalls.incrementAndGet();
                        Log.i("DELETE_SUCCESS", "Condena ID " + condenaId + " desassociada (Resposta: " + response.code() + ")");
                    } else {
                        Log.e("DELETE_FAILURE", "Condena ID " + condenaId + " falhou. Código HTTP: " + response.code() + ". Mensagem: " + response.message());
                    }
                    checkCompletion();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("API_CALL", "Erro de rede ao desassociar Condena ID: " + condenaId, t);
                    checkCompletion();
                }

                private void checkCompletion() {
                    if (totalCalls.decrementAndGet() == 0) {
                        handleCompletion(successfulCalls.get());
                    }
                }
            });
        }
    }

    private void handleCompletion(int successfulCalls) {
        if (!isAdded() || getActivity() == null) return;

        int totalCalls = adapter.getCondenasParaAdicionarIds().size() + adapter.getCondenasParaRemoverIds().size();

        if (successfulCalls == totalCalls) {
            Toast.makeText(requireContext(), "Sucesso! Todas as alterações foram salvas.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireContext(), "Atenção: Apenas " + successfulCalls + " de " + totalCalls + " alterações foram salvas com sucesso. Verifique o log.", Toast.LENGTH_LONG).show();
        }

        carregarCondenasDeUnidade(getClienteIdSalvo());

        if (txt_concluir_alteracoes != null) txt_concluir_alteracoes.setVisibility(View.GONE);
        if (txt_descartar_alteracoes != null) txt_descartar_alteracoes.setVisibility(View.GONE);

        if (bt_enviar_contagens != null) bt_enviar_contagens.setVisibility(View.VISIBLE);
        if (bt_filtrar_total != null) bt_filtrar_total.setVisibility(View.VISIBLE);
        if (bt_filtrar_parcial != null) bt_filtrar_parcial.setVisibility(View.VISIBLE);
        if (btn_option != null) btn_option.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void gerarPlanilha(List<Condena> condenas, Uri uri) {
        if (getContext() == null) return;

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Contagem Condenas");


        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Condena");
        headerRow.createCell(1).setCellValue("Tipo");
        headerRow.createCell(2).setCellValue("Quantidade");
        headerRow.createCell(3).setCellValue("Porcentagem");


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

    private void loadProfileImage() {
        String documentId = getUsuarioCredencial();

        if (documentId == null) {
            Log.e("Firebase", "Credencial não encontrada. Imagem não pode ser carregada.");
            return;
        }

        db.collection("usuarios").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) {
                        Log.d("GlideFix", "Fragment not attached. Cancelling profile image load.");
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("imagem");

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(profileImageUrl)
                                    .override(175, 175)
                                    .centerCrop()
                                    .placeholder(R.mipmap.fotoperfil)
                                    .error(R.mipmap.fotoperfil)
                                    .into(fotoPerfilCondenas);

                            Log.i("Firebase", "Foto de perfil carregada do Firebase.");
                        } else {
                            Log.d("Firebase", "URL da foto de perfil (campo 'imagem') não encontrada no Firestore.");
                        }
                    } else {
                        Log.d("Firebase", "Documento do usuário não encontrado no Firestore (ID: " + documentId + ").");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Erro ao buscar documento do usuário: " + e.getMessage());
                });
    }
    private void limparTodasAsContagens() {
        if (adapter != null) {
            adapter.zerarTodasAsContagens();
        }
    }
}