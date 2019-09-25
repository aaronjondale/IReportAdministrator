package com.neireport.administrator;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        delayHomeActivity();
    }

    public void delayHomeActivity() {
        int splashTime = 2000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startHomeActivity();
            }
        }, splashTime); //Timeout
    }

    private void startHomeActivity() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
