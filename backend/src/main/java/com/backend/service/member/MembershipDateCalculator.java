package com.backend.service.member;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import static com.backend.utility.DataTypeUtility.*;

/**
 * MembershipDateCalculator - Membership ki saari date / status calculation ek jagah (pure logic, DB nahi).
 * Status rule MemberStatusSql ke SQL CASE ke saath same rakhna, warna list aur dashboard ke total match nahi honge.
 */
public final class MembershipDateCalculator {

    public static final int EXPIRING_DAYS = 7;

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXPIRING = "EXPIRING";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_UPCOMING = "UPCOMING";
    public static final String STATUS_NO_PLAN = "NO_PLAN";

    public static final String DURATION_MONTHLY = "MONTHLY";
    public static final String DURATION_QUARTERLY = "QUARTERLY";
    public static final String DURATION_HALF_YEARLY = "HALF_YEARLY";
    public static final String DURATION_YEARLY = "YEARLY";
    public static final String DURATION_CUSTOM = "CUSTOM";

    private MembershipDateCalculator() {}

    /** Plan type se din. CUSTOM ya galat type pe null (CUSTOM ke din user deta hai). */
    public static Integer defaultDurationDays(String durationType) {
        switch (stringValue(durationType).toUpperCase()) {
            case DURATION_MONTHLY: return 30;
            case DURATION_QUARTERLY: return 90;
            case DURATION_HALF_YEARLY: return 180;
            case DURATION_YEARLY: return 365;
            default: return null;
        }
    }

    public static boolean isValidDurationType(String durationType) {
        String t = stringValue(durationType).toUpperCase();
        return DURATION_CUSTOM.equals(t) || defaultDurationDays(t) != null;
    }

    /** 30 din ka plan 1 Jan se -> 30 Jan tak (start din bhi gina jaata hai). */
    public static LocalDate endDate(LocalDate start, int durationDays) {
        return start.plusDays(durationDays - 1L);
    }

    /**
     * Renew kab se shuru ho: chalu / aage wali membership ke end ke agle din se,
     * aur agar sab khatam ho chuki hain to aaj se.
     */
    public static LocalDate renewalStartDate(LocalDate latestActiveEnd, LocalDate today) {
        if (latestActiveEnd == null || latestActiveEnd.isBefore(today)) return today;
        return latestActiveEnd.plusDays(1);
    }

    /** Do date range overlap karti hain? (dono end inclusive) */
    public static boolean overlaps(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    /**
     * Member ka status uski ACTIVE memberships ke periods se.
     * periods: har item {start, end}
     */
    public static String memberStatus(List<LocalDate[]> activePeriods, LocalDate today) {
        if (activePeriods == null || activePeriods.isEmpty()) return STATUS_NO_PLAN;
        boolean covering = false;
        LocalDate maxEnd = null;
        for (LocalDate[] p : activePeriods) {
            if (!p[0].isAfter(today) && !p[1].isBefore(today)) covering = true;
            if (maxEnd == null || p[1].isAfter(maxEnd)) maxEnd = p[1];
        }
        if (covering) {
            return maxEnd.isAfter(today.plusDays(EXPIRING_DAYS)) ? STATUS_ACTIVE : STATUS_EXPIRING;
        }
        return maxEnd.isBefore(today) ? STATUS_EXPIRED : STATUS_UPCOMING;
    }

    /** "2026-10-27" -> LocalDate, khaali / galat pe null */
    public static LocalDate parseDate(Object value) {
        String str = stringValue(value);
        if (str.isEmpty()) return null;
        try {
            return LocalDate.parse(str.length() > 10 ? str.substring(0, 10) : str);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
