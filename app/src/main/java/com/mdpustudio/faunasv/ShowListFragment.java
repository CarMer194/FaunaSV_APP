package com.mdpustudio.faunasv;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mdpustudio.faunasv.models.Avistamiento;

import java.util.ArrayList;
import java.util.List;

//fragmento que mostrara la lista de avistamientos, esta implementa la interface de recyclerviewclickinterface para poder comunicarse con el adapter del recycler view
public class ShowListFragment extends Fragment implements RecyclerViewClickInterface, AdapterView.OnItemSelectedListener {

    LinearLayout distance;
    LinearLayout animal;
    LinearLayout usuario;
    LinearLayout listFragment;
    TextInputEditText filterDistance;
    TextInputEditText filterAnimal;
    TextInputEditText filterUsuario;
    Button submitfilter;
    RecyclerView myRecyclerView;
    RecyclerView.LayoutManager myLayoutManager;
    RecyclerView.Adapter myAdapter;
    RecyclerViewClickInterface recyclerViewClickInterface;
    String location;

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
        //se ligan nuestros objetos con sus respectivas views
        listFragment = root.findViewById(R.id.list_fragment);
        filterAnimal = root.findViewById(R.id.edit_text_filter_animal);
        filterDistance = root.findViewById(R.id.edit_text_filter_distance);
        filterUsuario = root.findViewById(R.id.edit_text_filter_usuario);
        Spinner filterSpinner = (Spinner)root.findViewById(R.id.filer_spinner);
        //se crea el adaptador para nuestro spinner
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(root.getContext(), R.array.filter_options, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setOnItemSelectedListener(this);

        //se liga nuestro objeto con la view del recyclerview
        myRecyclerView = (RecyclerView)root.findViewById(R.id.filter_list_recyclerview);
        recyclerViewClickInterface = this;

        //se obtiene la locacion actual guardada en el shared preferences
        location = getActivity().getPreferences(Context.MODE_PRIVATE).getString("LocationPoint", null);

        //se muestran todos los avistamientos
        showAllAvistamientos();


        return root;
    }

