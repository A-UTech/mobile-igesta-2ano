package com.example.igestamobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.igestamobile.R;
import com.example.igestamobile.data.model.CondenaDetalhe;
import java.util.List;
import java.util.Locale;

public class CondenaTurnoAdapter extends RecyclerView.Adapter<CondenaTurnoAdapter.CondenaViewHolder> {

    private final List<CondenaDetalhe> listaCondenas;

    public CondenaTurnoAdapter(List<CondenaDetalhe> listaCondenas) {
        this.listaCondenas = listaCondenas;
    }

    @NonNull
    @Override
    public CondenaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_planilha, parent, false);
        return new CondenaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CondenaViewHolder holder, int position) {
        CondenaDetalhe condena = listaCondenas.get(position);
        holder.textNome.setText(condena.getNome());
        holder.textTipo.setText(String.format("(%s)", condena.getTipo()));
        holder.textQuantidade.setText(String.valueOf(condena.getQuantidade()));
        holder.textPorcentagem.setText(String.format(Locale.getDefault(), "%.2f%%", condena.getPorcentagem()));
    }

    @Override
    public int getItemCount() {
        return listaCondenas.size();
    }

    public static class CondenaViewHolder extends RecyclerView.ViewHolder {
        public final TextView textNome;
        public final TextView textTipo;
        public final TextView textQuantidade;
        public final TextView textPorcentagem;

        public CondenaViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.txt_nome_condena);
            textTipo = itemView.findViewById(R.id.txt_tipo_condena);
            textQuantidade = itemView.findViewById(R.id.txt_qnt_condena);
            textPorcentagem = itemView.findViewById(R.id.txt_porcentagem_condena);
        }
    }
}