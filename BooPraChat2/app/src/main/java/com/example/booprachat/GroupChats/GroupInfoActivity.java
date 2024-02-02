package com.example.booprachat.GroupChats;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.ParticipantsAddAdapter;
import com.example.booprachat.Dashboard_Activity;
import com.example.booprachat.Model.Role;
import com.example.booprachat.Model.Users;
import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    //strings
    private String groupId;
    private String GroupTitle;
    private String theMessageCanSendBy;
    private String myGroupRole = "";
    private String namesList = "";
    private String IdsList = "";

    //ui views
    //toolbar bar
    private Toolbar toolbar;
    private TextView groupName, userGroupRole;
    private ImageView groupIcon;
    private TextView groupDescription, groupCreatedByAndTime, editGroup;
    private TextView addParticipants, exitGroup, readMore;
    private TextView participantsCount, commandersCount, soldiersCount;
    private RecyclerView recyclerView;
    private LinearLayout makeAdminCanSendMsgLock, makeAllCanSendMsgUnlock;
    //firebase services
    private FirebaseAuth firebaseAuth;

    private ArrayList<Users> usersList;
    private ArrayList<Role> groupCommanderRole;
    private ArrayList<Role> groupSoldierRole;
    private ParticipantsAddAdapter participantsAddAdapter;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocalLanguage();
        setContentView(R.layout.activity_group_info);

        //declaring ids
        groupIcon = findViewById(R.id.group_icon);
        groupDescription = findViewById(R.id.group_description);
        groupCreatedByAndTime = findViewById(R.id.group_created_by_and_time);
        toolbar = findViewById(R.id.group_info_toolbar);
        groupName = findViewById(R.id.group_name);
        userGroupRole = findViewById(R.id.user_group_role);
        editGroup = findViewById(R.id.edit_group);
        addParticipants = findViewById(R.id.add_participants);
        exitGroup = findViewById(R.id.exit_group);
        makeAdminCanSendMsgLock = findViewById(R.id.make_admin_can_send_msg_lock);
        makeAllCanSendMsgUnlock = findViewById(R.id.make_all_can_send_msg_unlock);
        participantsCount = findViewById(R.id.participants_count);
        soldiersCount = findViewById(R.id.soldiers_count);
        commandersCount = findViewById(R.id.commanders_count);
        readMore = findViewById(R.id.read_more);
        recyclerView = findViewById(R.id.all_participants_recycleview);

        //set toolbar bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //enable back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        //get intent from group message activity or group chatList fragment
        groupId = getIntent().getStringExtra("groupId");

        //calling method
        loadGroupInfo();
        loadMyGroupRole();

        makeAdminCanSendMsgLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeOnlyAdminCanSendMessage();
            }
        });

        makeAllCanSendMsgUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeAllCanSendMessage();
            }
        });

        addParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, AddParticipantsToGroupActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        exitGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if user is participant/admin leave group
                //if user is creator delete group
                String dialogTitle = "";
                String dialogDescription = "";
                String positiveButtonTitle = "";

                if (myGroupRole.equals("leader")) {
                    dialogTitle = getString(R.string.delete_team);
                    dialogDescription = getString(R.string.are_you_sure_you_want_to_delete_team);
                    positiveButtonTitle = getString(R.string.ok);
                } else {
                    dialogTitle = getString(R.string.leave_team);
                    dialogDescription = getString(R.string.are_you_sure_you_want_to_leave_from_this_team);
                    positiveButtonTitle = getString(R.string.ok);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (myGroupRole.equals("leader")) {
                                    deleteGroup();
                                } else {
                                    leaveGroup();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });

        editGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, EditGroupDetailsActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        readMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, ReadGroupDescriptionActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });
    }

    private void makeAllCanSendMessage() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("theMessageCanSendBy", "all");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // successfully done means
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.now_all_can_send_message), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeOnlyAdminCanSendMessage() {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("theMessageCanSendBy", "onlyCommander");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // successfully done means
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.now_only_commanders_can_send_message), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //group deleted successfully
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.team_deleted_successfully), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, Dashboard_Activity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to delete this group
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void leaveGroup() {

        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", myGroupRole);
        hashMap.put("time", timestamp);
        hashMap.put("type", "leavedFromGroup");

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Groups");
        reference1.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                        reference.child(groupId).child("participants").child(firebaseAuth.getUid())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //if user successfully leave from group
                                        startActivity(new Intent(GroupInfoActivity.this, Dashboard_Activity.class));
                                        finish();
                                        Toast.makeText(GroupInfoActivity.this, getString(R.string.successfully_your_leaved_from_team), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed to leave from this group
                                        Toast.makeText(GroupInfoActivity.this, getString(R.string.failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });


    }

    private void loadMyGroupRole() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("participants").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get data
                            myGroupRole = "" + ds.child("role").getValue();

                            userGroupRole.setText("(" + myGroupRole + ")");

                            if (myGroupRole.equals("soldier")) { //if the user is only the group member means
                                editGroup.setVisibility(View.GONE); //edit group option will not show
                                addParticipants.setVisibility(View.GONE); //add participants option will not show
                                makeAdminCanSendMsgLock.setVisibility(View.GONE); //makeAdminCanSendMsg lock option will not show
                                makeAllCanSendMsgUnlock.setVisibility(View.GONE); //makeAdminCanSendMsg unlock option will not show
                                exitGroup.setText(getString(R.string.leave_from_team));

                            } else if (myGroupRole.equals("leader")) { //if the user is the group creator means
                                editGroup.setVisibility(View.VISIBLE); //edit group option will show
                                addParticipants.setVisibility(View.VISIBLE); //add participants option will show
                                exitGroup.setText(getString(R.string.delete_team));

                            } else if (myGroupRole.equals("commander")) { //if the user is a group admin means
                                editGroup.setVisibility(View.VISIBLE); //edit group option will show
                                addParticipants.setVisibility(View.VISIBLE); //add participants option will show
                                exitGroup.setText(getString(R.string.leave_from_team));
                            }

                            loadParticipants();
                            loadGroupCommanderAndSoldierCountRole();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadGroupCommanderAndSoldierCountRole() {
        // for getting how many commanders and soldier are in this group
        groupCommanderRole = new ArrayList<>();
        groupSoldierRole = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupCommanderRole.clear();
                groupSoldierRole.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String role = "" + ds.child("role").getValue();
                    Role role1 = ds.getValue(Role.class);

                    if (role.equals("commander") || role.equals("leader")) {
                        groupCommanderRole.add(role1); //getting how many commanders in this group

                    } else if (role.equals("soldier")) {
                        groupSoldierRole.add(role1); //getting how many solder in this group
                    }

                    commandersCount.setText(getString(R.string.commander) + "(" + groupCommanderRole.size() + ")");
                    soldiersCount.setText(getString(R.string.soldier) + "(" + groupSoldierRole.size() + ")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadParticipants() {
        usersList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String uid = "" + ds.child("uid").getValue();

                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
                    reference1.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                //get data
                                Users users = ds.getValue(Users.class);
                                usersList.add(users);
                            }
                            //setup adapter
                            participantsAddAdapter = new ParticipantsAddAdapter(GroupInfoActivity.this, usersList, groupId, myGroupRole);
                            //set adapter in recycle view
                            recyclerView.setAdapter(participantsAddAdapter);
                            participantsCount.setText(GroupTitle + " " + getString(R.string.team) + "(" + usersList.size() + ")"); //set how many members are in this group
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

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            //set data
                            GroupTitle = "" + ds.child("groupTitle").getValue();
                            String GroupDescription = "" + ds.child("groupDescription").getValue();
                            String GroupIcon = "" + ds.child("groupIcon").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String GroupCreatedBy = "" + ds.child("groupCreatedBy").getValue();
                            theMessageCanSendBy = "" + ds.child("theMessageCanSendBy").getValue();

                            if (theMessageCanSendBy.equals("onlyCommander")) {
                                makeAdminCanSendMsgLock.setVisibility(View.GONE);
                                makeAllCanSendMsgUnlock.setVisibility(View.VISIBLE);
                            } else {
                                makeAdminCanSendMsgLock.setVisibility(View.VISIBLE);
                                makeAllCanSendMsgUnlock.setVisibility(View.GONE);
                            }

                            //convert timestamp to dd/mm/yyyy hh:mm am/pm
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                            //set data
                            groupName.setText(GroupTitle);
                            groupName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                            groupName.setSelected(true);

                            groupDescription.setText(getString(R.string.team_description) + GroupDescription);

                            //calling method
                            loadCreatedInfo(dateTime, GroupCreatedBy);

                            try {
                                //if there is a group icon set that icon
                                Picasso.get().load(GroupIcon).into(groupIcon);
                            } catch (Exception e) {
                                groupIcon.setImageResource(R.drawable.ic_baseline_for_group_info);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadCreatedInfo(String dateTime, String groupCreatedBy) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(groupCreatedBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    //set data
                    groupCreatedByAndTime.setText(getString(R.string.team_created_by) + " " + name + " " + dateTime + getString(R.string.on));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void loadLocalLanguage() {
        SharedPreferences editor = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = editor.getString("My_language", "");
        setLocal(language);
    }

    private void setLocal(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_language", language);
        editor.apply();
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