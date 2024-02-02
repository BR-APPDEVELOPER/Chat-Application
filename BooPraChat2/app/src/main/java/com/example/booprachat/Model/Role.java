package com.example.booprachat.Model;

public class Role {

    String uid, role, timestamp;

    public Role() {
    }

    public Role(String uid, String role, String timestamp) {
        this.uid = uid;
        this.role = role;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
