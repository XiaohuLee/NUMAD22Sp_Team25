package com.example.numad22sp_team25.model;

import com.example.numad22sp_team25.R;

import java.util.ArrayList;

public class User {
    public String username;
    public String token;
    public ArrayList<Sticker> receivedHistory;
    public int stickersSend;

    public User() {
    }

    public User(String username, String token) {
        this.username = username;
        this.token = token;
        this.receivedHistory = new ArrayList<Sticker>();
        // Need to put a dummy, otherwise, firebase db will not record the key
        this.receivedHistory.add(new Sticker("dummyFrom", "dummyTo", R.drawable.lol));
        this.stickersSend = 0;
    }

    public void addSticker(Sticker newSticker) {
        this.receivedHistory.add(newSticker);
    }
}
