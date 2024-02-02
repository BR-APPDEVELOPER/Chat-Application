package com.example.booprachat.GroupChats;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.ParticipantsAddAdapter;
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
import java.util.Locale;

public class AddParticipantsToGroupActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private String groupId, myGroupRole = "";
    private ArrayList<Users> usersList;
    private ParticipantsAddAdapter participantsAddAdapter;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocal();
        setContentView(R.layout.activity_add_participants_to_group);

        //declaring ids
        recyclerView = findViewById(R.id.add_participant_recycleview);
        toolbar = findViewById(R.id.add_participants_toolbar);
        usersList = new ArrayList<>();

        //set toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.addSoldiers);
        toolbar.setTitleTextColor(Color.WHITE);
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        //get intent from group message activity
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        //calling method
        loadGroupInfo();

    }

    private void loadLocal() {
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String lan = preferences.getString("My_language", "");
        setLocal(lan);
    }

    private void setLocal(String lan) {
        Locale locale = new Locale(lan);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_language", lan);
        editor.apply();
    }

    private void loadGroupInfo() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Groups");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String groupId = "" + ds.child("groupId").getValue();
                    String groupTitle = "" + ds.child("groupTitle").getValue();
                    String groupDescription = "" + ds.child("groupDescription").getValue();
                    String groupIcon = "" + ds.child("groupIcon").getValue();
                    String timestamp = "" + ds.child("timestamp").getValue();
                    String groupCreatedBy = "" + ds.child("groupCreatedBy").getValue();

                    reference1.child(groupId).child("participants").child(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        myGroupRole = "" + snapshot.child("role").getValue();
                                        toolbar.setTitle(groupTitle + "(" + myGroupRole + ")");
                                        getUsers();
                                    }
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
                            participantsAddAdapter = new ParticipantsAddAdapter(AddParticipantsToGroupActivity.this, usersList, "" + groupId, "" + myGroupRole);
                            //set adapter
                            recyclerView.setAdapter(participantsAddAdapter);
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
        onBackPressed(); // goto previous page
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