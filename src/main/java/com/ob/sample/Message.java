package com.ob.sample;

public class Message {
    private String message;
    private String address;

    public Message(String message, String address) {
        this.message = message;
        this.address = address;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
