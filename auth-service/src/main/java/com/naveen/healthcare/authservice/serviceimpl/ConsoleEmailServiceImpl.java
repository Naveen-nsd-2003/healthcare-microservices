package com.naveen.healthcare.authservice.serviceimpl;

import com.naveen.healthcare.authservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


@Service
@Profile("dev")
public class ConsoleEmailServiceImpl
        implements EmailService {


    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ConsoleEmailServiceImpl.class
            );


    // =====================================================
    // DISPLAY OTP IN DEVELOPMENT CONSOLE
    // =====================================================

    @Override
    public void sendOtpEmail(
            String to,
            String firstName,
            String otp
    ) {


        LOGGER.info(
                """
                
                =================================================
                DEVELOPMENT EMAIL - OTP VERIFICATION
                =================================================
                Recipient : {}
                Name      : {}
                OTP       : {}
                Validity  : 15 minutes
                =================================================
                """,
                to,
                firstName,
                otp
        );
    }


    // =====================================================
    // DISPLAY TEMPORARY PASSWORD IN DEVELOPMENT CONSOLE
    // =====================================================

    @Override
    public void sendTemporaryPasswordEmail(
            String to,
            String firstName,
            String temporaryPassword
    ) {


        LOGGER.info(
                """
                
                =================================================
                DEVELOPMENT EMAIL - ACCOUNT CREATED
                =================================================
                Recipient          : {}
                Name               : {}
                Temporary Password : {}
                First Login        : Password change required
                =================================================
                """,
                to,
                firstName,
                temporaryPassword
        );
    }
}