package com.mdpustudio.faunasv.models;

public class Especie_Animal {

    private int id_especie_animal;
    private String nombre_especie_animal;
    private Familia_Animal familia_animal;


    public int getId_especie_animal() {
        return id_especie_animal;
    }

    public void setId_especie_animal(int id_especie_animal) {
        this.id_especie_animal = id_especie_animal;
    }

    public String getNombre_especie_animal() {
        return nombre_especie_animal;
    }

    public void setNombre_especie_animal(String nombre_especie_animal) {
        this.nombre_especie_animal = nombre_especie_animal;
    }

    public Familia_Animal getFamilia_animal() {
        return familia_animal;
    }

    public void setFamilia_animal(Familia_Animal familia_animal) {
        this.familia_animal = familia_animal;
    }
}
