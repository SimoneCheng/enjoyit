package com.enjoyit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    @Test
    @DisplayName("測試建立 Group：建構子應正確綁定 id 與 password")
    void testGroupCreation() {
        String testId = "my_office_group";
        String testPassword = "super_secret_password";

        Group group = new Group(testId, testPassword);

        assertEquals(testId, group.getId(), "群組 ID 應與傳入值相符");
        assertEquals(testPassword, group.getPassword(), "密碼應與傳入值相符");
    }
}