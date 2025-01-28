package com.capstone.funpath.Models;

public class KidItem {
    private String key; // Add this field
    private String name;
    private String avatarResource;
    private boolean isSelected;

    public KidItem(String key, String name, String avatarResource) {
        this.key = key; // Initialize the key
        this.name = name;
        this.avatarResource = avatarResource;
        this.isSelected = false;
    }

    public String getKey() {
        return key; // Getter for the key
    }

    public String getName() {
        return name;
    }

    public String getAvatarResource() {
        return avatarResource;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }
}

