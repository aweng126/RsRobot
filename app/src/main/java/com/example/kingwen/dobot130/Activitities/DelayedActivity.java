package com.example.kingwen.dobot130.Activitities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.kingwen.dobot130.R;

/**
 * Created by kingwen on 2016/9/16.
 */
public class DelayedActivity extends AppCompatActivity {
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delaystart_layout);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(DelayedActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        }, 1500);

    }
}
