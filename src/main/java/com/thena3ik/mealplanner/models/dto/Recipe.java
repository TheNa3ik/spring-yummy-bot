package com.thena3ik.mealplanner.models.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class Recipe {
    private int id;
    private String title;
    private String image;
    private int servings;
    @SerializedName("readyInMinutes")
    private int readyInMinutes;
}