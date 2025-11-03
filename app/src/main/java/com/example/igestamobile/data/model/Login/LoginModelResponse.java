package com.example.igestamobile.data.model.Login;
import com.google.gson.annotations.SerializedName;

public class LoginModelResponse {

    @SerializedName("id")
    private Integer id;

    @SerializedName("tipoUsuario")
    private String tipoUsuario;

    public LoginModelResponse() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
}