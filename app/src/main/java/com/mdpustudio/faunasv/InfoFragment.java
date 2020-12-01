package com.mdpustudio.faunasv;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mdpustudio.faunasv.models.Avistamiento;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    public static final String BASE_URL = "https://faunaelsalvador.herokuapp.com/";

    private String mParam1;
    private String mParam2;
    private Avistamiento avist;
    TextView animalName;
    TextView coords;
    TextView desc;
    TextView user;
    TextView date;
    TextView distance;
    Button mapButton;
    Button editButton;
    ImageView animalImage;

    //objeto Retrofit que nos permitira conectarnos con la API, a este se le envia la URL de nuestra API y con que herramienta usaremos para consumir los Json, en este caso sera Gson
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    //creame nuestro objeto de la interface que se conectara con la API y la igualamos a nuestro objeto retrofit.
    EndpointInterface apiService = retrofit.create(EndpointInterface.class);



    public InfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InfoFragment newInstance(String param1, String param2) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Se obtiene el objeto de avistamiento que se envio desde otro fragmento o actividad
        avist = (Avistamiento) requireArguments().getSerializable("object");
        super.onViewCreated(view, savedInstanceState);

        //asignamos a nuestras variables sus view correspondientes
        animalName = view.findViewById(R.id.info_animalname_textview);
        coords = view.findViewById(R.id.info_coords_textview);
        desc = view.findViewById(R.id.info_desc_textview);
        user = view.findViewById(R.id.info_user_textview);
        date = view.findViewById(R.id.info_date_textview);
        distance = view.findViewById(R.id.info_distance_textview);
        mapButton = view.findViewById(R.id.button_map);
        editButton = view.findViewById(R.id.button_edit);
        animalImage = view.findViewById(R.id.info_imageview);

        editButton.setVisibility(View.GONE);

        //se setea la informacion correspondiente a cada textview
        animalName.setText(avist.getAnimal());
        coords.setText(getCoords(avist.getGeom()));
        desc.setText(avist.getDescripcion());
        user.setText(avist.getUsuario());
        date.setText(getDateAvist(avist.getFecha_hora()));
        //cargamos nuestra imagen desde una URL en el API, le enviamos el String de la URL y el ImageView en donde queremos colocar nuestra imagen
        new ImageLoad(avist.getFotografia(),animalImage).execute();

        //obtenemos la locacion actual con las shared preferences
        String location = getActivity().getPreferences(Context.MODE_PRIVATE).getString("LocationPoint", null);

        //Usamos el objeto Call para poder hacer un enqueue del endpoint que queremos utilizar, en este caso encontrar la distancia de un punto a otro
        Call<Double> distanceMt = apiService.getDistanceByPoints(avist.getGeom(), location);
        //hacemos un enqueue de nuestro objeto call para que no corre en el main thread, ya que podria crashear la app
        distanceMt.enqueue(new Callback<Double>() {
            @Override
            public void onResponse(@NotNull Call<Double> call, @NotNull Response<Double> response) {    //response success
                if (!response.isSuccessful()){  //verificamos si el response fue un response successful
                    Toast.makeText(getActivity(), "Fallo el response "+ response.code(), Toast.LENGTH_LONG).show();
                    return; //si el response falla mostramos el codigo de error y retornamos
                }

                //obtenemos la distancia con el response.body()
                double mt = response.body();
                //pasamos el double a int para solo mostrar el valor entero
                int intvalue = (int)mt;
                //seteamos el resultado de la distancia en metros
                String resultado = intvalue+"m";
                distance.setText(resultado);
            }

            @Override
            public void onFailure(@NotNull Call<Double> call, @NotNull Throwable t) {                   //response fail
                Toast.makeText(getActivity(), "Error "+ t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //se crea un objeto Bundle para poder enviar informacion extra, en este caso las coordenadas del objeto seleccionado para que se muestren en el mapa
        Bundle bundle = new Bundle();
        bundle.putSerializable("objectCoords", avist);
        //se hace el click listener para ver el punto en el mapa
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //se cambia de fragmento y se pone la informacion extra de la locacion
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, MapFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

    //metodo para poder obtener las coordenadas de una manera entendible para el usuario
    public String getCoords(String st){
        String[] dataStr = st.split("\\(");
        String geoData = dataStr[1].substring(0,dataStr[1].length() -1);
        String[] latLong = geoData.split(" ");
        return latLong[1]+" "+latLong[0];
    }

    //metodo para poder obtener el formato de fecha correcto para mostrarlo en el textview
    public String getDateAvist(Date date){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        return dateFormat.format(date);

    }
}