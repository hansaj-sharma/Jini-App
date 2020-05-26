package com.example.jini;

public class Contacts {

    String name, status, image, Uid;

    public Contacts() {
    }

    public Contacts(String name, String status, String image, String uid) {
        this.name = name;
        this.status = status;
        this.image = image;
        Uid = uid;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }
}
