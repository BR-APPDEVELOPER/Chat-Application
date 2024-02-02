package com.example.booprachat.ReceiveDataFromOtherApps;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.SendToGroupAdapter;
import com.example.booprachat.Dashboard_Activity;
import com.example.booprachat.Model.GroupChatList;
import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SendToGroupActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ArrayList<GroupChatList> groupChatLists;
    private SendToGroupAdapter sendToGroupAdapter;
    private Button SendToChat, goToChatDashboard;
    private Toolbar toolbar;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_to_group);

        //declaring ids
        recyclerView = findViewById(R.id.send_to_group_recycleView);
        SendToChat = findViewById(R.id.send_to_chat);
        goToChatDashboard = findViewById(R.id.go_to_chat_dashboard);

        toolbar = findViewById(R.id.send_to_group_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //disable back button
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        String text = intent.getStringExtra("text");
        String requestCode = intent.getStringExtra("requestCode");
        String pdfUri = intent.getStringExtra("pdfUri");
        String imageUri = intent.getStringExtra("imageUri");
        String audioUri = intent.getStringExtra("audioUri");
        String videoUri = intent.getStringExtra("videoUri");
        String fileName = intent.getStringExtra("fileName");
        ArrayList<Uri> multipleImageUris = intent.getParcelableArrayListExtra("multipleImageUris");
        String secondaryRequestCode = intent.getStringExtra("secondaryRequestCode");


        if (secondaryRequestCode.equals("IRA")) {
            goToChatDashboard.setVisibility(View.VISIBLE);
            SendToChat.setVisibility(View.VISIBLE);

            goToChatDashboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent newActivity = new Intent(SendToGroupActivity.this, Dashboard_Activity.class);
                    startActivity(newActivity);
                    finish();
                }
            });

            SendToChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent1 = new Intent(SendToGroupActivity.this, SendToActivity.class);
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

            if (secondaryRequestCode.equals("CA") || secondaryRequestCode.equals("favourite")) {
                //enable back button
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                SendToChat.setVisibility(View.GONE);
                goToChatDashboard.setVisibility(View.GONE);
            }

            if (secondaryRequestCode.equals("GCA")) {
                SendToChat.setVisibility(View.VISIBLE);
                goToChatDashboard.setVisibility(View.GONE);

                SendToChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent1 = new Intent(SendToGroupActivity.this, SendToActivity.class);
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

            } else if (!secondaryRequestCode.equals("CA")) {
                SendToChat.setVisibility(View.GONE);
                goToChatDashboard.setVisibility(View.GONE);

            } else {
                //enable back button
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                SendToChat.setVisibility(View.GONE);
                goToChatDashboard.setVisibility(View.GONE);
            }
        }

        groupChatLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    //if current user, uid exists in Participants list of group then show that group
                    if (ds.child("participants").child(firebaseAuth.getUid()).exists()) {
                        GroupChatList modelGroupChatList = ds.getValue(GroupChatList.class);

                        groupChatLists.add(modelGroupChatList);
                    }
                    //set adapter
                    sendToGroupAdapter = new SendToGroupAdapter(SendToGroupActivity.this, groupChatLists, text, requestCode, pdfUri, imageUri, audioUri, videoUri, fileName, multipleImageUris);
                    recyclerView.setAdapter(sendToGroupAdapter);
                }
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