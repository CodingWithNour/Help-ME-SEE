package com.example.helpmesee_preview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.helpmesee_preview.location.presenter.LocationScreenPresenter;

public class ChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        findViewById(R.id.tellmyLocation).setOnClickListener(V->{
            Intent intent=new Intent(getBaseContext(), LocationScreenPresenter.class);
            startActivity(intent);
        });
    }
}