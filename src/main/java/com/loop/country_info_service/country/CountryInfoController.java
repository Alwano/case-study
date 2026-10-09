package com.loop.country_info_service.country;

import com.loop.country_info_service.country.dbEntities.CountryInfo;
import com.loop.country_info_service.country.dto.CountryInfoResponse;
import com.loop.country_info_service.country.dto.CountryRequest;
import com.loop.country_info_service.country.dto.CountryUpdateRequest;
import com.loop.country_info_service.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
public class CountryInfoController {

    private final CountryInfoService countryInfoService;
    private final CountryInfoRepository countryInfoRepository;

    public CountryInfoController(
            CountryInfoService countryInfoService,
            CountryInfoRepository countryInfoRepository) {

        this.countryInfoService = countryInfoService;
        this.countryInfoRepository = countryInfoRepository;
    }

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.CREATED)
    public CountryInfoResponse syncCountry(
            @Valid @RequestBody CountryRequest request) {

        CountryInfo country =
                countryInfoService.createOrUpdateCountry(request.name());

        return countryInfoService.toResponse(country);
    }

    @GetMapping
    public List<CountryInfoResponse> getAllCountries() {

        return countryInfoRepository.findAll()
                .stream()
                .map(countryInfoService::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public CountryInfoResponse getCountryById(
            @PathVariable Long id) {

        CountryInfo country = countryInfoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Country not found with id: " + id
                        ));

        return countryInfoService.toResponse(country);
    }
    @PutMapping("/{id}")
    public CountryInfoResponse updateCountry(
            @PathVariable Long id,
            @Valid @RequestBody CountryUpdateRequest request) {

        CountryInfo updated =
                countryInfoService.updateCountry(id, request);

        return countryInfoService.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCountry(@PathVariable Long id) {

        if (!countryInfoRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Country not found with id: " + id
            );
        }

        countryInfoRepository.deleteById(id);
    }
}