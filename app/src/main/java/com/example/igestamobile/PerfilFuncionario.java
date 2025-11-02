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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.igestamobile.data.api.GestorApi;
import com.example.igestamobile.data.api.LiderApi;
import com.example.igestamobile.data.api.SqlRetrofitClient;
import com.example.igestamobile.data.model.GestorModel;
import com.example.igestamobile.data.model.LiderModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFuncionario extends Fragment {

    private static final String ARG_NOME = "nome";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_CARGO = "cargo";
    private static final String ARG_URL_IMAGEM = "urlImagem";
    private static final String ARG_ID = "identificador";
    private static final String ARG_SENHA = "senha";
    private static final String ARG_UNIDADE_ID = "unidadeId";
    private static final String ARG_CPF = "cpf";

    private Dialog dialog_remover;
    private Dialog dialog_mudar_para_lider;
    private Dialog dialog_mudar_para_gestor;

    private ShapeableImageView imagePerfilFuncionario;
    private TextView textNomeFuncionario;
    private TextView textEmailFuncionario;

    private String nomeFuncionario;
    private String emailFuncionario;
    private String cargoFuncionario;
    private String urlImagemFuncionario;
    private String idFuncionario;
    private String senhaFuncionario;
    private Integer unidadeIdFuncionario;
    private String cpfFuncionario = "000.000.000-00";

    public PerfilFuncionario() {
    }

    public static PerfilFuncionario newInstance(String nome, String email, String cargo, String urlImagem, String id, String senha, Integer unidadeId, String cpf) {
        PerfilFuncionario fragment = new PerfilFuncionario();
        Bundle args = new Bundle();
        args.putString(ARG_NOME, nome);
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_CARGO, cargo);
        args.putString(ARG_URL_IMAGEM, urlImagem);
        args.putString(ARG_ID, id);
        args.putString(ARG_SENHA, senha);
        args.putInt(ARG_UNIDADE_ID, unidadeId);
        args.putString(ARG_CPF, cpf);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            nomeFuncionario = getArguments().getString(ARG_NOME);
            emailFuncionario = getArguments().getString(ARG_EMAIL);
            cargoFuncionario = getArguments().getString(ARG_CARGO);
            urlImagemFuncionario = getArguments().getString(ARG_URL_IMAGEM);
            idFuncionario = getArguments().getString(ARG_ID);
            unidadeIdFuncionario = getArguments().getInt(ARG_UNIDADE_ID);
            senhaFuncionario = getArguments().getString(ARG_SENHA, "senhaPadrao123");
            cpfFuncionario = getArguments().getString(ARG_CPF, "000.000.000-00");
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
            dialog_remover.dismiss();
            deletarFuncionario();
        });

        dialog_mudar_para_lider = new Dialog(requireContext());
        dialog_mudar_para_lider.setContentView(R.layout.dialog_area_lider);
        dialog_mudar_para_lider.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_mudar_para_lider.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog_mudar_para_gestor = new Dialog(requireContext());
        dialog_mudar_para_gestor.setContentView(R.layout.dialog_confirmar_cargo);
        dialog_mudar_para_gestor.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_mudar_para_gestor.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        if (btVoltar != null) {
            btVoltar.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
            imagePerfilFuncionario.setImageResource(R.mipmap.fotoperfil);
        }


        String[] funcoes = requireContext().getResources().getStringArray(R.array.cargos_opcoes);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                funcoes
        );

        AutoCompleteTextView dropdownCargos = view.findViewById(R.id.bt_cargos);

        dropdownCargos.setAdapter(adapter);

        if (cargoFuncionario != null && !cargoFuncionario.isEmpty()) {
            dropdownCargos.setText(cargoFuncionario, false);
        } else {
            dropdownCargos.setText(funcoes[0], false);
        }

        dropdownCargos.setOnItemClickListener((parent, v, position, id) -> {
            String novoCargo = (String) parent.getItemAtPosition(position);

            if (novoCargo.equals(cargoFuncionario)) {
                return;
            }

            if ("Líder".equals(novoCargo)) {
                abrirDialogMudarParaLider();
            } else if ("Gestor".equals(novoCargo)) {
                abrirDialogMudarParaGestor();
            }

            dropdownCargos.setText(cargoFuncionario, false);
        });
    }

    private void abrirDialogMudarParaLider() {
        TextInputEditText inputArea = dialog_mudar_para_lider.findViewById(R.id.input_area_lider);
        Button btConfirmar = dialog_mudar_para_lider.findViewById(R.id.bt_confirmar_area_dialog);
        Button btCancelar = dialog_mudar_para_lider.findViewById(R.id.bt_cancelar_area_dialog);

        btCancelar.setOnClickListener(v -> dialog_mudar_para_lider.dismiss());

        btConfirmar.setOnClickListener(v -> {
            String area = inputArea.getText() != null ? inputArea.getText().toString().trim() : "";
            if (area.isEmpty()) {
                Toast.makeText(getContext(), "A área de atuação é obrigatória.", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog_mudar_para_lider.dismiss();
            mudarDeGestorParaLider(area);
        });

        if (inputArea != null) {
            inputArea.setText("");
        }
        dialog_mudar_para_lider.show();
    }

    private void abrirDialogMudarParaGestor() {
        Button btConfirmar = dialog_mudar_para_gestor.findViewById(R.id.bt_s_confirmar_cargo);
        Button btCancelar = dialog_mudar_para_gestor.findViewById(R.id.bt_n_confirmar_cargo);

        btCancelar.setOnClickListener(v -> dialog_mudar_para_gestor.dismiss());

        btConfirmar.setOnClickListener(v -> {
            dialog_mudar_para_gestor.dismiss();
            mudarDeLiderParaGestor();
        });

        dialog_mudar_para_gestor.show();
    }

    private void mudarDeGestorParaLider(String area) {
        if (idFuncionario == null || !cargoFuncionario.equals("Gestor")) return;

        try {
            int id = Integer.parseInt(idFuncionario);
            GestorApi gestorApi = SqlRetrofitClient.getClient(getContext()).create(GestorApi.class);
            LiderApi liderApi = SqlRetrofitClient.getClient(getContext()).create(LiderApi.class);

            gestorApi.excluirGestor(id).enqueue(new Callback<GestorModel>() {
                @Override
                public void onResponse(@NonNull Call<GestorModel> call, @NonNull Response<GestorModel> response) {
                    if (response.isSuccessful()) {
                        LiderModel novoLider = new LiderModel(
                                unidadeIdFuncionario,
                                nomeFuncionario,
                                emailFuncionario,
                                senhaFuncionario,
                                area
                        );

                        liderApi.cadastrarLider(novoLider).enqueue(new Callback<LiderModel>() {
                            @Override
                            public void onResponse(@NonNull Call<LiderModel> call, @NonNull Response<LiderModel> response) {
                                if (response.isSuccessful()) {
                                    cargoFuncionario = "Líder";
                                    atualizarUICargo(cargoFuncionario);
                                    Toast.makeText(getContext(), "Cargo alterado para Líder.", Toast.LENGTH_LONG).show();
                                } else {
                                    String errorMsg = "Erro ao cadastrar Líder. Status: " + response.code();
                                    if (response.code() == 409 && response.errorBody() != null) {
                                        try {
                                            String body = response.errorBody().string();
                                            errorMsg = "Conflito (409). Detalhes: " + body;
                                        } catch (Exception e) {
                                            Log.e("API_ERROR", "Erro ao ler body: " + e.getMessage());
                                        }
                                    } else {
                                        errorMsg += ". Tente novamente.";
                                    }
                                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<LiderModel> call, @NonNull Throwable t) {
                                Toast.makeText(getContext(), "Falha de rede ao cadastrar Líder.", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Erro ao excluir Gestor. Status: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GestorModel> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Falha de rede ao excluir Gestor.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "ID do funcionário inválido.", Toast.LENGTH_SHORT).show();
        }
    }

    private void mudarDeLiderParaGestor() {
        if (idFuncionario == null || !cargoFuncionario.equals("Líder")) return;

        try {
            int id = Integer.parseInt(idFuncionario);
            GestorApi gestorApi = SqlRetrofitClient.getClient(getContext()).create(GestorApi.class);
            LiderApi liderApi = SqlRetrofitClient.getClient(getContext()).create(LiderApi.class);

            liderApi.excluirLider(id).enqueue(new Callback<LiderModel>() {
                @Override
                public void onResponse(@NonNull Call<LiderModel> call, @NonNull Response<LiderModel> response) {
                    if (response.isSuccessful()) {
                        GestorModel novoGestor = new GestorModel(
                                unidadeIdFuncionario,
                                nomeFuncionario,
                                emailFuncionario,
                                senhaFuncionario
                        );

                        gestorApi.cadastrarGestor(novoGestor).enqueue(new Callback<GestorModel>() {
                            @Override
                            public void onResponse(@NonNull Call<GestorModel> call, @NonNull Response<GestorModel> response) {
                                if (response.isSuccessful()) {
                                    cargoFuncionario = "Gestor";
                                    atualizarUICargo(cargoFuncionario);
                                    Toast.makeText(getContext(), "Cargo alterado para Gestor.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), "Erro ao cadastrar Gestor. Status: " + response.code(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<GestorModel> call, @NonNull Throwable t) {
                                Toast.makeText(getContext(), "Falha de rede ao cadastrar Gestor.", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Erro ao excluir Líder. Status: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LiderModel> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Falha de rede ao excluir Líder.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "ID do funcionário inválido.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deletarFuncionario() {
        if (idFuncionario == null || cargoFuncionario == null) {
            Toast.makeText(getContext(), "Dados do funcionário incompletos para exclusão.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int id = Integer.parseInt(idFuncionario);

            GestorApi gestorApi = SqlRetrofitClient.getClient(getContext()).create(GestorApi.class);
            LiderApi liderApi = SqlRetrofitClient.getClient(getContext()).create(LiderApi.class);

            Call<?> call;
            String cargoRemovido;

            if (cargoFuncionario.equals("Gestor")) {
                call = gestorApi.excluirGestor(id);
                cargoRemovido = "Gestor";
            } else if (cargoFuncionario.equals("Líder")) {
                call = liderApi.excluirLider(id);
                cargoRemovido = "Líder";
            } else {
                Toast.makeText(getContext(), "Cargo não suportado para exclusão.", Toast.LENGTH_SHORT).show();
                return;
            }

            ((Call<Void>) call).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), nomeFuncionario + " (" + cargoRemovido + ") excluído com sucesso!", Toast.LENGTH_LONG).show();
                        if (getView() != null) {
                            Navigation.findNavController(getView()).popBackStack();
                        }
                    } else {
                        String errorMsg = "Erro ao excluir " + cargoRemovido + ". Status: " + response.code();
                        try {
                            if (response.errorBody() != null) {
                                String body = response.errorBody().string();
                                errorMsg += ". Detalhes: " + body;
                            }
                        } catch (Exception e) {
                            Log.e("API_ERROR", "Erro ao ler body: " + e.getMessage());
                        }
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Falha de rede ao excluir funcionário.", Toast.LENGTH_LONG).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "ID do funcionário inválido.", Toast.LENGTH_SHORT).show();
        }
    }

    private void atualizarUICargo(String novoCargo) {
        AutoCompleteTextView dropdownCargos = requireView().findViewById(R.id.bt_cargos);
        if (dropdownCargos != null) {
            dropdownCargos.setText(novoCargo, false);
        }
    }
}