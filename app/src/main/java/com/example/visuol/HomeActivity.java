package com.example.visuol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class VisuolLaunch extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visuol_launch);
        Button startVr = findViewById(R.id.startVr);
        Button toInformation = findViewById(R.id.toInformation);
        startVr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(VisuolLaunch.this, MainActivity.class);
                VisuolLaunch.this.startActivity(newIntent);
            }
        });
        toInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(VisuolLaunch.this, AppInformation.class);
                VisuolLaunch.this.startActivity(newIntent);
            }
        });
    }

}
