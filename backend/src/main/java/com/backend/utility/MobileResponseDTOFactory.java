package com.backend.utility;

import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Component
public class MobileResponseDTOFactory {

    public Object failedMessage(String message) {
        return new GeneralResponse<>(false, message, null);
    }

    public Object successMessage(String message, Object data) {
        return new GeneralResponse<>(true, message, data);
    }

    public Object reportInternalServerError(Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(new GeneralResponse<>(false, "Internal Server Error", null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public Connection getConnectionFromEntityManager(EntityManager entityManager) {
        try {
            return entityManager.unwrap(Connection.class);
        } catch (Exception e) {
            throw new RuntimeException("Unable to unwrap connection from entity manager : " + e.getMessage(), e);
        }
    }
}
