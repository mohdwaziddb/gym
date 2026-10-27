package com.backend.constant;

public final class ApiPathConstants {
    private ApiPathConstants() {}

    public static final class Gym {
        public static final String BASE = "/rest/gym";
        public static final String LIST = "/list";
        public static final String SAVE = "/save";
        public static final String GET_BY_ID = "/get";
        public static final String DELETE = "/delete";
        // gym_details table endpoints
        private Gym() {}
    }

    public static final class Auth {
        public static final String BASE = "/rest/auth";
        public static final String VALIDATE_GYM = "/validateGym";
        public static final String LOGIN = "/login";
        private Auth() {}
    }

    // members table endpoints
    public static final class Member {
        public static final String BASE = "/rest/member";
        public static final String LIST = "/list";
        public static final String GET_BY_ID = "/get";
        public static final String SAVE = "/save";
        public static final String ARCHIVE = "/archive";
        private Member() {}
    }

    // membership_plans table endpoints
    public static final class Plan {
        public static final String BASE = "/rest/plan";
        public static final String LIST = "/list";
        public static final String SAVE = "/save";
        public static final String TOGGLE = "/toggle";
        public static final String DELETE = "/delete";
        private Plan() {}
    }

    // memberships table endpoints (member ko plan assign / renew)
    public static final class Membership {
        public static final String BASE = "/rest/membership";
        public static final String ASSIGN = "/assign";
        public static final String RENEW = "/renew";
        public static final String CANCEL = "/cancel";
        public static final String HISTORY = "/history";
        private Membership() {}
    }

    public static final class Dashboard {
        public static final String BASE = "/rest/dashboard";
        public static final String SUMMARY = "/summary";
        private Dashboard() {}
    }
}
