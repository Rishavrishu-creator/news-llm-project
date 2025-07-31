package com.smc.recurring.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigUtil {

    @Value("${clientCallbackUrl}")
    private String clientCallbackUrl;

    public static String MY_STATIC_PROP;

    @PostConstruct
    public void init() {
        MY_STATIC_PROP = clientCallbackUrl;
    }
}
