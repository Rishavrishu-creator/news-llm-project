package com.smc.recurring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smc.recurring.util.ApplicationUtil;
import com.smc.recurring.util.DateUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@EnableRetry
@EnableTransactionManagement
@EnableJpaRepositories
@EnableAsync
public class RecurringPaymentApp {

    public static void main(String[] args) {

        SpringApplication.run(RecurringPaymentApp.class, args);

    }
}
