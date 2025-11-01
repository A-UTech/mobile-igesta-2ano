package com.example.igestamobile;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.igestamobile.adapter.CondenaTurnoAdapter;
import com.example.igestamobile.data.model.RegistroCondenaModel;
import com.example.igestamobile.data.model.CondenaDetalhe;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Turnos extends Fragment {

    private static final String ARG_NOME_TURNO = "nomeTurno";
    private static final String ARG_CONDENAS = "condenas";

    private String nomeTurno;
    private ArrayList<RegistroCondenaModel> condenasBrutas;
    private RecyclerView recyclerViewCondenas;
    private CondenaTurnoAdapter adapter;

    public Turnos() {
    }

    public static Turnos newInstance(String nomeTurno, ArrayList<RegistroCondenaModel> condenas) {
        Turnos fragment = new Turnos();
        Bundle args = new Bundle();
        args.putString(ARG_NOME_TURNO, nomeTurno);
        args.putParcelableArrayList(ARG_CONDENAS, condenas);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nomeTurno = getArguments().getString(ARG_NOME_TURNO);
            condenasBrutas = getArguments().getParcelableArrayList(ARG_CONDENAS);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_turnos, container, false);

        TextView textTitulo = view.findViewById(R.id.txt_titulo_turno);
        if (nomeTurno != null) {
            textTitulo.setText(String.format("Turno %s", nomeTurno));
        }

        recyclerViewCondenas = view.findViewById(R.id.rv_planilha);
        recyclerViewCondenas.setLayoutManager(new LinearLayoutManager(getContext()));

        List<CondenaDetalhe> condenasCalculadas = calcularDetalhesCondenas(condenasBrutas);

        adapter = new CondenaTurnoAdapter(condenasCalculadas);
        recyclerViewCondenas.setAdapter(adapter);

        View btVoltar = view.findViewById(R.id.bt_voltar_turnos);
        if (btVoltar != null) {
            btVoltar.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        }

        return view;
    }

    private List<CondenaDetalhe> calcularDetalhesCondenas(List<RegistroCondenaModel> condenas) {
        if (condenas == null || condenas.isEmpty()) return new ArrayList<>();

        int totalCondenas = condenas.stream()
                .mapToInt(RegistroCondenaModel::getQuantidade)
                .sum();

        List<CondenaDetalhe> detalhes = new ArrayList<>();

        for (RegistroCondenaModel condena : condenas) {
            double porcentagem = (totalCondenas > 0) ?
                    ((double) condena.getQuantidade() / totalCondenas) * 100 : 0;

            detalhes.add(new CondenaDetalhe(
                    condena.getNome(),
                    condena.getTipo(),
                    condena.getQuantidade(),
                    porcentagem
            ));
        }

        return detalhes;
    }
}