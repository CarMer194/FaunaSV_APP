package com.mdpustudio.faunasv.models;

public class Animal {

    private String nombre_local;
    private String estacionalidad;
    private Especie_Animal especie;



    public String getNombre_local() {
        return nombre_local;
    }

    public void setNombre_local(String nombre_local) {
        this.nombre_local = nombre_local;
    }

    public String getEstacionalidad() {
        return estacionalidad;
    }

    public void setEstacionalidad(String estacionalidad) {
        this.estacionalidad = estacionalidad;
    }

    public Especie_Animal getEspecie() {
        return especie;
    }

    public void setEspecie(Especie_Animal especie) {
        this.especie = especie;
    }
}
