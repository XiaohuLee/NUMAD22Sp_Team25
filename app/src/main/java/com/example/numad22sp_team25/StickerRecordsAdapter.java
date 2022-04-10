package com.example.numad22sp_team25;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.numad22sp_team25.model.Sticker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
        holder.text.setText("Text: " + currentItem.text);
        holder.currentUserName.setText("From: " + currentItem.from);
        holder.senderName.setText("To: " + currentItem.to);
        holder.timestamp.setText("Time: " + convertTimestamp(currentItem.timestamp));
        holder.stickerIcon.setImageResource(currentItem.stickerId);
    }

    @Override
    public int getItemCount() {
        return stickerRecords.size();
    }

    private String convertTimestamp(Long secondsValue) {
        Date date = new Date(secondsValue * 1000);
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("dd MMM yyyy hh:mm:ss zzz");
        return df.format(date);
    }
}
