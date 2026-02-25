package com.malkeet.ExpenseTracker.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class LoginRequest {

    private String email;
    private String name;
    private String password;

}
