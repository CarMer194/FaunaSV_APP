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
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
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
        avist = (Avistamiento) requireArguments().getSerializable("object");
        super.onViewCreated(view, savedInstanceState);

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

        animalName.setText(avist.getAnimal());
        coords.setText(getCoords(avist.getGeom()));
        desc.setText(avist.getDescripcion());
        user.setText(avist.getUsuario());
        date.setText(getDateAvist(avist.getFecha_hora()));
        new ImageLoad(avist.getFotografia(),animalImage).execute();

        String test = getActivity().getPreferences(Context.MODE_PRIVATE).getString("LocationPoint", null);

        Call<Double> distanceMt = apiService.getDistanceByPoints(avist.getGeom(), test);
        distanceMt.enqueue(new Callback<Double>() {
            @Override
            public void onResponse(@NotNull Call<Double> call, @NotNull Response<Double> response) {
                if (!response.isSuccessful()){
                    Toast.makeText(getActivity(), "Fallo el response "+ response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                double mt = response.body();
                int intvalue = (int)mt;
                String resultado = intvalue+"m";
                distance.setText(resultado);
            }

            @Override
            public void onFailure(@NotNull Call<Double> call, @NotNull Throwable t) {
                Toast.makeText(getActivity(), "Error "+ t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Bundle bundle = new Bundle();
        bundle.putSerializable("objectCoords", avist);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, MapFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

    public String getCoords(String st){
        String[] dataStr = st.split("\\(");
        String geoData = dataStr[1].substring(0,dataStr[1].length() -1);
        String[] latLong = geoData.split(" ");
        return latLong[1]+" "+latLong[0];


    }

    public String getDateAvist(Date date){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        return dateFormat.format(date);

    }
}