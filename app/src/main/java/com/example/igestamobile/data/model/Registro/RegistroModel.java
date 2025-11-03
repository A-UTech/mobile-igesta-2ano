package com.example.igestamobile.data.model.Registro;

import java.util.List;

public class RegistroModel {
    private String gestor;
    private String empresa;
    private Integer idTurno;
    private String data;
    private String lote;
    private String unidade;
    private List<RegistroCondenaModel> condenas;

    public RegistroModel(String gestor, String empresa, Integer idTurno, String data, String lote, String unidade, List<RegistroCondenaModel> condenas) {
        this.gestor = gestor;
        this.empresa = empresa;
        this.idTurno = idTurno;
        this.data = data;
        this.lote = lote;
        this.unidade = unidade;
        this.condenas = condenas;
    }

    public String getGestor() {
        return gestor;
    }

    public void setGestor(String gestor) {
        this.gestor = gestor;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public Integer getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(Integer idTurno) {
        this.idTurno = idTurno;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public List<RegistroCondenaModel> getCondenas() {
        return condenas;
    }

    public void setCondenas(List<RegistroCondenaModel> condenas) {
        this.condenas = condenas;
    }
}
