package com.naveen.healthcare.payersservice.scheduler;

import com.naveen.healthcare.payersservice.entity.InsurancePolicy;
import com.naveen.healthcare.payersservice.enums.PolicyStatus;
import com.naveen.healthcare.payersservice.repository.InsurancePolicyRepository;
import com.naveen.healthcare.payersservice.service.InsuranceEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PolicyExpirationScheduler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    PolicyExpirationScheduler.class
            );

    private static final List<PolicyStatus>
            EXPIRABLE_STATUSES =
            List.of(
                    PolicyStatus.ACTIVE,
                    PolicyStatus.SUSPENDED
            );

    private final InsurancePolicyRepository
            insurancePolicyRepository;

    private final InsuranceEventPublisher
            insuranceEventPublisher;

    @Scheduled(
            cron = "${insurance.policy-expiration.cron}",
            zone = "${insurance.policy-expiration.zone}"
    )
    @Transactional
    public void expirePolicies() {

        LocalDate today =
                LocalDate.now();

        List<InsurancePolicy> expiredPolicies =
                insurancePolicyRepository
                        .findAllByStatusInAndCoverageEndDateBefore(
                                EXPIRABLE_STATUSES,
                                today
                        );

        if (expiredPolicies.isEmpty()) {

            LOGGER.debug(
                    "No insurance policies require expiration"
            );

            return;
        }

        for (InsurancePolicy policy : expiredPolicies) {

            policy.setStatus(
                    PolicyStatus.EXPIRED
            );

            InsurancePolicy updatedPolicy =
                    insurancePolicyRepository.saveAndFlush(
                            policy
                    );

            insuranceEventPublisher.publishPolicyExpired(
                    updatedPolicy
            );

            LOGGER.info(
                    "Insurance policy expired. "
                            + "Policy ID: {}, policy number: {}",
                    updatedPolicy.getId(),
                    updatedPolicy.getPolicyNumber()
            );
        }
    }
}