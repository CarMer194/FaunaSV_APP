package com.mdpustudio.faunasv;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mdpustudio.faunasv.models.Avistamiento;


import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

//este es el adaptador para el recyclerview
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    List<Avistamiento> avist;
    //creamos una interface del recyclerview para que este se pueda comunicar con el fragmento
    private static RecyclerViewClickInterface recyclerViewClickInterface;

    //creamos el constructor
    public MyAdapter(List<Avistamiento> avist, RecyclerViewClickInterface recyclerViewClickInterface){
        this.avist = avist;
        this.recyclerViewClickInterface = recyclerViewClickInterface;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_filter_item, parent, false);
        ViewHolder pvh = new ViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //seteamos el texto indicado a cada TextView
        holder.animalName.setText(avist.get(position).getAnimal());
        holder.animalDesc.setText(avist.get(position).getDescripcion());
        new ImageLoad(avist.get(position).getFotografia(), holder.avistPhoto).execute();
        holder.username.setText(avist.get(position).getUsuario());
    }

    @Override
    public int getItemCount() {
        return avist.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //creamos los objetos de nuestras views
        CardView cv;
        TextView animalName;
        TextView animalDesc;
        TextView username;
        ImageView avistPhoto;

        public ViewHolder(View itemView) {
            super(itemView);
            //ligamos nuestros objetos a sus respectivas views
            cv = (CardView)itemView.findViewById(R.id.cardview_item);
            animalName = (TextView)itemView.findViewById(R.id.filter_animalname_textview);
            animalDesc = (TextView)itemView.findViewById(R.id.filter_animaldesc_textview);
            username = (TextView)itemView.findViewById(R.id.filter_user_textview);
            avistPhoto = (ImageView)itemView.findViewById(R.id.filter_image);

            //creamos el click listener en cada elemento para que se pueda obtener su position
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerViewClickInterface.onItemClick(getAdapterPosition());
                }
            });

            //creamos el long click listener en cada elemento para que se pueda obtener su position
            itemView.setOnLongClickListener((view) -> {

                recyclerViewClickInterface.onLongItemClick(getAdapterPosition());
                return true;
            });
        }
    }



}
