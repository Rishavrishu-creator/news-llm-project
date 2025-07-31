package com.smc.recurring.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonConverter {


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> T convertToObject(String message, Class<T> t) {
        try {
            return OBJECT_MAPPER.readValue(message, t);
        } catch (JsonProcessingException e) {
            log.error(" Error while converting Message to Object", e.getMessage());
            throw new RuntimeException("Error while converting Message to Object", e);
        }
    }

    public static String convertObjectToString(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error(" Error while converting Object to String value ", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
