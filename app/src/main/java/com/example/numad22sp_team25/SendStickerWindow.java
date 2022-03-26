package com.example.numad22sp_team25;

import static com.example.numad22sp_team25.Resource.emojiIcon;
import static com.example.numad22sp_team25.Resource.emojiName;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

public class SendStickerWindow extends DialogFragment {
    Spinner spinner;
    SendStickerWindowListener stickerListener;

    public class customizedSpinnerAdapter extends ArrayAdapter<String> {
        private String[] names;
        private Integer[] icons;
        private Context context;

        public customizedSpinnerAdapter(Context context, String[] names, Integer[] icons) {
            super(context, R.layout.spinner_manager, R.id.emojiTitle, names);
            this.names = names;
            this.icons = icons;
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup p) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.spinner_manager, p, false);

            TextView emojiName = rowView.findViewById(R.id.emojiTitle);
            ImageView emojiIcon = rowView.findViewById(R.id.emojiIcon);

            emojiName.setText(names[position]);
            emojiIcon.setImageResource(icons[position]);

            return rowView;
        }

        @Override
        public View getDropDownView(int position, View view, ViewGroup p) {
            return getView(position, view, p);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = requireActivity().getLayoutInflater().inflate(R.layout.sticker_window, null);
        spinner = view.findViewById(R.id.stickerSpinner);
        spinner.setAdapter(new customizedSpinnerAdapter(getContext(), emojiName, emojiIcon));
        builder.setView(view).setPositiveButton("Send Sticker", ((d, i) -> stickerListener.windowClick(SendStickerWindow.this)));
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        stickerListener = (SendStickerWindowListener) context;
    }
}
