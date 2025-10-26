package com.example.igestamobile.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igestamobile.R;
import com.example.igestamobile.data.api.CondenaApi;
import com.example.igestamobile.data.model.CondenaModel;
import com.example.igestamobile.data.model.CondenaUnidadeResponse;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CondenaUnidadeAdapter extends RecyclerView.Adapter<CondenaUnidadeAdapter.ViewHolder> {
    private CondenaApi condenaApi;
    private List<CondenaUnidadeResponse> condenasUnidades;
    private Context context;

    public CondenaUnidadeAdapter(List<CondenaUnidadeResponse> condenasUnidades, Context context, CondenaApi condenaApi) {
        this.condenasUnidades = condenasUnidades;
        this.context = context;
        this.condenaApi = condenaApi;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.nome_condena.setText("Carregando...");

        CondenaUnidadeResponse condenaUnidade = condenasUnidades.get(position);

        if (holder.quantidadeEditText.getTag() instanceof TextWatcher) {
            holder.quantidadeEditText.removeTextChangedListener((TextWatcher) holder.quantidadeEditText.getTag());
        }

        condenaApi.selecionarCondenaPorId(condenaUnidade.getIdCondena()).enqueue(new Callback<CondenaModel>() {
            @Override
            public void onResponse(Call<CondenaModel> call, Response<CondenaModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    holder.nome_condena.setText(response.body().getNome());
                    condenaUnidade.setNome(response.body().getNome());
                } else {
                    holder.nome_condena.setText("Erro");
                }
            }

            @Override
            public void onFailure(Call<CondenaModel> call, Throwable t) {
                holder.nome_condena.setText("Falha");
                Toast.makeText(context, "Não foi possível carregar as condenas", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btn_mais.setOnClickListener(v -> {
            int quantidadeAtual = condenaUnidade.getQuantidade();
            int novaQuantidade = quantidadeAtual + 1;

            condenaUnidade.setQuantidade(novaQuantidade);
            holder.quantidadeEditText.setText(String.valueOf(novaQuantidade));

        });

        holder.btn_menos.setOnClickListener(v -> {
            int quantidadeAtual = condenaUnidade.getQuantidade();
            int novaQuantidade = quantidadeAtual - 1;

            if (novaQuantidade < 0) {
                novaQuantidade = 0;
            }

            condenaUnidade.setQuantidade(novaQuantidade);
            holder.quantidadeEditText.setText(String.valueOf(novaQuantidade));
        });

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

        holder.quantidadeEditText.addTextChangedListener(textWatcher);
        holder.quantidadeEditText.setTag(textWatcher);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contagem_condenas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return condenasUnidades.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nome_condena;
        public Button btn_menos;
        public Button btn_mais;
        public TextInputEditText quantidadeEditText;

        public ViewHolder(View view) {
            super(view);
            nome_condena = view.findViewById(R.id.nome_condena);
            btn_menos = view.findViewById(R.id.btn_menos);
            btn_mais = view.findViewById(R.id.btn_mais);
            quantidadeEditText = view.findViewById(R.id.input_quantidade);
        }
    }

    public void setLista(List<CondenaUnidadeResponse> novaLista) {
        this.condenasUnidades = novaLista;
        notifyDataSetChanged();
    }

    public List<CondenaUnidadeResponse> getCondenasUnidades() {
        return condenasUnidades;
    }

}