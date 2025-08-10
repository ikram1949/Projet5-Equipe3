package com.example.vipcinema.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vipcinema.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Make sure this is correct

        // Get logo ImageView
        ImageView logo = findViewById(R.id.vipLogo);
        if (logo == null) {
            Log.e("Splash", "vipLogo not found in layout.");
        } else {
            Log.d("Splash", "vipLogo found.");
        }


        // Load and apply animation
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.logo_fade_scale);
        logo.startAnimation(anim);

        // Redirect after delay
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 2500); // 2.5 seconds
    }
}
