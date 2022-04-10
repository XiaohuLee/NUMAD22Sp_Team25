package com.example.numad22sp_team25.fcm;

import static com.example.numad22sp_team25.Resource.emojiIcon;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.numad22sp_team25.HomePageActivity;
import com.example.numad22sp_team25.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.net.CacheRequest;
import java.util.Objects;

public class FCMServer extends FirebaseMessagingService {
    private static final String TAG = FCMServer.class.getSimpleName();
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private static final String CHANNEL_NAME = "CHANNEL_NAME";
    private static final String CHANNEL_DESCRIPTION = "CHANNEL_DESCRIPTION";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onNewToken(String newToken) {
        Log.d(TAG, "Refreshed token: " + newToken);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        myClassifier(remoteMessage);
        Log.e("msgId", remoteMessage.getMessageId());
        Log.e("senderId", remoteMessage.getSenderId());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void myClassifier(RemoteMessage remoteMessage) {
        String identifier = remoteMessage.getFrom();
        if (identifier != null) {
            if (identifier.contains("topic")) {
                if (remoteMessage.getNotification() != null) {
                    RemoteMessage.Notification notification = remoteMessage.getNotification();
                    showNotification(notification);
                }
            } else {
                if (remoteMessage.getData().size() > 0) {
                    RemoteMessage.Notification notification = remoteMessage.getNotification();
                    showNotification(notification);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotification(RemoteMessage.Notification remoteMessage) {
        Intent intent = new Intent(this, HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request Code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification;
        NotificationCompat.Builder builder;
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        // At lease API level 26 - Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        notification = builder.setContentTitle(remoteMessage.getTitle())
                .setContentText("Alert: You received a new sticker!!!")
                .setSmallIcon(emojiIcon[Integer.parseInt(Objects.requireNonNull(remoteMessage.getBody()))])
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        emojiIcon[Integer.parseInt(Objects.requireNonNull(remoteMessage.getBody()))]))
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(0, notification);
    }
}
