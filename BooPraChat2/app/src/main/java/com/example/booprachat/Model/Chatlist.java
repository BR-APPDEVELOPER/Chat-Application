package com.example.booprachat.Model;

public class Chatlist {

    String id; // we need this id to get chatlist, sender/receiver

    public Chatlist(String id) {
        this.id = id;
    }

    public Chatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
