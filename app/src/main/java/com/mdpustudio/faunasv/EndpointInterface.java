package com.mdpustudio.faunasv;

import com.mdpustudio.faunasv.models.Avistamiento;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface EndpointInterface {

    @GET("avistamientos")
    Call<List<Avistamiento>> getAvistamientos();

}
