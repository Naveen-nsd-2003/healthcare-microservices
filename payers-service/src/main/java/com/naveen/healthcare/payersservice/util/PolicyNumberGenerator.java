package com.naveen.healthcare.payersservice.util;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

@Component
public class PolicyNumberGenerator {

    private static final String PREFIX = "HCPOL";

    private static final int UNIQUE_PART_LENGTH = 12;

    public String generatePolicyNumber() {

        int currentYear =
                Year.now(ZoneOffset.UTC).getValue();

        String uniquePart =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, UNIQUE_PART_LENGTH)
                        .toUpperCase(Locale.ROOT);

        return PREFIX
                + "-"
                + currentYear
                + "-"
                + uniquePart;
    }
}