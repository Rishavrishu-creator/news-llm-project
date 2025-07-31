package com.smc.recurring.external.util;

public class ApplicationUtil {

    public static void validatePayload() {
    }

    public static boolean isWithinRadius(double artLat, double artLon, double centerLat, double centerLon, double radiusKm) {
        double earthRadius = 6371; // unit is km
        double dLat = Math.toRadians(centerLat - artLat);
        double dLon = Math.toRadians(centerLon - artLon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(artLat)) * Math.cos(Math.toRadians(centerLat)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;
        return distance <= radiusKm;
    }

}
