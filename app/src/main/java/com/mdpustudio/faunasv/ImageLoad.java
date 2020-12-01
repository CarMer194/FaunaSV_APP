package com.mdpustudio.faunasv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoad extends AsyncTask<Void, Void, Bitmap> {

    String url;
    ImageView imageView;

    public ImageLoad(String url, ImageView imageView){
        this.url = url;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        //se hace en el metodo de doInBackground ya que si se hace en el main thread puede trabar la app.
        try {
            //creamos nuestro objeto URL
            URL urlConnection = new URL(url);
            //creamos la connection de nuestra url
            HttpURLConnection connection = (HttpURLConnection) urlConnection
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            //obtenemos el input stream de nuestra imagen
            InputStream input = connection.getInputStream();
            //hacemos un decode de nuestro inputstream para pasarlo a bitmap y poder utilizarlo en las imageview
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        //se pone la imagen en el imageView
        imageView.setImageBitmap(bitmap);
    }
}
