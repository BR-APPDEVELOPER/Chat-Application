package com.example.booprachat.FavouriteMessages;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;

public class FavouriteDashboardActivity extends AppCompatActivity {

    CardView audioAndVideo, image, pdf, message;
    Toolbar toolbar;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_dashboard);

        //declaring ui ids
        audioAndVideo = findViewById(R.id.favourite_audio_video_cardView);
        image = findViewById(R.id.favourite_image_cardView);
        pdf = findViewById(R.id.favourite_pdf_cardView);
        message = findViewById(R.id.favourite_message_cardView);
        toolbar = findViewById(R.id.favourite_dashboard_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        audioAndVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FavouriteDashboardActivity.this, FavouriteVideoActivity.class));
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FavouriteDashboardActivity.this, FavouriteImageActivity.class));
            }
        });

        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FavouriteDashboardActivity.this, FavouritePdfActivity.class));
            }
        });

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FavouriteDashboardActivity.this, FavouriteMessagesActivity.class));
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previous page
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        //for checking internet connection
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, intentFilter);

        super.onStart();
    }

    @Override
    protected void onStop() {
        //for checking internet connection
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}