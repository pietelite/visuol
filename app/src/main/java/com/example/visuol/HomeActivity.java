package com.example.visuol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * The main activity to launch upon starting the app. The purpose of it is to allow the user
 * to first learn about the app and about multivariate calculus and change any settings they
 * would like to <b>outside</b> of a virtual reality format. The user can start other activities
 * by pushing the appropriate buttons.
 */
public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visuol_launch);
        Button startVr = findViewById(R.id.startVr);
        Button toInformation = findViewById(R.id.toInformation);
        startVr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(HomeActivity.this, MainActivity.class);
                HomeActivity.this.startActivity(newIntent);
            }
        });
        toInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(
                        HomeActivity.this, AppInformationActivity.class);
                HomeActivity.this.startActivity(newIntent);
            }
        });
    }

}
