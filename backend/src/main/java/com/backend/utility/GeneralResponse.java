package com.backend.utility;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public GeneralResponse() {
    }

    public GeneralResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
