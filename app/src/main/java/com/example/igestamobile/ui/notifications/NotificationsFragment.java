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

    
    private Button bt_filtrar_total;
    private Button bt_filtrar_parcial;
    private String currentFilterType = null; 

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

        
        bt_filtrar_total = root.findViewById(R.id.btn_total);
        bt_filtrar_parcial = root.findViewById(R.id.btn_parcial);


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

            List<CondenaUnidadeResponse> contagensFinais = adapter.getContagensFinais();

            List<Condena> listaParaPlanilha = new ArrayList<>();
            for (CondenaUnidadeResponse item : contagensFinais) {
                
                listaParaPlanilha.add(new Condena(item.getNome(), item.getQuantidade(), item.getTipo()));
            }

            gerarPlanilha(listaParaPlanilha);
        });

        

        
        View.OnClickListener filtroClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFilterType = null;

                
                if (v.getId() == R.id.btn_total) {
                    newFilterType = "Total";
                } else if (v.getId() == R.id.btn_parcial) {
                    newFilterType = "Parcial";
                }

                
                if (newFilterType != null && newFilterType.equals(currentFilterType)) {
                    currentFilterType = null; 
                } else {
                    currentFilterType = newFilterType; 
                }

                
                adapter.aplicarFiltroVisual(currentFilterType);

                
                updateFiltroButtons(currentFilterType);
            }
        };

        if (bt_filtrar_total != null) {
            bt_filtrar_total.setOnClickListener(filtroClickListener);
        }

        if (bt_filtrar_parcial != null) {
            bt_filtrar_parcial.setOnClickListener(filtroClickListener);
        }

        
        updateFiltroButtons(currentFilterType);

        

        return root;
    }

    private void updateFiltroButtons(String activeType) {
        if (getContext() == null) return;
        try {
            int activeColor = requireContext().getResources().getColor(R.color.cinza_claro, null);
            int inactiveColor = requireContext().getResources().getColor(R.color.verde_escuro, null);
            int textColorActive = requireContext().getResources().getColor(R.color.branco, null);
            int textColorInactive = requireContext().getResources().getColor(R.color.branco, null);

            if (bt_filtrar_total != null) {
                if ("Total".equals(activeType)) {
                    bt_filtrar_total.setBackgroundColor(activeColor);
                    bt_filtrar_total.setTextColor(textColorActive);
                } else {
                    bt_filtrar_total.setBackgroundColor(inactiveColor);
                    bt_filtrar_total.setTextColor(textColorInactive);
                }
            }

            if (bt_filtrar_parcial != null) {
                if ("Parcial".equals(activeType)) {
                    bt_filtrar_parcial.setBackgroundColor(activeColor);
                    bt_filtrar_parcial.setTextColor(textColorActive);
                } else {
                    bt_filtrar_parcial.setBackgroundColor(inactiveColor);
                    bt_filtrar_parcial.setTextColor(textColorInactive);
                }
            }
        } catch (Exception e) {
            
            
        }
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
                }
            }
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
            row.createCell(1).setCellValue(c.getTipo());
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
        private String tipo;

        public Condena(String nome, int quantidade, String tipo) {
            this.nome = nome;
            this.quantidade = quantidade;
            this.tipo = tipo;
        }

        public String getNome() { return nome; }
        public int getQuantidade() { return quantidade; }
        public String getTipo() { return tipo; }
    }
}
