package com.mdpustudio.faunasv;

import com.mdpustudio.faunasv.models.Animal;
import com.mdpustudio.faunasv.models.Avistamiento;
import com.mdpustudio.faunasv.models.Experto;
import com.mdpustudio.faunasv.models.Token;
import com.mdpustudio.faunasv.models.User;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

//esta es la interfaz con la que se comunicara con nuestra API en Django con la ayuda de retrofit
public interface EndpointInterface {

    //este endpoint nos devolvera todos los avistamientos que estan en la base de datos
    @GET("avistamientos")
    Call<List<Avistamiento>> getAvistamientos();

    //este endpoint lo utilizamos para enviar un nuevo avistamiento a nuestra API
    @Headers("Content-Type: multipart/form-data")
    @Multipart
    @POST("avistamientos/")
    Call<Avistamiento> addAvistamiento(@Header("Authorization") String token,
                                       @Part("geom") String geom,
                                       @Part("confirmado") String confirmado,
                                       @Part("fecha_hora") String fecha_hora,
                                       @Part("fotografia") RequestBody fotografia,
                                       @Part("descripcion") String descripcion,
                                       @Part("animal") String animal);

    //este endpoint se utilizara para mostrar todos los avistamientos cercanos en una distancia determinada en m.
    @GET("getcercanos/point={location}&m={distance}")
    Call<List<Integer>> getAvistamientosByDistance(@Path("location") String point, @Path("distance") String distance);

    //este endpoint se utilizara para conocer la distancia entre la locacion actual del usuario y un avistamiento en especifico.
    @GET("getdistpuntos/point1={location1}&point2={location2}")
    Call<Double> getDistanceByPoints(@Path("location1") String location1, @Path("location2") String location2);

    //endpoint para obtener el token de login
    @POST("api/token/")
    Call<Token> getToken(@Body User user);

    //endpoint para obtener a todos los animales por un nombre en especifico
    @GET("animal/")
    Call<List<Animal>> getAnimalesByName(@Query("search") String animal);

    //endpoint para obetener todos los avistamientos por animal o por usuario
    @GET("avistamientos/")
    Call<List<Avistamiento>> getAvistamientoByUserorAnimal(@Query("search") String data);

}
