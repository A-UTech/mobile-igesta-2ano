package com.example.igestamobile.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igestamobile.R;
import com.example.igestamobile.data.api.CondenaApi;
import com.example.igestamobile.data.model.CondenaModel;
import com.example.igestamobile.data.model.CondenaUnidadeResponse;
import com.example.igestamobile.data.model.RegistroCondenaModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CondenaUnidadeAdapter extends RecyclerView.Adapter<CondenaUnidadeAdapter.ViewHolder> {
    private CondenaApi condenaApi;
    private List<CondenaUnidadeResponse> condenasUnidadesOriginal;
    private List<CondenaUnidadeResponse> condenasUnidadesExibida;
    public static final int MODO_CONTAGEM = 0;
    public static final int MODO_EDICAO = 1;
    private int modoAtual = MODO_CONTAGEM;
    private Context context;
    private List<Integer> condenasExistentesIds = new ArrayList<>();

    private List<Integer> condenasParaAdicionarIds = new ArrayList<>();
    private List<Integer> condenasParaRemoverIds = new ArrayList<>();
    private OnCondenaSelectionListener selectionListener;

    public interface OnCondenaSelectionListener {
        void onCondenaSelectionChanged(boolean hasPendingChanges);
    }

    public CondenaUnidadeAdapter(List<CondenaUnidadeResponse> condenasUnidades, Context context, CondenaApi condenaApi, OnCondenaSelectionListener selectionListener) {
        this.condenasUnidadesOriginal = condenasUnidades;
        this.condenasUnidadesExibida = new ArrayList<>(condenasUnidades);
        this.context = context;
        this.condenaApi = condenaApi;
        this.selectionListener = selectionListener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CondenaUnidadeResponse condenaUnidade = condenasUnidadesExibida.get(position);
        Integer condenaId = condenaUnidade.getIdCondena() != null ?
                condenaUnidade.getIdCondena().intValue() : -1;

        if (modoAtual == MODO_EDICAO) {

            if (holder.nome_condena_editar != null) {
                holder.nome_condena_editar.setText(condenaUnidade.getNome() != null ? condenaUnidade.getNome() : "Carregando...");
            }
            if (holder.imageViewAcao != null) holder.imageViewAcao.setOnClickListener(null);

            if (holder.nome_condena != null) holder.nome_condena.setText("");
            if (holder.btn_mais != null) holder.btn_mais.setOnClickListener(null);
            if (holder.btn_menos != null) holder.btn_menos.setOnClickListener(null);
            if (holder.quantidadeEditText != null && holder.quantidadeEditText.getTag() instanceof TextWatcher) {
                holder.quantidadeEditText.removeTextChangedListener((TextWatcher) holder.quantidadeEditText.getTag());
            }

            if (condenaUnidade.getNome() == null) {
                condenaApi.selecionarCondenaPorId(condenaUnidade.getIdCondena()).enqueue(new Callback<CondenaModel>() {
                    @Override
                    public void onResponse(Call<CondenaModel> call, Response<CondenaModel> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            CondenaModel model = response.body();
                            if (holder.nome_condena_editar != null) {
                                holder.nome_condena_editar.setText(model.getNome());
                            }
                            condenaUnidade.setNome(model.getNome());
                            condenaUnidade.setTipo(model.getTipo());
                        } else if (holder.nome_condena_editar != null) {
                            holder.nome_condena_editar.setText("Erro");
                        }
                    }

                    @Override
                    public void onFailure(Call<CondenaModel> call, Throwable t) {
                        if (holder.nome_condena_editar != null) {
                            holder.nome_condena_editar.setText("Falha");
                        }
                        Toast.makeText(context, "Não foi possível carregar as condenas", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            boolean isInicialmenteExistente = condenasExistentesIds.contains(condenaId);
            boolean isPendenteParaAdicionar = condenasParaAdicionarIds.contains(condenaId);
            boolean isPendenteParaRemover = condenasParaRemoverIds.contains(condenaId);

            boolean isAtualmenteSelecionada;

            if (isInicialmenteExistente) {
                isAtualmenteSelecionada = !isPendenteParaRemover;
            } else {
                isAtualmenteSelecionada = isPendenteParaAdicionar;
            }

            if (holder.imageViewAcao != null) {
                if (isAtualmenteSelecionada) {
                    holder.imageViewAcao.setImageResource(R.drawable.full_btn_editar_condena);
                } else {
                    holder.imageViewAcao.setImageResource(R.drawable.borda_btn_editar_condenas);
                }

                holder.imageViewAcao.setOnClickListener(v -> {
                    if (isAtualmenteSelecionada) {
                        handleDeselection(condenaId, isInicialmenteExistente);
                    } else {
                        handleSelection(condenaId, isInicialmenteExistente);
                    }
                    notifyItemChanged(position);

                    if (selectionListener != null) {
                        selectionListener.onCondenaSelectionChanged(hasPendingChanges());
                    }
                });
            }


        } else {

            if (holder.nome_condena != null) {
                holder.nome_condena.setText("Carregando...");
            }
            if (holder.imageViewAcao != null) holder.imageViewAcao.setOnClickListener(null);

            if (holder.quantidadeEditText != null) {
                holder.quantidadeEditText.setText(String.valueOf(condenaUnidade.getQuantidade()));

                if (holder.quantidadeEditText.getTag() instanceof TextWatcher) {
                    holder.quantidadeEditText.removeTextChangedListener((TextWatcher) holder.quantidadeEditText.getTag());
                }
            }


            if (condenaUnidade.getNome() == null) {
                condenaApi.selecionarCondenaPorId(condenaUnidade.getIdCondena()).enqueue(new Callback<CondenaModel>() {
                    @Override
                    public void onResponse(Call<CondenaModel> call, Response<CondenaModel> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            CondenaModel model = response.body();
                            if (holder.nome_condena != null) {
                                holder.nome_condena.setText(model.getNome());
                            }
                            condenaUnidade.setNome(model.getNome());
                            condenaUnidade.setTipo(model.getTipo());
                        } else if (holder.nome_condena != null) {
                            holder.nome_condena.setText("Erro");
                        }
                    }

                    @Override
                    public void onFailure(Call<CondenaModel> call, Throwable t) {
                        if (holder.nome_condena != null) {
                            holder.nome_condena.setText("Falha");
                        }
                        Toast.makeText(context, "Não foi possível carregar as condenas", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (holder.nome_condena != null) {
                    holder.nome_condena.setText(condenaUnidade.getNome());
                }
            }

            if (holder.btn_mais != null) {
                holder.btn_mais.setOnClickListener(v -> {
                    int novaQuantidade = condenaUnidade.getQuantidade() + 1;
                    condenaUnidade.setQuantidade(novaQuantidade);
                    holder.quantidadeEditText.setText(String.valueOf(novaQuantidade));
                });
            }

            if (holder.btn_menos != null) {
                holder.btn_menos.setOnClickListener(v -> {
                    int quantidadeAtual = condenaUnidade.getQuantidade();
                    int novaQuantidade = quantidadeAtual - 1;

                    if (novaQuantidade < 0) {
                        novaQuantidade = 0;
                    }

                    condenaUnidade.setQuantidade(novaQuantidade);
                    holder.quantidadeEditText.setText(String.valueOf(novaQuantidade));
                });
            }


            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String texto = s.toString();
                    int novaQuantidade = 0;

                    if (!texto.isEmpty()) {
                        try {
                            novaQuantidade = Integer.parseInt(texto);

                            if (novaQuantidade < 0) {
                                novaQuantidade = 0;
                            }
                        } catch (NumberFormatException e) {

                        }
                    }

                    condenaUnidade.setQuantidade(novaQuantidade);
                }
            };

            if (holder.quantidadeEditText != null) {
                holder.quantidadeEditText.addTextChangedListener(textWatcher);
                holder.quantidadeEditText.setTag(textWatcher);
                holder.quantidadeEditText.setText(String.valueOf(condenaUnidade.getQuantidade()));
            }
        }
    }

    private void handleSelection(Integer condenaId, boolean isInicialmenteExistente) {
        if (isInicialmenteExistente) {
            condenasParaRemoverIds.remove(condenaId);
        } else {
            if (!condenasParaAdicionarIds.contains(condenaId)) {
                condenasParaAdicionarIds.add(condenaId);
            }
        }
    }

    private void handleDeselection(Integer condenaId, boolean isInicialmenteExistente) {
        if (isInicialmenteExistente) {
            if (!condenasParaRemoverIds.contains(condenaId)) {
                condenasParaRemoverIds.add(condenaId);
            }
        } else {
            condenasParaAdicionarIds.remove(condenaId);
        }
    }

    private boolean hasPendingChanges() {
        return !condenasParaAdicionarIds.isEmpty() || !condenasParaRemoverIds.isEmpty();
    }

    public List<Integer> getCondenasParaAdicionarIds() {
        return condenasParaAdicionarIds;
    }

    public List<Integer> getCondenasParaRemoverIds() {
        return condenasParaRemoverIds;
    }

    public void resetPendencias() {
        condenasParaAdicionarIds.clear();
        condenasParaRemoverIds.clear();
        if (selectionListener != null) {
            selectionListener.onCondenaSelectionChanged(false);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == MODO_EDICAO) {
            view = LayoutInflater.from(context).inflate(R.layout.item_editar_condenas, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_contagem_condenas, parent, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {

        return condenasUnidadesExibida.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nome_condena;
        public Button btn_menos;
        public Button btn_mais;
        public TextInputEditText quantidadeEditText;
        public TextView nome_condena_editar;
        public ImageView imageViewAcao;

        public ViewHolder(View view) {
            super(view);

            nome_condena = view.findViewById(R.id.contar_nome_condena);
            btn_menos = view.findViewById(R.id.btn_menos);
            btn_mais = view.findViewById(R.id.btn_mais);
            quantidadeEditText = view.findViewById(R.id.input_quantidade);

            nome_condena_editar = view.findViewById(R.id.editar_nome_condena);
            imageViewAcao = view.findViewById(R.id.btn_acao_edicao);
        }
    }

    public void setLista(List<CondenaUnidadeResponse> novaLista) {
        this.condenasUnidadesOriginal = novaLista;

        this.condenasUnidadesExibida = new ArrayList<>(novaLista);
        notifyDataSetChanged();
    }

    public void setCondenasExistentesIds(List<CondenaUnidadeResponse> existentes) {
        this.condenasExistentesIds = existentes.stream()
                .map(item -> item.getIdCondena().intValue())
                .collect(Collectors.toList());
        resetPendencias();
    }

    public void aplicarFiltroVisual(String tipoFiltro) {
        if (tipoFiltro == null) {

            condenasUnidadesExibida = new ArrayList<>(condenasUnidadesOriginal);
        } else {

            String filtroLowerCase = tipoFiltro.toLowerCase(Locale.ROOT);

            List<CondenaUnidadeResponse> listaFiltrada = condenasUnidadesOriginal.stream()
                    .filter(item ->
                            item.getTipo() != null && item.getTipo().toLowerCase(Locale.ROOT).equals(filtroLowerCase)
                    )
                    .collect(Collectors.toList());

            condenasUnidadesExibida = listaFiltrada;
        }

        notifyDataSetChanged();
    }

    public List<CondenaUnidadeResponse> getContagensFinais() {
        List<CondenaUnidadeResponse> contagensFinais = new ArrayList<>();

        for (CondenaUnidadeResponse item : condenasUnidadesOriginal) {
            if (item.getQuantidade() > 0) {
                contagensFinais.add(item);
            }
        }
        return contagensFinais;
    }

    public List<RegistroCondenaModel> getContagensFinaisMongo() {
        List<RegistroCondenaModel> contagensFinais = new ArrayList<>();

        for (CondenaUnidadeResponse item : condenasUnidadesOriginal) {
            if (item.getQuantidade() > 0) {

                RegistroCondenaModel registro = new RegistroCondenaModel(
                        item.getIdCondena(),
                        item.getQuantidade(),
                        item.getNome(),
                        item.getTipo()
                );
                contagensFinais.add(registro);
            }
        }
        return contagensFinais;
    }
    public void setModo(int novoModo) {
        if (this.modoAtual != novoModo) {
            this.modoAtual = novoModo;
        }
    }
    @Override
    public int getItemViewType(int position) {
        return modoAtual;
    }
}