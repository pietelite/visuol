package com.example.visuol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * This activity has the sole purpose of showing the user the information about the development
 * of the app.
 */
public class AppInformationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_information);
        Button toLaunch = findViewById(R.id.information_to_launch);
        toLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(AppInformationActivity.this, HomeActivity.class);
                AppInformationActivity.this.startActivity(newIntent);
            }
        });
    }

}
