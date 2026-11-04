package com.atm.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Format {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private Format() { }

    public static String money(BigDecimal value) {
        return "\u20B9 " + new DecimalFormat("#,##0.00").format(value);
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "-" : DATE_TIME.format(value);
    }
}
