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
    //creamos la URL en donde esta alojado nuestro API
    public static final String BASE_URL = "https://faunaelsalvador.herokuapp.com/";

    TextInputEditText userTextField;
    TextInputEditText passTextField;

    //objeto Retrofit que nos permitira conectarnos con la API, a este se le envia la URL de nuestra API y con que herramienta usaremos para consumir los Json, en este caso sera Gson
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    //creame nuestro objeto de la interface que se conectara con la API y la igualamos a nuestro objeto retrofit.
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

        userTextField = view.findViewById(R.id.userTextField);
        passTextField = view.findViewById(R.id.passTextField);

        //click listener que nos enviara al fragmento de registro.
        view.findViewById(R.id.button_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        //click listener que nos verificara el usuario y la contraseña y nos retornara un token
        view.findViewById(R.id.button_enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                User user = new User();
                user.setUsername(String.valueOf(userTextField.getText()));
                user.setPassword(String.valueOf(passTextField.getText()));

                //usamos el call con un objeto, el objeto sera del tipo que la api retorne, para poder hacerle un enqueue.
                Call<Token> token = apiInterface.getToken(user);
                //hacemos un encueue de nuestro objeto para no hacer el request en el main thread, ya que esto podria crashear nuestra app
                token.enqueue(new Callback<Token>() {
                    @Override
                    public void onResponse(Call<Token> call, Response<Token> response) {    //api envio un response
                        //verificamos si el response que la API envio fue successful
                        if (!response.isSuccessful()){
                            Toast.makeText(getActivity(), "Datos incorrectos", Toast.LENGTH_LONG).show();
                            return; //si el api no fue successful se imprime el codigo de error y se retorna.
                        }

                        //obtenemos nuestro token del response que nos retorna la API
                        Token userToken = response.body();
                        String userName = user.getUsername();

                        //hacemos el intent para cambiar de actividad, y agregamos como dato extra el token de access para que pueda ser utilizado en la otra actividad.
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.putExtra("TOKENID", userToken.getAccess());
                        intent.putExtra("USERNAME", userName);
                        startActivity(intent);

                    }

                    @Override
                    public void onFailure(Call<Token> call, Throwable t) {  //api fallo
                        //se muestra el mensaje de error que la API retorna
                        Toast.makeText(getActivity(), "Error: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

}