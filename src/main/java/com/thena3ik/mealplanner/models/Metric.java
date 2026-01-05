package com.thena3ik.mealplanner.models;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class Metric {
    private double amount;
    @SerializedName("unitShort")
    private String unit;

}