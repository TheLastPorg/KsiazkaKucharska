package com.example.kucharska;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SensorDataListener{

    private PrzepisRepository przepisRepository;
    private PrzepisAdapter przepisAdapter;
    private PrzepisViewModel przepisViewModel;

    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_add) {
                    Intent intent = new Intent(MainActivity.this, DodajPrzepisActivity.class);
                    startActivity(intent);
                    return true;
                }
                else if(item.getItemId() == R.id.navigation_home) {
                        return true;
                }
                return false;
            }
        });
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != getPackageManager().PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
        }

        Log.d("MainActivity", "Start");
        Intent serviceIntent = new Intent(MainActivity.this, SensorService.class);
        startService(serviceIntent);
        SensorService.setSensorDataListener(this);
        przepisRepository = new PrzepisRepository(getApplication());
        przepisAdapter = new PrzepisAdapter();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(przepisAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d("MainActivity", "Start sensor");

        przepisViewModel = new ViewModelProvider(this).get(PrzepisViewModel.class);
        przepisViewModel.findAll().observe(this, przepisAdapter::setPrzepisy);
    }


    @Override
    public void onColorsChanged(int textColor, int backgroundColor) {
        Log.d("MainActivity", "onColorsChanged");

        RelativeLayout relativeLayout = findViewById(R.id.main_view);
        relativeLayout.setBackgroundColor(backgroundColor);
    }

    /*public void loadRecipesFromDatabase() {
        Log.d("MainActivity", "loadRecipesFromDatabase");
        przepisRepository.getPrzepisy().observe(this, new Observer<List<Przepis>>() {
            @Override
            public void onChanged(List<Przepis> przepis) {
                Log.d("MainActivity", "loadRecipesFromDatabase2");
                if (przepis != null) {
                    Log.d("MainActivity", "loadRecipesFromDatabase3");
                    przepisAdapter.setPrzepisy(przepis);
                }
            }
        });
    }*/

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
                if (TextUtils.isEmpty(query)) {
                    przepisAdapter.clearPrzepisy(); // Usuń wyniki z RecyclerView
                } else {
                    fetchRecipesData(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Log.d("MainActivity", "onQueryTextChange");
                if (TextUtils.isEmpty(newText)) {
                    przepisAdapter.clearPrzepisy(); // Usuń wyniki z RecyclerView
                }
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
                List<Przepis> przepisy = response.body();

                if(response != null && !przepisy.isEmpty()) {
                    przepisAdapter.setPrzepisy(przepisy);
                    Log.d("MainActivity", response.toString());
                } else {
                    przepisAdapter.clearPrzepisy();
                    Snackbar.make(findViewById(R.id.main_view), "No results found", Snackbar.LENGTH_LONG).show();
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

    private class PrzepisHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
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
            itemView.setOnLongClickListener(this);
            titleTextView = itemView.findViewById(R.id.recipe_name);
            ingredientsTextView = itemView.findViewById(R.id.recipe_ingredients);
            instructionsTextView = itemView.findViewById(R.id.recipe_instructions);
            servingsTextView = itemView.findViewById(R.id.recipe_servings);
            imageView = itemView.findViewById(R.id.img);
            Log.d("Image", imageView.toString());
        }

        public void bind(Przepis przepis) {
            Log.d("MainActivity", "bind");
            if (przepis != null && checkNullOrEmpty(przepis.getTitle())) {
                Log.d("MainActivity", "bind2");
                this.przepis = przepis;
                Log.d("MainActivity", przepis.getTitle());
                titleTextView.setText(przepis.getTitle());
                Log.d("MainActivity", "titleTextView set");
                servingsTextView.setText(String.valueOf(przepis.getServings()));
                Log.d("MainActivity", "servingsTextView set");
                String image = przepis.getImage();
                Log.d("zdj", "zdj " + image);

                if (image != null) {
                    // Użyj metody loadImageFromUri do załadowania obrazu z galerii
                    loadImageFromUri(Uri.parse(image));
                } else {
                    // Jeśli nie ma obrazu, użyj domyślnej grafiki
                    Log.d("Image", "bind4");
                    imageView.setImageResource(R.drawable.meal);
                }
            }
        }


        @Override
        public void onClick(View v) {
            if (przepis != null) {
                Intent intent = new Intent(MainActivity.this, PrzepisInfo.class);
                intent.putExtra(PrzepisInfo.RECIPE_INFO_TITLE, przepis.getTitle());
                intent.putExtra(PrzepisInfo.RECIPE_INFO_INGREDIENTS, przepis.getIngredients());
                intent.putExtra(PrzepisInfo.RECIPE_INFO_INSTRUCTIONS, przepis.getInstructions());
                intent.putExtra(PrzepisInfo.RECIPE_INFO_SERVINGS, przepis.getServings());
                intent.putExtra(PrzepisInfo.RECIPE_INFO_IMAGE, przepis.getImage());
                intent.putExtra(PrzepisInfo.PRZEPIS, przepis);
                startActivity(intent);
            }
        }
        @Override
        public boolean onLongClick(View v) {
            if (przepis != null) {
                przepisViewModel.delete(this.przepis);
                return true;
            }
            return false;
        }

        private void loadImageFromUri(Uri uri) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("MainActivity", "FileNotFoundException: " + e.getMessage());
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
            if(przepisy != null) {
                return przepisy.size();
            }
            else {
                return 0;
            }
        }

        public void setPrzepisy(List<Przepis> przepisy) {
            Log.d("MainActivity", "setPrzepisy");
            this.przepisy = przepisy;
            notifyDataSetChanged();
        }

        public void clearPrzepisy() {
            Log.d("MainActivity", "clearPrzepisy");
            przepisy.clear();
            notifyDataSetChanged();
        }
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
    public void onHintColorChanged(int hintColor) {
        return;
    }
}