package com.example.booprachat.Settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

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

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings_Activity extends AppCompatActivity {

    CircleImageView ProfileImage;
    TextView ProfileText;
    TextView Username, UserEmail;
    Toolbar SettingsToolbar;

    LinearLayout ChangeUserDetails, ChangeWallpaper, ChangeLanguage, ChangeTypingToWallpaper, BlockedUsers;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocal();
        setContentView(R.layout.activity_settings);

        //declaring ids
        ChangeUserDetails = findViewById(R.id.edit_profile);
        ChangeWallpaper = findViewById(R.id.edit_wallpaper);
        ChangeLanguage = findViewById(R.id.change_language);
        ChangeTypingToWallpaper = findViewById(R.id.change_typing_to_wallpaper);
        BlockedUsers = findViewById(R.id.blocked_users);
        Username = findViewById(R.id.username);
        UserEmail = findViewById(R.id.user_email);
        ProfileImage = findViewById(R.id.profileImage);
        ProfileText = findViewById(R.id.profile_text);

        SettingsToolbar = findViewById(R.id.settings_toolbar);//for action toolbar

        //action tool bar
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setTitle("");
        //enable back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        Username.setText("");

        ChangeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeLanguageDialog();
            }
        });

        ChangeTypingToWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings_Activity.this, TypingToWallpaperActivity.class));
            }
        });

        BlockedUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings_Activity.this, BlockedUsersActivity.class));
            }
        });

        //one click to open the profile layout
        ChangeUserDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings_Activity.this, EditUserDetailsActivity.class));
            }
        });


        //one click to open the wallpaper layout
        ChangeWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings_Activity.this, EditWallPaperActivity.class));
            }
        });

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String image = "" + ds.child("image").getValue();

                    //set data
                    Username.setText(name); //set username
                    UserEmail.setText(email);

                    //set profile image
                    if (image.equals("")) {
                        //if there is any exception while getting this image then set this
                        ///it means if there is no image set this

                        ProfileImage.setVisibility(View.GONE);
                        ProfileText.setVisibility(View.VISIBLE);

                        String firstLetter = String.valueOf(name.charAt(0)).toLowerCase();

                        Drawable drawable = ProfileText.getBackground();
                        drawable = DrawableCompat.wrap(drawable);

                        if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#FF00BCD4")); // color blue

                            ProfileText.setBackground(drawable);

                        } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#FFFF5722")); //color red

                            ProfileText.setBackground(drawable);

                        } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#FFC107")); //color yellow

                            ProfileText.setBackground(drawable);

                        } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#FF9800")); //color orange

                            ProfileText.setBackground(drawable);

                        } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#52b788")); //color old: #cccc00

                            ProfileText.setBackground(drawable);

                        } else if (firstLetter.equals("z")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#F33D73")); //color darker pink
                            ProfileText.setBackground(drawable);

                        }
                        ProfileText.setText(firstLetter);

                    } else {
                        //if image is received then set this
                        //if there is image set that image
                        ProfileText.setVisibility(View.GONE);
                        ProfileImage.setVisibility(View.VISIBLE);

                        Picasso.get().load(image).into(ProfileImage); //for user profile
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showChangeLanguageDialog() {
        String[] listLanguages = {"Tamil(தமிழ்)", "English"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_language);
        builder.setSingleChoiceItems(listLanguages, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    setLocal("ta");
                    recreate();
                }

                if (i == 1) {
                    setLocal("en");
                    recreate();
                }
            }
        }).show();


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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // go to previous page
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