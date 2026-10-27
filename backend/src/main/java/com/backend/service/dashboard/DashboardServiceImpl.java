package com.backend.service.dashboard;

import com.backend.service.member.MemberStatusSql;
import com.backend.service.member.MembershipDateCalculator;
import com.backend.utility.AuthContext;
import com.backend.utility.MobileResponseDTOFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.backend.utility.DataTypeUtility.*;

/**
 * DashboardServiceImpl - Owner dashboard ke numbers.
 * Status count wahi MemberStatusSql use karta hai jo member list filter, isliye tile pe tap karke
 * list me utne hi members dikhte hain.
 * "membership_value_assigned" = is mahine assign hui memberships ka final_amount - yeh collection NAHI hai
 * (payment Phase 2 me aayega).
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private MobileResponseDTOFactory mobileResponseDTOFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Object getSummary(Map<String, Object> param, HttpServletRequest request) {
        try {
            String gymCode = AuthContext.gymCode(request);
            LocalDate today = LocalDate.now();
            LocalDate monthStart = today.withDayOfMonth(1);

            Map<String, Object> counts = new HashMap<>();
            for (String s : new String[]{MembershipDateCalculator.STATUS_ACTIVE, MembershipDateCalculator.STATUS_EXPIRING,
                    MembershipDateCalculator.STATUS_EXPIRED, MembershipDateCalculator.STATUS_UPCOMING, MembershipDateCalculator.STATUS_NO_PLAN}) {
                counts.put(s.toLowerCase(), 0L);
            }
            long total = 0;
            for (Object rowObj : entityManager.createNativeQuery(
                            "SELECT " + MemberStatusSql.STATUS_CASE + " AS member_status, COUNT(*) "
                                    + MemberStatusSql.FROM_MEMBERS_WITH_STATUS + " GROUP BY member_status")
                    .setParameter("gymCode", gymCode)
                    .setParameter("today", today)
                    .setParameter("expiringDate", today.plusDays(MembershipDateCalculator.EXPIRING_DAYS))
                    .getResultList()) {
                Object[] r = (Object[]) rowObj;
                long c = longZeroValue(r[1]);
                counts.put(stringValue(r[0]).toLowerCase(), c);
                total += c;
            }

            long newMembers = longZeroValue(entityManager.createNativeQuery(
                            "SELECT COUNT(*) FROM members WHERE gym_code = :gymCode AND isdelete = 0 AND joining_date >= :monthStart AND joining_date <= :today")
                    .setParameter("gymCode", gymCode)
                    .setParameter("monthStart", monthStart)
                    .setParameter("today", today)
                    .getSingleResult());

            Object[] assigned = (Object[]) entityManager.createNativeQuery(
                            "SELECT COUNT(*), COALESCE(SUM(final_amount), 0) FROM memberships"
                                    + " WHERE gym_code = :gymCode AND status = 'ACTIVE' AND created_on >= :monthStart")
                    .setParameter("gymCode", gymCode)
                    .setParameter("monthStart", monthStart.atStartOfDay())
                    .getSingleResult();

            Map<String, Object> data = new HashMap<>(counts);
            data.put("total_members", total);
            data.put("new_members_this_month", newMembers);
            data.put("memberships_assigned_this_month", longZeroValue(assigned[0]));
            data.put("membership_value_assigned", stringValue(new BigDecimal(stringValue(assigned[1])).setScale(2).toPlainString()));
            data.put("expiring_within_days", MembershipDateCalculator.EXPIRING_DAYS);
            data.put("as_of", stringValue(today));
            return mobileResponseDTOFactory.successMessage("Dashboard summary", data);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage("Something went wrong, please try again");
        }
    }
}
