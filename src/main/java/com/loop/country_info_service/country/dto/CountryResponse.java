package com.loop.country_info_service.country.dto;

public record CountryResponse(
        Long id,
        String name,
        String isoCode
) {}
