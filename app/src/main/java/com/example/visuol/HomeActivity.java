package com.example.visuol;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
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
        toLaunch();
    }

    /** Opens the main launcher layout */
    void toLaunch() {
        setContentView(R.layout.visuol_launch);
        Button startVr = findViewById(R.id.startVr);
        Button toInformation = findViewById(R.id.toInformation);
        Button startAr = findViewById(R.id.startAr);
        Button toLearnMore = findViewById(R.id.toLearnMore);

        //Set the actions to occur when clicking the buttons
        startVr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start VR
                Intent newIntent = new Intent(HomeActivity.this, MainActivity.class);
                HomeActivity.this.startActivity(newIntent);
            }
        });
        startAr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start AR
                Intent newIntent = new Intent(HomeActivity.this, SceneformActivity.class);
                HomeActivity.this.startActivity(newIntent);
            }
        });
        toInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toInformation();
            }
        });
        toLearnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toLearnMore();
            }
        });
    }

    /** Opens the app information layout */
    void toInformation() {
        setContentView(R.layout.app_information);
        Button toLaunch = findViewById(R.id.toLaunch);
        toLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toLaunch();
            }
        });
    }

    void toLearnMore() {
        setContentView(R.layout.learn_more);
        Button toLaunch = findViewById(R.id.toLaunch2);
        Button toQuadricSurfaces = findViewById(R.id.toQuadricSurfaces);
        Button toGoogleVr = findViewById(R.id.toGoogleVr);
        toLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toLaunch();
            }
        });
        toQuadricSurfaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open("http://en.wikipedia.org/wiki/Quadric");
            }
        });
        toGoogleVr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open("http://vr.google.com/daydream/");
            }
        });
    }

    void create() {
        Button createOb = findViewById(R.id.createObject);
        createOb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateObject a = new CreateObject();
                //a.writeObject();
            }
        });
    }

    void open(String url) {
        Uri uri = Uri.parse("googlechrome://navigate?url=" + url);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        if (i.resolveActivity(this.getPackageManager()) == null) {
            i.setData(Uri.parse(url));
        }
        this.startActivity(i);
    }
}
