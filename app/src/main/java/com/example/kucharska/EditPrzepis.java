package com.example.kucharska;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.kucharska.database.PrzepisRepository;
import com.example.kucharska.model.Przepis;
import com.example.kucharska.sensor.SensorDataListener;
import com.example.kucharska.sensor.SensorService;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditPrzepis extends AppCompatActivity implements MediaScannerConnection.OnScanCompletedListener, SensorDataListener {

    private static final int REQUEST_GALLERY_IMAGE = 3;
    private static final int REQUEST_CAMERA_IMAGE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_PICK = 3;
    public static final String RECIPE_INFO_TITLE = "RECIPE_INFO_TITLE";
    public static final String RECIPE_INFO_INGREDIENTS = "RECIPE_INFO_INGREDIENTS";
    public static final String RECIPE_INFO_INSTRUCTIONS = "RECIPE_INFO_INSTRUCTIONS";
    public static final String RECIPE_INFO_SERVINGS = "RECIPE_INFO_SERVINGS";
    public static final String RECIPE_INFO_IMAGE = "RECIPE_INFO_IMAGE";
    public static final String PRZEPIS = "PRZEPIS";
    private String imageString;
    private EditText titleEditText;
    private EditText ingredientsEditText;
    private EditText instructionsEditText;
    private EditText servingsEditText;
    private ImageView imageView;
    private String imagePath;
    private Uri selectedImageUri;
    private PrzepisRepository przepisRepository;
    private PrzepisViewModel przepisViewModel;
    private List<Przepis> przepisy;
    private Przepis przepis;
    private SensorService sensorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_przepis);
        sensorService = new SensorService();

        titleEditText = findViewById(R.id.edit_text_title);
        ingredientsEditText = findViewById(R.id.edit_text_ingredients);
        instructionsEditText = findViewById(R.id.edit_text_instructions);
        servingsEditText = findViewById(R.id.edit_text_servings);
        imageView = findViewById(R.id.image_view);

        Intent intent = getIntent();
        titleEditText.setText(intent.getStringExtra(RECIPE_INFO_TITLE));
        ingredientsEditText.setText(intent.getStringExtra(RECIPE_INFO_INGREDIENTS));
        instructionsEditText.setText(intent.getStringExtra(RECIPE_INFO_INSTRUCTIONS));
        servingsEditText.setText(intent.getStringExtra(RECIPE_INFO_SERVINGS));
        String image = intent.getStringExtra(RECIPE_INFO_IMAGE);
        imageString = image;
        przepis = (Przepis) intent.getSerializableExtra(PRZEPIS);
        Log.d("edit", "przepis: " + przepis.getTitle());

        if (image != null) {
            Picasso.get().load(image).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.meal);
        }

        przepisViewModel = new ViewModelProvider(this).get(PrzepisViewModel.class);
        przepisViewModel.findAll().observe(this, new Observer<List<Przepis>>() {
            @Override
            public void onChanged(List<Przepis> przepisy) {
                EditPrzepis.this.przepisy = przepisy;
            }
        });

        Button buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedImageUri != null) {
                    imageString = selectedImageUri.toString();
                }
                Przepis p = new Przepis(titleEditText.getText().toString(), ingredientsEditText.getText().toString(), instructionsEditText.getText().toString(), servingsEditText.getText().toString(), image);
                p.setImage(imageString);
                przepisViewModel.delete(przepis);
                przepisViewModel.insert(p);
                Toast.makeText(EditPrzepis.this, "Przepis zapisany", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        Button buttonGallery = findViewById(R.id.button_gallery);
        buttonGallery.setOnClickListener(view -> {
            Intent camIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(camIntent, REQUEST_GALLERY_IMAGE);
        });

        Button buttonCamera = findViewById(R.id.button_camera);
        buttonCamera.setOnClickListener(view -> {
            Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camIntent, REQUEST_CAMERA_IMAGE);
        });

        if (ContextCompat.checkSelfPermission(EditPrzepis.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditPrzepis.this, new String[]{
                    android.Manifest.permission.CAMERA
            }, 100);
        }
        if (ActivityCompat.checkSelfPermission(EditPrzepis.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditPrzepis.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
        }
        if(ActivityCompat.checkSelfPermission(EditPrzepis.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditPrzepis.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 100);
        }
        SensorService.setSensorDataListener(this);
        onColorsChanged(SensorService.getTextColor(), SensorService.getBackgroundColor());
        onHintColorChanged(SensorService.getHintColor());
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
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        selectedImageUri = uri;
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
    }

    @Override
    public void OnPause() {
        super.onPause();
        sensorService.setSensorDataListener(null);
    }

    @Override
    public void onColorsChanged(int textColor, int backgroundColor) {
        findViewById(R.id.edit_przepis).setBackgroundColor(backgroundColor);
        findViewById(R.id.edit_przepis_scroll).setBackgroundColor(backgroundColor);
        setTextColorForViewGroup((ViewGroup) findViewById(android.R.id.content), textColor);
    }

    @Override
    public void onHintColorChanged(int hintColor){
        titleEditText.setHintTextColor(hintColor);
        ingredientsEditText.setHintTextColor(hintColor);
        instructionsEditText.setHintTextColor(hintColor);
        servingsEditText.setHintTextColor(hintColor);
        Log.d("zdj", "Zmiana koloru wskazówki została zastosowana");
    }
}
