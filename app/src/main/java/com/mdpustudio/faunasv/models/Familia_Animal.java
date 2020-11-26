package com.mdpustudio.faunasv.models;

public class Familia_Animal {

    private int id_familia_animal;
    private String nombre_familia_animal;
    private Grupo_Animal grupo_animal;


    public int getId_familia_animal() {
        return id_familia_animal;
    }

    public void setId_familia_animal(int id_familia_animal) {
        this.id_familia_animal = id_familia_animal;
    }

    public String getNombre_familia_animal() {
        return nombre_familia_animal;
    }

    public void setNombre_familia_animal(String nombre_familia_animal) {
        this.nombre_familia_animal = nombre_familia_animal;
    }

    public Grupo_Animal getGrupo_animal() {
        return grupo_animal;
    }

    public void setGrupo_animal(Grupo_Animal grupo_animal) {
        this.grupo_animal = grupo_animal;
    }
}
