package com.enjoyit.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ModifierGroup {
    private String id;
    private String name;
    private List<ModifierOption> options;
    @JsonProperty("isActive")
    private boolean isActive = true; // 新增這行

    public ModifierGroup(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.options = new ArrayList<>();
    }

    public ModifierGroup() {
        this.id = java.util.UUID.randomUUID().toString();
        this.options = new java.util.ArrayList<>(); }

    public void addOption(ModifierOption option) {
        this.options.add(option);
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<ModifierOption> getOptions() { return options; }
}