package com.example.numad22sp_team25;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class StickerRecordsHolder extends RecyclerView.ViewHolder {
    public TextView currentUserName;
    public ImageView stickerIcon;

    public StickerRecordsHolder(View view) {
        super(view);
        currentUserName = view.findViewById(R.id.currentUsername);
        stickerIcon = view.findViewById(R.id.stickerIcon);
    }
}
