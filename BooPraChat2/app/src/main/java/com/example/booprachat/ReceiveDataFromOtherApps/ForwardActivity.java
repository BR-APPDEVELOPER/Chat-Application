package com.example.booprachat.ReceiveDataFromOtherApps;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.ForwardAdapter;
import com.example.booprachat.Dashboard_Activity;
import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ForwardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseUser currentUser;
    RecyclerView recyclerView;
    Button GoToChat;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private ArrayList<String> namesList, IdsList;
    ForwardAdapter forwardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward);

        //declaring ui ids
        recyclerView = findViewById(R.id.forward_recycleView);
        GoToChat = findViewById(R.id.go_to_chat);
        namesList = new ArrayList<>();
        IdsList = new ArrayList<>();


        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //get intent from image receive activity
        Intent intent = getIntent();
        String imageUri = intent.getStringExtra("imageUri");
        String text = intent.getStringExtra("text");

        forwardAdapter = new ForwardAdapter(ForwardActivity.this, namesList, IdsList, imageUri, text);
        recyclerView.setAdapter(forwardAdapter);

        //calling method
        getChatLists();

        GoToChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(ForwardActivity.this, Dashboard_Activity.class);
                startActivity(intent1);
                finish();
            }
        });
    }

    private void getChatLists() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        //for getting chatlist of current user
        userRef.child(currentUser.getUid()).child("ChatLists")
                .orderByValue().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    String userNameId = snapshot.getKey();
                    userRef.child(userNameId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            if (snapshot2.exists()) {
                                namesList.add(0, snapshot2.child("name").getValue().toString());
                                IdsList.add(0, snapshot2.getKey());
                                forwardAdapter.notifyDataSetChanged();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    final String userNameId = snapshot.getKey();
                    userRef.child(userNameId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            if (snapshot2.exists()) {
                                int index = IdsList.indexOf(userNameId);
                                namesList.remove(index);
                                IdsList.remove(index);

                                namesList.add(0, snapshot2.child("name").getValue().toString());
                                IdsList.add(0, snapshot2.getKey());
                                forwardAdapter.notifyDataSetChanged();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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