package com.example.igestamobile.ui.turnos;

import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.igestamobile.R;
import com.example.igestamobile.adapter.Turno.CondenaTurnoAdapter;
import com.example.igestamobile.data.model.Registro.RegistroCondenaModel;
import com.example.igestamobile.data.model.Condena.CondenaDetalhe;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Turnos extends Fragment {

    private static final String ARG_NOME_TURNO = "nomeTurno";
    private static final String ARG_CONDENAS = "condenas";

    private String nomeTurno;
    private ArrayList<RegistroCondenaModel> condenasBrutas;
    private RecyclerView recyclerViewCondenas;
    private CondenaTurnoAdapter adapter;
    private List<CondenaDetalhe> condenasCalculadas;
    private Button btExportarPlanilha;
    private ActivityResultLauncher<String> createDocumentLauncher;

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

        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                uri -> {
                    if (uri != null && condenasCalculadas != null) {
                        gerarPlanilha(condenasCalculadas, uri);
                    } else if (uri == null) {
                        Toast.makeText(requireContext(), "Criação de arquivo cancelada pelo usuário.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
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

        condenasCalculadas = calcularDetalhesCondenas(condenasBrutas);

        adapter = new CondenaTurnoAdapter(condenasCalculadas);
        recyclerViewCondenas.setAdapter(adapter);

        View btVoltar = view.findViewById(R.id.bt_voltar_turnos);
        if (btVoltar != null) {
            btVoltar.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        }

        btExportarPlanilha = view.findViewById(R.id.bt_exportar_planilha);
        if (btExportarPlanilha != null) {
            btExportarPlanilha.setOnClickListener(v -> {
                if (condenasCalculadas == null || condenasCalculadas.isEmpty()) {
                    Toast.makeText(requireContext(), "Não há dados para exportar.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String nomeArquivo = String.format("contagem_turno_%s.xlsx", nomeTurno != null ? nomeTurno : "detalhe");
                createDocumentLauncher.launch(nomeArquivo);
            });
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

    private void gerarPlanilha(List<CondenaDetalhe> detalhes, Uri uri) {
        if (getContext() == null) return;

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Contagem Condenas");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Condena");
        headerRow.createCell(1).setCellValue("Tipo");
        headerRow.createCell(2).setCellValue("Quantidade");
        headerRow.createCell(3).setCellValue("Porcentagem");

        int rowIndex = 1;
        for (CondenaDetalhe c : detalhes) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(c.getNome());
            row.createCell(1).setCellValue(c.getTipo());
            row.createCell(2).setCellValue(c.getQuantidade());
            row.createCell(3).setCellValue(String.format(java.util.Locale.US, "%.2f%%", c.getPorcentagem()));
        }

        try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
            if (os != null) {
                workbook.write(os);
                workbook.close();
                Toast.makeText(requireContext(), "Planilha salva com sucesso!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), "Não foi possível abrir o fluxo de saída.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Erro ao salvar planilha: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}