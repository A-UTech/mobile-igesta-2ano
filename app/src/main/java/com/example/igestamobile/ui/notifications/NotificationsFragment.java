package com.example.igestamobile.ui.notifications;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.igestamobile.R;
import com.example.igestamobile.adapter.CondenaUnidadeAdapter;
import com.example.igestamobile.data.api.CondenaApi;
import com.example.igestamobile.data.api.CondenaUnidadeApi;
import com.example.igestamobile.data.api.RetrofitClient;
import com.example.igestamobile.data.model.CondenaModel;
import com.example.igestamobile.data.model.CondenaUnidadeResponse;
import com.example.igestamobile.databinding.FragmentNotificationsBinding;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_CLIENTE_ID = "CLIENTE_ID";
    private CondenaApi condenaApi;
    private CondenaUnidadeApi condenaUnidadeApi;
    private FragmentNotificationsBinding binding;
    private CondenaUnidadeAdapter adapter;
    private Dialog dialog_enviar_contagens, dialog_enviar_condenas;
    private Integer getClienteIdSalvo() {
        Integer clienteId = -1;

        if (getContext() != null) {
            SharedPreferences sharedPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            clienteId = sharedPrefs.getInt(KEY_CLIENTE_ID, -1);
        }
        return clienteId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        condenaApi = RetrofitClient.getClient().create(CondenaApi.class);
        condenaUnidadeApi = RetrofitClient.getClient().create(CondenaUnidadeApi.class);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CondenaUnidadeAdapter(new ArrayList<>(), requireContext(), condenaApi);
        binding.recyclerView.setAdapter(adapter);

        carregarCondenasDeUnidade(getClienteIdSalvo());

        TextView bt_enviar_contagens = root.findViewById(R.id.bt_enviar_contagens);

        dialog_enviar_contagens = new Dialog(requireContext());
        dialog_enviar_condenas = new Dialog(requireContext());

        dialog_enviar_contagens.setContentView(R.layout.dialog_enviar);
        dialog_enviar_condenas.setContentView(R.layout.dialog_enviar_condenas);

        dialog_enviar_contagens.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_enviar_condenas.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_enviar_contagens.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_enviar_condenas.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        Button bt_n_enviar_contagens = dialog_enviar_contagens.findViewById(R.id.bt_n_remover);
        Button bt_enviar_contagens_dialog = dialog_enviar_contagens.findViewById(R.id.bt_cadastrar_func_dialog);
        Button bt_enviar_condenas_dialog = dialog_enviar_condenas.findViewById(R.id.bt_enviar_condenas_dialog);

        bt_enviar_contagens.setOnClickListener(v -> {
            dialog_enviar_contagens.show();
        });

        bt_n_enviar_contagens.setOnClickListener(v -> {
            dialog_enviar_contagens.dismiss();
        });

        bt_enviar_contagens_dialog.setOnClickListener(v -> {
           dialog_enviar_contagens.dismiss();
           dialog_enviar_condenas.show();
        });

        bt_enviar_condenas_dialog.setOnClickListener(v -> {
            dialog_enviar_condenas.dismiss();

            List<Condena> listaParaPlanilha = new ArrayList<>();
            for (CondenaUnidadeResponse item : adapter.getCondenasUnidades()) {
                if (item.getQuantidade() > 0) {
                    listaParaPlanilha.add(new Condena(item.getNome(), item.getQuantidade()));
                }
            }

            gerarPlanilha(listaParaPlanilha);
        });

        return root;
    }

    private void carregarCondenasDeUnidade(Integer unidadeId) {
        condenaUnidadeApi.selecionarCondenasUnidade(unidadeId).enqueue(new Callback<List<CondenaUnidadeResponse>>() {
            @Override
            public void onResponse(Call<List<CondenaUnidadeResponse>> call, Response<List<CondenaUnidadeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setLista(response.body());
                } else {
                    Toast.makeText(requireContext(), "Falha ao carregar IDs de associação.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<CondenaUnidadeResponse>> call, Throwable t) {
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(requireContext(), "Erro de rede!", Toast.LENGTH_SHORT).show();
                }            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void gerarPlanilha(List<Condena> condenas) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Condenas");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Condena");
        header.createCell(1).setCellValue("Tipo");
        header.createCell(2).setCellValue("Quantidade");
        header.createCell(3).setCellValue("Porcentagem");

        int total = 0;
        for (Condena c : condenas) total += c.getQuantidade();

        int rowIndex = 1;
        for (Condena c : condenas) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(c.getNome());
            row.createCell(1).setCellValue("Total");
            row.createCell(2).setCellValue(c.getQuantidade());
            double porcentagem = (total > 0) ? ((double) c.getQuantidade() / total) * 100 : 0;
            row.createCell(3).setCellValue(String.format("%.2f%%", porcentagem));
        }

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File file = new File(downloadsDir, "condenas.xlsx");

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            Toast.makeText(requireContext(), "Planilha salva em: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Erro ao salvar planilha: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Permissão de Armazenamento não concedida. Por favor, conceda a permissão.", Toast.LENGTH_LONG).show();
        }
    }

    public static class Condena {
        private String nome;
        private int quantidade;

        public Condena(String nome, int quantidade) {
            this.nome = nome;
            this.quantidade = quantidade;
        }

        public String getNome() { return nome; }
        public int getQuantidade() { return quantidade; }
    }
}
