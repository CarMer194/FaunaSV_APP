package com.mdpustudio.faunasv;

import com.mdpustudio.faunasv.models.Animal;
import com.mdpustudio.faunasv.models.Avistamiento;
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
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface EndpointInterface {

    @GET("avistamientos")
    Call<List<Avistamiento>> getAvistamientos();

    @Multipart
    @POST("avistamientos")
    Call<Avistamiento> addAvistamiento(@Header("Authorization") String token,
                                       @Part("geom") String geom,
                                       @Part("confirmado") Boolean confirmado,
                                       @Part("fecha_hora") String fecha_hora,
                                       @Part("fotografia") RequestBody fotografia,
                                       @Part("descripcion") String descripcion,
                                       @Part("animal") String animal);

    @GET("avistamientos/?search={username}")
    Call<List<Avistamiento>> getAvistamientosByUser(@Path("username") String username);

    @GET("avistamientos/?search={animal}")
    Call<List<Avistamiento>> getAvistamientosByAnimal(@Path("animal") String animal);

    @GET("getcercanos/point={location}&m={distance}")
    Call<List<Integer>> getAvistamientosByDistance(@Path("location") String point, @Path("distance") String distance);

    @GET("getdistpuntos/point1={location1}&point2={location2}")
    Call<Double> getDistanceByPoints(@Path("location1") String location1, @Path("location2") String location2);

    @GET("avistamientos/{id}")
    Call<Avistamiento> getAvistamientoSingle(@Path("id") String id);

    @POST("api/token/")
    Call<Token> getToken(@Body User user);

    @GET("animal/")
    Call<List<Animal>> getAnimalesByName(@Query("search") String animal);

}
