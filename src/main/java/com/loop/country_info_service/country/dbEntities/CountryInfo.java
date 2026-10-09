package com.loop.country_info_service.country.dbEntities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "country_info",
        uniqueConstraints = @UniqueConstraint(columnNames = "iso_code")
)
public class CountryInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iso_code", nullable = false, unique = true, length = 3)
    private String isoCode;

    @Column(nullable = false)
    private String name;

    private String capitalCity;
    private String phoneCode;
    private String continentCode;
    private String currencyIsoCode;

    @Column(length = 1000)
    private String flagUrl;

    @OneToMany(
            mappedBy = "country",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Language> languages = new ArrayList<>();

    public CountryInfo() {
    }

    public void addLanguage(Language language) {
        languages.add(language);
        language.setCountry(this);
    }

    public void clearLanguages() {
        languages.clear();
    }

    public Long getId() { return id; }
    public String getIsoCode() { return isoCode; }
    public void setIsoCode(String isoCode) { this.isoCode = isoCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCapitalCity() { return capitalCity; }
    public void setCapitalCity(String capitalCity) {
        this.capitalCity = capitalCity;
    }

    public String getPhoneCode() { return phoneCode; }
    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public String getContinentCode() { return continentCode; }
    public void setContinentCode(String continentCode) {
        this.continentCode = continentCode;
    }

    public String getCurrencyIsoCode() { return currencyIsoCode; }
    public void setCurrencyIsoCode(String currencyIsoCode) {
        this.currencyIsoCode = currencyIsoCode;
    }

    public String getFlagUrl() { return flagUrl; }
    public void setFlagUrl(String flagUrl) { this.flagUrl = flagUrl; }

    public List<Language> getLanguages() { return languages; }
}
