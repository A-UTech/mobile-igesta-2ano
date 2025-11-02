package com.example.igestamobile;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.igestamobile.R;
import com.example.igestamobile.data.api.MongoRetrofitClient;
import com.example.igestamobile.data.api.RegistroApi;
import com.example.igestamobile.data.api.SqlRetrofitClient;
import com.example.igestamobile.data.api.UnidadeApi;
import com.example.igestamobile.data.model.CondenaTopModel;
import com.example.igestamobile.data.model.UnidadeModel;
import com.example.igestamobile.databinding.FragmentHomeBinding;
import com.example.igestamobile.utils.MaskUtil;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TelaInicial extends Fragment {

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USUARIO_CREDENCIAL = "USUARIO_CREDENCIAL";
    private static final String KEY_USUARIO_NOME = "USUARIO_NOME";
    private static final String KEY_UNIDADE_ID = "UNIDADE_ID";
    private FragmentHomeBinding binding;
    private ShapeableImageView imageFuncionarioHome;
    private TextView txtOlaTelaInicial, txtQuantidadeCondenaTotal, txtQuantidadeCondenaParcial, txtTotalCondenas, txtComparacaoMesPassado, txtQntdTotal_1, txtQntdTotal_2, txtQntdTotal_3, txtQntdParcial_1, txtQntdParcial_2, txtQntdParcial_3, txtQntdTotal_1_nome, txtQntdTotal_2_nome, txtQntdTotal_3_nome, txtQntdParcial_1_nome, txtQntdParcial_2_nome, txtQntdParcial_3_nome;
    private FirebaseFirestore db;
    private RegistroApi registroApi;
    private String unidade;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tela_inicial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        registroApi = MongoRetrofitClient.getClient().create(RegistroApi.class);

        imageFuncionarioHome = view.findViewById(R.id.imageFuncionarioHome);
        txtOlaTelaInicial = view.findViewById(R.id.txt_ola_tela_inicial);
        txtQuantidadeCondenaTotal = view.findViewById(R.id.txt_quantidade_condena_total);
        txtQuantidadeCondenaParcial = view.findViewById(R.id.txt_quantidade_condena_parcial);
        txtTotalCondenas = view.findViewById(R.id.txt_total_condenas);
        txtComparacaoMesPassado = view.findViewById(R.id.txt_comparacao_mes_passado);
        txtQntdTotal_1 = view.findViewById(R.id.txt_qntd_1);
        txtQntdTotal_2 = view.findViewById(R.id.txt_qntd_2);
        txtQntdTotal_3 = view.findViewById(R.id.txt_qntd_3);
        txtQntdParcial_1 = view.findViewById(R.id.txt_qntd_1_parcial);
        txtQntdParcial_2 = view.findViewById(R.id.txt_qntd_2_parcial);
        txtQntdParcial_3 = view.findViewById(R.id.txt_qntd_3_parcial);
        txtQntdTotal_1_nome = view.findViewById(R.id.txt_qntd_1_nome);
        txtQntdTotal_2_nome = view.findViewById(R.id.txt_qntd_2_nome);
        txtQntdTotal_3_nome = view.findViewById(R.id.txt_qntd_3_nome);
        txtQntdParcial_1_nome = view.findViewById(R.id.txt_qntd_1_parcial_nome);
        txtQntdParcial_2_nome = view.findViewById(R.id.txt_qntd_2_parcial_nome);
        txtQntdParcial_3_nome = view.findViewById(R.id.txt_qntd_3_parcial_nome);

        String nome = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USUARIO_NOME, null);
        txtOlaTelaInicial.setText("Olá, " + nome + "!");

        Integer unidadeId = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_UNIDADE_ID, -1);
        UnidadeApi unidadeApi = SqlRetrofitClient.getClient(getContext()).create(UnidadeApi.class);
        unidadeApi.selecionarUnidadePorId(unidadeId).enqueue(new Callback<UnidadeModel>() {
            @Override
            public void onResponse(Call<UnidadeModel> call, Response<UnidadeModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UnidadeModel unidadeModel = response.body();
                    unidade = unidadeModel.getNome();
                    if (unidade != null) {
                        loadApiData();
                    } else {
                        Toast.makeText(getContext(), "Não foi possível obter a unidade do usuário.", Toast.LENGTH_LONG).show();
                        Log.e("API_LOAD", "Unidade do usuário é nula.");
                    }
                } else {
                    Log.e("API_UNIDADE", "Erro na resposta Unidade: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UnidadeModel> call, Throwable t) {
                Log.e("API_UNIDADE", "Erro na requisição Unidade: " + t.getMessage());
            }
        });

        loadProfileImage();
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
                                    .into(imageFuncionarioHome);

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

    private void loadApiData() {
        if (unidade == null || registroApi == null) return;

        registroApi.buscarTotalCondenasRegistradas(unidade).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    txtTotalCondenas.setText(String.valueOf(response.body()));
                } else {
                    Log.e("API_TOTAL", "Erro na resposta Total Condenas: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e("API_TOTAL", "Falha na chamada Total Condenas: " + t.getMessage());
            }
        });

        registroApi.buscarTipoTotalPorUnidade(unidade).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    txtQuantidadeCondenaTotal.setText(String.valueOf(response.body()));
                } else {
                    Log.e("API_TIPO_TOTAL", "Erro na resposta Tipo Total: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e("API_TIPO_TOTAL", "Falha na chamada Tipo Total: " + t.getMessage());
            }
        });

        registroApi.buscarTipoParcialPorUnidade(unidade).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    txtQuantidadeCondenaParcial.setText(String.valueOf(response.body()));
                } else {
                    Log.e("API_TIPO_PARCIAL", "Erro na resposta Tipo Parcial: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e("API_TIPO_PARCIAL", "Falha na chamada Tipo Parcial: " + t.getMessage());
            }
        });

        registroApi.buscarComparacaoMesPassado(unidade).enqueue(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double comparacao = response.body();
                    String texto;
                    if (comparacao > 0) {
                        texto = String.format("+%.1f%%", comparacao);
                    } else {
                        texto = String.format("%.1f%%", comparacao);
                    }
                    txtComparacaoMesPassado.setText(texto);
                } else {
                    Log.e("API_COMPARACAO", "Erro na resposta Comparação: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Double> call, Throwable t) {
                Log.e("API_COMPARACAO", "Falha na chamada Comparação: " + t.getMessage());
            }
        });

        registroApi.buscarCondenasTotaisMaisRegistradas(unidade).enqueue(new Callback<List<CondenaTopModel>>() {
            @Override
            public void onResponse(Call<List<CondenaTopModel>> call, Response<List<CondenaTopModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CondenaTopModel> lista = response.body();
                    if (lista.size() > 0) {
                        txtQntdTotal_1_nome.setText(lista.get(0).getNome());
                        txtQntdTotal_1.setText(String.valueOf(lista.get(0).getTotalQuantidade()));
                    }
                    if (lista.size() > 1) {
                        txtQntdTotal_2_nome.setText(lista.get(1).getNome());
                        txtQntdTotal_2.setText(String.valueOf(lista.get(1).getTotalQuantidade()));
                    }
                    if (lista.size() > 2) {
                        txtQntdTotal_3_nome.setText(lista.get(2).getNome());
                        txtQntdTotal_3.setText(String.valueOf(lista.get(2).getTotalQuantidade()));
                    }
                } else {
                    Log.e("API_TOP_TOTAL", "Erro na resposta Condenas Totais: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<CondenaTopModel>> call, Throwable t) {
                Log.e("API_TOP_TOTAL", "Falha na chamada Condenas Totais: " + t.getMessage());
            }
        });

        registroApi.buscarCondenasParciaisMaisRegistradas(unidade).enqueue(new Callback<List<CondenaTopModel>>() {
            @Override
            public void onResponse(Call<List<CondenaTopModel>> call, Response<List<CondenaTopModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CondenaTopModel> lista = response.body();
                    if (lista.size() > 0) {
                        txtQntdParcial_1_nome.setText(lista.get(0).getNome());
                        txtQntdParcial_1.setText(String.valueOf(lista.get(0).getTotalQuantidade()));
                    }
                    if (lista.size() > 1) {
                        txtQntdParcial_2_nome.setText(lista.get(1).getNome());
                        txtQntdParcial_2.setText(String.valueOf(lista.get(1).getTotalQuantidade()));
                    }
                    if (lista.size() > 2) {
                        txtQntdParcial_3_nome.setText(lista.get(2).getNome());
                        txtQntdParcial_3.setText(String.valueOf(lista.get(2).getTotalQuantidade()));
                    }
                } else {
                    Log.e("API_TOP_PARCIAL", "Erro na resposta Condenas Parciais: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<CondenaTopModel>> call, Throwable t) {
                Log.e("API_TOP_PARCIAL", "Falha na chamada Condenas Parciais: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}