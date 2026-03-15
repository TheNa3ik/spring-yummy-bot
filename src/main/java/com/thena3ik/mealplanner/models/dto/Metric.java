package com.thena3ik.mealplanner.models.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class Metric {
    private double amount;
    @SerializedName("unitShort")
    private String unit;

}