    @Override
    public void onItemClick(int position) { //metodo implementado de la interface, para poder obtener el objeto seleccionado
        //se crea un objeto bundle para poder enviar el objeto seleccionado en el cambio de fragmento
        Bundle bundle = new Bundle();
        bundle.putSerializable("object", avistResult.get(position));
        //hacemos el cambio de fragmento
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, InfoFragment.class, bundle)
                .addToBackStack(null)
                .commit();
        listFragment.setVisibility(View.GONE);
    }

    @Override
    public void onLongItemClick(int position) {//no se utiliza
    }

    private void showAllAvistamientos(){
        //obtenemos todos los avistamientos desde nuestra api
        Call<List<Avistamiento>> avistamientos = apiService.getAvistamientos();

        //utilizamos el enqueue para que no se haga en el main thread ya que podria crashear la app
        avistamientos.enqueue(new Callback<List<Avistamiento>>() {
            @Override
            public void onResponse(Call<List<Avistamiento>> call, Response<List<Avistamiento>> response) {      //success response

                if (!response.isSuccessful()){
                    Toast.makeText(getActivity(), "Fallo el response "+ response.code(), Toast.LENGTH_LONG).show();
                    return; //se verifica si fue successful y si no lo fue se muestra el codigo de error
                }
                //se guarda el resultado en el objeto de avistResult
                avistResult = response.body();
                //se crea un nuevo adaptador del recyclerview y se le setea a la view del recyclerview, asi como tambien su respectivo linearlayoutmanager
                myAdapter = new MyAdapter(avistResult, recyclerViewClickInterface);
                myRecyclerView.setAdapter(myAdapter);
                myLayoutManager = new LinearLayoutManager(getContext());
                myRecyclerView.setLayoutManager(myLayoutManager);

           }

            @Override
            public void onFailure(Call<List<Avistamiento>> call, Throwable t) {                                 //fail response
                Toast.makeText(getActivity(), "Hubo un error con la API "+t.getMessage(), Toast.LENGTH_LONG).show(); //se da el mensaje de error si no se da un response
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        //se conectan los objetos con sus respectivas views
        distance = getView().findViewById(R.id.filterDistanceTextView);
        animal = getView().findViewById(R.id.filterAnimalTextView);
        usuario = getView().findViewById(R.id.filterSpeciesTextView);
        submitfilter = getView().findViewById(R.id.submit_filter);
        //se hace un switch que permitira filtrar la busqueda dependiendo de lo que el usuario requiera
        switch (i){
            case 0: //no se filtra nada
                distance.setVisibility(View.GONE);
                distance.animate().translationX(distance.getWidth());
                animal.setVisibility(View.GONE);
                animal.animate().translationX(animal.getWidth());
                usuario.setVisibility(View.GONE);
                usuario.animate().translationX(usuario.getWidth());
                submitfilter.setVisibility(View.GONE);
                break;
            case 1: //filtrado por distancia
                distance.setVisibility(View.VISIBLE);
                distance.animate().translationX(0);
                animal.setVisibility(View.GONE);
                animal.animate().translationX(animal.getWidth());
                usuario.setVisibility(View.GONE);
                usuario.animate().translationX(usuario.getWidth());
                submitfilter.setVisibility(View.VISIBLE);
                submitfilter.animate().translationX(0);
                submitfilter.setOnClickListener(new View.OnClickListener() { //cuando se le da submit se hace toda la conexion con la API para obtener los avistamientos a una determinada distancia.
                    @Override
                    public void onClick(View view) { //mismo codigo que en el mapfragment
                        Call<List<Integer>> idavistamientos = apiService.getAvistamientosByDistance(location, String.valueOf(filterDistance.getText()));
                        idavistamientos.enqueue(new Callback<List<Integer>>() {
                            @Override
                            public void onResponse(Call<List<Integer>> call, Response<List<Integer>> response) {      //success response

                                if (!response.isSuccessful()){
                                    Toast.makeText(getActivity(), "Fallo el response "+ response.code(), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                List<Integer> idavistResult = response.body();

                                String auxx = "";
                                for (int i=0; i<idavistResult.size(); i++){
                                    auxx += idavistResult.get(i).toString() + " ";
                                }

                                Call<List<Avistamiento>> allavistamientos = apiService.getAvistamientos();
                                allavistamientos.enqueue(new Callback<List<Avistamiento>>() {
                                    @Override
                                    public void onResponse(Call<List<Avistamiento>> call, Response<List<Avistamiento>> response) {
                                        if (!response.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Fallo el response " + response.code(), Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        List<Avistamiento> avistResult = response.body();
                                        List<Avistamiento> filteredResult = new ArrayList<Avistamiento>();

                                        for (int i = 0; i < idavistResult.size(); i++) {
                                            for (int j = 0; j < avistResult.size(); j++) {
                                                if (avistResult.get(j).getId_avistamiento() == idavistResult.get(i)) {
                                                    filteredResult.add(avistResult.get(j));
                                                }
                                            }
                                        }

                                        //se adaptan los resultados obtenidos y se le agregan al recyclerview
                                        myAdapter = new MyAdapter(filteredResult, recyclerViewClickInterface);
                                        myRecyclerView.setAdapter(myAdapter);
                                        myLayoutManager = new LinearLayoutManager(getContext());
                                        myRecyclerView.setLayoutManager(myLayoutManager);

                                    }

                                    @Override
                                    public void onFailure(Call<List<Avistamiento>> call, Throwable t) {
                                        Toast.makeText(getActivity(), "Error: "+ t.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });

                            }

                            @Override
                            public void onFailure(Call<List<Integer>> call, Throwable t) {                                 //fail response
                                Toast.makeText(getActivity(), "Hubo un error con la API "+t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                break;
            case 2: //filtrado por animal
                distance.setVisibility(View.GONE);
                distance.animate().translationX(distance.getWidth());
                animal.setVisibility(View.VISIBLE);
                animal.animate().translationX(0);
                usuario.setVisibility(View.GONE);
                usuario.animate().translationX(usuario.getWidth());
                submitfilter.setVisibility(View.VISIBLE);
                submitfilter.animate().translationX(0);
                submitfilter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { //se hace la conexion a la api de la misma menera que se ha hecho pero ahora se le envia al endpoint que nos retorna los avistamientos filtrados por animal
                        Call<List<Avistamiento>> filterList = apiService.getAvistamientoByUserorAnimal(String.valueOf(filterAnimal.getText()));
                        filterList.enqueue(new Callback<List<Avistamiento>>() {
                            @Override
                            public void onResponse(Call<List<Avistamiento>> call, Response<List<Avistamiento>> response) {
                                if (!response.isSuccessful()){
                                    Toast.makeText(getActivity(), "Fallo el response"+ response.code(), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                //se adaptan los resultados obtenidos y se le agregan al recyclerview
                                avistResult = response.body();
                                myAdapter = new MyAdapter(avistResult, recyclerViewClickInterface);
                                myRecyclerView.setAdapter(myAdapter);
                                myLayoutManager = new LinearLayoutManager(getContext());
                                myRecyclerView.setLayoutManager(myLayoutManager);
                            }

                            @Override
                            public void onFailure(Call<List<Avistamiento>> call, Throwable t) {
                                Toast.makeText(getActivity(), "Error: "+ t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                break;
            case 3: //filtrado por usuario
                distance.setVisibility(View.GONE);
                distance.animate().translationX(distance.getWidth());
                animal.setVisibility(View.GONE);
                animal.animate().translationX(animal.getWidth());
                usuario.setVisibility(View.VISIBLE);
                usuario.animate().translationX(0);
                submitfilter.setVisibility(View.VISIBLE);
                submitfilter.animate().translationX(0);
                submitfilter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {//se hace la conexion a la api de la misma menera que se ha hecho pero ahora se le envia al endpoint que nos retorna los avistamientos filtrados por usuario
                        Call<List<Avistamiento>> filterList = apiService.getAvistamientoByUserorAnimal(String.valueOf(filterUsuario.getText()));
                        filterList.enqueue(new Callback<List<Avistamiento>>() {
                            @Override
                            public void onResponse(Call<List<Avistamiento>> call, Response<List<Avistamiento>> response) {
                                if (!response.isSuccessful()){
                                    Toast.makeText(getActivity(), "Fallo el response"+ response.code(), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                //se adaptan los resultados obtenidos y se le agregan al recyclerview
                                avistResult = response.body();
                                myAdapter = new MyAdapter(avistResult, recyclerViewClickInterface);
                                myRecyclerView.setAdapter(myAdapter);
                                myLayoutManager = new LinearLayoutManager(getContext());
                                myRecyclerView.setLayoutManager(myLayoutManager);
                            }

                            @Override
                            public void onFailure(Call<List<Avistamiento>> call, Throwable t) {
                                Toast.makeText(getActivity(), "Error: "+ t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}