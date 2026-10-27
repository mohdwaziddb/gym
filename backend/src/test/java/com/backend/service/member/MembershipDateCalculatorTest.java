package com.backend.service.member;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MembershipDateCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 27);

    @Test
    void endDateCountsStartDay() {
        assertEquals(LocalDate.of(2026, 1, 30), MembershipDateCalculator.endDate(LocalDate.of(2026, 1, 1), 30));
        assertEquals(LocalDate.of(2026, 1, 1), MembershipDateCalculator.endDate(LocalDate.of(2026, 1, 1), 1));
    }

    @Test
    void defaultDurationDays() {
        assertEquals(30, MembershipDateCalculator.defaultDurationDays("monthly"));
        assertEquals(90, MembershipDateCalculator.defaultDurationDays("QUARTERLY"));
        assertEquals(180, MembershipDateCalculator.defaultDurationDays("HALF_YEARLY"));
        assertEquals(365, MembershipDateCalculator.defaultDurationDays("YEARLY"));
        assertNull(MembershipDateCalculator.defaultDurationDays("CUSTOM"));
        assertTrue(MembershipDateCalculator.isValidDurationType("CUSTOM"));
        assertFalse(MembershipDateCalculator.isValidDurationType("WEEKLY"));
    }

    @Test
    void renewWhileActiveStartsDayAfterEnd() {
        assertEquals(LocalDate.of(2026, 11, 6), MembershipDateCalculator.renewalStartDate(LocalDate.of(2026, 11, 5), TODAY));
        // aaj hi khatam ho rahi ho to kal se
        assertEquals(TODAY.plusDays(1), MembershipDateCalculator.renewalStartDate(TODAY, TODAY));
    }

    @Test
    void renewAfterExpiryOrWithoutPlanStartsToday() {
        assertEquals(TODAY, MembershipDateCalculator.renewalStartDate(LocalDate.of(2026, 9, 1), TODAY));
        assertEquals(TODAY, MembershipDateCalculator.renewalStartDate(null, TODAY));
    }

    @Test
    void overlapIsInclusive() {
        LocalDate s = LocalDate.of(2026, 10, 1);
        LocalDate e = LocalDate.of(2026, 10, 30);
        assertTrue(MembershipDateCalculator.overlaps(s, e, LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 28)));
        assertFalse(MembershipDateCalculator.overlaps(s, e, LocalDate.of(2026, 10, 31), LocalDate.of(2026, 11, 29)));
        assertTrue(MembershipDateCalculator.overlaps(s, e, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 1)));
    }

    @Test
    void memberStatusThresholds() {
        assertEquals("NO_PLAN", MembershipDateCalculator.memberStatus(List.of(), TODAY));
        assertEquals("ACTIVE", MembershipDateCalculator.memberStatus(List.<LocalDate[]>of(period(TODAY.minusDays(5), TODAY.plusDays(8))), TODAY));
        assertEquals("EXPIRING", MembershipDateCalculator.memberStatus(List.<LocalDate[]>of(period(TODAY.minusDays(5), TODAY.plusDays(7))), TODAY));
        assertEquals("EXPIRING", MembershipDateCalculator.memberStatus(List.<LocalDate[]>of(period(TODAY.minusDays(5), TODAY)), TODAY));
        assertEquals("EXPIRED", MembershipDateCalculator.memberStatus(List.<LocalDate[]>of(period(TODAY.minusDays(40), TODAY.minusDays(1))), TODAY));
        assertEquals("UPCOMING", MembershipDateCalculator.memberStatus(List.<LocalDate[]>of(period(TODAY.plusDays(2), TODAY.plusDays(31))), TODAY));
    }

    @Test
    void queuedRenewalKeepsMemberActive() {
        // chalu membership 3 din me khatam, par renewal pehle se lagi hai -> EXPIRING nahi, ACTIVE
        List<LocalDate[]> periods = List.of(
                period(TODAY.minusDays(27), TODAY.plusDays(3)),
                period(TODAY.plusDays(4), TODAY.plusDays(33)));
        assertEquals("ACTIVE", MembershipDateCalculator.memberStatus(periods, TODAY));
    }

    @Test
    void parseDate() {
        assertEquals(LocalDate.of(2026, 1, 5), MembershipDateCalculator.parseDate("2026-01-05"));
        assertEquals(LocalDate.of(2026, 1, 5), MembershipDateCalculator.parseDate("2026-01-05T10:00:00"));
        assertNull(MembershipDateCalculator.parseDate("05/01/2026"));
        assertNull(MembershipDateCalculator.parseDate(""));
        assertNull(MembershipDateCalculator.parseDate(null));
    }

    private static LocalDate[] period(LocalDate start, LocalDate end) {
        return new LocalDate[]{start, end};
    }
}
