package com.example.numad22sp_team25;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    //correct the form of server key: key=serverKey
    private static final String SERVER_KEY = "key=AAAAJrUF7t0:APA91bGQk29x23e6kZ9c-qIKkzqThfxB_Kz4jjbPvV10wE8PT6exne4fWpR5HXxl2fgR3k7s-xbjMicTL8mbBRXeN-MX5Rial6oSkJBnTijrIcByEgj8hZK3xxh1YvTuQHXeTVeTEGaM";
//    private static final String CLIENT_REGISTRATION_TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void button_test_db_method(View view) {
        Intent intent = new Intent(MainActivity.this, testDB.class);
        MainActivity.this.startActivity(intent);
    }
}