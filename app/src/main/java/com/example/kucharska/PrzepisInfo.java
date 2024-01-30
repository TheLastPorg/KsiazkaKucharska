package com.example.kucharska;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

public class PrzepisInfo extends AppCompatActivity implements SensorDataListener{

    private TextView przepisTitleTextView;
    private TextView przepisIngredientsTextView;
    private TextView przepisInstructionsTextView;
    private ImageView przepisImage;
    public static final String RECIPE_INFO_TITLE = "RECIPE_INFO_TITLE";
    public static final String RECIPE_INFO_INGREDIENTS = "RECIPE_INFO_INGREDIENTS";
    public static final String RECIPE_INFO_INSTRUCTIONS = "RECIPE_INFO_INSTRUCTIONS";
    public static final String RECIPE_INFO_SERVINGS = "RECIPE_INFO_SERVINGS";
    public static final String RECIPE_INFO_IMAGE = "RECIPE_INFO_IMAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.przepisinfo);

        przepisTitleTextView = findViewById(R.id.recipe_name);
        przepisIngredientsTextView = findViewById(R.id.recipe_ingredients);
        przepisInstructionsTextView = findViewById(R.id.recipe_instructions);
        przepisImage = findViewById(R.id.img);

        Intent intent = getIntent();
        przepisTitleTextView.setText(intent.getStringExtra(RECIPE_INFO_TITLE));
        przepisIngredientsTextView.setText(getFormattedIngredients(intent.getStringExtra(RECIPE_INFO_INGREDIENTS)));
        przepisInstructionsTextView.setText(getFormattedInstructions(intent.getStringExtra(RECIPE_INFO_INSTRUCTIONS)));
        String image = intent.getStringExtra(RECIPE_INFO_IMAGE);

        if (image != null) {
            Picasso.with(this)
                    .load(image)
                    .placeholder(R.drawable.meal)
                    .into(przepisImage);
        } else {
            przepisImage.setImageResource(R.drawable.meal);
        }

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

        recipeNameTextView.setTextColor(textColor);
        ingredientsLabelTextView.setTextColor(textColor);
        recipeIngredientsTextView.setTextColor(textColor);
        instructionsLabelTextView.setTextColor(textColor);
        recipeInstructionsTextView.setTextColor(textColor);

        LinearLayout linearLayout = findViewById(R.id.przepisinfo);
        linearLayout.setBackgroundColor(backgroundColor);

        ScrollView scrollView = findViewById(R.id.przepisinfo_scroll);
        scrollView.setBackgroundColor(backgroundColor);
    }

    private String getFormattedIngredients(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> items = Arrays.asList(text.split("\\|"));
        for (String item : items) {
            stringBuilder.append("- ").append(item.trim()).append("\n");
        }
        return stringBuilder.toString();
    }

    private String getFormattedInstructions(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> items = Arrays.asList(text.split("\\."));
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i).trim();
            if (!item.isEmpty()) {
                stringBuilder.append((i + 1) + ". ").append(item).append("\n\n");
            }
        }
        return stringBuilder.toString();
    }
}
