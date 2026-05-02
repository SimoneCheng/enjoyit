package com.enjoyit.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MenuItem {
    private String id;
    private String name;
    private int unitPrice;
    private String image;
    @JsonProperty("isActive")
    private boolean isActive; // true = 上架中, false = 下架/停售
    private List<ModifierGroup> modifierGroups;

    public MenuItem(String name, int unitPrice) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.unitPrice = unitPrice;
        this.isActive = true; // 預設新增時為上架狀態
        this.modifierGroups = new ArrayList<>();
    }

    // 修正後的空建構子
    public MenuItem() {
        this.id = java.util.UUID.randomUUID().toString(); // 補上這行，確保反序列化時有唯一 ID
        this.modifierGroups = new java.util.ArrayList<>();
    }

    public void addModifierGroup(ModifierGroup group) {
        this.modifierGroups.add(group);
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getUnitPrice() { return unitPrice; }
    public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public List<ModifierGroup> getModifierGroups() { return modifierGroups; }
}