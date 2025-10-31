package com.example.igestamobile;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide; // Importação adicionada
import com.google.android.gms.common.images.ImageManager;
import com.google.android.material.imageview.ShapeableImageView; // Importação adicionada

import com.example.igestamobile.R; // Garanta que o seu pacote seja o correto

public class PerfilFuncionario extends Fragment {

    // Constantes de Chave de Argumentos (DEVEM ser as mesmas usadas no GerenciarFuncionario!)
    private static final String ARG_NOME = "nome";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_CARGO = "cargo";
    private static final String ARG_URL_IMAGEM = "urlImagem";


    private Dialog dialog_remover;

    // Elementos de UI
    private ShapeableImageView imagePerfilFuncionario;
    private TextView textNomeFuncionario;
    private TextView textEmailFuncionario;
    private TextView textCargoFuncionario;


    // Variáveis para armazenar os dados do funcionário
    private String nomeFuncionario;
    private String emailFuncionario;
    private String cargoFuncionario;
    private String urlImagemFuncionario;


    public PerfilFuncionario() {
        // Required empty public constructor
    }

    public static PerfilFuncionario newInstance(String nome, String email, String cargo, String urlImagem) {
        PerfilFuncionario fragment = new PerfilFuncionario();
        Bundle args = new Bundle();
        args.putString(ARG_NOME, nome);
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_CARGO, cargo);
        args.putString(ARG_URL_IMAGEM, urlImagem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Resgatar os dados do funcionário do Bundle
        if (getArguments() != null) {
            nomeFuncionario = getArguments().getString(ARG_NOME);
            emailFuncionario = getArguments().getString(ARG_EMAIL);
            cargoFuncionario = getArguments().getString(ARG_CARGO);
            urlImagemFuncionario = getArguments().getString(ARG_URL_IMAGEM);

            Log.d("PerfilFuncionario", "Dados Recebidos: " + nomeFuncionario + " | " + emailFuncionario);
        } else {
            Log.e("PerfilFuncionario", "Nenhum argumento recebido.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil_funcionario, container, false);

        imagePerfilFuncionario = view.findViewById(R.id.img_funcionario_perfil);
        textNomeFuncionario = view.findViewById(R.id.nome_funcionario);
        textEmailFuncionario = view.findViewById(R.id.email_funcionario);

        View btVoltar = view.findViewById(R.id.bt_voltar_pf);
        LinearLayout bt_remover = view.findViewById(R.id.bt_remover_funcionario);

        dialog_remover = new Dialog(requireContext());
        dialog_remover.setContentView(R.layout.dialog_remover);
        dialog_remover.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_remover.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        Button bt_n_remover = dialog_remover.findViewById(R.id.bt_n_remover);
        Button bt_remover_dialog = dialog_remover.findViewById(R.id.bt_cadastrar_func_dialog);

        bt_remover.setOnClickListener(v -> {
            dialog_remover.show();
        });

        bt_n_remover.setOnClickListener(v -> {
            dialog_remover.dismiss();
        });

        bt_remover_dialog.setOnClickListener(v -> {
            // Lógica para remover o funcionário do banco
        });

        if (btVoltar != null) {
            btVoltar.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2. Popular os elementos de UI
        if (nomeFuncionario != null) {
            textNomeFuncionario.setText(nomeFuncionario);
        }
        if (emailFuncionario != null) {
            textEmailFuncionario.setText(emailFuncionario);
        }

        if (urlImagemFuncionario != null && !urlImagemFuncionario.isEmpty()) {
            Glide.with(this)
                    .load(urlImagemFuncionario)
                    .override(175, 175)
                    .centerCrop()
                    .placeholder(R.mipmap.fotoperfil)
                    .error(R.mipmap.fotoperfil)
                    .into(imagePerfilFuncionario);
        } else {
            // Se a URL estiver vazia, use a imagem padrão
            imagePerfilFuncionario.setImageResource(R.mipmap.fotoperfil);
        }


        // Lógica do Dropdown (Função/Cargo)
        String[] funcoes = requireContext().getResources().getStringArray(R.array.cargos_opcoes);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                funcoes
        );

        AutoCompleteTextView dropdownCargos = view.findViewById(R.id.bt_cargos);

        dropdownCargos.setAdapter(adapter);

        // Para definir a função "padrão" usando o cargo recebido
        if (cargoFuncionario != null && !cargoFuncionario.isEmpty()) {
            dropdownCargos.setText(cargoFuncionario, false);
        } else {
            // Se não houver cargo, define o primeiro da lista
            dropdownCargos.setText(funcoes[0], false);
        }

        dropdownCargos.setOnItemClickListener((parent, v, position, id) -> {
            String itemSelecionado = (String) parent.getItemAtPosition(position);
            // Aqui você pode adicionar lógica para salvar a mudança de cargo, se necessário.
            Log.d("PerfilFuncionario", "Novo cargo selecionado: " + itemSelecionado);
        });
    }
}