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

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    List<Avistamiento> avist;
    private static RecyclerViewClickInterface recyclerViewClickInterface;

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
        CardView cv;
        TextView animalName;
        TextView animalDesc;
        TextView username;
        ImageView avistPhoto;

        public ViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cardview_item);
            animalName = (TextView)itemView.findViewById(R.id.filter_animalname_textview);
            animalDesc = (TextView)itemView.findViewById(R.id.filter_animaldesc_textview);
            username = (TextView)itemView.findViewById(R.id.filter_user_textview);
            avistPhoto = (ImageView)itemView.findViewById(R.id.filter_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerViewClickInterface.onItemClick(getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener((view) -> {

                recyclerViewClickInterface.onLongItemClick(getAdapterPosition());
                return true;
            });
        }
    }



}
