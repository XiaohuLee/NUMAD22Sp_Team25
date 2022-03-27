package com.example.numad22sp_team25;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.numad22sp_team25.model.Sticker;

import java.util.ArrayList;

public class StickerRecordsAdapter extends RecyclerView.Adapter<StickerRecordsHolder> {
    public ArrayList<Sticker> stickerRecords;

    public StickerRecordsAdapter(ArrayList<Sticker> stickerRecords) {
        this.stickerRecords = stickerRecords;
    }

    @Override
    public StickerRecordsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_records_card, parent, false);
        return new StickerRecordsHolder(view);
    }

    @Override
    public void onBindViewHolder(StickerRecordsHolder holder, int position) {
        Sticker currentItem = stickerRecords.get(position);
        holder.currentUserName.setText("From: " + currentItem.from);
        holder.senderName.setText("To: " + currentItem.to);
        holder.stickerIcon.setImageResource(currentItem.stickerId);
    }

    @Override
    public int getItemCount() {
        return stickerRecords.size();
    }
}
