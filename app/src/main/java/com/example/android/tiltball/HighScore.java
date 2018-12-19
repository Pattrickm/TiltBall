package com.example.android.tiltball;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class HighScore extends AppCompatActivity {

    TextView highscore0;
    TextView highscore1;
    TextView highscore2;
    TextView highscore3;
    TextView highscore4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);
        highscore0 = findViewById(R.id.time0);
        highscore1 = findViewById(R.id.time1);
        highscore2 = findViewById(R.id.time2);
        highscore3 = findViewById(R.id.time3);
        highscore4 = findViewById(R.id.time4);

    }

    public void onBack(View v){
        startActivity(new Intent(HighScore.this, MainMenu_Activity.class));
    }
    public void onClear(View v) {
        highscore0.setText("Highscore0");
        highscore1.setText("Highscore1");
        highscore2.setText("Highscore2");
        highscore3.setText("Highscore3");
        highscore4.setText("Highscore4");

    }

}
