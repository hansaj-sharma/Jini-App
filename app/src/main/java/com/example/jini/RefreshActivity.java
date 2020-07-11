package com.example.jini;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class RefreshActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh);

        Intent i= new Intent(RefreshActivity.this,RegistrationActivity.class);
        startActivity(i);
        finishAffinity();


    }
}