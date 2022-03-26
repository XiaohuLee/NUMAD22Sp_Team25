package com.example.numad22sp_team25;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.numad22sp_team25.realtimedatabase.model.Sticker;
import com.example.numad22sp_team25.realtimedatabase.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;

public class HomePageActivity extends AppCompatActivity {
    // components
    private RecyclerView.LayoutManager rLayoutManager;
    private RecyclerView recyclerView;
    private StickerRecordsAdapter rviewAdapter;
    private Button send;
    private Button userInfo;

    // current user-info
    private String currentUsername;
    private String currentToken;
    private int stickerSend;
    private ArrayList<Sticker> stickerRecords;
    private StickerRecordsAdapter stickerRecordsAdapter;

    // all users
    private HashMap<String, Boolean> allUsersHashmap;

    // infrastructure resources
    private FirebaseDatabase db;
    private DatabaseReference allUsersRecords;
    private DatabaseReference currentUserRecord;
    private DatabaseReference currentUserStickerHistory;

    // event listeners to above DatabaseReference
    ChildEventListener currentUserHistoryListener;
    ChildEventListener allUsersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // cache components
        send = findViewById(R.id.send);
        userInfo = findViewById(R.id.userInfo);

        // current user and its token
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreference", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "N/A");
        currentToken = sharedPreferences.getString("token", "N/A");
        stickerRecords = new ArrayList<>();

        // init all users' resources
        allUsersHashmap = new HashMap<>();

        // in case user or token missing, restart LoginActivity
        restartLoginActivity();

        // bind resources
        db = FirebaseDatabase.getInstance();
        allUsersRecords = db.getReference("Users");
        currentUserRecord = db.getReference("Users/" + currentUsername);
        currentUserStickerHistory = db.getReference("Users/" + currentUsername + "/History");

        // set listener
        receivedListener();
        allUsersListener();
        stickerSendListener();

        // create notification
        createNotificationChannel();

        // current user subscribe to their own topic to get notifications
        subscribe();

        setContentView(R.layout.activity_homepage);

        // initialize recycleview
        initializeRecyclerview(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // save state on orientation change
        int stickerSize = stickerRecords.size();
        outState.putInt("StickerSize", stickerSize);

        for (int i = 0; i < stickerSize; ++i) {
            outState.putString(i + " From", stickerRecords.get(i).from);
            outState.putString(i + " To", stickerRecords.get(i).to);
            outState.putInt(i + " id", stickerRecords.get(i).stickerId);
        }

        super.onSaveInstanceState(outState);
    }

    private void restartLoginActivity() {
        Intent intent = new Intent(HomePageActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void receivedListener() {
        currentUserHistoryListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String prevChild) {
                Sticker newSticker = snapshot.getValue(Sticker.class);
                stickerRecords.add(0, newSticker); // add to front
                stickerRecordsAdapter.notifyItemInserted(0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        currentUserStickerHistory.addChildEventListener(currentUserHistoryListener);
    }

    private void allUsersListener() {
        allUsersListener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User newUser = snapshot.getValue(User.class);
                allUsersHashmap.put(newUser.username, true);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        allUsersRecords.addChildEventListener(allUsersListener);
    }

    private void stickerSendListener() {
        currentUserRecord.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && currentUsername != null) {
                    User newUser = snapshot.getValue(User.class);
                    if (newUser != null) {
                        stickerSend = newUser.stickersSend;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("A7 Channel", currentUsername, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("A7 Channel for user sticker messaging, current user is: " + currentUsername);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void subscribe() {
        FirebaseMessaging.getInstance().subscribeToTopic(currentUsername).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = currentUsername + " subscribed to own topic" ;
                if (!task.isSuccessful()) {
                    Toast.makeText(HomePageActivity.this ,currentUsername + " failed to subscribe", Toast.LENGTH_SHORT).show();
                } else {
                }
            }
        });
    }

    private void initializeRecyclerview(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("StickerSize")) {
            int size = savedInstanceState.getInt("StickerSize");

            stickerRecords = new ArrayList<>();

            for (int i = 0; i < size; ++i) {
                String from = savedInstanceState.getString(i + " From");
                String to = savedInstanceState.getString(i + " To");
                int stickerId = savedInstanceState.getInt(i + " id");
                stickerRecords.add(new Sticker(from, to, stickerId));
            }
        }

        // create recyclerView
        createRecyclerView();
    }

    private void createRecyclerView() {
        rLayoutManager = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        rviewAdapter = new StickerRecordsAdapter(stickerRecords);
        recyclerView.setAdapter(rviewAdapter);
        recyclerView.setLayoutManager(rLayoutManager);
    }

    public void sendSticker(View view) {

    }

    public void showUserInfo(View view) {

    }
}
