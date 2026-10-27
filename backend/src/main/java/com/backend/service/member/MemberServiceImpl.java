package com.backend.service.member;

import com.backend.entity.member.MemberModel;
import com.backend.repo.member.MemberRepo;
import com.backend.repo.member.MembershipRepo;
import com.backend.utility.AuthContext;
import com.backend.utility.MobileResponseDTOFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

import static com.backend.utility.DataTypeUtility.*;

/**
 * MemberServiceImpl - members CRUD. Har query login wale gym ke gym_code (JWT) se filter hoti hai.
 */
@Service
public class MemberServiceImpl implements MemberService {

    private static final String GENERIC_ERROR = "Something went wrong, please try again";
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9][0-9]{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Set<String> GENDERS = Set.of("MALE", "FEMALE", "OTHER");
    private static final Set<String> LIST_STATUSES = Set.of(
            MembershipDateCalculator.STATUS_ACTIVE, MembershipDateCalculator.STATUS_EXPIRING,
            MembershipDateCalculator.STATUS_EXPIRED, MembershipDateCalculator.STATUS_UPCOMING,
            MembershipDateCalculator.STATUS_NO_PLAN);
    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private MemberRepo memberRepo;

    @Autowired
    private MembershipRepo membershipRepo;

    @Autowired
    private MobileResponseDTOFactory mobileResponseDTOFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Object getMemberList(Map<String, Object> param, HttpServletRequest request) {
        try {
            String gymCode = AuthContext.gymCode(request);
            String search = stringValue(param.get("search"));
            String status = stringValue(param.get("status")).toUpperCase();
            if (!status.isEmpty() && !LIST_STATUSES.contains(status)) {
                return mobileResponseDTOFactory.failedMessage("Invalid status filter");
            }
            int page = Math.max(integerValue(param.get("page")) == null ? 0 : integerValue(param.get("page")), 0);
            Integer sizeParam = integerValue(param.get("size"));
            int size = sizeParam == null || sizeParam <= 0 ? 20 : Math.min(sizeParam, MAX_PAGE_SIZE);
            LocalDate today = LocalDate.now();

            String inner = "SELECT m.id, m.member_code, m.full_name, m.phone, m.email, m.gender, m.joining_date, s.max_end, "
                    + MemberStatusSql.STATUS_CASE + " AS member_status "
                    + MemberStatusSql.FROM_MEMBERS_WITH_STATUS
                    + (search.isEmpty() ? "" : " AND (m.full_name LIKE :search OR m.phone LIKE :search OR m.member_code LIKE :search) ");
            String where = status.isEmpty() ? "" : " WHERE t.member_status = :status ";

            Query listQuery = entityManager.createNativeQuery(
                    "SELECT * FROM (" + inner + ") t " + where + " ORDER BY t.id DESC LIMIT :size OFFSET :offset");
            Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM (" + inner + ") t " + where);
            for (Query q : List.of(listQuery, countQuery)) {
                q.setParameter("gymCode", gymCode);
                q.setParameter("today", today);
                q.setParameter("expiringDate", today.plusDays(MembershipDateCalculator.EXPIRING_DAYS));
                if (!search.isEmpty()) q.setParameter("search", "%" + search + "%");
                if (!status.isEmpty()) q.setParameter("status", status);
            }
            listQuery.setParameter("size", size);
            listQuery.setParameter("offset", page * size);

            List<Map<String, Object>> dataArray = new LinkedList<>();
            for (Object rowObj : listQuery.getResultList()) {
                Object[] r = (Object[]) rowObj;
                Map<String, Object> row = new HashMap<>();
                row.put("id", longValue(r[0]));
                row.put("member_code", stringValue(r[1]));
                row.put("full_name", stringValue(r[2]));
                row.put("phone", stringValue(r[3]));
                row.put("email", stringValue(r[4]));
                row.put("gender", stringValue(r[5]));
                row.put("joining_date", stringValue(r[6]));
                row.put("expiry_date", stringValue(r[7]));
                row.put("status", stringValue(r[8]));
                dataArray.add(row);
            }
            long total = longZeroValue(countQuery.getSingleResult());

            Map<String, Object> result = new HashMap<>();
            result.put("data_array", dataArray);
            result.put("result_size", dataArray.size());
            result.put("total", total);
            result.put("page", page);
            result.put("has_more", (long) (page + 1) * size < total);
            return mobileResponseDTOFactory.successMessage("Member list", result);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Object getMemberById(Map<String, Object> param, HttpServletRequest request) {
        try {
            Long id = longValue(param.get("id"));
            if (id == null) return mobileResponseDTOFactory.failedMessage("id is required");
            String gymCode = AuthContext.gymCode(request);
            Optional<MemberModel> opt = memberRepo.findByIdAndGymCodeAndIsdeleteFalse(id, gymCode);
            if (opt.isEmpty()) return mobileResponseDTOFactory.failedMessage("Member not found");

            MemberModel m = opt.get();
            Map<String, Object> data = memberToMap(m, AuthContext.isManager(request));
            data.putAll(MembershipServiceImpl.membershipSummary(membershipRepo.findByGymCodeAndMemberIdOrderByStartDateDescIdDesc(gymCode, m.getId()), LocalDate.now()));
            return mobileResponseDTOFactory.successMessage("Member details", data);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    @Override
    @Transactional
    public Object saveMember(Map<String, Object> param, HttpServletRequest request) {
        try {
            String gymCode = AuthContext.gymCode(request);
            String username = AuthContext.username(request);
            boolean manager = AuthContext.isManager(request);
            Long id = longValue(param.get("id"));

            String fullName = stringValue(param.get("full_name"));
            String phone = stringValue(param.get("phone")).replaceAll("[\\s-]", "");
            if (phone.startsWith("+91")) phone = phone.substring(3);
            String email = stringValue(param.get("email"));
            String gender = stringValue(param.get("gender")).toUpperCase();
            String emergencyPhone = stringValue(param.get("emergency_phone")).replaceAll("[\\s-]", "");

            if (fullName.isEmpty()) return mobileResponseDTOFactory.failedMessage("full_name is mandatory");
            if (fullName.length() > 150) return mobileResponseDTOFactory.failedMessage("full_name is too long");
            if (!PHONE_PATTERN.matcher(phone).matches()) return mobileResponseDTOFactory.failedMessage("Enter a valid 10 digit mobile number");
            if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) return mobileResponseDTOFactory.failedMessage("Enter a valid email");
            if (!gender.isEmpty() && !GENDERS.contains(gender)) return mobileResponseDTOFactory.failedMessage("gender must be MALE, FEMALE or OTHER");
            if (!emergencyPhone.isEmpty() && !PHONE_PATTERN.matcher(emergencyPhone).matches()) {
                return mobileResponseDTOFactory.failedMessage("Enter a valid emergency contact number");
            }

            LocalDate dob = MembershipDateCalculator.parseDate(param.get("dob"));
            if (!stringValue(param.get("dob")).isEmpty() && dob == null) return mobileResponseDTOFactory.failedMessage("dob must be YYYY-MM-DD");
            if (dob != null && !dob.isBefore(LocalDate.now())) return mobileResponseDTOFactory.failedMessage("dob must be in the past");
            LocalDate joiningDate = MembershipDateCalculator.parseDate(param.get("joining_date"));
            if (!stringValue(param.get("joining_date")).isEmpty() && joiningDate == null) {
                return mobileResponseDTOFactory.failedMessage("joining_date must be YYYY-MM-DD");
            }

            MemberModel model;
            if (id != null) {
                Optional<MemberModel> opt = memberRepo.findByIdAndGymCodeAndIsdeleteFalse(id, gymCode);
                if (opt.isEmpty()) return mobileResponseDTOFactory.failedMessage("Member not found");
                model = opt.get();
            } else {
                model = new MemberModel();
                model.setGymCode(gymCode);
                model.setCreatedBy(username);
            }

            // Ek gym me ek mobile number ek hi member ka
            Optional<MemberModel> samePhone = memberRepo.findFirstByGymCodeAndPhoneAndIsdeleteFalse(gymCode, phone);
            if (samePhone.isPresent() && !Objects.equals(samePhone.get().getId(), model.getId())) {
                return mobileResponseDTOFactory.failedMessage("Phone already registered: " + samePhone.get().getMemberCode());
            }

            if (id == null) {
                model.setMemberCode(nextMemberCode(gymCode));
                model.setJoiningDate(joiningDate == null ? LocalDate.now() : joiningDate);
            } else if (joiningDate != null) {
                model.setJoiningDate(joiningDate);
            }

            model.setFullName(fullName);
            model.setPhone(phone);
            // Edit me sirf bheji hui fields badlo
            if (id == null || param.containsKey("email")) model.setEmail(email);
            if (id == null || param.containsKey("gender")) model.setGender(gender);
            if (id == null || param.containsKey("dob")) model.setDob(dob);
            if (id == null || param.containsKey("address")) model.setAddress(stringValue(param.get("address")));
            if (id == null || param.containsKey("emergency_name")) model.setEmergencyName(stringValue(param.get("emergency_name")));
            if (id == null || param.containsKey("emergency_phone")) model.setEmergencyPhone(emergencyPhone);
            // Medical / private notes sirf owner-admin likh / dekh sakte hain
            if (manager && param.containsKey("notes")) model.setNotes(stringValue(param.get("notes")));
            model.setUpdatedBy(username);

            memberRepo.save(model);

            Map<String, Object> data = new HashMap<>();
            data.put("id", model.getId());
            data.put("member_code", model.getMemberCode());
            data.put("full_name", model.getFullName());
            return mobileResponseDTOFactory.successMessage("Member saved successfully", data);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    @Override
    @Transactional
    public Object archiveMember(Map<String, Object> param, HttpServletRequest request) {
        try {
            if (!AuthContext.isManager(request)) return mobileResponseDTOFactory.failedMessage("Permission denied");
            Long id = longValue(param.get("id"));
            if (id == null) return mobileResponseDTOFactory.failedMessage("id is required");
            Optional<MemberModel> opt = memberRepo.findByIdAndGymCodeAndIsdeleteFalse(id, AuthContext.gymCode(request));
            if (opt.isEmpty()) return mobileResponseDTOFactory.failedMessage("Member not found");
            MemberModel m = opt.get();
            m.setIsdelete(true);
            m.setUpdatedBy(AuthContext.username(request));
            memberRepo.save(m);
            return mobileResponseDTOFactory.successMessage("Member archived successfully", null);
        } catch (Exception e) {
            e.printStackTrace();
            return mobileResponseDTOFactory.failedMessage(GENERIC_ERROR);
        }
    }

    /**
     * LAJPAT -> LAJ-0001, LAJ-0002 ...
     * FOR UPDATE se is gym ke members range lock hoti hai, do staff ek saath add karein to bhi code duplicate nahi hoga.
     */
    private String nextMemberCode(String gymCode) {
        String prefix = gymCode.replaceAll("[^A-Z0-9]", "");
        prefix = prefix.length() > 3 ? prefix.substring(0, 3) : prefix;
        Object max = entityManager.createNativeQuery(
                        "SELECT MAX(CAST(SUBSTRING_INDEX(member_code, '-', -1) AS UNSIGNED)) FROM members WHERE gym_code = :gymCode FOR UPDATE")
                .setParameter("gymCode", gymCode)
                .getSingleResult();
        long next = longZeroValue(max) + 1;
        return String.format("%s-%04d", prefix, next);
    }

    private Map<String, Object> memberToMap(MemberModel m, boolean includeNotes) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", longValue(m.getId()));
        row.put("member_code", stringValue(m.getMemberCode()));
        row.put("full_name", stringValue(m.getFullName()));
        row.put("phone", stringValue(m.getPhone()));
        row.put("email", stringValue(m.getEmail()));
        row.put("gender", stringValue(m.getGender()));
        row.put("dob", stringValue(m.getDob()));
        row.put("address", stringValue(m.getAddress()));
        row.put("emergency_name", stringValue(m.getEmergencyName()));
        row.put("emergency_phone", stringValue(m.getEmergencyPhone()));
        row.put("joining_date", stringValue(m.getJoiningDate()));
        if (includeNotes) row.put("notes", stringValue(m.getNotes()));
        return row;
    }
}
