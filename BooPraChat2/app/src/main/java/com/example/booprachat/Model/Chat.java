package com.example.booprachat.Model;

public class Chat {
    String sender, receiver, message, fileName, secondaryMessage, time, type, messageSeenOrNot, groupIdForFavourite, mediaType;

    public Chat() {
    }

    public Chat(String sender, String receiver, String message, String fileName, String secondaryMessage, String time, String type, String messageSeenOrNot, String groupIdForFavourite, String mediaType) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.fileName = fileName;
        this.secondaryMessage = secondaryMessage;
        this.time = time;
        this.type = type;
        this.messageSeenOrNot = messageSeenOrNot;
        this.groupIdForFavourite = groupIdForFavourite;
        this.mediaType = mediaType;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSecondaryMessage() {
        return secondaryMessage;
    }

    public void setSecondaryMessage(String secondaryMessage) {
        this.secondaryMessage = secondaryMessage;
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

    public String getMessageSeenOrNot() {
        return messageSeenOrNot;
    }

    public void setMessageSeenOrNot(String messageSeenOrNot) {
        this.messageSeenOrNot = messageSeenOrNot;
    }

    public String getGroupIdForFavourite() {
        return groupIdForFavourite;
    }

    public void setGroupIdForFavourite(String groupIdForFavourite) {
        this.groupIdForFavourite = groupIdForFavourite;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
