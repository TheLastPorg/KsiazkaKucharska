package com.example.kucharska.model;

import android.media.Image;
import android.widget.ImageView;

import com.google.gson.annotations.SerializedName;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "przepisy")
public class Przepis implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("ingredients")
    private String ingredients;
    @SerializedName("instructions")
    private String instructions;
    @SerializedName("servings")
    private String servings;
    @SerializedName("image")
    private String image;
    @SerializedName("query")
    private String query;
    @SerializedName("isFromApi")
    private boolean isFromApi;

    public Przepis(String title, String ingredients, String instructions, String servings, String image) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.servings = servings;

        if(image != null) {
            this.image = image;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getServings() {
        return servings;
    }

    public void setServings(String servings) {
        this.servings = servings;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    public String getQuery() {
        return query;
    }
    public void setQuery(String query) {
        this.query = query;
    }
    public boolean getIsFromApi() {
        return isFromApi;
    }
    public void setFromApi(boolean isFromApi) {
        this.isFromApi = isFromApi;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
