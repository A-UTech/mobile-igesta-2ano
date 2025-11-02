package com.example.igestamobile.adapter;

import android.content.Context;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igestamobile.R;
import com.example.igestamobile.data.model.CondenaModel;

import java.util.ArrayList;
import java.util.List;

public class CondenaAdapter extends RecyclerView.Adapter<CondenaAdapter.ViewHolder> {
    private List<CondenaModel> condenas = new ArrayList<>();
    private Context context;

    public CondenaAdapter(Context context) {
        this.context = context;
    }

    public void setCondenas(List<CondenaModel> condenas) {
        if (condenas != null) {
            for (CondenaModel condena : condenas) {
                condena.setSelecionada(true);
            }
        }
        this.condenas = condenas != null ? condenas : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return condenas.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_condenas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CondenaModel condena = condenas.get(position);
        holder.nome.setText(condena.getNome());

        aplicarEstiloSelecao(holder, condena.isSelecionada());

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                CondenaModel item = condenas.get(currentPosition);

                item.setSelecionada(!item.isSelecionada());

                aplicarEstiloSelecao(holder, item.isSelecionada());
            }
        });
    }

    private void aplicarEstiloSelecao(ViewHolder holder, boolean isSelected) {
        if (isSelected) {
            Drawable bg = ContextCompat.getDrawable(context, R.drawable.borda_btn).mutate();
            bg.setTint(ContextCompat.getColor(context, R.color.verde_escuro));
            holder.itemView.setBackground(bg);
            holder.nome.setTextColor(ContextCompat.getColor(context, R.color.branco));
        } else {
            Drawable bg = ContextCompat.getDrawable(context, R.drawable.borda_btn).mutate();
            bg.setTint(ContextCompat.getColor(context, R.color.cinza_claro));
            holder.itemView.setBackground(bg);
            holder.nome.setTextColor(ContextCompat.getColor(context, R.color.cinza_escuro));
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public List<CondenaModel> getCondenasSelecionadas() {
        List<CondenaModel> condenasSelecionadas = new ArrayList<>();
        for (CondenaModel condena : condenas) {
            if (condena.isSelecionada()) {
                condenasSelecionadas.add(condena);
            }
        }
        return condenasSelecionadas;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nome;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.condena_txt);
        }
    }
}