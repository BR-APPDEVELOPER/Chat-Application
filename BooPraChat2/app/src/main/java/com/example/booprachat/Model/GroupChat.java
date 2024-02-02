package com.example.booprachat.Model;

public class GroupChat {

    String sender, message, time, type, secondaryMessage, fileName;

    public GroupChat() {
    }

    public GroupChat(String sender, String message, String time, String type, String secondaryMessage, String fileName) {
        this.sender = sender;
        this.message = message;
        this.time = time;
        this.type = type;
        this.secondaryMessage = secondaryMessage;
        this.fileName = fileName;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSecondaryMessage() {
        return secondaryMessage;
    }

    public void setSecondaryMessage(String secondaryMessage) {
        this.secondaryMessage = secondaryMessage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
