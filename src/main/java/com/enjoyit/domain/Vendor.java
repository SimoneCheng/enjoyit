package com.enjoyit.domain;

import java.util.UUID;

public class Vendor {
    private String id;
    private String name;
    private String phone;
    private String address;
    private String businessHours;
    private boolean isActive;
    private Menu menu;

    public Vendor(String name, String phone, String address) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.isActive = true;
        this.menu = new Menu();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Menu getMenu() { return menu; }
    public void setMenu(Menu menu) { this.menu = menu; }
}