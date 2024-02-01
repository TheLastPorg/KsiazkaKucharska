package com.example.kucharska.api;

import com.example.kucharska.model.Przepis;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

public interface RecipeService {
    @GET("recipe")
    Call<List<Przepis>> getRecipes(@Query("query") String query);
}
