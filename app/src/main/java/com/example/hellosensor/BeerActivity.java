package com.example.hellosensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class BeerActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ImageView beerImageView;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable fullRunnable;
    private Runnable halfEmptyRunnable;
    private Runnable emptyRunnable;

    private Runnable resetRunnable;
    private boolean isTiltInRange = false;
    private Vibrator vibrator;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer);

        beerImageView = findViewById(R.id.beerImageView); // Make sure to have this ImageView in your layout
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        fullRunnable = () -> {
            vibratePhone(3000);
            playSound(R.raw.minecraft_drinking);
        };

        halfEmptyRunnable = () -> {
            vibratePhone(3000);
            beerImageView.setImageResource(R.drawable.half_empty_beer);
        };

        emptyRunnable = () -> beerImageView.setImageResource(R.drawable.empty_beer);

        resetRunnable = () -> beerImageView.setImageResource(R.drawable.full_beer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        // Not needed for this roll calculation
        float y = event.values[1];
        float z = event.values[2];

        // Calculate the roll angle in degrees on the xy-plane
        double tiltX = Math.atan2(x, Math.sqrt(y * y + z * z)) * (180.0 / Math.PI);

        // Check if the roll angle is within the range [-30, 30]
        if (tiltX > 30 || tiltX < -30) {
            if (!isTiltInRange) {
                // Roll just entered the range, schedule the image resource changes
                isTiltInRange = true;
                handler.postDelayed(fullRunnable, 0);
                handler.postDelayed(halfEmptyRunnable, 3000);
                handler.postDelayed(emptyRunnable, 6000);
                handler.postDelayed(resetRunnable, 9000);
            }
        } else {
            // Roll is out of range, reset the flags and remove any pending runnables
            if (isTiltInRange) {
                isTiltInRange = false;
                try {
                    stopSound();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                stopVibration();
                // handler.removeCallbacks(fullRunnable);
                handler.removeCallbacks(halfEmptyRunnable);
                handler.removeCallbacks(emptyRunnable);
                handler.removeCallbacks(resetRunnable);
            }
        }
    }

    private void stopSound() throws InterruptedException {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
        catch(IllegalStateException e) {
            System.out.println(e);
            Thread.sleep(1000);
            beerImageView.setImageResource(R.drawable.full_beer);
        }
    }

    private void playSound(int soundResourceId) {
        // Release any previous MediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        // Initialize MediaPlayer with the provided sound resource
        mediaPlayer = MediaPlayer.create(this, soundResourceId);
        if (mediaPlayer != null) {
            mediaPlayer.start();

            // Optional: Set a listener to release the MediaPlayer once it's done playing
            mediaPlayer.setOnCompletionListener(mp -> {
                if (mp != null) {
                    mp.release();
                }
            });
        }
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }


    private void vibratePhone(int duration) {
        // Get instance of Vibrator from current Context
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Check whether the Vibrator hardware is present
        if (vibrator.hasVibrator()) {
            // Vibrate with different methods depending on the SDK version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Newer API use VibrationEffect (requires API 26 or higher)
                VibrationEffect effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE);
                // Vibrate with the effect
                vibrator.vibrate(effect);
            } else {
                // Deprecated method, used on older devices (before API 26)
                // Vibrate for 500 milliseconds
                vibrator.vibrate(duration);
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the MediaPlayer when the activity is destroyed to avoid memory leaks
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        // Remove all callbacks and messages to avoid leaking the handler
        handler.removeCallbacksAndMessages(null);
    }
}