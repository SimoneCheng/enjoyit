package com.enjoyit.domain;

import java.util.UUID;

public class Vendor {
    private String id;
    private String name;
    private Menu menu;

    public Vendor(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.menu = new Menu(); // 店家建立時預設有一個空菜單
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Menu getMenu() { return menu; }
    public void setMenu(Menu menu) { this.menu = menu; }
    public void setId(String id) { this.id = id; }
}