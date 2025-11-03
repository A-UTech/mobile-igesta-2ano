package com.example.igestamobile.adapter.Turno;

import com.example.igestamobile.data.model.Turno.TurnoResponse;
import com.example.igestamobile.data.model.Registro.RegistroModel;
import java.util.List;

public interface OnTurnoClickListener {
    void onTurnoClick(TurnoResponse turnoClicado, List<RegistroModel> registrosDoDia);
}