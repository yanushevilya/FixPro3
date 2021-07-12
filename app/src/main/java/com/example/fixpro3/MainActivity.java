package com.example.fixpro3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_IMAGE_CAPTURE = 100; // код-идентификатор запроса - по нему определяем откуда пришел результат
    private Uri photoUri; // Uri снимка
    private String photoGeo; // координаты снимка
    private String photoDate; // дата снимка
    private Button btnFixPro; // кнопка фиксации проблемы
    private ImageView imageView;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFixPro = findViewById(R.id.btnFixPro);
        imageView = findViewById(R.id.imageView);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this); // for location

        btnFixPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check permission
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    // requesting permission
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) { // если нашлось приложение для этого намерения...
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }

    // когда вызванное (дочернее) окно закроется - вызывается этот метод
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // Создаем Preview изображения и помещаем его в наш imageView
            Bitmap bitmap =  ImageUtils.getBitmapFromIntent(this, data);
            imageView.setImageBitmap(bitmap);// mImage is a ImageView which is bind previously.

            // Сохраняем наше изображение в файл
            String imgPath = ImageUtils.createFile(this, bitmap);
            File imageFile = new File(imgPath);
            Toast toastPathPhoto = Toast.makeText(this, "Path to photo: " + imageFile, Toast.LENGTH_SHORT);
            Toast toastGeoPhoto = Toast.makeText(this, "Geolocation photo: " + photoGeo, Toast.LENGTH_SHORT);
            toastPathPhoto.show();
            toastGeoPhoto.show();
        }
    }

    // location
    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                //init location
                Location location = task.getResult();
                if (location != null) {
                    try {
                        // init Geocoder
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        //init AddressList
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                        photoGeo = "Latitude: " + addresses.get(0).getLatitude() + ", Longitude: " + addresses.get(0).getLongitude();
                        System.out.println("Latitude: " + addresses.get(0).getLatitude());
                        System.out.println("Longitude: " + addresses.get(0).getLongitude());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }






    public static class ImageUtils {

        public static Bitmap getBitmapFromIntent(Context context, Intent data) {
            Bitmap bitmap = null;

            if (data.getData() == null) {
                bitmap = (Bitmap) data.getExtras().get("data");
            } else {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return bitmap;
        }

        public static String createFile(Context context, Bitmap data) {
            Uri selectedImage = getImageUri(context,data);
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = context.getContentResolver().query(selectedImage, filePath, null, null, null);
            c.moveToFirst();
            c.getColumnIndex(filePath[0]);
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            c.close();

            return picturePath;
        }

        public static Uri getImageUri(Context context, Bitmap inImage) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Pet_Image", null);
            return Uri.parse(path);
        }




    }
}