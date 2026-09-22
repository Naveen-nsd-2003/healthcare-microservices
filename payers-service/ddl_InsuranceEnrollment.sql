CREATE TABLE insurance_enrollments
(
    id                            BINARY(16)   NOT NULL,
    member_id                     BINARY(16)   NOT NULL,
    member_email                  VARCHAR(150) NOT NULL,
    plan_id                       BINARY(16)   NOT NULL,
    payer_id                      BINARY(16)   NOT NULL,
    requested_coverage_start_date date         NOT NULL,
    nominee_name                  VARCHAR(150) NOT NULL,
    nominee_relationship          VARCHAR(30)  NOT NULL,
    nominee_contact_number        VARCHAR(20)  NOT NULL,
    status                        VARCHAR(20)  NOT NULL,
    review_remarks                VARCHAR(500) NULL,
    reviewed_at                   datetime     NULL,
    created_at                    datetime     NOT NULL,
    updated_at                    datetime     NOT NULL,
    CONSTRAINT pk_insurance_enrollments PRIMARY KEY (id)
);

CREATE INDEX idx_enrollment_created_at ON insurance_enrollments (created_at);

CREATE INDEX idx_enrollment_member_email ON insurance_enrollments (member_email);

CREATE INDEX idx_enrollment_member_id ON insurance_enrollments (member_id);

CREATE INDEX idx_enrollment_status ON insurance_enrollments (status);

ALTER TABLE insurance_enrollments
    ADD CONSTRAINT FK_ENROLLMENT_INSURANCE_PLAN FOREIGN KEY (plan_id) REFERENCES insurance_plans (id);

CREATE INDEX idx_enrollment_plan_id ON insurance_enrollments (plan_id);

ALTER TABLE insurance_enrollments
    ADD CONSTRAINT FK_ENROLLMENT_PAYER_ORGANIZATION FOREIGN KEY (payer_id) REFERENCES payer_organizations (id);

CREATE INDEX idx_enrollment_payer_id ON insurance_enrollments (payer_id);