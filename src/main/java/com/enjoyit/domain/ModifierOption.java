package com.enjoyit.domain;

import java.util.UUID;

public class ModifierOption {
    private String id;
    private String name;
    private int extraPrice; // 需要加多少錢

    public ModifierOption(String name, int extraPrice) {
        this.id = UUID.randomUUID().toString(); // 自動產生唯一 ID
        this.name = name;
        this.extraPrice = extraPrice;
    }

    public ModifierOption() {}

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getExtraPrice() { return extraPrice; }
    public void setExtraPrice(int extraPrice) { this.extraPrice = extraPrice; }
}