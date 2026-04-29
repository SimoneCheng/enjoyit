package com.enjoyit.domain;

import java.time.LocalDateTime;

/**
 * 領域物件：群組公告
 */
public class Announcement {
    private String content;      // 公告內容
    private LocalDateTime time;   // 發布時間

    public Announcement(String content) {
        this.content = content;
        this.time = LocalDateTime.now(); // 建立時自動記錄當下時間
    }

    // Getters
    public String getContent() { return content; }
    public LocalDateTime getTime() { return time; }
}