package com.loop.country_info_service.country.dto;

import java.util.List;

public record CountryInfoResponse(
        Long id,
        String isoCode,
        String name,
        String capitalCity,
        String phoneCode,
        String continentCode,
        String currencyIsoCode,
        String flagUrl,
        List<LanguageResponse> languages
) {
}