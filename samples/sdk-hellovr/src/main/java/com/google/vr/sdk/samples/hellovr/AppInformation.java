package com.google.vr.sdk.samples.hellovr;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class AppInformation extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_information);
        Button toLaunch = findViewById(R.id.information_to_launch);
        toLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(AppInformation.this, VisuolLaunch.class);
                AppInformation.this.startActivity(newIntent);
            }
        });
    }

}
