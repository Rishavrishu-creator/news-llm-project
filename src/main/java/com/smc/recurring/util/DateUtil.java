package com.smc.recurring.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {

    public static long UnixTimestampFuture() {
        ZoneId indiaZone = ZoneId.of("Asia/Kolkata");

        // Get current timestamp in IST
        long currentTimestamp = LocalDateTime.now(indiaZone)
                .atZone(indiaZone)
                .toEpochSecond();

        // Calculate timestamp for 2 years later in IST
        long futureTimestamp = LocalDateTime.now(indiaZone)
                .plusYears(2)
                .atZone(indiaZone)
                .toEpochSecond();
        return futureTimestamp;
    }

    public static String getCurrentDate() {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        // Define the desired format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Format the current date and time
        String formattedDate = now.format(formatter);
        return formattedDate;
    }

    public static String getDueDate(String currentDate, String frequency) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate now = LocalDate.parse(currentDate, formatter);
        LocalDate dueDate = null;


        if (frequency.equals("monthly"))
            dueDate = now.plusDays(29);
        else if (frequency.equals("yearly"))
            dueDate = now.plusDays(359);
        else if (frequency.equals("daily"))
            dueDate = now.plusDays(1);
        else if (frequency.equals("four"))
            dueDate = now.plusDays(4);
        else if (frequency.equals("twentySix"))
            dueDate = now.plusDays(26);
        else if (frequency.equals("threefiftysix"))
            dueDate = now.plusDays(356);
        else if (frequency.equals("threetwentysix"))
            dueDate = now.plusDays(326);
        else if (frequency.equals("threethirty"))
            dueDate = now.plusDays(330);
        else if (frequency.equals("minusone"))
            dueDate = now.minusDays(1);
        else
            dueDate = now.plusDays(1);

        // Format the current date and time
        String formattedDate = dueDate.format(formatter);
        return formattedDate;
    }

    public static String getDueDateForFreeDays(String currentDate, int freeDay) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate now = LocalDate.parse(currentDate, formatter);
        LocalDate dueDate = null;
        dueDate = now.plusDays(freeDay);

        // Format the current date and time
        String formattedDate = dueDate.format(formatter);
        return formattedDate;
    }

    public static String compareDates(String currentDate, String dueDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String ans = null;
        try {
            Date date1 = sdf.parse(currentDate);
            Date date2 = sdf.parse(dueDate);

            if (date1.equals(date2)) {
                ans = dueDate;
            } else if (date1.after(date2)) {
                ans = currentDate;
            } else {
                ans = dueDate;
            }
        } catch (ParseException e) {
            System.out.println("Invalid date format. Please use dd-MM-yyyy.");
        }
        return ans;
    }
}
