package com.enjoyit.service;

import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {
    public boolean isValid(String inputPassword, String savedPassword) {
        if (inputPassword == null || savedPassword == null) return false;
        return inputPassword.equals(savedPassword);
    }
}
