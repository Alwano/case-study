package com.loop.country_info_service.country;

import com.loop.country_info_service.country.dbEntities.CountryInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryInfoRepository
        extends JpaRepository<CountryInfo, Long> {

    Optional<CountryInfo> findByIsoCode(String isoCode);
}
