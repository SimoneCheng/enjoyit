package com.enjoyit.domain;

public class Group {
    private String id;
    private String password;

    public Group(String id, String password) {
        this.id = id;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }
}
