package com.loop.country_info_service.country.dto;

import jakarta.validation.constraints.NotBlank;

public record CountryUpdateRequest(

        @NotBlank(message = "Country name is required")
        String name,

        String capitalCity,

        String phoneCode,

        String continentCode,

        String currencyIsoCode,

        String flagUrl
) {
}