package com.example.booprachat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.booprachat.ChatViews.ImageViewPage;
import com.example.booprachat.Fragments.GroupChatFragment;
import com.example.booprachat.Fragments.Home_Fragment;
import com.example.booprachat.Fragments.Users_Fragment;
import com.example.booprachat.Model.Chat;
import com.example.booprachat.Notification.Token;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Dashboard_Activity extends AppCompatActivity {

    CircleImageView ProfileView;
    TextView profileText;
    TextView Username, unreadMessageCount;
    Toolbar toolbar;
    CardView unreadMessageCountCardLayout;

    ArrayList<Chat> chats;

    //firebase services
    FirebaseAuth firebaseAuth;

    //strings
    String mUID;
    String storagePermissions[];

    private static final int STORAGE_REQUEST_CODE = 100;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocal();
        setContentView(R.layout.activity_dashboard);

        //declaring ui ids
        Username = findViewById(R.id.username);
        unreadMessageCount = findViewById(R.id.unread_message_count);
        unreadMessageCountCardLayout = findViewById(R.id.unread_message_count_card_layout);
        ProfileView = findViewById(R.id.profile_image);
        profileText = findViewById(R.id.profile_text);
        toolbar = findViewById(R.id.toolbar);

        //bottom navigation
        BottomNavigationView navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //set toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //firebase services
        firebaseAuth = FirebaseAuth.getInstance();

        //home fragment transaction (default on start)
        Home_Fragment home_fragment = new Home_Fragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, home_fragment, "");
        ft1.commit();

        checkUserStatus();
        updateToken(FirebaseInstanceId.getInstance().getToken());//bug //update token
        loadUserInfo();
        loadUnreadMessageCount();
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        //check if storage permissin is enabled or not
        //return true if enable
        //return false if not enable
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case STORAGE_REQUEST_CODE: {
                //picking from gallery first check if camera and storage permissions are allowed or not
                if (grantResults.length > 0) {

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //permissions enable

                    } else {
                        //permission denied
                        //System.exit(0);
                        showPermissionDialogBox();
                    }
                }
            }
            break;
        }
    }

    private void showPermissionDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard_Activity.this);
        builder.setTitle(R.string.notice);
        builder.setMessage(R.string.storage_permission);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.requestPermissions(Dashboard_Activity.this, storagePermissions, STORAGE_REQUEST_CODE);
            }
        }).setNegativeButton(R.string.open, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        builder.show();
    }

    private void loadUnreadMessageCount() {
        chats = new ArrayList<>();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get value
                    Chat chat = ds.getValue(Chat.class);

                    String messageSeenOrNot = "" + ds.child("messageSeenOrNot").getValue();

                    if (chat.getReceiver().equals(firebaseAuth.getUid())) {
                        if (messageSeenOrNot.equals("false")) {
                            chats.add(chat);
                        }
                    }
                }

                String messageCount = String.valueOf(chats.size());
                int count = Integer.parseInt(messageCount);

                if (messageCount.equals("0")) {
                    unreadMessageCountCardLayout.setVisibility(View.GONE);

                } else if (count > 10000) {
                    unreadMessageCountCardLayout.setVisibility(View.VISIBLE);
                    unreadMessageCount.setText("10000+");

                } else {
                    unreadMessageCountCardLayout.setVisibility(View.VISIBLE);
                    unreadMessageCount.setText(messageCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadUserInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    String image = "" + ds.child("image").getValue();

                    //set data
                    Username.setText(name);

                    if (image.equals("")) {
                        ProfileView.setVisibility(View.GONE);
                        profileText.setVisibility(View.VISIBLE);

                        String firstLetter = String.valueOf(name.charAt(0)).toLowerCase();

                        Drawable drawable = profileText.getBackground();
                        drawable = DrawableCompat.wrap(drawable);

                        if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#FF00BCD4")); // color blue

                            profileText.setBackground(drawable);

                        } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#FFFF5722")); //color red

                            profileText.setBackground(drawable);

                        } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#bfd200")); //color

                            profileText.setBackground(drawable);

                        } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#FF9800")); //color orange

                            profileText.setBackground(drawable);

                        } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#52b788")); //color old: #cccc00

                            profileText.setBackground(drawable);

                        } else if (firstLetter.equals("z")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#F33D73")); //color darker pink

                            profileText.setBackground(drawable);

                        } else {
                            DrawableCompat.setTint(drawable, Color.parseColor("#6c757d")); //color

                            profileText.setBackground(drawable);

                        }

                        profileText.setText(firstLetter);

                    } else {
                        profileText.setVisibility(View.GONE);
                        ProfileView.setVisibility(View.VISIBLE);

                        Picasso.get().load(image).into(ProfileView);

                        ProfileView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String requestCode = "DA"; // DA means this activity
                                Intent intent = new Intent(Dashboard_Activity.this, ImageViewPage.class);
                                intent.putExtra("imageUrl", image);
                                intent.putExtra("requestCode", requestCode);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkOnlineStatus(String status) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("onlineStatus", status);
            //update onlineStatus of current user
            reference.child(user.getUid()).updateChildren(hashMap);
        }
    }

    public void updateToken(String token) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        reference.child(user.getUid()).setValue(mToken);//bug
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // handle item clicks
            switch (item.getItemId()) {
                case R.id.nav_home:
                    //home fragment
                    Home_Fragment home_fragment = new Home_Fragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.content, home_fragment, "");
                    ft1.commit();
                    return true;

                case R.id.nav_users:
                    //users fragment
                    Users_Fragment users_fragment = new Users_Fragment();
                    FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                    ft2.replace(R.id.content, users_fragment, "");
                    ft2.commit();
                    return true;

                case R.id.nav_group_Chat:
                    //group Chat fragment
                    GroupChatFragment groupChatFragment = new GroupChatFragment();
                    FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                    ft4.replace(R.id.content, groupChatFragment, "");
                    ft4.commit();
                    return true;
            }
            return false;
        }
    };

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {

            if (!checkStoragePermission()) {
                requestStoragePermission();
            }
            // if user signed in already
            mUID = user.getUid();
            //set online
            //save userid of current signed in user in shared perferences
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();
        } else {
            //if user is not signed in goto mainActivity
            startActivity(new Intent(Dashboard_Activity.this, MainActivity.class));
            finish();
        }
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

    public void loadLocal() {
        SharedPreferences editor = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String lan = editor.getString("My_language", "");
        setLocal(lan);
    }

    /*@Override
    protected void onPause() {
        super.onPause();
        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        //set offline with last seen
        checkOnlineStatus(timestamp);
    }*/

    @Override
    protected void onResume() {
        checkUserStatus();

        super.onResume();
    }


    @Override
    protected void onStart() {
        // check on start app
        checkUserStatus();
        checkOnlineStatus("online");
        //for checking internet connetion
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, intentFilter);
        super.onStart();
    }

    @Override
    public void onDestroy() {
        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        //set offline with last seen
        checkOnlineStatus(timestamp);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        //for checking internet connetion
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

}