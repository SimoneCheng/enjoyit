package com.enjoyit.domain;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private boolean isActive = true; // 【新增】控制整份菜單的上下架，預設為上架
    private List<MenuCategory> categories = new ArrayList<>();

    // 空建構子 (避免 Spring Boot 報錯)
    public Menu() {}

    // --- 新增的 Getters & Setters ---
    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }

    public List<MenuCategory> getCategories() { return categories; }
    public void setCategories(List<MenuCategory> categories) { this.categories = categories; }

    public void addCategory(MenuCategory category) {
        this.categories.add(category);
    }
}