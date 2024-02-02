package com.example.booprachat;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;
import com.zolad.zoominimageview.ZoomInImageView;

public class ProfileViewActivity extends AppCompatActivity {

    TextView ReceiverName;
    Toolbar Toolbar;
    ZoomInImageView ProfileImageView;
    DatabaseReference reference;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        ReceiverName = findViewById(R.id.username);
        ProfileImageView = findViewById(R.id.receiver_image_view);

        //action toolbar
        Toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(Toolbar);
        getSupportActionBar().setTitle("");
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        //getting receiver details from ChatlistAdapter
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        Uri image = Uri.parse(intent.getStringExtra("image"));

        ReceiverName.setText(name); // set user name
        Picasso.get().load(image).into(ProfileImageView); // set user profile

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previous page
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        //for checking internet connetion
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, intentFilter);

        super.onStart();
    }

    @Override
    protected void onStop() {
        //for checking internet connetion
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

}