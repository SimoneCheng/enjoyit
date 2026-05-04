package com.enjoyit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncementTest {

    @Test
    @DisplayName("測試建立公告：應正確儲存內容並自動記錄當下的時間")
    void testAnnouncementCreation() {
        String expectedContent = "明天中午 12 點準時收單喔！";

        // 記錄建立物件「前」的時間
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        Announcement announcement = new Announcement(expectedContent);

        // 記錄建立物件「後」的時間
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        assertEquals(expectedContent, announcement.getContent());
        assertNotNull(announcement.getTime(), "時間不應為 null");

        // 驗證自動產生的時間是否合理落在我們捕捉的時間區間內
        assertTrue(announcement.getTime().isAfter(beforeCreation));
        assertTrue(announcement.getTime().isBefore(afterCreation));
    }
}