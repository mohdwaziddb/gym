package com.backend.core;

import java.util.HashMap;

/**
 * Registry - Holds global runtime flags and DB credentials map.
 * Simplified for DATABASE-per-tenant demo.
 */
public class Registry {

    public static boolean IS_ONLINE = false;

    // key: url, username, password, databasename, driverClassName
    public static final HashMap<String, String> dbmap = new HashMap<>();
}
