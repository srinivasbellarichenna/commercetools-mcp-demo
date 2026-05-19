package com.example.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequestDTO {
    @NotBlank(message = "Country is required (ISO 3166-1 alpha-2 format, e.g., US, DE)")
    private String country;
    
    private String id;
    private String key;
    private String title;
    private String salutation;
    private String firstName;
    private String lastName;
    
    @NotBlank(message = "Street name is required")
    private String streetName;
    
    private String streetNumber;
    private String additionalStreetInfo;
    private String postalCode;
    
    @NotBlank(message = "City is required")
    private String city;
    
    private String region;
    private String state;
    private String company;
    private String department;
    private String building;
    private String apartment;
    private String pOBox;
    private String phone;
    private String mobile;
    private String email;
    private String fax;
    private String additionalAddressInfo;
    private String externalId;
}
