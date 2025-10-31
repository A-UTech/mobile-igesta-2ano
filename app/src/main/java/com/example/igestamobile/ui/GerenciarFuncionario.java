package com.example.igestamobile.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.example.igestamobile.R;
import com.example.igestamobile.utils.MaskUtil;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GerenciarFuncionario#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GerenciarFuncionario extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USUARIO_CREDENCIAL = "USUARIO_CREDENCIAL";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Dialog dialog_cadastrar_func;
    ShapeableImageView imageFuncionarioGerenciar;
    private FirebaseFirestore db;

    public GerenciarFuncionario() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GerenciarFuncionario.
     */
    // TODO: Rename and change types and number of parameters
    public static GerenciarFuncionario newInstance(String param1, String param2) {
        GerenciarFuncionario fragment = new GerenciarFuncionario();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gerenciar_funcionario, container, false);

        View adicionarFuncionario = view.findViewById(R.id.bt_adicionar_funcionario);
        View btVoltar = view.findViewById(R.id.bt_voltar_gf);

        dialog_cadastrar_func = new Dialog(requireContext());
        dialog_cadastrar_func.setContentView(R.layout.dialog_cadastrar_func);
        dialog_cadastrar_func.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_cadastrar_func.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        db = FirebaseFirestore.getInstance();

        imageFuncionarioGerenciar = view.findViewById(R.id.imageFuncionarioGerenciar);
        Button bt_cadastrar_func = dialog_cadastrar_func.findViewById(R.id.bt_cadastrar_func_dialog);

        loadProfileImage();

        adicionarFuncionario.setOnClickListener(v -> {
            dialog_cadastrar_func.show();
        });

        bt_cadastrar_func.setOnClickListener(v -> {
            //Lógica para cadastrar o funcionário no banco
        });

        if (btVoltar != null) {
            btVoltar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Navigation.findNavController(v).popBackStack();
                }
            });
        }

        return view;
    }

    private void loadProfileImage() {
        String documentId = getUsuarioCredencial();

        if (documentId == null) {
            Log.e("Firebase", "Credencial não encontrada. Imagem não pode ser carregada.");
            return;
        }

        db.collection("usuarios").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) {
                        Log.d("GlideFix", "Fragment not attached. Cancelling profile image load.");
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("imagem");

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(profileImageUrl)
                                    .override(175, 175)
                                    .centerCrop()
                                    .placeholder(R.mipmap.fotoperfil)
                                    .error(R.mipmap.fotoperfil)
                                    .into(imageFuncionarioGerenciar);

                            Log.i("Firebase", "Foto de perfil carregada do Firebase.");
                        } else {
                            Log.d("Firebase", "URL da foto de perfil (campo 'imagem') não encontrada no Firestore.");
                        }
                    } else {
                        Log.d("Firebase", "Documento do usuário não encontrado no Firestore (ID: " + documentId + ").");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Erro ao buscar documento do usuário: " + e.getMessage());
                });
    }

    private String getUsuarioCredencial() {
        if (getActivity() == null) return null;

        String rawCredencial = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USUARIO_CREDENCIAL, null);

        if (rawCredencial != null) {
            return MaskUtil.unmaskCnpj(rawCredencial);
        }
        return null;
    }
}