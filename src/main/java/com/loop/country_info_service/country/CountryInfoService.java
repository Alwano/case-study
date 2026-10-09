package com.loop.country_info_service.country;

import com.loop.country_info_service.country.dbEntities.CountryInfo;
import com.loop.country_info_service.country.dbEntities.Language;
import com.loop.country_info_service.country.dto.CountryUpdateRequest;
import com.loop.country_info_service.exceptions.ResourceNotFoundException;
import com.loop.country_info_service.soap.CountrySoapClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.loop.country_info_service.country.dto.CountryInfoResponse;
import com.loop.country_info_service.country.dto.LanguageResponse;
import java.util.List;
import java.util.Locale;

@Service
public class CountryInfoService {

    private final CountryInfoRepository countryInfoRepository;
    private final CountrySoapClient countrySoapClient;

    public CountryInfoService(
            CountryInfoRepository countryInfoRepository,
            CountrySoapClient countrySoapClient) {

        this.countryInfoRepository = countryInfoRepository;
        this.countrySoapClient = countrySoapClient;
    }

    @Transactional
    public CountryInfo createOrUpdateCountry(String countryName) {

        String normalizedName = normalizeCountryName(countryName);

        String isoCode = countrySoapClient.getIsoCode(normalizedName);

        CountrySoapClient.CountrySoapData soapData =
                countrySoapClient.getFullCountryInfo(isoCode);

        CountryInfo country = countryInfoRepository
                .findByIsoCode(soapData.isoCode())
                .orElseGet(CountryInfo::new);

        country.setIsoCode(soapData.isoCode());
        country.setName(soapData.name());
        country.setCapitalCity(soapData.capitalCity());
        country.setPhoneCode(soapData.phoneCode());
        country.setContinentCode(soapData.continentCode());
        country.setCurrencyIsoCode(soapData.currencyIsoCode());
        country.setFlagUrl(soapData.flagUrl());

        country.clearLanguages();

        for (CountrySoapClient.LanguageData languageData
                : soapData.languages()) {

            Language language = new Language();

            language.setIsoCode(languageData.isoCode());
            language.setName(languageData.name());

            country.addLanguage(language);
        }

        // 6. Save country + languages
        return countryInfoRepository.save(country);
    }

    private String normalizeCountryName(String countryName) {

        if (countryName == null || countryName.isBlank()) {
            throw new IllegalArgumentException(
                    "Country name is required"
            );
        }

        String value = countryName.trim().toLowerCase(Locale.ROOT);

        return Character.toUpperCase(value.charAt(0))
                + value.substring(1);
    }
    @Transactional(readOnly = true)
    public CountryInfoResponse toResponse(CountryInfo country) {

        List<LanguageResponse> languages = country.getLanguages()
                .stream()
                .map(language -> new LanguageResponse(
                        language.getId(),
                        language.getIsoCode(),
                        language.getName()
                ))
                .toList();

        return new CountryInfoResponse(
                country.getId(),
                country.getIsoCode(),
                country.getName(),
                country.getCapitalCity(),
                country.getPhoneCode(),
                country.getContinentCode(),
                country.getCurrencyIsoCode(),
                country.getFlagUrl(),
                languages
        );
    }

    @Transactional
    public CountryInfo updateCountry(
            Long id,
            CountryUpdateRequest request) {

        CountryInfo country = countryInfoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Country not found with id: " + id
                        ));

        country.setName(request.name().trim());
        country.setCapitalCity(request.capitalCity());
        country.setPhoneCode(request.phoneCode());
        country.setContinentCode(request.continentCode());
        country.setCurrencyIsoCode(request.currencyIsoCode());
        country.setFlagUrl(request.flagUrl());

        return countryInfoRepository.save(country);
    }
}