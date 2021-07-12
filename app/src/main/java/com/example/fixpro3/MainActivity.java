package com.example.fixpro3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_IMAGE_CAPTURE = 100; // код-идентификатор запроса - по нему определяем откуда пришел результат
    private Uri photoUri; // Uri снимка
    private String photoGeo; // координаты снимка
    private String photoDate; // дата снимка
    private Button btnFixPro; // кнопка фиксации проблемы
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFixPro = findViewById(R.id.btnFixPro);
        imageView = findViewById(R.id.imageView);

        btnFixPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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
            // Bundle extras = data.getExtras();
            Bitmap bitmap =  ImageUtils.getBitmapFromIntent(this, data);
            imageView.setImageBitmap(bitmap);// mImage is a ImageView which is bind previously.
            String imgPath = ImageUtils.createFile(this, bitmap);
            File imageFile = new File(imgPath);
            Toast toast = Toast.makeText(this, "Path to photo: " + imageFile, Toast.LENGTH_SHORT);
            toast.show();
        }
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