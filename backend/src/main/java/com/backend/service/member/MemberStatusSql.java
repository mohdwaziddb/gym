package com.backend.service.member;

/**
 * MemberStatusSql - Member status ka SQL (member list filter + dashboard count dono yahi use karte hain,
 * isliye dono ke total hamesha match karte hain). Rule MembershipDateCalculator.memberStatus() jaisa hi hai.
 *
 * Params: :gymCode, :today, :expiringDate (today + 7)
 */
public final class MemberStatusSql {

    private MemberStatusSql() {}

    /** members m + har member ki ACTIVE memberships ka summary s */
    public static final String FROM_MEMBERS_WITH_STATUS =
            " FROM members m" +
            " LEFT JOIN (" +
            "   SELECT member_id, MAX(end_date) AS max_end," +
            "          SUM(CASE WHEN start_date <= :today AND end_date >= :today THEN 1 ELSE 0 END) AS covering" +
            "   FROM memberships" +
            "   WHERE gym_code = :gymCode AND status = 'ACTIVE'" +
            "   GROUP BY member_id" +
            " ) s ON s.member_id = m.id" +
            " WHERE m.gym_code = :gymCode AND m.isdelete = 0 ";

    public static final String STATUS_CASE =
            " CASE" +
            "   WHEN s.member_id IS NULL THEN 'NO_PLAN'" +
            "   WHEN s.covering > 0 AND s.max_end > :expiringDate THEN 'ACTIVE'" +
            "   WHEN s.covering > 0 THEN 'EXPIRING'" +
            "   WHEN s.max_end < :today THEN 'EXPIRED'" +
            "   ELSE 'UPCOMING'" +
            " END ";
}
