package com.movie_booking.util;

import com.movie_booking.config.AppConfig;

public class RazorpayConfig {

    public static String getKeyId() {
        return AppConfig.getRequired("RAZORPAY_KEY_ID");
    }

    public static String getKeySecret() {
        return AppConfig.getRequired("RAZORPAY_KEY_SECRET");
    }
}