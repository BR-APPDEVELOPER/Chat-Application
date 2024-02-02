package com.example.booprachat.Settings;

import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.TypingToWallpaperAdapter;
import com.example.booprachat.Model.Users;
import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TypingToWallpaperActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    FirebaseAuth firebaseAuth;
    ArrayList<Users> usersList;
    TypingToWallpaperAdapter typingToWallpaperAdapter;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typing_to_wallpaper);

        toolbar = findViewById(R.id.typing_to_toolbar);
        recyclerView = findViewById(R.id.typing_to_recycleview);

        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFFFF"));
        getSupportActionBar().setTitle(R.string.typing_to_wallpaper);
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //set Grid layout for recycle view
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(gridLayoutManager);


        firebaseAuth = FirebaseAuth.getInstance();
        usersList = new ArrayList<>();

        getUsers();
    }

    private void getUsers() {

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        //for getting chatlist of current user
        userRef.child(firebaseAuth.getUid()).child("GroupChatLists").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {

                    String uid = "" + ds.child("uid").getValue();

                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                    userRef.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {

                                Users users = ds.getValue(Users.class);
                                usersList.add(users);

                            }
                            //setup adapter
                            typingToWallpaperAdapter = new TypingToWallpaperAdapter(TypingToWallpaperActivity.this, usersList);
                            //set adapter
                            recyclerView.setAdapter(typingToWallpaperAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // goto previous activity
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