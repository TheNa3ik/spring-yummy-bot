package com.thena3ik.yummybot.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Measures(
        UnitInfo metric,
        @JsonProperty("us") UnitInfo imperial)
{}