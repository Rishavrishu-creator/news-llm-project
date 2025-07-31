package com.smc.recurring.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private List<String> notifyUrl = List.of("/getStatus", "/getSubscriptionStatus", "/cancelSubscription", "/initiateSubscription","/changeSubscription");

    @Bean
    public FilterRegistrationBean<ApiKeyAuthFilter> txnAuthFilter() {
        FilterRegistrationBean<ApiKeyAuthFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(new ApiKeyAuthFilter());
        registrationBean.setUrlPatterns(notifyUrl);
        registrationBean.setOrder(-1);
        return registrationBean;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(header -> header.frameOptions(frameoptions -> frameoptions.sameOrigin()))
                .headers(header -> header.contentTypeOptions(contentOpts -> contentOpts.disable()))
                .headers(headers -> headers.xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)))
                //  .headers(headers -> headers.contentSecurityPolicy(csp  -> csp.policyDirectives("form-action 'self'")))
                //  .headers(headers -> headers.contentSecurityPolicy(csp  -> csp.policyDirectives("script-src 'self' https://checkout.razorpay.com/v1/checkout.js")))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    // @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList(
//                "http://localhost:4006",
//                "https://cugwebtrade.smctradeonline.com",
//                "https://webtrade.smctradeonline.com",
//                "https://payment-uat.smcindiaonline.org",
//                "https://payment.smcindiaonline.org",
//                "https://uat-asba.smcindiaonline.org",
//                "https://asba.smcindiaonline.org"
//        ));
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.OPTIONS.name(), HttpMethod.PUT.name()));
        configuration.setAllowedHeaders(Arrays.asList("content-type", "X-Platform", "X-Client-Id", "session", "Access-Control-Allow-Headers", "Access-Control-Allow-Origin"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
