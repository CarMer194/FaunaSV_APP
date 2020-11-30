package com.mdpustudio.faunasv;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.mdpustudio.faunasv.models.Animal;
import com.mdpustudio.faunasv.models.Avistamiento;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;

public class AddAvistamientoFragment extends Fragment {

    public static final String BASE_URL = "https://faunaelsalvador.herokuapp.com/";
    static final int REQUEST_IMAGE_CAPTURE = 1;

    Button addImage;
    Button filterAnimal;
    Button addAvist;
    EditText searchAnimal;
    ListView listAnimal;
    ImageView avistImage;
    TextInputEditText descTxt;

    Bitmap imageBitmap;
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    EndpointInterface apiService = retrofit.create(EndpointInterface.class);
    String location;
    String selectedAnimal;
    String token;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        location = getActivity().getPreferences(Context.MODE_PRIVATE).getString("LocationPoint", null);
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        token = getActivity().getPreferences(Context.MODE_PRIVATE).getString("token", null);

        View root = inflater.inflate(R.layout.fragment_addavistamiento, container, false);
        addImage = root.findViewById(R.id.addimage_button);
        filterAnimal = root.findViewById(R.id.buscar_animal_button);
        searchAnimal = root.findViewById(R.id.search_animal_edittex);
        listAnimal = root.findViewById(R.id.list_animal_search);
        avistImage = root.findViewById(R.id.new_avist_image);
        addAvist = root.findViewById(R.id.addavistamiento_button);
        descTxt = root.findViewById(R.id.add_avist_desc);

        listAnimal.setVisibility(View.GONE);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        filterAnimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<List<Animal>> filterAnimales = apiService.getAnimalesByName(searchAnimal.getText().toString());
                filterAnimales.enqueue(new Callback<List<Animal>>() {
                    @Override
                    public void onResponse(Call<List<Animal>> call, Response<List<Animal>> response) {
                        if (!response.isSuccessful()){
                            Toast.makeText(getActivity(), "Fallo el response "+ response.code(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<Animal> aniFilter = response.body();
                        listAnimal.setVisibility(View.VISIBLE);

                        ArrayList<String> arrayList = new ArrayList<>();

                        for (int i=0; i<aniFilter.size();i++){
                            arrayList.add(aniFilter.get(i).getNombre_local());
                        }

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrayList);
                        listAnimal.setAdapter(arrayAdapter);

                        listAnimal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                view.setSelected(true);
                                selectedAnimal = aniFilter.get(i).getNombre_local();
                                //Toast.makeText(getActivity(), selectedAnimal, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<Animal>> call, Throwable t) {
                        Toast.makeText(getActivity(), "Error: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        addAvist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToAddAvist();
            }
        });
        return root;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), "Ocurrio un error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            avistImage.setImageBitmap(imageBitmap);
        }
    }

    public void tryToAddAvist(){
        //confirmado
        Boolean confirmado = false;
        //fecha_hora
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        Date date = new Date();
        String dateString = formatter.format(date);
        //fotografia
        MediaType media = MediaType.parse("image/png");
        File file = bitmapToFile(imageBitmap, dateString+"_image.png");
        RequestBody requestBody = RequestBody.create(media, file);
        //descripcion
        String description = String.valueOf(descTxt.getText());
        //token
        String responseToken = "Token "+token;

        Call<Avistamiento> addAvistamiento = apiService.addAvistamiento(responseToken, location, confirmado, dateString, requestBody, description, selectedAnimal);
        addAvistamiento.enqueue(new Callback<Avistamiento>() {
            @Override
            public void onResponse(Call<Avistamiento> call, Response<Avistamiento> response) {
                if (!response.isSuccessful()){
                    Toast.makeText(getActivity(), "Fallo el response "+ response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(getActivity(), "funciono?", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFailure(Call<Avistamiento> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: "+ t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    public static File bitmapToFile(Bitmap bitmap, String fileNameToSave) { // File name like "image.png"
        //create a file to write bitmap data
        File file = null;
        try {
            file = new File(Environment.getExternalStorageDirectory() + File.separator + fileNameToSave);
            file.createNewFile();

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 , bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;
        }catch (Exception e){
            e.printStackTrace();
            return file; // it will return null
        }
    }

}