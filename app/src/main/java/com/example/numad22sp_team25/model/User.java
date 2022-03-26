package com.example.numad22sp_team25.model;

import java.util.ArrayList;

public class User {
    public String username;
    public String token;
    public ArrayList<Sticker> receivedHistory;
    public int stickersSend;

    public User() {}

    public User(String username, String token) {
        this.username = username;
        this.token = token;
        this.receivedHistory = new ArrayList<Sticker>();
        this.stickersSend = 0;
    }
}
