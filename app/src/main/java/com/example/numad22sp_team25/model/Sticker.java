package com.example.numad22sp_team25.model;

import android.os.Build;

import java.time.Instant;

public class Sticker {
    public String from;
    public String to;
    public int stickerId;
    public long timestamp;
    public String text;

    public Sticker() {}

    public Sticker(String from, String to, int stickerId, String text) {
        this.from = from;
        this.to = to;
        this.stickerId = stickerId;
        this.text = text;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.timestamp = Instant.now().getEpochSecond();
        }
    }
}
