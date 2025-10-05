// Create new file: src/main/java/com/example/gigconnect/dto/ApiResponse.java
package com.example.gigconnect.dto;

import lombok.Data;

@Data
public class ApiResponse {
    private String message;

    public ApiResponse(String message) {
        this.message = message;
    }
}