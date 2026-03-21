package com.thena3ik.mealplanner.model.dto;

import com.google.gson.annotations.SerializedName;

public record Measures(
        UnitInfo metric,
        @SerializedName("us") UnitInfo imperial)
{}