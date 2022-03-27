package com.example.numad22sp_team25;

import static com.example.numad22sp_team25.Resource.SERVER_KEY;
import static com.example.numad22sp_team25.Resource.emojiIcon;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.numad22sp_team25.fcm.Utils;
import com.example.numad22sp_team25.model.Sticker;
import com.example.numad22sp_team25.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class HomePageActivity extends AppCompatActivity implements SendStickerWindowListener {
    // components
    private RecyclerView.LayoutManager rLayoutManager;
    private RecyclerView recyclerView;
    private StickerRecordsAdapter rViewAdapter;
    private Button send;
    private Button userInfo;

    // current user-info
    private String currentUsername;
    private String currentToken;
    //private String text;
    private int stickerSend;
    private ArrayList<Sticker> stickerRecords;
    private StickerRecordsAdapter stickerRecordsAdapter;

    // all users
    private HashMap<String, Boolean> allUsersHashmap;

    // send metadata
    private Integer newStickerId;

    // infrastructure resources
    private FirebaseDatabase db;
    private DatabaseReference allUsersRecords;
    private DatabaseReference currentUserRecord;
    private DatabaseReference currentUserStickerHistory;

    // event listeners to above DatabaseReference
    ChildEventListener currentUserHistoryListener;
    ChildEventListener allUsersListener;

    private static final String TAG = HomePageActivity.class.getSimpleName();

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
//        restartLoginActivity();

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

        setContentView(R.layout.activity_homepage);

        // initialize recyclerView
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

    public void sendSticker(View view) {
        DialogFragment window = new SendStickerWindow();
        window.show(getSupportFragmentManager(), "send sticker");
    }

    public void showUserInfo(View view) {
        currentUserRecord.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && currentUsername != null){
                    User newUser = snapshot.getValue(User.class);
                    stickerRecords = newUser.receivedHistory;
                    createRecyclerView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Toast.makeText(HomePageActivity.this ,currentUsername + " has sent " + stickerSend + " stickers.", Toast.LENGTH_SHORT).show();
    }

    // TODO: cleanup this function
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
            subscribe();
        }
    }

    private void subscribe() {
        FirebaseMessaging.getInstance().subscribeToTopic(currentUsername)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(HomePageActivity.this ,currentUsername + " subscribed to own topic", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomePageActivity.this ,currentUsername + " failed to subscribe", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initializeRecyclerview(Bundle savedInstanceState) {
        // Restore recyclerView on orientation change
        if (savedInstanceState != null && savedInstanceState.containsKey("StickerSize")) {
            int size = savedInstanceState.getInt("StickerSize");

            stickerRecords = new ArrayList<>();

            for (int i = 0; i < size; ++i) {
                String from = savedInstanceState.getString(i + " From");
                String to = savedInstanceState.getString(i + " To");
                int stickerId = savedInstanceState.getInt(i + " id");
                String text = savedInstanceState.getString(i + "text");
                stickerRecords.add(new Sticker(from, to, stickerId, text));
            }
        }

        // create recyclerView
        createRecyclerView();
    }

    private void createRecyclerView() {
        rLayoutManager = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        rViewAdapter = new StickerRecordsAdapter(stickerRecords);
        recyclerView.setAdapter(rViewAdapter);
        recyclerView.setLayoutManager(rLayoutManager);
    }

    @Override
    public void windowClick(DialogFragment send) {
        String recipient = ((EditText) send.getDialog().findViewById(R.id.spinnerUsername)).getText().toString();
        String text = ((EditText) send.getDialog().findViewById(R.id.etText)).getText().toString();
        newStickerId = emojiIcon[((Spinner) send.getDialog().findViewById(R.id.stickerSpinner)).getSelectedItemPosition()];

        if (validRecipient(recipient)) {
            // Close the dialog window
            send.dismiss();
            sendStickerToUser(recipient, newStickerId);//!!!!
            sendStickerToDB(recipient, newStickerId, text);//!!!!
            Toast.makeText(HomePageActivity.this ,"Successfully send sticker to " + recipient, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(HomePageActivity.this , "Username:" + recipient + " not present, please choose another user", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validRecipient(String recipient) {
        if (allUsersHashmap.containsKey(recipient)) return true;

        DatabaseReference recipientRef = db.getReference("Users/" + recipient);
        recipientRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    allUsersHashmap.put(recipient, true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        return allUsersHashmap.containsKey(recipient);
    }

    private void sendStickerToUser(String recipient, int newSticker) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();

                try {
                    // update notification
                    jNotification.put("title", "Sticker from " + currentUsername + " to " + recipient);
                    jNotification.put("body", newSticker);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");

                    // construct payload
                    jPayload.put("to", "/Users/" + recipient);
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // send payload
                String response = Utils.fcmHttpConnection(SERVER_KEY, jPayload);
                Log.d(TAG, "Response for sending stickers: " + response);
            }
        }).start();
    }

    private void sendStickerToDB(String recipient, Integer newSticker, String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference recipientRef = db.getReference("Users/" + recipient);

                // update recipients sticker history
                recipientRef.addValueEventListener(new ValueEventListener() {
                    boolean doOnce = true;
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User recipientUser = snapshot.getValue(User.class);
                        if (recipientUser != null && doOnce) {
                            recipientUser.addSticker(new Sticker(currentUsername, recipient, newSticker, text));
                            recipientRef.setValue(recipientUser);
                        }
                        doOnce = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });


                // update sender's metadata
                currentUserRecord.addValueEventListener(new ValueEventListener() {
                    boolean doOnce = true;

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User currentUser = snapshot.getValue(User.class);
                        if (doOnce) {
                            currentUser.stickersSend += 1;
                            currentUser.receivedHistory.add(new Sticker(currentUsername, recipient, newSticker, text));
                            currentUserRecord.setValue(currentUser);
                        }
                        doOnce = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }).start();
    }
}
