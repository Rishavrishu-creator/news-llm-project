package com.smc.recurring.util;

import org.springframework.beans.factory.annotation.Value;

public class CheckoutPage {

    public static String getCheckoutPage(String razorpayKeyId, String orderId, String customerId, String callbackUrl, Boolean redirect, Boolean retryEnabled, String clientCallbackUrl) {

        Boolean negativeFlag = false;
        Boolean positiveFlag = true;
        String htmlBegin = "<html>\n" +
                "\n" +
                "<body>\n";

        String checkoutScript =
                "  <script src = \"https://checkout.razorpay.com/v1/checkout.js\"> </script>\n" +
                        "  <script>\n" +
                        "    var options = {\n" +
                        "      \"key\": \"" + razorpayKeyId + "\",\n" +
                        "      \"order_id\": \"" + orderId + "\",\n" +
                        "      \"customer_id\": \"" + customerId + "\",\n" +
                        "      \"recurring\": \"1\",\n" +
                        "      \"callback_url\": \"" + callbackUrl + "\",\n" +
                        "      \"redirect\":" + redirect + ",\n" +
                        "      \"notes\": {\n" +
                        "        \"note_key 1\": \"SmartTrader\",\n" +
                        "        \"note_key 2\": \"SmartTrader\"\n" +
                        "      },\n" +
                        "      \"modal\": {\n" +
                        "        \"ondismiss\": function(){\n" +
                        "       window.location.href = \"" + clientCallbackUrl + "\";" +
                        "      },\n" +
                        "      },\n" +
                        "      \"retry\": {\n" +
                        "        \"enabled\":" + retryEnabled +
                        "      },\n" +
                        "      \"theme\": {\n" +
                        "        \"color\": \"#0CA750\"\n" +
                        "      }\n" +
                        "    };\n" +
                        "    var rzp1 = new Razorpay(options);\n" +
                        "    window.onload = function (e) {\n" +
                        "      rzp1.open();\n" +
                        "      e.preventDefault();\n" +
                        "    }\n" +
                        "  </script>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n";

        String htmlEnd = "\n" +
                "\n" +
                "</body>\n" +
                "</html>";

        return sanitizeCheckoutPage(htmlBegin.concat(checkoutScript).concat(htmlEnd));
    }

    private static String sanitizeCheckoutPage(String htmlPage) {

        String cleanString = htmlPage
                .replace("\\n", "<br>")
                .replace("\\", "")
                .replace("\\+", "");
        return cleanString;
    }
}
