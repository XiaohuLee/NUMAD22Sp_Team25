package com.example.numad22sp_team25;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.numad22sp_team25.realtimedatabase.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private TextView username;
    private FirebaseDatabase db;
    private FirebaseMessaging fcm;
    private static String CLIENT_REGISTRATION_TOKEN;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acvitity_login);

        // If the user is returning
        if (savedInstanceState != null && savedInstanceState.containsKey("USERNAME")) {
            gotoHomePage();
        }

        username = findViewById(R.id.username);
        db = FirebaseDatabase.getInstance();
        fcm = FirebaseMessaging.getInstance();

        // generate token for the first time, then no need to do later
        fcm.getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    task.getException().printStackTrace();
                } else {
                    if (CLIENT_REGISTRATION_TOKEN == null) {
                        CLIENT_REGISTRATION_TOKEN = task.getResult();
                    }
                }
            }
        });

        // store preference
        currentUsername = getSharedPreferences("sharedPreference", MODE_PRIVATE).getString("username", null);

        if (currentUsername != null) {
            gotoHomePage();
        }
    }

    public void loginUser(View view) {
        currentUsername = username.getText().toString();

        // validate input
        if (currentUsername.length() == 0) {
            Toast.makeText(getApplicationContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // create user, write to db
        DatabaseReference newUserReference = db.getReference("Users/" + currentUsername);
        newUserReference.addValueEventListener(new ValueEventListener() {
            public User currentUser;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUser = snapshot.getValue(User.class);
                } else {
                    currentUser = new User(currentUsername, CLIENT_REGISTRATION_TOKEN);
                    newUserReference.setValue(currentUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // store preference
        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences("sharedPreference", MODE_PRIVATE).edit();
        sharedPreferencesEditor.putString("username", currentUsername);
        sharedPreferencesEditor.putString("token", CLIENT_REGISTRATION_TOKEN);
        sharedPreferencesEditor.commit();

        // start home page
        gotoHomePage();
    }

    private void gotoHomePage() {
        startActivity(new Intent(LoginActivity.this, HomePageActivity.class));
    }
}
