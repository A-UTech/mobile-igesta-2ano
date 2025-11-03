package com.example.igestamobile.adapter.Turno;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.igestamobile.R;
import com.example.igestamobile.data.model.Turno.TurnoResponse;
import com.example.igestamobile.data.model.Registro.RegistroModel;
import java.util.List;

public class TurnosAdapter extends RecyclerView.Adapter<TurnosAdapter.TurnoViewHolder> {

    private final List<TurnoResponse> listaTurnos;
    private final List<RegistroModel> todosRegistrosDoDia;
    private final Context context;
    private final OnTurnoClickListener listener;

    public TurnosAdapter(List<TurnoResponse> listaTurnos, List<RegistroModel> todosRegistrosDoDia, Context context, OnTurnoClickListener listener) {
        this.listaTurnos = listaTurnos;
        this.todosRegistrosDoDia = todosRegistrosDoDia;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TurnoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_turnos, parent, false);
        return new TurnoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TurnoViewHolder holder, int position) {
        TurnoResponse turno = listaTurnos.get(position);
        holder.textNomeTurno.setText(turno.getNome());
        holder.textHorario.setText(String.format("%s - %s", turno.getInicio(), turno.getFim()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTurnoClick(turno, todosRegistrosDoDia);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTurnos.size();
    }

    public void updateList(List<TurnoResponse> novaListaTurnos, List<RegistroModel> novosRegistrosDoDia) {
        this.listaTurnos.clear();
        this.listaTurnos.addAll(novaListaTurnos);

        this.todosRegistrosDoDia.clear();
        this.todosRegistrosDoDia.addAll(novosRegistrosDoDia);

        notifyDataSetChanged();
    }

    public static class TurnoViewHolder extends RecyclerView.ViewHolder {

        public final TextView textNomeTurno;
        public final TextView textHorario;

        public TurnoViewHolder(@NonNull View itemView) {
            super(itemView);
            textNomeTurno = itemView.findViewById(R.id.txt_nome_turno);
            textHorario = itemView.findViewById(R.id.txt_horario_turno);
        }
    }
}