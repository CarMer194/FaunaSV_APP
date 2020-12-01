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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    FloatingActionButton addAvistamiento;
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
            //ponemos algunas preferencias para el zoom del mapa
            map.setMinZoomPreference(8.0f);
            map.setMaxZoomPreference(21.0f);

            if (flag){
                //si nos envian un avistamiento en especifico desde el fragmento de informacion, lo mostramos
                showAvistamiento(currentAvist);
            }else {
                //si no se encuentra ningun avistamiento enviado desde el fragmento informacion se muestran todos
                showAllAvistamientos();
            }
            //metodo para habilitar la locacion en el mapa
            enableMyLocation();

            //obtenemos la locacion y el username de las shared preferences
            String locationPoint = getActivity().getPreferences(Context.MODE_PRIVATE).getString("LocationPoint", null);
            String usernameshared = getActivity().getPreferences(Context.MODE_PRIVATE).getString("usernameglobal", null);
            //se obtiene la fecha actual
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
            Date date = new Date();
            String dateString = formatter.format(date);
            //creames el click listener para agregar un nuevo avistamiento
            addAvistamiento.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //se hace un intent al formulario en linea para poder agregar el avistamiento, se le agrega el punto donde se esta ubicado y el nombre de usuario
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://faunaelsalvador.herokuapp.com/avistamientoform/point="+locationPoint+"&user="+usernameshared+"&date="+dateString));
                    getActivity().startActivity(browserIntent);
                }
            });
        }
    };

    private void enableMyLocation() {
        //se verifica si la app tiene permiso de gps
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if ( map != null) { //usamos map!= null para verificar que el mapa ya este listo
                //activamos la flag para poder obtener nuestra locacion
                map.setMyLocationEnabled(true);
                //hacemos nuestro objeto para poder tener nuestra locacion
                LocationManager lm = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
                assert lm != null;
                //obtenemos la locacion desde gps
                @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                assert location != null;
                //obtenemos la longitud y latitud de nuestra locacion
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                //creamos el objeto de latitud y longitud para usarlo con el mapa
                LatLng currentLocation = new LatLng(latitude, longitude);
                //guardamos el punto como se necesita en la API en shared preferences para que pueda usarse en toda la actividad
                point = "SRID=4326;POINT ("+longitude+" "+latitude+")";
                SharedPreferences mPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                prefsEditor.putString("LocationPoint", point);
                prefsEditor.apply();

                if (!flag) {
                    //se mueve la camara a donde esta el usuario si este no ha recibido un avistamiento desde el InfoFragment
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
        addAvistamiento = root.findViewById(R.id.addAvistButton);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //hacemos nuestro objeto de tipo bundle para obtener la informacion extra que se nos envio
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("objectCoords")){ //verificamos si viene tenemos argumentos que obtener
            flag = true; //activamos la flag para decir que si tenemos un avistamiento que se tiene que mostrar
            currentAvist = (Avistamiento) requireArguments().getSerializable("objectCoords"); //se obtiene el objeto que se quiere ver
        }
        super.onViewCreated(view, savedInstanceState);


        SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        model.getSelected().observe(getViewLifecycleOwner(), this::showAvistamiento);

        //obtenemos el objeto del boton flotante
        FloatingActionButton filterDist = getActivity().findViewById(R.id.distancefilterbutton);
        //le ponemos un click listener a nuestro boton flotante que nos permitira filtrar por distancia en el mapa
        filterDist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creamos nuestro alert dialog para que el usuario pueda filtrar por distancia
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                //ponemos las settings de nuestro alert dialog
                alert.setTitle("Filtrar");
                alert.setMessage("Distancia (m):");
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                alert.setView(input);
                //ponemos el boton para una respuesta positiva, en este caso para filtrar por distancia
                alert.setPositiveButton("Filtrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(getActivity(), point+" Metros: " + input.getText(), Toast.LENGTH_SHORT).show();
                        //llamamos a nuestro metodo para mostrar avistamientos por distancia y le mandamos la distancia que el usuario puso
                        showDistanceAvistamiento(point, String.valueOf(input.getText()));
                    }
                });
                //ponemos el boton para una respuesta negativa, en este caso para regresar al mapa
                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                //se muestra la alerta
                alert.show();

            }
        });

        //obtenemos el objeto del boton flotante
        FloatingActionButton resetView = getActivity().findViewById(R.id.resetMapButton);
        //ponemos el click listener que va a resetear el mapa
        resetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.clear(); //se limpia el mapa de los marcadores
                //se hace un zoom a el zoom determinado
                map.moveCamera(CameraUpdateFactory.zoomTo(8.0F));
                //se vuelven a mostrar todos los avistamientos
                showAllAvistamientos();
            }
        });

        //se coloca el mapa en el fragmento
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    //muestra todos los avistamientos
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
                //se hace un click listener del info windows para poder abrir el fragmento de informacion del avistamiento
                map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                    @Override
                    public void onInfoWindowLongClick(Marker marker) { //usamos un long click para abrir la ventana de informacion del avistamiento
                        for (int i=0; i<avistResult.size();i++){
                            String geom = avistResult.get(i).getGeom();                                                //obtenemos el string de la geometria
                            String[] dataStr = geom.split("\\(");                                               //partimos el string para obtener solo la latitud y longitud
                            String geoData = dataStr[1].substring(0,dataStr[1].length() -1);                           //borramos el parentesis del final
                            String[] latLong = geoData.split(" ");

                            LatLng pos = new LatLng(Double.parseDouble(latLong[1]), Double.parseDouble(latLong[0])); //hacemos un objeto que tendra la informacion de la latitud y longitud
                            //verificamos si el nombre del animal y la posicion son iguales tanto en el objeto actual como en el del marcador
                            if (marker.getTitle().equals(avistResult.get(i).getAnimal()) && marker.getPosition().equals(pos)){
                                //se crea un objeto de tipo bundle y se agrega el avistamiento para enviarlo a un nuevo fragmento, al InfoFragment
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("object", avistResult.get(i));
                                //se empieza a hacer la transaction para cambiar al nuevo fragment y se envia el objeto seleccionado
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
                Toast.makeText(getActivity(), "Hubo un error con la API "+t.getMessage(), Toast.LENGTH_LONG).show(); //se informa que hubo un error y se da el mensaje
            }
        });

    }

    //muestra un avistamiento en especifico
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

        //se mueve la camara a la posicion del marcador
        map.moveCamera(CameraUpdateFactory.newLatLng(marker));
        map.moveCamera(CameraUpdateFactory.zoomTo(14.0F));
        //se hace un click listener del info windows para poder abrir el fragmento de informacion del avistamiento
        map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                //se crea un objeto de tipo bundle y se agrega el avistamiento para enviarlo a un nuevo fragmento, al InfoFragment
                Bundle bundle = new Bundle();
                bundle.putSerializable("object", item);
                //se empieza a hacer la transaction para cambiar al nuevo fragment y se envia el objeto seleccionado
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, InfoFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
                mapcontainer.setVisibility(View.GONE);

            }
        });
    }

    public void showDistanceAvistamiento(String point, String distance){
        //limpiamos el mapa
        map.clear();
        //creamos el objeto para obtener el id de todos los avistamientos que estan en la ditancia especificada
        Call<List<Integer>> idavistamientos = apiService.getAvistamientosByDistance(point, distance);
        //se hace un enqueue del objeto para que la app no tenga error
        idavistamientos.enqueue(new Callback<List<Integer>>() {
            @Override
            public void onResponse(Call<List<Integer>> call, Response<List<Integer>> response) {      //success response

                if (!response.isSuccessful()){ //se verifica si el response fue successful
                    Toast.makeText(getActivity(), "Fallo el response "+ response.code(), Toast.LENGTH_LONG).show();
                    return; //si no fue successful se informa que fallo el response y se da el codigo, luego se retorna
                }

                //Se obtienen todos los id que el response devuelve
                List<Integer> idavistResult = response.body();

                //creamos un objeto para obtener todos los avistamientos
                Call<List<Avistamiento>> allavistamientos = apiService.getAvistamientos();
                //se hace un enqueue del objeto para que la app no tenga error
                allavistamientos.enqueue(new Callback<List<Avistamiento>>() {
                    @Override
                    public void onResponse(Call<List<Avistamiento>> call, Response<List<Avistamiento>> response) {
                        if (!response.isSuccessful()) { //se verifica si el response fue successful
                            Toast.makeText(getActivity(), "Fallo el response " + response.code(), Toast.LENGTH_LONG).show();
                            return; //si no fue successful se informa que fallo el response y se da el codigo, luego se retorna
                        }

                        //se obtienen todos los objetos de los resultados
                        List<Avistamiento> avistResult = response.body();
                        //se crea la lista que tendra todos los objetos filtrados
                        List<Avistamiento> filteredResult = new ArrayList<Avistamiento>();

                        //se filtran todos los bojetos para que coincidan con los id deseados
                        for (int i = 0; i < idavistResult.size(); i++) {
                            for (int j = 0; j < avistResult.size(); j++) {
                                if (avistResult.get(j).getId_avistamiento() == idavistResult.get(i)) {
                                    filteredResult.add(avistResult.get(j));
                                }
                            }
                        }
                        //se limpia el mapa
                        map.clear();
                        //se hace un objeto latlng y se le envia la latitud y longitud actual para poder crear el circulo
                        LatLng currentLocation = new LatLng(latitude, longitude);
                        //se crea el circulo con un radio definido con la distancia que el usuario puso, luego se le ponen las settings del borde y el relleno
                        Circle circle = map.addCircle(new CircleOptions()
                        .center(currentLocation)
                        .radius(Integer.parseInt(distance))
                        .strokeColor(Color.BLUE)
                        .fillColor(Color.TRANSPARENT));
                        //ponemos todos los marcadores correspondientes a los avistamientos que se encontraron
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

                        //se hace un click listener del info windows para poder abrir el fragmento de informacion del avistamiento
                        map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                            @Override
                            public void onInfoWindowLongClick(Marker marker) {
                                for (int i = 0; i < filteredResult.size(); i++) {
                                    String geom = filteredResult.get(i).getGeom();                                                //obtenemos el string de la geometria
                                    String[] dataStr = geom.split("\\(");                                               //partimos el string para obtener solo la latitud y longitud
                                    String geoData = dataStr[1].substring(0, dataStr[1].length() - 1);                           //borramos el parentesis del final
                                    String[] latLong = geoData.split(" ");

                                    LatLng pos = new LatLng(Double.parseDouble(latLong[1]), Double.parseDouble(latLong[0])); //hacemos un objeto que tendra la informacion de la latitud y longitud
                                    //verificamos si el nombre del animal y la posicion son iguales tanto en el objeto actual como en el del marcador
                                    if (marker.getTitle().equals(filteredResult.get(i).getAnimal()) && marker.getPosition().equals(pos)) {
                                        //se crea un objeto de tipo bundle y se agrega el avistamiento para enviarlo a un nuevo fragmento, al InfoFragment
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("object", filteredResult.get(i));
                                        //se empieza a hacer la transaction para cambiar al nuevo fragment y se envia el objeto seleccionado
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
                        Toast.makeText(getActivity(), "Hubo un error con la API "+t.getMessage(), Toast.LENGTH_LONG).show(); //se informa que hubo un error y se da el mensaje
                    }
                });

            }

            @Override
            public void onFailure(Call<List<Integer>> call, Throwable t) {                                 //fail response
                Toast.makeText(getActivity(), "Hubo un error con la API "+t.getMessage(), Toast.LENGTH_LONG).show(); //se informa que hubo un error y se da el mensaje
            }
        });
    }

}