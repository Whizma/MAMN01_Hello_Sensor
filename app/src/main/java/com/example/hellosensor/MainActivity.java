package com.example.hellosensor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.startButton);
        Button beerButton = findViewById(R.id.beerButton);

        startButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AccelerometerActivity.class);
            startActivity(intent);
        });
        beerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BeerActivity.class);
            startActivity(intent);
        });
    }
}
