package com.example.kucharska;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SensorDataListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d("MainActivity", "Start");
        Intent serviceIntent = new Intent(MainActivity.this, SensorService.class);
        startService(serviceIntent);
        SensorService.setSensorDataListener(this);

        Log.d("MainActivity", "Start sensor");
    }


    @Override
    public void onColorsChanged(int textColor, int backgroundColor) {
        Log.d("MainActivity", "onColorsChanged");

        RelativeLayout relativeLayout = findViewById(R.id.main_view);
        relativeLayout.setBackgroundColor(backgroundColor);
    }

    private boolean checkNullOrEmpty(String text) {
        Log.d("MainActivity", "checkNullOrEmpty");
        return text != null && !TextUtils.isEmpty(text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("MainActivity", "onCreateOptionsMenu");
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.przepis_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);

        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("MainActivity", query);
                fetchRecipesData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Log.d("MainActivity", "onQueryTextChange");
                return false;
            }
        });

        return true;
    }

    private void fetchRecipesData(String query) {
        Log.d("MainActivity", "fetchRecipesData");
        String finalQuery = prepareQuery(query);
        Log.d("MainActivity", finalQuery);
        RecipeService recipeService = RetrofitInstance.getRetrofitInstance().create(RecipeService.class);

        Call<List<Przepis>> call = recipeService.getRecipes(query);


        call.enqueue(new Callback<List<Przepis>>() {
            @Override
            public void onResponse(Call<List<Przepis>> call, @NonNull Response<List<Przepis>> response) {
                Log.d("MainActivity", "onResponse");
                if(response != null) {
                    Log.d("MainActivity", response.toString());
                    setupPrzepisyListView(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Przepis>> call, @NonNull Throwable t) {
                Log.d("MainActivity", t.toString());
                Snackbar.make(findViewById(R.id.main_view), "Error while fetching data", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private String prepareQuery(String query) {
        Log.d("MainActivity", "prepareQuery");
        String[] queryParts = query.split("\\s+");
        return TextUtils.join("+", queryParts);
    }

    private void setupPrzepisyListView(List<Przepis> przepisy) {
        Log.d("MainActivity", "setupPrzepisyListView");
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        PrzepisAdapter adapter = new PrzepisAdapter();
        adapter.setPrzepisy(przepisy);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private class PrzepisHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView titleTextView;
        private TextView ingredientsTextView;
        private TextView instructionsTextView;
        private TextView servingsTextView;
        private ImageView imageView;
        private Przepis przepis;

        public PrzepisHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.przepis_list_item, parent, false));
            Log.d("MainActivity", "PrzepisHolder");
            itemView.setOnClickListener(this);
            titleTextView = itemView.findViewById(R.id.recipe_name);
            ingredientsTextView = itemView.findViewById(R.id.recipe_ingredients);
            instructionsTextView = itemView.findViewById(R.id.recipe_instructions);
            servingsTextView = itemView.findViewById(R.id.recipe_servings);
            imageView = itemView.findViewById(R.id.img);
        }

        public void bind(Przepis przepis) {
            Log.d("MainActivity", "bind");
            if(przepis != null && checkNullOrEmpty(przepis.getTitle())) {
                Log.d("MainActivity", "bind2");
                this.przepis = przepis;
                Log.d("MainActivity", przepis.getTitle());
                titleTextView.setText(przepis.getTitle());
                Log.d("MainActivity", "titleTextView set");
                servingsTextView.setText(String.valueOf(przepis.getServings()));
                Log.d("MainActivity", "servingsTextView set");
                String image = przepis.getImage();
                if (image != null) {
                    Log.d("MainActivity", "bind3");
                    Picasso.with(itemView.getContext())
                            .load(image)
                            .placeholder(R.drawable.meal)
                            .into(imageView);
                }
                else {
                    Log.d("MainActivity", "bind4");
                    imageView.setImageResource(R.drawable.meal);
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (przepis != null) {
                Log.d("Click", "onClick");
                Intent intent = new Intent(MainActivity.this, PrzepisInfo.class);
                Log.d("Click", "intent");
                intent.putExtra(PrzepisInfo.RECIPE_INFO_TITLE, przepis.getTitle());
                Log.d("Click", intent.getStringExtra(PrzepisInfo.RECIPE_INFO_TITLE));
                intent.putExtra(PrzepisInfo.RECIPE_INFO_INGREDIENTS, przepis.getIngredients());
                Log.d("Click", intent.getStringExtra(PrzepisInfo.RECIPE_INFO_INGREDIENTS));
                intent.putExtra(PrzepisInfo.RECIPE_INFO_INSTRUCTIONS, przepis.getInstructions());
                Log.d("Click", intent.getStringExtra(PrzepisInfo.RECIPE_INFO_INSTRUCTIONS));
                intent.putExtra(PrzepisInfo.RECIPE_INFO_SERVINGS, przepis.getServings());
                Log.d("Click", intent.getStringExtra(PrzepisInfo.RECIPE_INFO_SERVINGS));
                intent.putExtra(PrzepisInfo.RECIPE_INFO_IMAGE, przepis.getImage());
                Log.d("Click", intent.toString());
                startActivity(intent);
            }
        }
    }

    private class PrzepisAdapter extends RecyclerView.Adapter<PrzepisHolder> {
        private List<Przepis> przepisy;

        @NotNull
        @Override
        public PrzepisHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            Log.d("MainActivity", "PrzepisAdapter");
            return new PrzepisHolder(getLayoutInflater(), parent);
        }

        @Override
        public void onBindViewHolder(PrzepisHolder holder, int position) {
            Log.d("MainActivity", "onBindViewHolder");
           if(przepisy != null) {
               Log.d("MainActivity", "onBindViewHolder2");
                    Przepis przepis = przepisy.get(position);
                    holder.bind(przepis);
           }
           else {
               Log.d("MainActivity", "No recipes");
           }
        }

        @Override
        public int getItemCount() {
            Log.d("MainActivity", "getItemCount");
            if(przepisy != null) {
                Log.d("MainActivity", "getItemCount2");
                return przepisy.size();
            }
            else {
                Log.d("MainActivity", "getItemCount3");
                return 0;
            }
        }

        public void setPrzepisy(List<Przepis> przepisy) {
            Log.d("MainActivity", "setPrzepisy");
            this.przepisy = przepisy;
            notifyDataSetChanged();
        }
    }
}