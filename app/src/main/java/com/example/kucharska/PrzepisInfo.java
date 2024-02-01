package com.example.kucharska;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

public class PrzepisInfo extends AppCompatActivity implements SensorDataListener{

    private boolean isRecipeInDatabase;
    private TextView przepisTitleTextView;
    private TextView przepisIngredientsTextView;
    private TextView przepisInstructionsTextView;
    private TextView przepisServingsTextView;
    private ImageView przepisImage;
    public static final String RECIPE_INFO_TITLE = "RECIPE_INFO_TITLE";
    public static final String RECIPE_INFO_INGREDIENTS = "RECIPE_INFO_INGREDIENTS";
    public static final String RECIPE_INFO_INSTRUCTIONS = "RECIPE_INFO_INSTRUCTIONS";
    public static final String RECIPE_INFO_SERVINGS = "RECIPE_INFO_SERVINGS";
    public static final String RECIPE_INFO_IMAGE = "RECIPE_INFO_IMAGE";
    public static final String PRZEPIS = "PRZEPIS";
    private PrzepisViewModel przepisViewModel;
    private Przepis przepis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.przepisinfo);

        przepisTitleTextView = findViewById(R.id.recipe_name);
        przepisIngredientsTextView = findViewById(R.id.recipe_ingredients);
        przepisInstructionsTextView = findViewById(R.id.recipe_instructions);
        przepisImage = findViewById(R.id.img);
        przepisServingsTextView = findViewById(R.id.recipe_servings);

        Intent intent = getIntent();
        przepisTitleTextView.setText(intent.getStringExtra(RECIPE_INFO_TITLE));
        przepisIngredientsTextView.setText(getFormattedIngredients(intent.getStringExtra(RECIPE_INFO_INGREDIENTS)));
        przepisInstructionsTextView.setText(getFormattedInstructions(intent.getStringExtra(RECIPE_INFO_INSTRUCTIONS)));
        przepisServingsTextView.setText(intent.getStringExtra(RECIPE_INFO_SERVINGS));
        String image = intent.getStringExtra(RECIPE_INFO_IMAGE);
        przepis = (Przepis) intent.getSerializableExtra(PRZEPIS);

        Log.d("przepisinfo", "image string: " + image);


        if (image != null) {
            Picasso.get().load(image).into(przepisImage);
        } else {
            Picasso.get().load(R.drawable.meal).into(przepisImage);
        }

        Button editRecipeButton = findViewById(R.id.edit_recipe_button);
        editRecipeButton.setText("Dodaj przepis");

        przepisViewModel = new ViewModelProvider(this).get(PrzepisViewModel.class);
        przepisViewModel.findAll().observe(this, new Observer<List<Przepis>>() {
            @Override
            public void onChanged(List<Przepis> przepisList) {
                if(przepisList != null) {
                    for (Przepis przepis : przepisList) {
                        if (przepis.getTitle().equals(przepisTitleTextView.getText().toString())) {
                            editRecipeButton.setText("Edytuj przepis");
                            isRecipeInDatabase = true;
                            break;
                        }
                    }
                } else {
                    isRecipeInDatabase = false;
                }
            }
        });

        SensorService.setSensorDataListener(this);
        onColorsChanged(SensorService.getTextColor(), SensorService.getBackgroundColor());
    }

    @Override
    public void onColorsChanged(int textColor, int backgroundColor) {
        // Zastosowanie zmiany koloru tekstu dla wszystkich TextView
        TextView recipeNameTextView = findViewById(R.id.recipe_name);
        TextView ingredientsLabelTextView = findViewById(R.id.ingredients_label);
        TextView recipeIngredientsTextView = findViewById(R.id.recipe_ingredients);
        TextView instructionsLabelTextView = findViewById(R.id.instructions_label);
        TextView recipeInstructionsTextView = findViewById(R.id.recipe_instructions);
        TextView servingsTextView = findViewById(R.id.recipe_servings);

        recipeNameTextView.setTextColor(textColor);
        ingredientsLabelTextView.setTextColor(textColor);
        recipeIngredientsTextView.setTextColor(textColor);
        instructionsLabelTextView.setTextColor(textColor);
        recipeInstructionsTextView.setTextColor(textColor);
        servingsTextView.setTextColor(textColor);

        LinearLayout linearLayout = findViewById(R.id.przepisinfo);
        linearLayout.setBackgroundColor(backgroundColor);

        ScrollView scrollView = findViewById(R.id.przepisinfo_scroll);
        scrollView.setBackgroundColor(backgroundColor);
    }

    private String getFormattedIngredients(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> items = Arrays.asList(text.split("\\|"));
        for (String item : items) {
            String trimmedItem = item.trim();
            if (!trimmedItem.startsWith("-")) {
                stringBuilder.append("- ");
            }
            stringBuilder.append(trimmedItem).append("\n");
        }
        return stringBuilder.toString();
    }

    private String getFormattedInstructions(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> items = Arrays.asList(text.split("\\."));
        int stepNumber = 1; // zaczynamy od kroku 1
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i).trim();
            if (!item.isEmpty()) {
                if (item.startsWith("step")) {
                    // Jeśli zdanie zaczyna się od "step", nie dodawaj kolejnego numeru kroku
                    stringBuilder.append(item).append("\n\n");
                } else {
                    // Dodaj numer kroku tylko wtedy, gdy zdanie nie zaczyna się od "step"
                    stringBuilder.append("step ").append(stepNumber++).append(": ").append(item).append("\n\n");
                }
            }
        }
        return stringBuilder.toString();
    }


    public void onEditRecipeButtonClick(View view) {
        if(isRecipeInDatabase) {
            Intent intent = new Intent(this, EditPrzepis.class);
            intent.putExtra(RECIPE_INFO_TITLE, przepisTitleTextView.getText().toString());
            intent.putExtra(RECIPE_INFO_INGREDIENTS, przepisIngredientsTextView.getText().toString());
            intent.putExtra(RECIPE_INFO_INSTRUCTIONS, przepisInstructionsTextView.getText().toString());
            intent.putExtra(RECIPE_INFO_IMAGE, getIntent().getStringExtra(RECIPE_INFO_IMAGE));
            intent.putExtra(RECIPE_INFO_SERVINGS, przepisServingsTextView.getText().toString());
            intent.putExtra(PRZEPIS, przepis);
            startActivity(intent);
        } else {
            dodajPrzepisDoZapisanych();
        }
        finish();
    }

    @Override
    public void OnResume() {
        super.onResume();
        SensorService.setSensorDataListener(this);
    }
    @Override
    public void OnPause() {
        super.onPause();
        SensorService.setSensorDataListener(null);
    }
    @Override
    public void onHintColorChanged(int hint) {
        return;
    }

    private void dodajPrzepisDoZapisanych() {
        String przepisTitle = przepisTitleTextView.getText().toString();
        String przepisIngredients = przepisIngredientsTextView.getText().toString();
        String przepisInstructions = przepisInstructionsTextView.getText().toString();
        String przepisImage = null;
        String przepisServings = przepisServingsTextView.getText().toString();

        Przepis przepis = new Przepis(przepisTitle, przepisIngredients, przepisInstructions, przepisServings, przepisImage);
        przepisViewModel.insert(przepis);
        Intent intent = new Intent();
        intent.putExtra("przepis", (CharSequence) przepis);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
