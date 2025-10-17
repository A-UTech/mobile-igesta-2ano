package com.example.igestamobile.ui.notifications;

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
import androidx.lifecycle.ViewModelProvider;

import com.example.igestamobile.R;
import com.example.igestamobile.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textView31;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        binding.btnOption.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenuInflater().inflate(R.menu.menu_opcoes, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.editar_condenas) {
                    Toast.makeText(requireContext(), "Editar condenas clicado", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.limpar_condenas) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Limpar contagens")
                            .setMessage("Tem certeza que deseja limpar todas as contagens?")
                            .setPositiveButton("Sim", (dialog, which) -> {
                                Toast.makeText(requireContext(), "Contagens limpas!", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                    return true;
                }

                return false;
            });

            popup.show();
        });

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}