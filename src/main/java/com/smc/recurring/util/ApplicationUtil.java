package com.smc.recurring.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationUtil {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static <T> T convertToObject(String message, Class<T> t) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(message, t);
    }

    public static <T> String convertToString(Object message) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(message);
    }

    public static String getRandomAlphaNumericNumber(int length) {
        String randomNumber = RandomStringUtils.randomAlphanumeric(length);
        return randomNumber.toUpperCase();
    }

    public static Integer getMetaDataValue(String notes) {

        // Remove curly braces
        notes = notes.substring(1, notes.length() - 1);

        // Split into key-value pairs
        String[] entries = notes.split(", ");
        Map<String, String> resultMap = new HashMap<>();

        for (String entry : entries) {
            String[] keyValue = entry.split("=");
            if (keyValue.length == 2) {
                resultMap.put(keyValue[0], keyValue[1]);
            }
        }

        String dateValue = resultMap.get("FirstInstallmentDay");
        if (dateValue == null)
            return null;
        return Integer.parseInt(dateValue);
    }
}
