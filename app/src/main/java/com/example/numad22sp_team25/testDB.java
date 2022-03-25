package com.example.numad22sp_team25;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class testDB extends AppCompatActivity {
    private static final String TAG = testDB.class.getSimpleName();
    Button btnTestDb;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testdb);

        btnTestDb = findViewById(R.id.button_test_db);
        text = findViewById(R.id.text_test_db);
        btnTestDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Write a meesage to the database
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("message");

                Task t = myRef.setValue("hello world");

                if (t.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Failed to write value into database", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Read from the database by listening for a change to that item
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String value = snapshot.getValue(String.class);
                        Log.d(TAG, "Value is: " + value);
                        text.setText(value);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                        Toast.makeText(getApplicationContext(), "Failed to write value into firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
