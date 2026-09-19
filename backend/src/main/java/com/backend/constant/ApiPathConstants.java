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
}
