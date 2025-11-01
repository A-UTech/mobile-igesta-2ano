package com.example.igestamobile.adapter;

import com.example.igestamobile.data.model.TurnoResponse;
import com.example.igestamobile.data.model.RegistroModel;
import java.util.List;

public interface OnTurnoClickListener {
    void onTurnoClick(TurnoResponse turnoClicado, List<RegistroModel> registrosDoDia);
}