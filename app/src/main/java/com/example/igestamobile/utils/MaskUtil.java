package com.example.igestamobile.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MaskUtil {

    private static final int CNPJ_LENGTH = 14;

    public static void aplicarMascara(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                String currentText = s.toString();

                String unmaskedCnpj = unmaskCnpj(currentText);

                boolean isLikelyCnpj = currentText.matches("[\\d.-/]*") || unmaskedCnpj.length() > 0;

                if (currentText.contains("@") || (isLikelyCnpj && unmaskedCnpj.length() > CNPJ_LENGTH)) {
                    return;
                }

                isUpdating = true;

                if (isLikelyCnpj) {
                    if (unmaskedCnpj.length() == CNPJ_LENGTH) {
                        String formatado = formatarCnpj(unmaskedCnpj);
                        if (!currentText.equals(formatado)) {
                            editText.setText(formatado);
                            editText.setSelection(formatado.length());
                        }
                    } else if (currentText.length() > 0 && unmaskedCnpj.length() < CNPJ_LENGTH) {
                    }
                }

                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public static String unmaskCnpj(String s) {
        return s.replaceAll("[^\\d]", "");
    }

    public static String formatarCnpj(String cnpj) {
        if (cnpj.length() != CNPJ_LENGTH) return cnpj;
        return cnpj.substring(0, 2) + "." +
                cnpj.substring(2, 5) + "." +
                cnpj.substring(5, 8) + "/" +
                cnpj.substring(8, 12) + "-" +
                cnpj.substring(12, 14);
    }
}