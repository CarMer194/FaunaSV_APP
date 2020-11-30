package com.mdpustudio.faunasv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.mdpustudio.faunasv.models.Token;
import com.mdpustudio.faunasv.models.User;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FirstFragment extends Fragment {
    public static final String BASE_URL = "https://faunaelsalvador.herokuapp.com/";
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    EndpointInterface apiInterface = retrofit.create(EndpointInterface.class);

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        view.findViewById(R.id.button_enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                User user = new User();
                user.setUsername("alexander");
                user.setPassword("cris1234");


                Call<Token> token = apiInterface.getToken(user);
                token.enqueue(new Callback<Token>() {
                    @Override
                    public void onResponse(Call<Token> call, Response<Token> response) {
                        Token userToken = response.body();

                        //Toast.makeText(getActivity(), userToken.getAccess(), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.putExtra("TOKENID", userToken.getAccess());
                        startActivity(intent);

                    }

                    @Override
                    public void onFailure(Call<Token> call, Throwable t) {
                        Toast.makeText(getActivity(), "Error: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

}