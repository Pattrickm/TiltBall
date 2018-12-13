package com.example.android.tiltball;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainMenu_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_);
    }


    public void onStart(View v){
        startActivity(new Intent(MainMenu_Activity.this, MainActivity.class));
    }
}
