package com.loop.country_info_service.country.dbEntities;

import jakarta.persistence.*;

@Entity
@Table(name = "languages")
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 3)
    private String isoCode;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_info_id", nullable = false)
    private CountryInfo country;

    public Language() {
    }

    public Long getId() { return id; }

    public String getIsoCode() { return isoCode; }
    public void setIsoCode(String isoCode) { this.isoCode = isoCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CountryInfo getCountry() { return country; }
    public void setCountry(CountryInfo country) {
        this.country = country;
    }
}
