package com.naveen.healthcare.claimservice.util;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Locale;
import java.util.UUID;

@Component
public class ClaimNumberGenerator {

    private static final String CLAIM_PREFIX = "HCCLM";

    private static final int RANDOM_SECTION_LENGTH = 12;

    public String generateClaimNumber() {

        String currentYear =
                String.valueOf(Year.now().getValue());

        String uniqueSection =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(
                                0,
                                RANDOM_SECTION_LENGTH
                        )
                        .toUpperCase(Locale.ROOT);

        return CLAIM_PREFIX
                + "-"
                + currentYear
                + "-"
                + uniqueSection;
    }
}