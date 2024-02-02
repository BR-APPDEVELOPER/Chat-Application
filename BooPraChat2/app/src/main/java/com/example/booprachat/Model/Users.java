package com.example.booprachat.Model;

public class Users {

    //use same name as in firebase database
    String name, email, search, image, uid, onlineStatus, typingTo, homeScreenImage, chatScreenImage;

    public Users() {
    }

    public Users(String name, String email, String search, String image, String uid, String onlineStatus, String typingTo, String homeScreenImage, String chatScreenImage) {
        this.name = name;
        this.email = email;
        this.search = search;
        this.image = image;
        this.uid = uid;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.homeScreenImage = homeScreenImage;
        this.chatScreenImage = chatScreenImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public String getHomeScreenImage() {
        return homeScreenImage;
    }

    public void setHomeScreenImage(String homeScreenImage) {
        this.homeScreenImage = homeScreenImage;
    }

    public String getChatScreenImage() {
        return chatScreenImage;
    }

    public void setChatScreenImage(String chatScreenImage) {
        this.chatScreenImage = chatScreenImage;
    }
}
