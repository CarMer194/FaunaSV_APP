package com.mdpustudio.faunasv;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mdpustudio.faunasv.models.Avistamiento;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    //esta sera la url "base" en donde esta alojada nuestra API
    public static final String BASE_URL = "https://faunaelsalvador.herokuapp.com/";

    private boolean permissionDenied = false;

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

    private GoogleMap map;
    Avistamiento currentAvist;
    Boolean flag = false;
    CoordinatorLayout mapcontainer;
    double longitude;
    double latitude;
    String point;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */



        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            map.setMinZoomPreference(8.0f);
            map.setMaxZoomPreference(21.0f);

            if (flag){
                showAvistamiento(currentAvist);
            }else {
                showAllAvistamientos();
            }
            enableMyLocation();
        }
    };

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if ( map != null) {
                map.setMyLocationEnabled(true);
                LocationManager lm = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
                assert lm != null;
                @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                assert location != null;
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                LatLng currentLocation = new LatLng(latitude, longitude);
                point = "SRID=4326;POINT ("+longitude+" "+latitude+")";
                SharedPreferences mPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                prefsEditor.putString("LocationPoint", point);
                prefsEditor.apply();

                if (!flag) {
                    map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                }
            }
        } else {
            /* Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);*/
            Toast.makeText(getActivity(), "Permiso incorrecto", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        mapcontainer = root.findViewById(R.id.mapcontainer);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("objectCoords")){
            flag = true;
            currentAvist = (Avistamiento) requireArguments().getSerializable("objectCoords");
        }
        super.onViewCreated(view, savedInstanceState);

        SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        model.getSelected().observe(getViewLifecycleOwner(), this::showAvistamiento);

        FloatingActionButton filterDist = getActivity().findViewById(R.id.distancefilterbutton);
        filterDist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setTitle("Filtrar");
                alert.setMessage("Distancia (m):");
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                alert.setView(input);
                alert.setPositiveButton("Filtrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getActivity(), point+" Metros: " + input.getText(), Toast.LENGTH_SHORT).show();
                        showDistanceAvistamiento(point, String.valueOf(input.getText()));
                    }
                });

                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                alert.show();

            }
        });

        FloatingActionButton addAvistamineto = getActivity().findViewById(R.id.resetMapButton);
        addAvistamineto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.clear();
                map.moveCamera(CameraUpdateFactory.zoomTo(8.0F));
                showAllAvistamientos();
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void showAllAvistamientos(){
        //obtenemos todos los avistamientos desde nuestra api
        Call<List<Avistamiento>> avistamientos = apiService.getAvistamientos();

        //utilizamos el enqueue para que no se haga en el main thread ya que podria crashear la app
        avistamientos.enqueue(new Callback<List<Avistamiento>>() {
            @Override
            public void onResponse(Call<List<Avistamiento>> call, Response<List<Avistamiento>> response) {      //success response

                //se verifica que el response fue exitoso verdaderamente
                if (!response.isSuccessful()){
                    Toast.makeText(getActivity(), "Fallo el response"+ response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                //obtenemos la lista de objetos JAVA que la API retorno
                List<Avistamiento> avistResult = response.body();

                //empezamos a mostrar todos los markers de avistamientos en el mapa
                for (int i=0; i<avistResult.size();i++){
                    String geom = avistResult.get(i).getGeom();                                                //obtenemos el string de la geometria
                    String[] dataStr = geom.split("\\(");                                               //partimos el string para obtener solo la latitud y longitud
                    String geoData = dataStr[1].substring(0,dataStr[1].length() -1);                           //borramos el parentesis del final
                    String[] latLong = geoData.split(" ");

                    LatLng marker = new LatLng(Double.parseDouble(latLong[1]), Double.parseDouble(latLong[0])); //hacemos un objeto que tendra la informacion de la latitud y longitud
                    map.addMarker(new MarkerOptions()                                                           //creamos el marcador en el mapa, le enviamos la position y el titulo que debe de tener
                            .position(marker)
                            .draggable(true)
                            .title(avistResult.get(i).getAnimal()+"")
                            .snippet(avistResult.get(i).getUsuario()));
                }
                map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                    @Override
                    public void onInfoWindowLongClick(Marker marker) {
                        for (int i=0; i<avistResult.size();i++){
                            String geom = avistResult.get(i).getGeom();                                                //obtenemos el string de la geometria
                            String[] dataStr = geom.split("\\(");                                               //partimos el string para obtener solo la latitud y longitud
                            String geoData = dataStr[1].substring(0,dataStr[1].length() -1);                           //borramos el parentesis del final
                            String[] latLong = geoData.split(" ");

                            LatLng pos = new LatLng(Double.parseDouble(latLong[1]), Double.parseDouble(latLong[0])); //hacemos un objeto que tendra la informacion de la latitud y longitud
                            if (marker.getTitle().equals(avistResult.get(i).getAnimal()) && marker.getPosition().equals(pos)){
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("object", avistResult.get(i));
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.nav_host_fragment, InfoFragment.class, bundle)
                                        .addToBackStack(null)
                                        .commit();
                                mapcontainer.setVisibility(View.GONE);
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Avistamiento>> call, Throwable t) {                                 //fail response
                Toast.makeText(getActivity(), "Hubo un error con la API "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    public void showAvistamiento(Avistamiento item){
        String geom = item.getGeom();                                                               //obtenemos el string de la geometria
        String[] dataStr = geom.split("\\(");                                                //partimos el string para obtener solo la latitud y longitud
        String geoData = dataStr[1].substring(0,dataStr[1].length() -1);                            //borramos el parentesis del final
        String[] latLong = geoData.split(" ");

        LatLng marker = new LatLng(Double.parseDouble(latLong[1]), Double.parseDouble(latLong[0])); //hacemos un objeto que tendra la informacion de la latitud y longitud
        map.addMarker(new MarkerOptions()                                                           //creamos el marcador en el mapa, le enviamos la position y el titulo que debe de tener
                .position(marker)
                .draggable(true)
                .title(item.getAnimal())
                .snippet(item.getUsuario()));
        map.moveCamera(CameraUpdateFactory.newLatLng(marker));
        map.moveCamera(CameraUpdateFactory.zoomTo(14.0F));

        map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("object", item);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, InfoFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
                mapcontainer.setVisibility(View.GONE);

            }
        });
    }

    public void showDistanceAvistamiento(String point, String distance){
        map.clear();
        Call<List<Integer>> idavistamientos = apiService.getAvistamientosByDistance(point, distance);
        //Toast.makeText(getActivity(), "getcercanos/point="+point+"&m="+distance, Toast.LENGTH_SHORT).show();
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

                //Toast.makeText(getActivity(), auxx, Toast.LENGTH_SHORT).show();

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
                        map.clear();
                        LatLng currentLocation = new LatLng(latitude, longitude);
                        Circle circle = map.addCircle(new CircleOptions()
                        .center(currentLocation)
                        .radius(Integer.parseInt(distance))
                        .strokeColor(Color.BLUE)
                        .fillColor(Color.TRANSPARENT));
                        for (int i = 0; i < filteredResult.size(); i++) {
                            String geom = filteredResult.get(i).getGeom();                                                //obtenemos el string de la geometria
                            String[] dataStr = geom.split("\\(");                                               //partimos el string para obtener solo la latitud y longitud
                            String geoData = dataStr[1].substring(0, dataStr[1].length() - 1);                           //borramos el parentesis del final
                            String[] latLong = geoData.split(" ");

                            LatLng marker = new LatLng(Double.parseDouble(latLong[1]), Double.parseDouble(latLong[0])); //hacemos un objeto que tendra la informacion de la latitud y longitud
                            map.addMarker(new MarkerOptions()                                                           //creamos el marcador en el mapa, le enviamos la position y el titulo que debe de tener
                                    .position(marker)
                                    .draggable(true)
                                    .title(filteredResult.get(i).getAnimal() + "")
                                    .snippet(filteredResult.get(i).getUsuario()));
                        }

                        map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                            @Override
                            public void onInfoWindowLongClick(Marker marker) {
                                for (int i = 0; i < filteredResult.size(); i++) {
                                    String geom = filteredResult.get(i).getGeom();                                                //obtenemos el string de la geometria
                                    String[] dataStr = geom.split("\\(");                                               //partimos el string para obtener solo la latitud y longitud
                                    String geoData = dataStr[1].substring(0, dataStr[1].length() - 1);                           //borramos el parentesis del final
                                    String[] latLong = geoData.split(" ");

                                    LatLng pos = new LatLng(Double.parseDouble(latLong[1]), Double.parseDouble(latLong[0])); //hacemos un objeto que tendra la informacion de la latitud y longitud
                                    if (marker.getTitle().equals(filteredResult.get(i).getAnimal()) && marker.getPosition().equals(pos)) {
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("object", filteredResult.get(i));
                                        getActivity().getSupportFragmentManager().beginTransaction()
                                                .replace(R.id.nav_host_fragment, InfoFragment.class, bundle)
                                                .addToBackStack(null)
                                                .commit();
                                        mapcontainer.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<Avistamiento>> call, Throwable t) {

                    }
                });

            }

            @Override
            public void onFailure(Call<List<Integer>> call, Throwable t) {                                 //fail response
                Toast.makeText(getActivity(), "Hubo un error con la API "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}