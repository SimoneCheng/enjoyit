package com.enjoyit.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeadlineRequestTest {

    @Test
    @DisplayName("測試 DeadlineRequest：Setter 與 Getter 應能正常運作")
    void testGetAndSetDeadline() {
        DeadlineRequest request = new DeadlineRequest();
        String expectedDeadline = "2026-12-31T23:59:59";

        request.setDeadline(expectedDeadline);

        assertEquals(expectedDeadline, request.getDeadline());
    }
}