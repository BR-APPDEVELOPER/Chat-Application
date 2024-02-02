package com.example.booprachat.GroupChats;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.GroupChatAdapter;
import com.example.booprachat.Model.GroupChat;
import com.example.booprachat.Model.Users;
import com.example.booprachat.R;
import com.example.booprachat.Settings.EditWallPaperActivity;
import com.example.booprachat.Settings.TypingToWallpaperActivity;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessageActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView groupProfile;
    private TextView groupProfileText;
    private EditText messageBox;
    private TextView groupName, groupMembersName;
    private ImageButton SendMessage, BackButton;
    private RecyclerView recyclerView;
    private ImageView ChatScreenImage, attachFile;
    private ImageView delete, copy, forward, share, favourite, info, closeToolbar1;
    private ImageView camera, gallery, audio, video, pdf, closeToolbar2;
    //layout
    private LinearLayout onlyAdminSendMessage, allCanSendMessage, groupNameLayout;
    private LinearLayout toolbar1, toolbar2;
    //strings
    private String groupId, myGroupRole = "";
    private String imageFileName;
    //private String uid = "";
    //firebase services
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private ArrayList<GroupChat> groupChats;
    private GroupChatAdapter groupChatAdapter;
    private ProgressDialog pg;
    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //arrays of permissions to be requested
    private String cameraPermissions[];
    private String storagePermissions[];

    private Uri image_uri = null;
    private Uri audioUri, videoUrl, pdfUri;

    private ArrayList<String> usersList;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocal();
        setContentView(R.layout.activity_group_message);

        //declaring ids
        groupProfile = findViewById(R.id.group_Profile);
        groupProfileText = findViewById(R.id.group_profile_text);
        messageBox = findViewById(R.id.typing_box);
        groupName = findViewById(R.id.group_name);
        groupMembersName = findViewById(R.id.group_members_name);
        SendMessage = findViewById(R.id.btn_send);
        BackButton = findViewById(R.id.back_btn);
        ChatScreenImage = findViewById(R.id.chat_screen_image);
        onlyAdminSendMessage = findViewById(R.id.only_admin_send_msg_layout);
        allCanSendMessage = findViewById(R.id.all_can_send_msg_layout);
        groupNameLayout = findViewById(R.id.group_name_layout);
        recyclerView = findViewById(R.id.group_chat_recycleview);
        attachFile = findViewById(R.id.attach_file);
        copy = findViewById(R.id.copy_message);
        delete = findViewById(R.id.delete_message);
        forward = findViewById(R.id.forward_message);
        share = findViewById(R.id.share_message);
        favourite = findViewById(R.id.favourite);
        info = findViewById(R.id.message_info);
        closeToolbar1 = findViewById(R.id.back_arrow);
        toolbar1 = findViewById(R.id.linear_toolbar_1);
        toolbar2 = findViewById(R.id.linear_toolbar_2);
        camera = findViewById(R.id.camera);
        gallery = findViewById(R.id.gallery);
        audio = findViewById(R.id.audio);
        video = findViewById(R.id.video);
        pdf = findViewById(R.id.pdf);
        closeToolbar2 = findViewById(R.id.toolbar2_back_arrow);

        //hide toolbar2 and 3
        toolbar1.setVisibility(View.GONE);
        toolbar2.setVisibility(View.GONE);

        toolbar = findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        //getting groupid from groupChatListAdapter
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //declaring firebase services
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //calling method
        loadGroupMessages();
        loadMyGroupRole();
        loadGroupInfo();
        loadChatScreenImage();
        loadGroupParticipantsNames();

        //on click to make visible toolbar 2
        attachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar1.setVisibility(View.GONE);

                if (toolbar2.getVisibility() == View.GONE) {
                    toolbar2.setVisibility(View.VISIBLE);
                } else {
                    toolbar2.setVisibility(View.GONE);
                }
            }
        });

        //back to dashboard activity
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //close toolbar 1
        closeToolbar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar1.setVisibility(View.GONE);
            }
        });

        // if camera clicked
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for camera
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromCamera();
                }

                toolbar2.setVisibility(View.GONE); //close toolbar2
            }
        });

        //if gallery clicked
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for gallery
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickFromGallery();
                }

                toolbar2.setVisibility(View.GONE); //close toolbar2
            }
        });

        // if audio clicked
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for sending audio
                if (ContextCompat.checkSelfPermission(GroupMessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectAudio();
                } else {
                    ActivityCompat.requestPermissions(GroupMessageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
                }

                toolbar2.setVisibility(View.GONE); //close toolbar2
            }
        });

        //if video clicked
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for sending video
                if (ContextCompat.checkSelfPermission(GroupMessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectVideo();
                } else {
                    ActivityCompat.requestPermissions(GroupMessageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
                }

                toolbar2.setVisibility(View.GONE); //close toolbar2
            }
        });

        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for sending pdf
                if (ContextCompat.checkSelfPermission(GroupMessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectPdf();
                } else {
                    ActivityCompat.requestPermissions(GroupMessageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
                }

                toolbar2.setVisibility(View.GONE); //close toolbar2
            }
        });

        //close toolbar 2
        closeToolbar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar2.setVisibility(View.GONE);
            }
        });

        SendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = messageBox.getText().toString().trim();

                if (TextUtils.isEmpty(message)) {
                    // if the message box is empty do not send message
                    Toast.makeText(GroupMessageActivity.this, getString(R.string.please_enter_message), Toast.LENGTH_SHORT).show();
                } else {
                    //send message
                    //calling method
                    sendMessage();
                    //messageBox.setText("");//clear message box
                }
            }
        });

        groupNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupMessageActivity.this, GroupInfoActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

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

    private void loadChatScreenImage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String chatScreenImage = "" + ds.child("chatScreenImage").getValue();

                    if (!chatScreenImage.equals("")) {
                        Picasso.get().load(chatScreenImage).into(ChatScreenImage);
                    }
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
                            String groupTitle = "" + ds.child("groupTitle").getValue();
                            String groupIcon = "" + ds.child("groupIcon").getValue();
                            String theMessageCanSendBy = "" + ds.child("theMessageCanSendBy").getValue();

                            //set data
                            groupName.setText(groupTitle);

                            if (theMessageCanSendBy.equals("onlyCommander")) {
                                if (myGroupRole.equals("leader")) {
                                    onlyAdminSendMessage.setVisibility(View.GONE);
                                    allCanSendMessage.setVisibility(View.VISIBLE);
                                } else if (myGroupRole.equals("commander")) {
                                    onlyAdminSendMessage.setVisibility(View.GONE);
                                    allCanSendMessage.setVisibility(View.VISIBLE);
                                } else if (myGroupRole.equals("soldier")) {
                                    allCanSendMessage.setVisibility(View.GONE);
                                    onlyAdminSendMessage.setVisibility(View.VISIBLE);
                                }

                            } else {
                                allCanSendMessage.setVisibility(View.VISIBLE);
                                onlyAdminSendMessage.setVisibility(View.GONE);
                            }

                            String firstLetter = String.valueOf(groupTitle.charAt(0)).toLowerCase();

                            Drawable drawable = groupProfileText.getBackground();
                            drawable = DrawableCompat.wrap(drawable);

                            if (groupIcon.equals("")) {
                                groupProfile.setVisibility(View.GONE);
                                groupProfileText.setVisibility(View.VISIBLE);

                                if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                                    DrawableCompat.setTint(drawable, Color.parseColor("#FF00BCD4")); // color blue

                                    groupProfileText.setBackground(drawable);

                                } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                                    DrawableCompat.setTint(drawable, Color.parseColor("#FFFF5722")); //color red

                                    groupProfileText.setBackground(drawable);

                                } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                                    DrawableCompat.setTint(drawable, Color.parseColor("#bfd200")); //color

                                    groupProfileText.setBackground(drawable);

                                } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                                    DrawableCompat.setTint(drawable, Color.parseColor("#FF9800")); //color orange

                                    groupProfileText.setBackground(drawable);

                                } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                                    DrawableCompat.setTint(drawable, Color.parseColor("#52b788")); //color old: #cccc00

                                    groupProfileText.setBackground(drawable);

                                } else if (firstLetter.equals("z")) {
                                    DrawableCompat.setTint(drawable, Color.parseColor("#F33D73")); //color darker pink

                                    groupProfileText.setBackground(drawable);

                                } else {
                                    DrawableCompat.setTint(drawable, Color.parseColor("#6c757d")); //color

                                    groupProfileText.setBackground(drawable);
                                }
                                //set first letter of name
                                groupProfileText.setText(firstLetter);


                            } else {
                                //if there is a group image set that image
                                groupProfileText.setVisibility(View.GONE);
                                groupProfile.setVisibility(View.VISIBLE);

                                Picasso.get().load(groupIcon).into(groupProfile);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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
                            myGroupRole = "" + ds.child("role").getValue();
                            //refresh menu items
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadGroupParticipantsNames() {
        usersList = new ArrayList<String>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String uid = "" + ds.child("uid").getValue();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                Users users = ds.getValue(Users.class);
                                usersList.add(users.getName());

                                String membersName = usersList.toString();
                                groupMembersName.setText(membersName);
                                groupMembersName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                groupMembersName.setSelected(true);
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

    private void sendMessage() {

        //getting user entered message and encoding the message
        String message = messageBox.getText().toString(); //this line for getting message
        messageBox.setText("");//clear message box

        //get message time
        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", message);
        hashMap.put("time", timestamp);
        hashMap.put("type", "text");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupMessageActivity.this, getString(R.string.failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadGroupMessages() {
        //init list
        groupChats = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChats.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    GroupChat groupChat = ds.getValue(GroupChat.class);
                    groupChats.add(groupChat);
                }
                //adapter
                groupChatAdapter = new GroupChatAdapter(GroupMessageActivity.this, groupChats, groupId, toolbar1, toolbar2, copy, delete, forward, share, favourite, info);
                groupChatAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(groupChatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void selectAudio() {
        //select a file using file manager
        //we will be using an Intent for this action
        Intent intent = new Intent();
        intent.setType("audio/*"); //for audio
        intent.setAction(Intent.ACTION_GET_CONTENT); //to fetch files
        startActivityForResult(intent, 87);
    }

    private void selectVideo() {
        Intent intent = new Intent();
        intent.setType("video/*"); //for video
        intent.setAction(Intent.ACTION_GET_CONTENT); //to fetch files
        startActivityForResult(intent, 88);
    }

    private void selectPdf() {
        //select a file using file manager
        //we will be using an Intent for this action
        Intent intent = new Intent();
        intent.setType("application/pdf"); //for pdf
        intent.setAction(Intent.ACTION_GET_CONTENT); //to fetch files
        startActivityForResult(intent, 86);
    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/* ");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        //put image url
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        //check if storage permission is enabled or not
        //return true if enable
        //return false if not enable
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //check if storage permission is enabled or not
        //return true if enable
        //return false if not enable
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void sendImageMessage(Uri image_uri, String imageFileName) throws IOException {
        //progress dialog
        pg = new ProgressDialog(this);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setMessage(getString(R.string.sending_image));
        pg.setCanceledOnTouchOutside(false);

        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        String fileNameAndPath = "GroupChatImages/" + groupId + "__" + timestamp + "__" + imageFileName;
        //Chats node will be created that will contain all image sent via chat
        //get bitmap from image uri

        /*try {
          Bitmap bitmap = new Compressor(this)
                .setMaxHeight(200) //Set height and width
              .setMaxWidth(200)
            .setQuality(100) // Set Quality
          .compressToBitmap(file);
          } catch (IOException e) {
            e.printStackTrace();
        }*/

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
        byte[] data = baos.toByteArray(); // convert image to bytes
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image uploaded
                //get uri of uploaded image
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {
                    //add image uri nad other info to database
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", firebaseAuth.getUid());
                    hashMap.put("message", downloadUri);
                    hashMap.put("time", timestamp);
                    hashMap.put("type", "image");
                    hashMap.put("fileName", imageFileName);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                    reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pg.dismiss();
                                    Toast.makeText(GroupMessageActivity.this, getString(R.string.image_uploaded), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(GroupMessageActivity.this, getString(R.string.failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // if upload failed
                pg.dismiss();
                Toast.makeText(GroupMessageActivity.this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pg.setProgress(currentProgress);
            }
        });
    }

    private void uploadAudio(Uri audioUri, String audioFileName) {
        pg = new ProgressDialog(this);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setCanceledOnTouchOutside(false);
        pg.setTitle(getString(R.string.sending_audio));
        pg.setProgress(0);

        pg.show();

        String timestamp = System.currentTimeMillis() + "";
        //StorageReference storageReference = storage.getReference(); //returns root path

        String fileNameAndPath = "GroupChatAudio/" + groupId + "__" + timestamp + "__" + audioFileName;
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);

        ref.putFile(audioUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();

                        //add image uri nad other info to database
                        //store the url in realtime database
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", firebaseAuth.getUid());
                            hashMap.put("message", downloadUri);
                            hashMap.put("time", timestamp);
                            hashMap.put("type", "audio");
                            hashMap.put("fileName", audioFileName);


                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                            reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pg.dismiss();
                                            Toast.makeText(GroupMessageActivity.this, getString(R.string.audio_uploaded), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pg.dismiss();
                                            Toast.makeText(GroupMessageActivity.this, getString(R.string.failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pg.dismiss();
                Toast.makeText(GroupMessageActivity.this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pg.setProgress(currentProgress);
            }
        });
    }

    private void uploadVideo(Uri videoUrl, String videoFileName) {
        pg = new ProgressDialog(this);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setCanceledOnTouchOutside(false);
        pg.setTitle(getString(R.string.sending_video));
        pg.setProgress(0);

        pg.show();

        String timestamp = System.currentTimeMillis() + "";

        String fileNameAndPath = "GroupChatVideo/" + groupId + "__" + timestamp + "__" + videoFileName;
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);

        ref.putFile(videoUrl)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();

                        //String url = taskSnapshot.getStorage().getDownloadUrl().toString(); // returns the url of you uploaded file..
                        //add image uri nad other info to database
                        //store the url in realtime database
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", firebaseAuth.getUid());
                            hashMap.put("message", downloadUri);
                            hashMap.put("time", timestamp);
                            hashMap.put("type", "video");
                            hashMap.put("fileName", videoFileName);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                            reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pg.dismiss();
                                            Toast.makeText(GroupMessageActivity.this, getString(R.string.video_uploaded), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pg.dismiss();
                                            Toast.makeText(GroupMessageActivity.this, getString(R.string.failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pg.dismiss();
                Toast.makeText(GroupMessageActivity.this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pg.setProgress(currentProgress);
            }
        });
    }

    private void uploadPdf(Uri pdfUri, String pdfFileName) {
        pg = new ProgressDialog(this);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setTitle(getString(R.string.sending_pdf));
        pg.setProgress(0);

        pg.show();

        String timestamp = System.currentTimeMillis() + "";

        String fileNameAndPath = "GroupChatPdf/" + groupId + "__" + timestamp + "__" + pdfFileName;
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);

        ref.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();

                        //add image uri nad other info to database
                        //store the url in realtime database
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", firebaseAuth.getUid());
                            hashMap.put("message", downloadUri);
                            hashMap.put("time", timestamp);
                            hashMap.put("type", "pdf");
                            hashMap.put("fileName", pdfFileName);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                            reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pg.dismiss();
                                            Toast.makeText(GroupMessageActivity.this, getString(R.string.pdf_uploaded), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pg.dismiss();
                                            Toast.makeText(GroupMessageActivity.this, getString(R.string.failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                }).

                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pg.dismiss();
                        Toast.makeText(GroupMessageActivity.this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show();
                    }
                }).

                addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        pg.setProgress(currentProgress);
                        pg.setCanceledOnTouchOutside(false);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //for Audio
        if (requestCode == 10 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectAudio();
        } else {
            Toast.makeText(this, getString(R.string.please_provide_permission_to_access_storage), Toast.LENGTH_SHORT).show();
        }

        //for video
        if (requestCode == 11 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectVideo();
        } else {
            Toast.makeText(this, getString(R.string.please_provide_permission_to_access_storage), Toast.LENGTH_SHORT).show();
        }

        //for pdf
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectPdf();
        } else {
            Toast.makeText(this, getString(R.string.please_provide_permission_to_access_storage), Toast.LENGTH_SHORT).show();
        }

        //for image
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                //picking from camera first check if camera and storage permissions are allowed or not
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //permissions enable
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(this, getString(R.string.please_enable_camera_and_storage_permission), Toast.LENGTH_SHORT).show();
                    }
                } else {

                }
            }
            break;

            case STORAGE_REQUEST_CODE: {
                //picking from gallery first check if camera and storage permissions are allowed or not
                if (grantResults.length > 0) {

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //permissions enable
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this, getString(R.string.please_provide_permission_to_access_storage), Toast.LENGTH_SHORT).show();
                    }
                } else {

                }

            }
            break;
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery, get uri of image
                image_uri = data.getData();

                String imageUri = image_uri.toString(); //convert uri to String

                File myFile = new File(imageUri);
                String path = myFile.getAbsolutePath();
                imageFileName = null;

                if (imageUri.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = getContentResolver().query(image_uri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            imageFileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    } finally {
                        cursor.close();
                    }

                } else if (imageUri.startsWith("file://")) {
                    imageFileName = myFile.getName();
                }

                //set to image view
                try {
                    sendImageMessage(image_uri, imageFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {

                String timestamp = "" + System.currentTimeMillis();
                imageFileName = "BPCIMG" + timestamp + ".jpg"; //for file name

                try {
                    sendImageMessage(image_uri, imageFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode == 87) {
                audioUri = data.getData();

                String AudioUri = audioUri.toString(); //convert uri to String

                File myFile = new File(AudioUri);
                String path = myFile.getAbsolutePath();
                String audioFileName = null;

                if (AudioUri.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = getContentResolver().query(audioUri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            audioFileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    } finally {
                        cursor.close();
                    }

                } else if (AudioUri.startsWith("file://")) {
                    audioFileName = myFile.getName();
                }

                uploadAudio(audioUri, audioFileName);

            } else if (requestCode == 88) {
                videoUrl = data.getData();

                String VideoUri = videoUrl.toString(); //convert uri to String

                File myFile = new File(VideoUri);
                String path = myFile.getAbsolutePath();
                String videoFileName = null;

                if (VideoUri.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = getContentResolver().query(videoUrl, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            videoFileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    } finally {
                        cursor.close();
                    }

                } else if (VideoUri.startsWith("file://")) {
                    videoFileName = myFile.getName();
                }

                uploadVideo(videoUrl, videoFileName);
            }
        }

        //check whether user has selected a file are not [ex: pdf]
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData(); //return the uri of selected file

            String PdfUri = pdfUri.toString(); //convert uri to String

            File myFile = new File(PdfUri);
            String path = myFile.getAbsolutePath();
            String pdfFileName = null;

            if (PdfUri.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(pdfUri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        pdfFileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }

            } else if (PdfUri.startsWith("file://")) {
                pdfFileName = myFile.getName();
            }

            uploadPdf(pdfUri, pdfFileName);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.group_message_menu, menu);

        if (myGroupRole.equals("leader") || myGroupRole.equals("commander")) {
            menu.findItem(R.id.add_participant).setVisible(true);
        } else {
            menu.findItem(R.id.add_participant).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_participant) {
            Intent intent = new Intent(this, AddParticipantsToGroupActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }
        if (id == R.id.group_info) {
            Intent intent = new Intent(this, GroupInfoActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }
        if (id == R.id.wallpaper) {
            startActivity(new Intent(GroupMessageActivity.this, EditWallPaperActivity.class));
        }
        if (id == R.id.typing_wallpaper) {
            startActivity(new Intent(GroupMessageActivity.this, TypingToWallpaperActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        //for checking internet connection
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, intentFilter);

        //calling method to load groupinfo
        super.onStart();
    }

    @Override
    protected void onStop() {
        //for checking internet connection
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}