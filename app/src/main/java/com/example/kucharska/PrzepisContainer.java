package com.example.kucharska;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PrzepisContainer {
    @SerializedName("przepisy")
    private List<Przepis> przepisy;

    public List<Przepis> getPrzepisy() {
        return przepisy;
    }

    public void setPrzepisy(List<Przepis> przepisy) {
        this.przepisy = przepisy;
    }
}
