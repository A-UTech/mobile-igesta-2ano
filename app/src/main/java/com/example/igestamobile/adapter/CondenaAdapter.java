package com.example.igestamobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igestamobile.R;
import com.example.igestamobile.data.api.CondenaApi;
import com.example.igestamobile.data.model.CondenaModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CondenaAdapter extends RecyclerView.Adapter<CondenaAdapter.ViewHolder> {
    private List<CondenaModel> condenas = new ArrayList<>();
    private Context context;

    public CondenaAdapter(Context context) {
        this.context = context;
    }

    public void setCondenas(List<CondenaModel> condenas) {
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
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nome;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.condena_txt);
        }
    }
}

