package com.mdpustudio.faunasv;

//interfaz para comunicacion del recyclerview y el fragmento
public interface RecyclerViewClickInterface {

    //metodos con los que se estaran obteniendo y enviando la informacion
    void onItemClick(int position);
    void onLongItemClick(int position);

}
