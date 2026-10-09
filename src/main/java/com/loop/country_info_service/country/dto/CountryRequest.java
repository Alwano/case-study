package com.loop.country_info_service.country.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CountryRequest(
        @NotBlank(message = "Country name is required")
        String name,

//        @NotBlank(message = "ISO code is required")
        @Size(min = 2, max = 3)
        String isoCode
) {
}