package com.sernova.dto;

import java.util.List;

public record PersonWithAddressesDto(Long id,
                                     String firstName,
                                     String lastName,
                                     List<AddressDto> addresses
) {}