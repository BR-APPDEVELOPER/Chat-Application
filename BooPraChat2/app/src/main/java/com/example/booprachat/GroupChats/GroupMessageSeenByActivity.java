package com.example.booprachat.GroupChats;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.GroupMessageSeenByAdapter;
import com.example.booprachat.Model.Users;
import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GroupMessageSeenByActivity extends AppCompatActivity {

    private TextView selectedMessage, messageSeenByCount;
    private ImageView ChatScreenImage;
    private RecyclerView recyclerView;

    private GroupMessageSeenByAdapter groupMessageSeenByAdapter;
    private ArrayList<Users> usersList;
    private ArrayList<String> groupMembersCount;
    private FirebaseAuth firebaseAuth;

    private Toolbar toolbar;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message_seen_by);

        //declaring ui ids
        selectedMessage = findViewById(R.id.selected_message);
        messageSeenByCount = findViewById(R.id.seen_by_tv);
        ChatScreenImage = findViewById(R.id.chat_screen_image);
        toolbar = findViewById(R.id.message_seen_by_toolbar);
        recyclerView = findViewById(R.id.group_message_seen_by_recycleview);

        firebaseAuth = FirebaseAuth.getInstance();
        usersList = new ArrayList<>();
        groupMembersCount = new ArrayList<>();

        //set toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //get intent from Group chat adapter in (button info.clickListener)
        Intent intent = getIntent();
        String messageTime = intent.getStringExtra("time");
        String groupId = intent.getStringExtra("groupId");
        String message = intent.getStringExtra("message");

        //show selected message
        selectedMessage.setText(message);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(messageTime).child("messageSeenMembers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get user ID
                    String uid = "" + ds.child("uid").getValue();

                    String time = "";
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                    userRef.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot ds : snapshot.getChildren()) {
                                Users users = ds.getValue(Users.class);
                                if (!uid.equals(firebaseAuth.getUid())) {
                                    usersList.add(users);
                                }
                            }
                            // setup adapter
                            groupMessageSeenByAdapter = new GroupMessageSeenByAdapter(GroupMessageSeenByActivity.this, usersList, time);
                            //set adapter to recycle view
                            recyclerView.setAdapter(groupMessageSeenByAdapter);

                            loadGroupMembersCount(groupId); //calling method
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

        //calling method
        loadChatScreenImage();
    }

    private void loadGroupMembersCount(String groupId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupMembersCount.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String uid = "" + ds.child("uid").getValue();
                    groupMembersCount.add(uid);
                }
                int messageSeenMemberCount = groupMembersCount.size() - 1;
                //set how many members seen the message, members seen count
                messageSeenByCount.setText(getString(R.string.message_seen_by) + "        " + usersList.size() + "/" + messageSeenMemberCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadChatScreenImage() {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String chatScreenImage = "" + ds.child("chatScreenImage").getValue();

                    if (chatScreenImage.equals("")) {
                        ChatScreenImage.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Picasso.get().load(chatScreenImage).into(ChatScreenImage);
                    }
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