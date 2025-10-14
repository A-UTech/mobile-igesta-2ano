package com.example.igestamobile.adapter;

import android.content.Context;
import androidx.core.content.ContextCompat;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
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
    private SparseArray<View> itemViews = new SparseArray<>();

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
        
        itemViews.put(position, holder.itemView);

        holder.itemView.setOnClickListener(v -> {
            boolean isSelected = !v.isSelected();
            v.setSelected(isSelected);
            
            itemViews.put(position, v);

            if (isSelected) {
                Drawable bg = ContextCompat.getDrawable(context, R.drawable.borda_btn).mutate();
                bg.setTint(ContextCompat.getColor(context, R.color.cinza_claro));

                holder.itemView.setBackground(bg);
                holder.nome.setTextColor(ContextCompat.getColor(context, R.color.cinza_escuro));
            } else {
                holder.itemView.setSelected(false);

                Drawable bg = ContextCompat.getDrawable(context, R.drawable.borda_btn).mutate();
                bg.setTint(ContextCompat.getColor(context, R.color.verde_escuro));

                holder.itemView.setBackground(bg);
                holder.nome.setTextColor(ContextCompat.getColor(context, R.color.branco));
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        itemViews.remove(holder.getAdapterPosition());
    }

    public List<CondenaModel> getCondenasSelecionadas() {
        List<CondenaModel> condenasSelecionadas = new ArrayList<>();
        for (int i = 0; i < itemViews.size(); i++) {
            int position = itemViews.keyAt(i);
            View view = itemViews.get(position);
            if (view != null && view.isSelected() && position < condenas.size()) {
                condenasSelecionadas.add(condenas.get(position));
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

