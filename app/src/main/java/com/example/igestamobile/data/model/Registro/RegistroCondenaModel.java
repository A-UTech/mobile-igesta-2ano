package com.example.igestamobile.data.model.Registro;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.example.igestamobile.data.model.CondenaUnidade.CondenaUnidadeResponse;

public class RegistroCondenaModel implements Parcelable {
    private Long id;
    private int quantidade;
    private String nome;
    private String tipo;

    public RegistroCondenaModel() {
    }

    public RegistroCondenaModel(Long id) {
        this.id = id;
        this.quantidade = 0;
    }

    public RegistroCondenaModel(Long id, int quantidade, String nome, String tipo) {
        this.id = id;
        this.quantidade = quantidade;
        this.nome = nome;
        this.tipo = tipo;
    }

    public RegistroCondenaModel(String nome, String tipo, int quantidade) {
        this.nome = nome;
        this.tipo = tipo;
        this.quantidade = quantidade;
    }

    public RegistroCondenaModel(CondenaUnidadeResponse response) {
        this.id = response.getIdCondena();
        this.quantidade = response.getQuantidade();
        this.nome = response.getNome();
        this.tipo = response.getTipo();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setQuantidade(int novaQuantidade) {
        this.quantidade = novaQuantidade;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    protected RegistroCondenaModel(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        quantidade = in.readInt();
        nome = in.readString();
        tipo = in.readString();
    }

    public static final Creator<RegistroCondenaModel> CREATOR = new Creator<RegistroCondenaModel>() {
        @Override
        public RegistroCondenaModel createFromParcel(Parcel in) {
            return new RegistroCondenaModel(in);
        }

        @Override
        public RegistroCondenaModel[] newArray(int size) {
            return new RegistroCondenaModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeInt(quantidade);
        dest.writeString(nome);
        dest.writeString(tipo);
    }
}