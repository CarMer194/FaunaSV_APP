package com.mdpustudio.faunasv;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mdpustudio.faunasv.models.Avistamiento;

import java.util.List;

public class ShowListFragment extends Fragment implements RecyclerViewClickInterface, AdapterView.OnItemSelectedListener {

    LinearLayout distance;
    LinearLayout animal;
    LinearLayout species;
    LinearLayout listFragment;
    Button submitfilter;
    RecyclerView myRecyclerView;
    RecyclerView.LayoutManager myLayoutManager;
    RecyclerView.Adapter myAdapter;
    RecyclerViewClickInterface recyclerViewClickInterface;

    List<Avistamiento> avistResult;

    //esta sera la url "base" en donde esta alojada nuestra API
    public static final String BASE_URL = "https://faunaelsalvador.herokuapp.com/";

    //creamos el gsonbuilder para darle formato a nuestra fecha
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

    //creamos nuestro objeto de retrofit, le enviamos la url donde esta nuestra API y el converterFactory que usaremos para obtener la info de los JSON
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    //se crea el objeto de nuestra interfaz, que es en donde accederemos a todas las funcionalidades de la API
    EndpointInterface apiService = retrofit.create(EndpointInterface.class);

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_showlist, container, false);
        listFragment = root.findViewById(R.id.list_fragment);

        Spinner filterSpinner = (Spinner)root.findViewById(R.id.filer_spinner);
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(root.getContext(), R.array.filter_options, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setOnItemSelectedListener(this);

        myRecyclerView = (RecyclerView)root.findViewById(R.id.filter_list_recyclerview);
        recyclerViewClickInterface = this;
        showAllAvistamientos();


        return root;
    }

    @Override
    public void onItemClick(int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("object", avistResult.get(position));
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, InfoFragment.class, bundle)
                .addToBackStack(null)
                .commit();
        listFragment.setVisibility(View.GONE);
        //Toast.makeText(getContext(), avistResult.get(position).getAnimal(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongItemClick(int position) {
        Toast.makeText(getContext(), avistResult.get(position).getAnimal() + " long press", Toast.LENGTH_SHORT).show();

    }

    private void showAllAvistamientos(){
        //obtenemos todos los avistamientos desde nuestra api
        Call<List<Avistamiento>> avistamientos = apiService.getAvistamientos();

        //utilizamos el enqueue para que no se haga en el main thread ya que podria crashear la app
        avistamientos.enqueue(new Callback<List<Avistamiento>>() {
            @Override
            public void onResponse(Call<List<Avistamiento>> call, Response<List<Avistamiento>> response) {      //success response

                if (!response.isSuccessful()){
                    Toast.makeText(getActivity(), "Fallo el response"+ response.code(), Toast.LENGTH_LONG).show();
                    return;
                }
                avistResult = response.body();
                myAdapter = new MyAdapter(avistResult, recyclerViewClickInterface);
                myRecyclerView.setAdapter(myAdapter);
                myLayoutManager = new LinearLayoutManager(getContext());
                myRecyclerView.setLayoutManager(myLayoutManager);

           }

            @Override
            public void onFailure(Call<List<Avistamiento>> call, Throwable t) {                                 //fail response
                Toast.makeText(getActivity(), "Hubo un error con la API "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        distance = getView().findViewById(R.id.filterDistanceTextView);
        animal = getView().findViewById(R.id.filterAnimalTextView);
        species = getView().findViewById(R.id.filterSpeciesTextView);
        submitfilter = getView().findViewById(R.id.submit_filter);
        switch (i){
            case 0:
                distance.setVisibility(View.GONE);
                distance.animate().translationX(distance.getWidth());
                animal.setVisibility(View.GONE);
                animal.animate().translationX(animal.getWidth());
                species.setVisibility(View.GONE);
                species.animate().translationX(species.getWidth());
                submitfilter.setVisibility(View.GONE);
                break;
            case 1:
                distance.setVisibility(View.VISIBLE);
                distance.animate().translationX(0);
                animal.setVisibility(View.GONE);
                animal.animate().translationX(animal.getWidth());
                species.setVisibility(View.GONE);
                species.animate().translationX(species.getWidth());
                submitfilter.setVisibility(View.VISIBLE);
                submitfilter.animate().translationX(0);
                submitfilter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                break;
            case 2:
                distance.setVisibility(View.GONE);
                distance.animate().translationX(distance.getWidth());
                animal.setVisibility(View.VISIBLE);
                animal.animate().translationX(0);
                species.setVisibility(View.GONE);
                species.animate().translationX(species.getWidth());
                submitfilter.setVisibility(View.VISIBLE);
                submitfilter.animate().translationX(0);
                break;
            case 3:
                distance.setVisibility(View.GONE);
                distance.animate().translationX(distance.getWidth());
                animal.setVisibility(View.GONE);
                animal.animate().translationX(animal.getWidth());
                species.setVisibility(View.VISIBLE);
                species.animate().translationX(0);
                submitfilter.setVisibility(View.VISIBLE);
                submitfilter.animate().translationX(0);
                break;
            case 4:
                distance.setVisibility(View.VISIBLE);
                distance.animate().translationX(0);
                animal.setVisibility(View.VISIBLE);
                animal.animate().translationX(0);
                species.setVisibility(View.VISIBLE);
                species.animate().translationX(0);
                submitfilter.setVisibility(View.VISIBLE);
                submitfilter.animate().translationX(0);
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}