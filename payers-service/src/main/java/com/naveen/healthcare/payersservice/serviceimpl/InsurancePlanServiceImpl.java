package com.naveen.healthcare.payersservice.serviceimpl;

import com.naveen.healthcare.payersservice.dto.request.CreateInsurancePlanRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdateInsurancePlanRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdateInsurancePlanStatusRequest;
import com.naveen.healthcare.payersservice.dto.response.InsurancePlanResponse;
import com.naveen.healthcare.payersservice.entity.InsurancePlan;
import com.naveen.healthcare.payersservice.entity.PayerOrganization;
import com.naveen.healthcare.payersservice.enums.InsurancePlanStatus;
import com.naveen.healthcare.payersservice.enums.PayerStatus;
import com.naveen.healthcare.payersservice.exception.DuplicateInsurancePlanException;
import com.naveen.healthcare.payersservice.exception.InsurancePlanNotFoundException;
import com.naveen.healthcare.payersservice.exception.InvalidInsurancePlanDataException;
import com.naveen.healthcare.payersservice.exception.InvalidPayerStatusException;
import com.naveen.healthcare.payersservice.exception.PayerNotFoundException;
import com.naveen.healthcare.payersservice.repository.InsurancePlanRepository;
import com.naveen.healthcare.payersservice.repository.PayerRepository;
import com.naveen.healthcare.payersservice.service.InsurancePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InsurancePlanServiceImpl
        implements InsurancePlanService {

    private static final BigDecimal MAXIMUM_PERCENTAGE =
            BigDecimal.valueOf(100);

    private final InsurancePlanRepository insurancePlanRepository;

    private final PayerRepository payerRepository;

    // =====================================================
    // PAYER: CREATE INSURANCE PLAN
    // =====================================================

    @Override
    @Transactional
    public InsurancePlanResponse createPlan(
            String authenticatedEmail,
            CreateInsurancePlanRequest request
    ) {

        PayerOrganization payerOrganization =
                findActivePayerByEmail(authenticatedEmail);

        validatePlanData(
                request.getMinimumAge(),
                request.getMaximumAge(),
                request.getCopayPercentage(),
                request.getCoinsurancePercentage()
        );

        String normalizedPlanCode =
                normalizePlanCode(request.getPlanCode());

        if (insurancePlanRepository
                .existsByPlanCodeIgnoreCase(normalizedPlanCode)) {

            throw new DuplicateInsurancePlanException(
                    "Insurance plan code already exists: "
                            + normalizedPlanCode
            );
        }

        InsurancePlan insurancePlan =
                InsurancePlan.builder()
                        .planName(
                                normalizeRequiredText(
                                        request.getPlanName()
                                )
                        )
                        .planCode(normalizedPlanCode)
                        .description(
                                normalizeRequiredText(
                                        request.getDescription()
                                )
                        )
                        .coverageAmount(
                                request.getCoverageAmount()
                        )
                        .monthlyPremium(
                                request.getMonthlyPremium()
                        )
                        .annualDeductible(
                                request.getAnnualDeductible()
                        )
                        .copayPercentage(
                                request.getCopayPercentage()
                        )
                        .coinsurancePercentage(
                                request.getCoinsurancePercentage()
                        )
                        .minimumAge(
                                request.getMinimumAge()
                        )
                        .maximumAge(
                                request.getMaximumAge()
                        )
                        .policyDurationMonths(
                                request.getPolicyDurationMonths()
                        )
                        .hospitalizationCoverage(
                                request.getHospitalizationCoverage()
                        )
                        .outpatientCoverage(
                                request.getOutpatientCoverage()
                        )
                        .prescriptionCoverage(
                                request.getPrescriptionCoverage()
                        )
                        .maternityCoverage(
                                request.getMaternityCoverage()
                        )
                        .waitingPeriodDays(
                                request.getWaitingPeriodDays()
                        )
                        .payerOrganization(payerOrganization)
                        .status(InsurancePlanStatus.DRAFT)
                        .build();

        InsurancePlan savedPlan =
                insurancePlanRepository.save(insurancePlan);

        return mapToResponse(savedPlan);
    }

    // =====================================================
    // PAYER: GET OWN INSURANCE PLANS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePlanResponse> getMyPlans(
            String authenticatedEmail
    ) {

        PayerOrganization payerOrganization =
                findPayerByEmail(authenticatedEmail);

        return insurancePlanRepository
                .findAllByPayerOrganizationAdminEmailIgnoreCaseOrderByCreatedAtDesc(
                        payerOrganization.getAdminEmail()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =====================================================
    // PAYER: GET OWN INSURANCE PLAN BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public InsurancePlanResponse getMyPlanById(
            String authenticatedEmail,
            UUID planId
    ) {

        InsurancePlan insurancePlan =
                findOwnedPlan(
                        authenticatedEmail,
                        planId
                );

        return mapToResponse(insurancePlan);
    }

    // =====================================================
    // PAYER: UPDATE OWN INSURANCE PLAN
    // =====================================================

    @Override
    @Transactional
    public InsurancePlanResponse updateMyPlan(
            String authenticatedEmail,
            UUID planId,
            UpdateInsurancePlanRequest request
    ) {

        findActivePayerByEmail(authenticatedEmail);

        InsurancePlan insurancePlan =
                findOwnedPlan(
                        authenticatedEmail,
                        planId
                );

        validatePlanData(
                request.getMinimumAge(),
                request.getMaximumAge(),
                request.getCopayPercentage(),
                request.getCoinsurancePercentage()
        );

        insurancePlan.setPlanName(
                normalizeRequiredText(
                        request.getPlanName()
                )
        );

        insurancePlan.setDescription(
                normalizeRequiredText(
                        request.getDescription()
                )
        );

        insurancePlan.setCoverageAmount(
                request.getCoverageAmount()
        );

        insurancePlan.setMonthlyPremium(
                request.getMonthlyPremium()
        );

        insurancePlan.setAnnualDeductible(
                request.getAnnualDeductible()
        );

        insurancePlan.setCopayPercentage(
                request.getCopayPercentage()
        );

        insurancePlan.setCoinsurancePercentage(
                request.getCoinsurancePercentage()
        );

        insurancePlan.setMinimumAge(
                request.getMinimumAge()
        );

        insurancePlan.setMaximumAge(
                request.getMaximumAge()
        );

        insurancePlan.setPolicyDurationMonths(
                request.getPolicyDurationMonths()
        );

        insurancePlan.setHospitalizationCoverage(
                request.getHospitalizationCoverage()
        );

        insurancePlan.setOutpatientCoverage(
                request.getOutpatientCoverage()
        );

        insurancePlan.setPrescriptionCoverage(
                request.getPrescriptionCoverage()
        );

        insurancePlan.setMaternityCoverage(
                request.getMaternityCoverage()
        );

        insurancePlan.setWaitingPeriodDays(
                request.getWaitingPeriodDays()
        );

        InsurancePlan updatedPlan =
                insurancePlanRepository.save(insurancePlan);

        return mapToResponse(updatedPlan);
    }

    // =====================================================
    // PAYER: UPDATE OWN INSURANCE PLAN STATUS
    // =====================================================

    @Override
    @Transactional
    public InsurancePlanResponse updateMyPlanStatus(
            String authenticatedEmail,
            UUID planId,
            UpdateInsurancePlanStatusRequest request
    ) {

        findActivePayerByEmail(authenticatedEmail);

        InsurancePlan insurancePlan =
                findOwnedPlan(
                        authenticatedEmail,
                        planId
                );

        InsurancePlanStatus requestedStatus =
                request.getStatus();

        validateStatusTransition(
                insurancePlan.getStatus(),
                requestedStatus
        );

        insurancePlan.setStatus(requestedStatus);

        InsurancePlan updatedPlan =
                insurancePlanRepository.save(insurancePlan);

        return mapToResponse(updatedPlan);
    }

    // =====================================================
    // MEMBER: GET AVAILABLE ACTIVE INSURANCE PLANS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePlanResponse> getAvailablePlans() {

        return insurancePlanRepository
                .findAllByStatusOrderByCreatedAtDesc(
                        InsurancePlanStatus.ACTIVE
                )
                .stream()
                .filter(insurancePlan ->
                        insurancePlan
                                .getPayerOrganization()
                                .getStatus()
                                == PayerStatus.ACTIVE
                )
                .map(this::mapToResponse)
                .toList();
    }

    // =====================================================
    // MEMBER: GET AVAILABLE ACTIVE PLAN BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public InsurancePlanResponse getAvailablePlanById(
            UUID planId
    ) {

        InsurancePlan insurancePlan =
                insurancePlanRepository
                        .findByIdAndStatus(
                                planId,
                                InsurancePlanStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new InsurancePlanNotFoundException(
                                        "Active insurance plan not found with ID: "
                                                + planId
                                )
                        );

        if (insurancePlan
                .getPayerOrganization()
                .getStatus() != PayerStatus.ACTIVE) {

            throw new InsurancePlanNotFoundException(
                    "Active insurance plan not found with ID: "
                            + planId
            );
        }

        return mapToResponse(insurancePlan);
    }

    // =====================================================
    // ADMIN: GET ALL INSURANCE PLANS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePlanResponse> getAllPlansForAdmin() {

        return insurancePlanRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =====================================================
    // ADMIN: GET ANY INSURANCE PLAN BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public InsurancePlanResponse getAnyPlanByIdForAdmin(
            UUID planId
    ) {

        InsurancePlan insurancePlan =
                insurancePlanRepository
                        .findById(planId)
                        .orElseThrow(() ->
                                new InsurancePlanNotFoundException(
                                        "Insurance plan not found with ID: "
                                                + planId
                                )
                        );

        return mapToResponse(insurancePlan);
    }

    // =====================================================
    // FIND PAYER USING AUTHENTICATED JWT EMAIL
    // =====================================================

    private PayerOrganization findPayerByEmail(
            String authenticatedEmail
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        return payerRepository
                .findByAdminEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new PayerNotFoundException(
                                "Payer organization not found for "
                                        + "authenticated email: "
                                        + normalizedEmail
                        )
                );
    }

    // =====================================================
    // FIND ACTIVE PAYER USING AUTHENTICATED JWT EMAIL
    // =====================================================

    private PayerOrganization findActivePayerByEmail(
            String authenticatedEmail
    ) {

        PayerOrganization payerOrganization =
                findPayerByEmail(authenticatedEmail);

        if (payerOrganization.getStatus()
                != PayerStatus.ACTIVE) {

            throw new InvalidPayerStatusException(
                    "Payer organization must be ACTIVE "
                            + "to manage insurance plans"
            );
        }

        return payerOrganization;
    }

    // =====================================================
    // FIND PLAN OWNED BY AUTHENTICATED PAYER
    // =====================================================

    private InsurancePlan findOwnedPlan(
            String authenticatedEmail,
            UUID planId
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        return insurancePlanRepository
                .findByIdAndPayerOrganizationAdminEmailIgnoreCase(
                        planId,
                        normalizedEmail
                )
                .orElseThrow(() ->
                        new InsurancePlanNotFoundException(
                                "Insurance plan not found for "
                                        + "the authenticated Payer"
                        )
                );
    }

    // =====================================================
    // INSURANCE PLAN BUSINESS VALIDATION
    // =====================================================

    private void validatePlanData(
            Integer minimumAge,
            Integer maximumAge,
            BigDecimal copayPercentage,
            BigDecimal coinsurancePercentage
    ) {

        if (minimumAge > maximumAge) {

            throw new InvalidInsurancePlanDataException(
                    "Minimum age cannot be greater than maximum age"
            );
        }

        BigDecimal totalMemberPercentage =
                copayPercentage.add(
                        coinsurancePercentage
                );

        if (totalMemberPercentage.compareTo(
                MAXIMUM_PERCENTAGE
        ) > 0) {

            throw new InvalidInsurancePlanDataException(
                    "The combined copay and coinsurance "
                            + "percentage cannot exceed 100"
            );
        }
    }

    // =====================================================
    // INSURANCE PLAN STATUS TRANSITION VALIDATION
    // =====================================================

    private void validateStatusTransition(
            InsurancePlanStatus currentStatus,
            InsurancePlanStatus requestedStatus
    ) {

        if (currentStatus == requestedStatus) {

            throw new InvalidInsurancePlanDataException(
                    "Insurance plan is already in "
                            + requestedStatus
                            + " status"
            );
        }

        boolean validTransition =
                switch (currentStatus) {

                    case DRAFT ->
                            requestedStatus
                                    == InsurancePlanStatus.ACTIVE
                                    || requestedStatus
                                    == InsurancePlanStatus.INACTIVE;

                    case ACTIVE ->
                            requestedStatus
                                    == InsurancePlanStatus.INACTIVE;

                    case INACTIVE ->
                            requestedStatus
                                    == InsurancePlanStatus.ACTIVE
                                    || requestedStatus
                                    == InsurancePlanStatus.DRAFT;
                };

        if (!validTransition) {

            throw new InvalidInsurancePlanDataException(
                    "Insurance plan status cannot change from "
                            + currentStatus
                            + " to "
                            + requestedStatus
            );
        }
    }

    // =====================================================
    // ENTITY TO RESPONSE DTO
    // =====================================================

    private InsurancePlanResponse mapToResponse(
            InsurancePlan insurancePlan
    ) {

        PayerOrganization payerOrganization =
                insurancePlan.getPayerOrganization();

        return InsurancePlanResponse.builder()
                .id(
                        insurancePlan.getId()
                )
                .planName(
                        insurancePlan.getPlanName()
                )
                .planCode(
                        insurancePlan.getPlanCode()
                )
                .description(
                        insurancePlan.getDescription()
                )
                .coverageAmount(
                        insurancePlan.getCoverageAmount()
                )
                .monthlyPremium(
                        insurancePlan.getMonthlyPremium()
                )
                .annualDeductible(
                        insurancePlan.getAnnualDeductible()
                )
                .copayPercentage(
                        insurancePlan.getCopayPercentage()
                )
                .coinsurancePercentage(
                        insurancePlan.getCoinsurancePercentage()
                )
                .minimumAge(
                        insurancePlan.getMinimumAge()
                )
                .maximumAge(
                        insurancePlan.getMaximumAge()
                )
                .policyDurationMonths(
                        insurancePlan.getPolicyDurationMonths()
                )
                .hospitalizationCoverage(
                        insurancePlan.getHospitalizationCoverage()
                )
                .outpatientCoverage(
                        insurancePlan.getOutpatientCoverage()
                )
                .prescriptionCoverage(
                        insurancePlan.getPrescriptionCoverage()
                )
                .maternityCoverage(
                        insurancePlan.getMaternityCoverage()
                )
                .waitingPeriodDays(
                        insurancePlan.getWaitingPeriodDays()
                )
                .status(
                        insurancePlan.getStatus()
                )
                .payerId(
                        payerOrganization.getId()
                )
                .payerOrganizationName(
                        payerOrganization.getOrganizationName()
                )
                .createdAt(
                        insurancePlan.getCreatedAt()
                )
                .updatedAt(
                        insurancePlan.getUpdatedAt()
                )
                .build();
    }

    // =====================================================
    // DATA NORMALIZATION
    // =====================================================

    private String normalizeEmail(
            String email
    ) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizePlanCode(
            String planCode
    ) {

        return planCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeRequiredText(
            String value
    ) {

        return value.trim();
    }
}