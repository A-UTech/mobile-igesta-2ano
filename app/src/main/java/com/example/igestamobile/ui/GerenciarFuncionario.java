package com.example.igestamobile.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.example.igestamobile.R;
import com.example.igestamobile.adapter.GerenciarFuncionariosAdapter;
import com.example.igestamobile.data.api.GestorApi;
import com.example.igestamobile.data.api.LiderApi;
import com.example.igestamobile.data.api.SqlRetrofitClient;
import com.example.igestamobile.data.model.GestorModel;
import com.example.igestamobile.data.model.LiderModel;
import com.example.igestamobile.data.model.FuncionarioAdapterModel;
import com.example.igestamobile.ui.dialogs.AcessoNegadoDialog;
import com.example.igestamobile.utils.MaskUtil;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class GerenciarFuncionario extends Fragment implements GerenciarFuncionariosAdapter.OnItemClickListener {

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USUARIO_CREDENCIAL = "USUARIO_CREDENCIAL";
    private static final String KEY_TIPO_USUARIO = "TIPO_USUARIO";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_NOME = "nome";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_CARGO = "cargo";
    private static final String ARG_URL_IMAGEM = "urlImagem";

    private String mParam1;
    private String mParam2;
    private Dialog dialog_cadastrar_func;
    ShapeableImageView imageFuncionarioGerenciar;
    private FirebaseFirestore db;

    private RecyclerView recyclerView;
    private GerenciarFuncionariosAdapter adapter;
    private List<FuncionarioAdapterModel> listaOriginalFuncionarios = new ArrayList<>();
    private TextInputEditText searchFuncionarios;

    private List<GestorModel> listaGestores = new ArrayList<>();
    private List<LiderModel> listaLideres = new ArrayList<>();

    private int apiCallsCompleted = 0;
    private final int totalApiCalls = 2;
    private int imagesLoadedCount = 0;

    public GerenciarFuncionario() {

    }

    public static GerenciarFuncionario newInstance(String param1, String param2) {
        GerenciarFuncionario fragment = new GerenciarFuncionario();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!isUsuarioUnidade()) {
            AcessoNegadoDialog dialog = new AcessoNegadoDialog();
            dialog.show(getParentFragmentManager(), "AcessoNegadoDialogTag");

            return inflater.inflate(R.layout.fragment_gerenciar_funcionario, container, false);
        }

        View view = inflater.inflate(R.layout.fragment_gerenciar_funcionario, container, false);

        View adicionarFuncionario = view.findViewById(R.id.bt_adicionar_funcionario);
        View btVoltar = view.findViewById(R.id.bt_voltar_gf);

        recyclerView = view.findViewById(R.id.recyclerView);
        searchFuncionarios = view.findViewById(R.id.search_funcionarios);

        dialog_cadastrar_func = new Dialog(requireContext());
        dialog_cadastrar_func.setContentView(R.layout.dialog_cadastrar_func);
        dialog_cadastrar_func.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_cadastrar_func.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        db = FirebaseFirestore.getInstance();

        imageFuncionarioGerenciar = view.findViewById(R.id.imageFuncionarioGerenciar);
        Button bt_cadastrar_func = dialog_cadastrar_func.findViewById(R.id.bt_cadastrar_func_dialog);

        loadProfileImage();

        setupRecyclerView();
        fetchFuncionariosFromApi();
        setupSearchListener();

        adicionarFuncionario.setOnClickListener(v -> {
            dialog_cadastrar_func.show();
        });

        bt_cadastrar_func.setOnClickListener(v -> {

        });

        if (btVoltar != null) {
            btVoltar.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        }

        return view;
    }


    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GerenciarFuncionariosAdapter(new ArrayList<>(listaOriginalFuncionarios), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchListener() {
        searchFuncionarios.addTextChangedListener(new TextWatcher() {
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
        String query = text.toLowerCase(Locale.getDefault()).trim();
        if (query.isEmpty()) {
            adapter.updateList(new ArrayList<>(listaOriginalFuncionarios));
            return;
        }

        List<FuncionarioAdapterModel> listaFiltrada = new ArrayList<>();

        for (FuncionarioAdapterModel funcionario : listaOriginalFuncionarios) {
            if (funcionario.getNome().toLowerCase(Locale.getDefault()).contains(query) ||
                    funcionario.getCargo().toLowerCase(Locale.getDefault()).contains(query) ||
                    (funcionario.getEmail() != null && funcionario.getEmail().toLowerCase(Locale.getDefault()).contains(query))) {
                listaFiltrada.add(funcionario);
            }
        }

        adapter.updateList(listaFiltrada);
    }

    @Override
    public void onItemClick(FuncionarioAdapterModel funcionario) {
        Log.d("FuncionarioClick", "Funcionário clicado: " + funcionario.getNome() + ", ID: " + funcionario.getIdentificador() + ", Cargo: " + funcionario.getCargo());

        Bundle bundle = new Bundle();
        bundle.putString(ARG_NOME, funcionario.getNome());
        bundle.putString(ARG_EMAIL, funcionario.getEmail());
        bundle.putString(ARG_CARGO, funcionario.getCargo());

        if (funcionario.getUrlImagem() != null) {
            bundle.putString(ARG_URL_IMAGEM, funcionario.getUrlImagem());
        }

        try {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_gerenciarFuncionario_to_navigation_perfilFuncionario, bundle);
        } catch (IllegalArgumentException e) {
            Log.e("Navigation", "Erro ao navegar. Verifique se a action R.id.action_gerenciarFuncionario_to_perfilFuncionario existe no seu nav_graph.xml: " + e.getMessage());
        }
    }

    private void fetchFuncionariosFromApi() {
        GestorApi gestorApi = SqlRetrofitClient.getClient(getContext()).create(GestorApi.class);
        LiderApi liderApi = SqlRetrofitClient.getClient(getContext()).create(LiderApi.class);

        gestorApi.selecionarAllGestores().enqueue(new Callback<List<GestorModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<GestorModel>> call, @NonNull Response<List<GestorModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaGestores = response.body();
                    Log.i("API", "Gestores carregados: " + listaGestores.size());
                } else {
                    Log.e("API", "Erro ao carregar gestores: " + response.code());
                    listaGestores = new ArrayList<>();
                }
                checkApiCallsCompleted();
            }

            @Override
            public void onFailure(@NonNull Call<List<GestorModel>> call, @NonNull Throwable t) {
                Log.e("API", "Falha na requisição de Gestores: " + t.getMessage());
                listaGestores = new ArrayList<>();
                checkApiCallsCompleted();
            }
        });

        liderApi.selecionarAllLideres().enqueue(new Callback<List<LiderModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<LiderModel>> call, @NonNull Response<List<LiderModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaLideres = response.body();
                    Log.i("API", "Líderes carregados: " + listaLideres.size());
                } else {
                    Log.e("API", "Erro ao carregar líderes: " + response.code());
                    listaLideres = new ArrayList<>();
                }
                checkApiCallsCompleted();
            }

            @Override
            public void onFailure(@NonNull Call<List<LiderModel>> call, @NonNull Throwable t) {
                Log.e("API", "Falha na requisição de Líderes: " + t.getMessage());
                listaLideres = new ArrayList<>();
                checkApiCallsCompleted();
            }
        });
    }

    private synchronized void checkApiCallsCompleted() {
        apiCallsCompleted++;
        if (apiCallsCompleted == totalApiCalls) {
            unifyAndSortLists();
        }
    }

    private void unifyAndSortLists() {
        listaOriginalFuncionarios.clear();

        Integer unidadeIdParaFiltrar = getUnidadeUsuarioIdInt();

        if (unidadeIdParaFiltrar == null) {
            Log.e("UNIFY", "ID da Unidade para filtro é nulo. Nenhum funcionário será exibido.");
            adapter.notifyDataSetChanged();
            return;
        }


        for (GestorModel gestor : listaGestores) {
            if (gestor.getIdUnidade() != null && gestor.getIdUnidade().equals(unidadeIdParaFiltrar)) {
                FuncionarioAdapterModel model = new FuncionarioAdapterModel(
                        String.valueOf(gestor.getId()),
                        gestor.getNome(),
                        "Gestor",
                        gestor.getEmail(),
                        null
                );
                listaOriginalFuncionarios.add(model);
            }
        }

        for (LiderModel lider : listaLideres) {
            if (lider.getIdUnidade() != null && lider.getIdUnidade().equals(unidadeIdParaFiltrar)) {
                FuncionarioAdapterModel model = new FuncionarioAdapterModel(
                        String.valueOf(lider.getId()),
                        lider.getNome(),
                        "Líder",
                        lider.getEmail(),
                        null
                );
                listaOriginalFuncionarios.add(model);
            }
        }

        Collections.sort(listaOriginalFuncionarios, (f1, f2) -> {
            if (!f1.getCargo().equals(f2.getCargo())) {
                return f1.getCargo().equals("Gestor") ? -1 : 1;
            }
            return f1.getNome().compareToIgnoreCase(f2.getNome());
        });

        Log.i("RV", "Lista unificada. Total de funcionários: " + listaOriginalFuncionarios.size());

        if (adapter != null) {
            adapter.updateList(new ArrayList<>(listaOriginalFuncionarios));
        }

        fetchImageUrlsForFuncionarios(listaOriginalFuncionarios);
    }

    private void fetchImageUrlsForFuncionarios(List<FuncionarioAdapterModel> funcionarios) {
        imagesLoadedCount = 0;
        final int totalFuncionarios = funcionarios.size();

        if (totalFuncionarios == 0) {
            adapter.notifyDataSetChanged();
            return;
        }

        for (FuncionarioAdapterModel funcionario : funcionarios) {
            db.collection("usuarios")
                    .whereEqualTo(FieldPath.of("email/cnpj"), funcionario.getEmail())
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        synchronized (GerenciarFuncionario.this) {
                            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {

                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                String imageUrl = document.getString("imagem");

                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    funcionario.setUrlImagem(imageUrl);
                                }
                            } else {
                                Log.d("FirebaseImages", "Imagem não encontrada para o email: " + funcionario.getEmail());
                            }

                            imagesLoadedCount++;
                            if (imagesLoadedCount == totalFuncionarios) {
                                Log.i("FirebaseImages", "Todas as URLs de imagem foram processadas. Atualizando RV.");
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }

    private Integer getUnidadeUsuarioIdInt() {
        return 1;
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
                                    .into(imageFuncionarioGerenciar);

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

    private boolean isUsuarioUnidade() {
        if (getContext() == null) return false;

        SharedPreferences sharedPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String cargo = sharedPrefs.getString(KEY_TIPO_USUARIO, "");

        return "Unidade".equalsIgnoreCase(cargo);
    }
}