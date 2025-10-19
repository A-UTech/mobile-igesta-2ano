package com.example.igestamobile.data.model;
import com.google.gson.annotations.SerializedName;

public class LoginModelResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("tipoUsuario")
    private String tipoUsuario;

    public LoginModelResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
}