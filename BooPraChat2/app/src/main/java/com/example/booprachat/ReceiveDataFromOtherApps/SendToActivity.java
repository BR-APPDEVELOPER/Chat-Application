
package com.example.booprachat.ReceiveDataFromOtherApps;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.SendToAdapter;
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

public class SendToActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseUser currentUser;
    RecyclerView recyclerView;
    Button SendToGroupChat, goToChatDashboard;
    Toolbar toolbar;

    private ArrayList<String> namesList, IdsList;
    SendToAdapter sendToAdapter;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_to);

        //declaring ui ids
        recyclerView = findViewById(R.id.send_to_recycleView);
        SendToGroupChat = findViewById(R.id.send_to_groupChat);
        goToChatDashboard = findViewById(R.id.go_to_chat_dashboard);
        toolbar = findViewById(R.id.send_to_toolbar);
        namesList = new ArrayList<>();
        IdsList = new ArrayList<>();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //disable back button
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //get intent from image receive activity and chat adapter
        Intent intent = getIntent();
        String imageUri = intent.getStringExtra("imageUri");
        String text = intent.getStringExtra("text");
        String requestCode = intent.getStringExtra("requestCode");
        String pdfUri = intent.getStringExtra("pdfUri");
        String audioUri = intent.getStringExtra("audioUri");
        String videoUri = intent.getStringExtra("videoUri");
        String fileName = intent.getStringExtra("fileName");
        ArrayList<Uri> multipleImageUris = intent.getParcelableArrayListExtra("multipleImageUris");
        String secondaryRequestCode = intent.getStringExtra("secondaryRequestCode");

        sendToAdapter = new SendToAdapter(SendToActivity.this, namesList, IdsList, imageUri, text, requestCode, pdfUri, audioUri, videoUri, fileName, multipleImageUris);
        recyclerView.setAdapter(sendToAdapter);

        //calling method
        getChatLists();

        if (secondaryRequestCode.equals("IRA")) {
            goToChatDashboard.setVisibility(View.VISIBLE);
            SendToGroupChat.setVisibility(View.VISIBLE);

            goToChatDashboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent newActivity = new Intent(SendToActivity.this, Dashboard_Activity.class);
                    startActivity(newActivity);
                    finish();
                }
            });

            SendToGroupChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent1 = new Intent(SendToActivity.this, SendToGroupActivity.class);
                    intent1.putExtra("imageUri", imageUri);
                    intent1.putExtra("text", text);
                    intent1.putExtra("requestCode", requestCode);
                    intent1.putExtra("pdfUri", pdfUri);
                    intent1.putExtra("audioUri", audioUri);
                    intent1.putExtra("videoUri", videoUri);
                    intent1.putExtra("fileName", fileName);
                    intent1.putExtra("multipleImageUris", multipleImageUris);
                    intent1.putExtra("secondaryRequestCode", secondaryRequestCode);
                    startActivity(intent1);
                }
            });

        } else {

            if (secondaryRequestCode.equals("GCA") || secondaryRequestCode.equals("favourite") || secondaryRequestCode.equals("MA") || secondaryRequestCode.equals("MDA")) {
                //enable back button
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                SendToGroupChat.setVisibility(View.GONE);
                goToChatDashboard.setVisibility(View.GONE);
            }

            if (secondaryRequestCode.equals("CA") || secondaryRequestCode.equals("favourite") || secondaryRequestCode.equals("MA") || secondaryRequestCode.equals("MDA")) {
                SendToGroupChat.setVisibility(View.VISIBLE);
                goToChatDashboard.setVisibility(View.GONE);

                SendToGroupChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent1 = new Intent(SendToActivity.this, SendToGroupActivity.class);
                        intent1.putExtra("imageUri", imageUri);
                        intent1.putExtra("text", text);
                        intent1.putExtra("requestCode", requestCode);
                        intent1.putExtra("pdfUri", pdfUri);
                        intent1.putExtra("audioUri", audioUri);
                        intent1.putExtra("videoUri", videoUri);
                        intent1.putExtra("fileName", fileName);
                        intent1.putExtra("multipleImageUris", multipleImageUris);
                        intent1.putExtra("secondaryRequestCode", secondaryRequestCode);
                        startActivity(intent1);
                    }
                });

            } else if (!secondaryRequestCode.equals("GCA")) {
                SendToGroupChat.setVisibility(View.VISIBLE);
                goToChatDashboard.setVisibility(View.GONE);

                SendToGroupChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent1 = new Intent(SendToActivity.this, SendToGroupActivity.class);
                        intent1.putExtra("imageUri", imageUri);
                        intent1.putExtra("text", text);
                        intent1.putExtra("requestCode", requestCode);
                        intent1.putExtra("pdfUri", pdfUri);
                        intent1.putExtra("audioUri", audioUri);
                        intent1.putExtra("videoUri", videoUri);
                        intent1.putExtra("fileName", fileName);
                        intent1.putExtra("multipleImageUris", multipleImageUris);
                        intent1.putExtra("secondaryRequestCode", secondaryRequestCode);
                        startActivity(intent1);
                    }
                });

            }

        }

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
                                sendToAdapter.notifyDataSetChanged();
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
                                sendToAdapter.notifyDataSetChanged();
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