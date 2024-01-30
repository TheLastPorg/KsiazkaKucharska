package com.example.kucharska;

import com.google.gson.annotations.SerializedName;

public class Przepis {
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

    public Przepis(String title, String ingredients, String instructions, String servings) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.servings = servings;
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
}
