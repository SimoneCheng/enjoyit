package com.enjoyit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GroupCreateRequest {
    @NotBlank(message = "群組帳號不可為空")
    private String id;

    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, message = "密碼長度需至少 8 碼")
    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
