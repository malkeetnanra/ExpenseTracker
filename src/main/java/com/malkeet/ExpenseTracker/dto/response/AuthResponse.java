package com.malkeet.ExpenseTracker.dto.response;
import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class AuthResponse {
    private String token;
    private String name;
    private String email;

}
