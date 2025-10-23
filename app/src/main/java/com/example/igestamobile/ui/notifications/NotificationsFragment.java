package com.example.igestamobile.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igestamobile.R;
import com.example.igestamobile.adapter.CondenaUnidadeAdapter;
import com.example.igestamobile.data.api.CondenaApi;
import com.example.igestamobile.data.api.CondenaUnidadeApi;
import com.example.igestamobile.data.api.RetrofitClient;
import com.example.igestamobile.data.model.CondenaUnidadeResponse;
import com.example.igestamobile.databinding.FragmentNotificationsBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationsFragment extends Fragment {
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_CLIENTE_ID = "CLIENTE_ID";
    private CondenaApi condenaApi;
    private CondenaUnidadeApi condenaUnidadeApi;
    private FragmentNotificationsBinding binding;
    private CondenaUnidadeAdapter adapter;
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
                Toast.makeText(requireContext(), "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}