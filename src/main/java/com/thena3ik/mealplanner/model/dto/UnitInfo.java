package com.thena3ik.mealplanner.model.dto;

import com.google.gson.annotations.SerializedName;

public record UnitInfo(
        double amount,
        @SerializedName("unitShort") String unit
) {}