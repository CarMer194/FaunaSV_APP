package com.mdpustudio.faunasv;

import com.mdpustudio.faunasv.models.Avistamiento;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface EndpointInterface {

    @GET("avistamientos")
    Call<List<Avistamiento>> getAvistamientos();

    @GET("avistamientos/?search={username}")
    Call<List<Avistamiento>> getAvistamientosByUser(@Path("username") String username);

    @GET("avistamientos/?search={animal}")
    Call<List<Avistamiento>> getAvistamientosByAnimal(@Path("animal") String animal);

}
