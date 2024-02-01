package com.example.hellosensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class BeerActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ImageView beerImageView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable halfEmptyRunnable;
    private Runnable emptyRunnable;
    private boolean isTiltInRange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer);

        beerImageView = findViewById(R.id.beerImageView); // Make sure to have this ImageView in your layout
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {

        }

        // Define the runnables for changing the image resources
        halfEmptyRunnable = new Runnable() {
            @Override
            public void run() {
                // Change to half empty beer image
                beerImageView.setImageResource(R.drawable.half_empty_beer);
            }
        };

        emptyRunnable = new Runnable() {
            @Override
            public void run() {
                // Change to empty beer image
                beerImageView.setImageResource(R.drawable.empty_beer);
            }
        };
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        // Not needed for this roll calculation
        float y = event.values[1];
        float z = event.values[2];

        // Calculate the roll angle in degrees on the xy-plane
        double tiltX = Math.atan2(x, Math.sqrt(y * y + z * z)) * (180.0 / Math.PI);
        System.out.println(tiltX);


        // Check if the roll angle is within the range [-30, 30]
        if (tiltX > 30 || tiltX < -30) {
            if (!isTiltInRange) {
                // Roll just entered the range, schedule the image resource changes
                isTiltInRange = true;

                handler.postDelayed(halfEmptyRunnable, 3000);
                handler.postDelayed(emptyRunnable, 6000);
            }
        } else {
            // Roll is out of range, reset the flags and remove any pending runnables
            if (isTiltInRange) {
                isTiltInRange = false;

                handler.removeCallbacks(halfEmptyRunnable);
                handler.removeCallbacks(emptyRunnable);
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Implement if needed
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

        // Cancel the runnables if the activity is paused
        handler.removeCallbacks(halfEmptyRunnable);
        handler.removeCallbacks(emptyRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }
}