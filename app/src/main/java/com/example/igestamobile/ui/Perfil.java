package com.example.igestamobile.ui;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.preprocess.BitmapDecoder;
import com.cloudinary.android.preprocess.BitmapEncoder;
import com.cloudinary.android.preprocess.DimensionsValidator;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.cloudinary.android.preprocess.Limit;
import com.cloudinary.android.preprocess.Rotate;
import com.example.igestamobile.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import com.example.igestamobile.utils.MaskUtil;

public class Perfil extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USUARIO_CREDENCIAL = "USUARIO_CREDENCIAL";
    private String cloudname = "dpbzx88eu";
    private String uploadProjeto = "IGesta";
    private Uri photoUri;

    private String mParam1;
    private String mParam2;
    private Button bt_n_logout, bt_logout_dialog;
    private Dialog dialog_logout;
    private TextView bt_alterar_foto_perfil, bt_selecionar_galeria, bt_tirar_foto;
    private ImageView img_foto_perfil;

    private FirebaseFirestore db;

    ActivityResultLauncher<Intent> galeriaLauncher;
    ActivityResultLauncher<Uri> cameraLauncher;

    public Perfil() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();

        initCloudinary();

        View view = inflater.inflate(R.layout.fragment_perfil, container, false);


        LinearLayout bt_config = view.findViewById(R.id.bt_config);
        LinearLayout bt_gerenciar = view.findViewById(R.id.bt_gerenciar);
        LinearLayout bt_historico = view.findViewById(R.id.bt_historico);
        LinearLayout bt_logout = view.findViewById(R.id.bt_logout);
        ConstraintLayout bt_alterar_imagem = view.findViewById(R.id.bt_alterar_imagem);

        BottomSheetDialog dialog_opcoes_imagem = new BottomSheetDialog(requireContext());
        dialog_opcoes_imagem.setContentView(R.layout.dialog_foto);

        BottomSheetDialog dialog_foto_opcoes = new BottomSheetDialog(requireContext());
        dialog_foto_opcoes.setContentView(R.layout.dialog_foto_opcoes);

        dialog_logout = new Dialog(requireContext());
        dialog_logout.setContentView(R.layout.dialog_logout);
        dialog_logout.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_logout.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        bt_n_logout = dialog_logout.findViewById(R.id.bt_n_remover);
        bt_logout_dialog = dialog_logout.findViewById(R.id.bt_cadastrar_func_dialog);
        bt_alterar_foto_perfil = dialog_opcoes_imagem.findViewById(R.id.bt_alterar_foto_perfil);
        bt_selecionar_galeria = dialog_foto_opcoes.findViewById(R.id.bt_selecionar_galeria);
        img_foto_perfil = view.findViewById(R.id.img_foto_perfil);
        bt_tirar_foto = dialog_foto_opcoes.findViewById(R.id.bt_tirar_foto);

        setGallery();
        setCamera();

        checkUserDocumentExistence();
        loadProfileImage();

        bt_logout.setOnClickListener(v -> {
            dialog_logout.show();
        });

        bt_n_logout.setOnClickListener(v -> {
            dialog_logout.dismiss();
        });

        bt_logout_dialog.setOnClickListener(v -> {
            clearUsuarioCredencial();

            Intent intent = new Intent(requireActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            dialog_logout.dismiss();
        });

        bt_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_perfil_to_configuracao);
            }
        });

        bt_gerenciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_perfil_to_gerenciarFuncionario);
            }
        });

        bt_historico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_navigation_perfil_to_navigation_historico);
            }
        });

        bt_alterar_imagem.setOnClickListener(v -> {
            dialog_opcoes_imagem.show();
        });

        bt_alterar_foto_perfil.setOnClickListener(v -> {
            dialog_opcoes_imagem.dismiss();
            dialog_foto_opcoes.show();
        });

        bt_selecionar_galeria.setOnClickListener(v -> openGallery());
        bt_tirar_foto.setOnClickListener(v -> captureImage());

        return view;
    }

    private String getUsuarioCredencial() {
        if (getActivity() == null) return null;

        String rawCredencial = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USUARIO_CREDENCIAL, null);

        if (rawCredencial != null) {
            return MaskUtil.unmask(rawCredencial);
        }
        return null;
    }

    private void clearUsuarioCredencial() {
        if (getActivity() == null) return;
        getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_USUARIO_CREDENCIAL)
                .apply();
    }

    private void setCamera() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                o -> {
                    if (o) {
                        preUpload(photoUri);
                    } else {
                        Toast.makeText(requireContext(), "Foto não foi tirada", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setGallery() {
        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                o -> {
                    if (o.getResultCode() == RESULT_OK && o.getData() != null) {
                        Uri imageUri = o.getData().getData();

                        Glide.with(this).load(imageUri).override(175, 175).centerCrop().into(img_foto_perfil);

                        uploadImagem(imageUri);
                    }
                }
        );
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galeriaLauncher.launch(intent);
    }

    private void captureImage() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 101);
            return;
        }

        try {
            String tempo = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
            String arquivo = "F_" + tempo;
            File pasta = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            File photo = File.createTempFile(arquivo, ".jpg", pasta);

            photoUri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", photo);

            cameraLauncher.launch(photoUri);

        } catch (IOException e) {
            Log.e("Camera", "Erro ao criar arquivo de imagem: " + e.getMessage());
            Toast.makeText(requireContext(), "Erro ao preparar a câmera.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initCloudinary() {
        try {
            MediaManager.get();
        } catch (IllegalStateException e) {
            Map config = new HashMap<>();
            config.put("cloud_name", cloudname);
            MediaManager.init(requireContext().getApplicationContext(), config);
            Log.i("Cloudinary", "Cloudinary inicializado com sucesso.");
        } catch (Exception e) {
            Log.e("Cloudinary", "Erro inesperado ao inicializar Cloudinary: " + e.getMessage());
        }
    }

    private void uploadImagem(Uri imageUri) {
        MediaManager.get().upload(imageUri)
                .option("folder", "fotos_IGesta")
                .unsigned(uploadProjeto)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(requireContext(), "Upload (Galeria) iniciado...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");

                        Toast.makeText(requireContext(), "Galeria - Upload Sucesso! URL: " + url, Toast.LENGTH_LONG).show();

                        Glide.with(Perfil.this).load(url).override(175, 175).centerCrop().into(img_foto_perfil);

                        saveProfileImageUrl(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(requireContext(), "Erro no Upload (Galeria): " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch(requireContext());
    }

    private void preUpload(Uri imageUri) {
        MediaManager.get().upload(imageUri)
                .option("folder", "fotos_IGesta")
                .unsigned(uploadProjeto)
                .preprocess(new ImagePreprocessChain()
                        .loadWith(new BitmapDecoder(1000, 1000))
                        .addStep(new Limit(1000, 1000))
                        .addStep(new DimensionsValidator(10, 10, 1000, 1000))
                        .addStep(new Rotate(90))
                        .saveWith(new BitmapEncoder(BitmapEncoder.Format.JPEG, 60))
                )
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(requireContext(), "Pré-Upload (Câmera) iniciado...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");

                        Toast.makeText(requireContext(), "Câmera - Upload Sucesso! URL: " + url, Toast.LENGTH_LONG).show();

                        Glide.with(Perfil.this).load(url).override(175, 175).centerCrop().into(img_foto_perfil);

                        saveProfileImageUrl(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(requireContext(), "Erro no Pré-Upload (Câmera): " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch(requireContext());
    }

    private void saveProfileImageUrl(String imageUrl) {
        String documentId = getUsuarioCredencial();

        if (documentId == null) {
            Toast.makeText(requireContext(), "Erro: Credencial não encontrada. Não é possível salvar a foto.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("imagem", imageUrl);

        updates.put("email/cnpj", documentId);

        db.collection("usuarios").document(documentId)
                .set(updates, SetOptions.merge());
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
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("imagem");

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(profileImageUrl)
                                    .override(175, 175)
                                    .centerCrop()
                                    .placeholder(R.mipmap.fotoperfil)
                                    .error(R.mipmap.fotoperfil)
                                    .into(img_foto_perfil);

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

    private void checkUserDocumentExistence() {
        String documentId = getUsuarioCredencial();

        if (documentId == null) {
            Log.w("Firebase", "Credencial não encontrada. Não é possível verificar a credencial.");
            return;
        }

        db.collection("usuarios").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.i("Credencial", "Credencial (email/cnpj) ENCONTRADA no Firestore para o ID: " + documentId);
                    } else {
                        Log.w("Credencial", "Credencial (email/cnpj) NÃO ENCONTRADA no Firestore para o ID: " + documentId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Credencial", "Erro ao verificar credencial no Firestore: " + e.getMessage());
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            } else {
                Toast.makeText(requireContext(), "Permissão da câmera negada", Toast.LENGTH_SHORT).show();
            }
        }
    }
}