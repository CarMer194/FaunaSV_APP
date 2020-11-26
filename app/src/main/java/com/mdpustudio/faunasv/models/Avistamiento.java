package com.mdpustudio.faunasv.models;

import java.util.Date;

public class Avistamiento {

    private int id_avistamiento;
    private String geom;
    private Boolean confirmado;
    private Date fecha_hora;
    private String fotografia;
    private String descripcion;
    private String usuario;
    private int animal;


    public int getId_avistamiento() {
        return id_avistamiento;
    }

    public void setId_avistamiento(int id_avistamiento) {
        this.id_avistamiento = id_avistamiento;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public Boolean getConfirmado() {
        return confirmado;
    }

    public void setConfirmado(Boolean confirmado) {
        this.confirmado = confirmado;
    }

    public Date getFecha_hora() {
        return fecha_hora;
    }

    public void setFecha_hora(Date fecha_hora) {
        this.fecha_hora = fecha_hora;
    }

    public String getFotografia() {
        return fotografia;
    }

    public void setFotografia(String fotografia) {
        this.fotografia = fotografia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }


    public int getAnimal() {
        return animal;
    }

    public void setAnimal(int animal) {
        this.animal = animal;
    }
}
