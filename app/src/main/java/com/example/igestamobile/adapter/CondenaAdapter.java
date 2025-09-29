package com.example.igestamobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igestamobile.R;
import com.example.igestamobile.data.model.CondenaModel;

import java.util.List;

public class CondenaAdapter extends RecyclerView.Adapter<CondenaAdapter.CondenaViewHolder>{
    private List<CondenaModel> condenaList;

    public CondenaAdapter(List<CondenaModel> condenaList) {
        this.condenaList = condenaList;
    }

    @NonNull
    @Override
    public CondenaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_condenas, parent, false);
        return new CondenaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CondenaViewHolder holder, int position) {
        CondenaModel condenaModel = condenaList.get(position);
        holder.name.setText(condenaModel.getNome());
    }

    @Override
    public int getItemCount() {
        return condenaList.size();
    }

    static class CondenaViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        public CondenaViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.condena_txt);
        }
    }
}
