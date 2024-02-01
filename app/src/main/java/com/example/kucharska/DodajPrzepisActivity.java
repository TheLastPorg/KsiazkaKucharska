package com.example.kucharska;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DodajPrzepisActivity extends AppCompatActivity implements SensorDataListener, MediaScannerConnection.OnScanCompletedListener {

    private static final int REQUEST_GALLERY_IMAGE = 3;
    private static final int REQUEST_CAMERA_IMAGE = 100;
    private EditText titleEditText;
    private EditText ingredientsEditText;
    private EditText instructionsEditText;
    private EditText servingsEditText;
    private ImageView imageView;
    private String imagePath;
    private Uri selectedImageUri;
    private PrzepisRepository przepisRepository;
    private SensorService sensorService;

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_PICK = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dodaj_przepis);
        Log.d("zdj", "Metoda onCreate została wywołana");

        sensorService = new SensorService();
        przepisRepository = new PrzepisRepository(getApplication());

        titleEditText = findViewById(R.id.edit_text_title);
        ingredientsEditText = findViewById(R.id.edit_text_ingredients);
        instructionsEditText = findViewById(R.id.edit_text_instructions);
        servingsEditText = findViewById(R.id.edit_text_servings);
        imageView = findViewById(R.id.image_view);

        if (ContextCompat.checkSelfPermission(DodajPrzepisActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DodajPrzepisActivity.this, new String[]{
                    android.Manifest.permission.CAMERA
            }, 100);
        }
        if (ActivityCompat.checkSelfPermission(DodajPrzepisActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DodajPrzepisActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
        }
        if(ActivityCompat.checkSelfPermission(DodajPrzepisActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DodajPrzepisActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 100);
        }

        Button buttonGallery = findViewById(R.id.button_gallery);
        buttonGallery.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_GALLERY_IMAGE);
        });

        Button buttonCamera = findViewById(R.id.button_camera);
        buttonCamera.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA_IMAGE);
        });

        Button buttonAdd = findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dodajPrzepisDoBazy();
            }
        });

        SensorService.setSensorDataListener(this);
        onColorsChanged(SensorService.getTextColor(), SensorService.getBackgroundColor());
        onHintColorChanged(SensorService.getHintColor());
    }

    private void dodajPrzepisDoBazy() {
        String title = titleEditText.getText().toString().trim();
        String ingredients = ingredientsEditText.getText().toString().trim();
        String instructions = instructionsEditText.getText().toString().trim();
        String servings = servingsEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(ingredients) || TextUtils.isEmpty(instructions) || TextUtils.isEmpty(servings) || selectedImageUri == null) {
            Toast.makeText(this, "Wszystkie pola są wymagane", Toast.LENGTH_SHORT).show();
            return;
        }
        String imagePath = selectedImageUri.toString();
        Log.d("zdj", "dodajPrzepisDoBazy: " + imagePath);

        // Walidacja danych, dodanie przepisu do bazy danych
        Przepis przepis = new Przepis(title, ingredients, instructions, servings, imagePath);
        przepisRepository.insert(przepis);

        Toast.makeText(this, "Przepis dodany pomyślnie", Toast.LENGTH_SHORT).show();
        Log.d("zdj", "Przepis dodany pomyślnie");
        // Możesz dodać też kod do zamykania tej aktywności po dodaniu przepisu, jeśli jest to wymagane
    }

    private void setTextColorForViewGroup(ViewGroup viewGroup, int color) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup) {
                setTextColorForViewGroup((ViewGroup) view, color);
            } else if (view instanceof TextView) {
                ((TextView) view).setTextColor(color);
            }
        }
    }

    @Override
    public void OnResume() {
        super.onResume();
        sensorService.setSensorDataListener(this);
        Log.d("zdj", "Metoda OnResume została wywołana");
    }

    @Override
    public void OnPause() {
        super.onPause();
        sensorService.setSensorDataListener(null);
        Log.d("zdj", "Metoda OnPause została wywołana");
    }

    @Override
    public void onColorsChanged(int textColor, int backgroundColor) {
        // Zastosowanie zmiany koloru tekstu dla wszystkich TextView
        findViewById(R.id.dodaj_przepis).setBackgroundColor(backgroundColor);
        setTextColorForViewGroup((ViewGroup) findViewById(android.R.id.content), textColor);
        Log.d("zdj", "Zmiana kolorów została zastosowana");
    }

    @Override
    public void onHintColorChanged(int hintColor){
        titleEditText.setHintTextColor(hintColor);
        ingredientsEditText.setHintTextColor(hintColor);
        instructionsEditText.setHintTextColor(hintColor);
        servingsEditText.setHintTextColor(hintColor);
        Log.d("zdj", "Zmiana koloru wskazówki została zastosowana");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("zdj", "onActivityResult wywołane");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Log.d("zdj", "resultCode == RESULT_OK" + requestCode);
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Log.d("zdj", "requestCode == REQUEST_IMAGE_CAPTURE");
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                saveImageToGallery(imageBitmap);
                Log.d("zdj", "Zdjęcie z aparatu zapisane, URI: " + selectedImageUri);

                if (imageView != null) {
                    imageView.setImageBitmap(imageBitmap);
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                Log.d("zdj", "requestCode == REQUEST_IMAGE_PICK" + requestCode);
                selectedImageUri = data.getData();
                imageView.setImageURI(selectedImageUri);
                Log.d("zdj", "Zdjęcie z galerii wybrane, URI: " + selectedImageUri);

                imagePath = copyImageToAppDirectory(selectedImageUri);
                Log.d("zdj", "Zdjęcie z galerii skopiowane do lokalnego katalogu, ścieżka: " + imagePath);

                // Ustawienie selectedImageUri na wartość zwróconą przez Intent.ACTION_PICK
                selectedImageUri = data.getData();
                Log.d("zdj", "selectedImageUri ustawione na wartość zwróconą przez Intent.ACTION_PICK: " + selectedImageUri);
            }
        }
    }



    private String copyImageToAppDirectory(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File outputFile = new File(getFilesDir(), "image.jpg");
            OutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveImageToGallery(Bitmap imageBitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "PhotoHub");
        if (!imagesFolder.exists()) {
            boolean created = imagesFolder.mkdirs();
            Log.d("zdj", "Folder obrazów utworzony: " + created);
        }

        File image = new File(imagesFolder, imageFileName);

        try {
            FileOutputStream fos = new FileOutputStream(image);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            Log.d("zdj", "Obraz zapisany pomyślnie: " + image.getAbsolutePath());

            // Ustawienie naszej klasy jako obserwatora skanowania pliku
            MediaScannerConnection.scanFile(this,
                    new String[]{image.getPath()},
                    new String[]{"image/jpeg"}, (path, uri) -> {
                        selectedImageUri = uri;
                    });
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("zdj", "Błąd podczas zapisywania obrazu: " + e.getMessage());
        }
    }


    @Override
    public void onScanCompleted(String path, Uri uri) {
        selectedImageUri = uri;
        Log.d("zdj", "URI obrazu po skanowaniu: " + selectedImageUri);

        // Tutaj możesz wykonywać dodatkowe operacje związane z ustawieniem URI obrazu, np. aktualizację interfejsu użytkownika.
    }
}