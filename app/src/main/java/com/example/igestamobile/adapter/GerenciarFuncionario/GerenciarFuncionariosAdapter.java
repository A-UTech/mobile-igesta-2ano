package com.example.igestamobile.adapter.GerenciarFuncionario;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.igestamobile.R;
import com.example.igestamobile.data.model.Funcionario.FuncionarioAdapterModel;

import java.util.List;
import java.util.Locale;

public class GerenciarFuncionariosAdapter extends RecyclerView.Adapter<GerenciarFuncionariosAdapter.FuncionarioViewHolder> {

    private final List<FuncionarioAdapterModel> listaFuncionarios;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FuncionarioAdapterModel funcionario);
    }

    public GerenciarFuncionariosAdapter(List<FuncionarioAdapterModel> listaFuncionarios, OnItemClickListener listener) {
        this.listaFuncionarios = listaFuncionarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FuncionarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_funcionario, parent, false);
        return new FuncionarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FuncionarioViewHolder holder, int position) {
        FuncionarioAdapterModel funcionario = listaFuncionarios.get(position);
        holder.bind(funcionario, listener);
    }

    @Override
    public int getItemCount() {
        return listaFuncionarios.size();
    }

    public static class FuncionarioViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNome;
        private final TextView tvCargo;
        private final ImageView ivFoto;

        public FuncionarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.txt_nome_funcionario);
            tvCargo = itemView.findViewById(R.id.txt_cargo_funcionario);
            ivFoto = itemView.findViewById(R.id.img_funcionario);
        }

        public void bind(final FuncionarioAdapterModel funcionario, final OnItemClickListener listener) {
            tvNome.setText(funcionario.getNome());

            String cargoDisplay = funcionario.getCargo().toUpperCase(Locale.getDefault());
            tvCargo.setText(cargoDisplay);

            ivFoto.setImageResource(R.mipmap.fotoperfil);

            if (funcionario.getUrlImagem() != null && !funcionario.getUrlImagem().isEmpty()) {
                Context context = itemView.getContext();
                Glide.with(context)
                        .load(funcionario.getUrlImagem())
                        .override(175, 175)
                        .centerCrop()
                        .placeholder(R.mipmap.fotoperfil)
                        .error(R.mipmap.fotoperfil)
                        .into(ivFoto);
            } else {
                ivFoto.setImageResource(R.mipmap.fotoperfil);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(funcionario));
        }
    }

    public void updateList(List<FuncionarioAdapterModel> newList) {
        listaFuncionarios.clear();
        listaFuncionarios.addAll(newList);
        notifyDataSetChanged();
    }
}