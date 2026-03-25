package com.thena3ik.yummybot.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UnitInfo(
        double amount,
        @JsonProperty("unitShort") String unit
) {}