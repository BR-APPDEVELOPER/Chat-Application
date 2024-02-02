package com.example.booprachat;

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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.downloader.PRDownloader;
import com.example.booprachat.Adapter.ChatAdapter;
import com.example.booprachat.Model.Chat;
import com.example.booprachat.Model.Users;
import com.example.booprachat.Notification.APIService;
import com.example.booprachat.Notification.Client;
import com.example.booprachat.Notification.Data;
import com.example.booprachat.Notification.Response;
import com.example.booprachat.Notification.Sender;
import com.example.booprachat.Notification.Token;
import com.example.booprachat.Settings.EditWallPaperActivity;
import com.example.booprachat.Settings.TypingToWallpaperActivity;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.database.ServerValue;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

import static android.view.View.GONE;

public class MessageActivity extends AppCompatActivity {

    CircleImageView receiverProfile;
    TextView receiverProfileText;
    TextView receiverName, lastSeen;
    EditText messageBox;
    ImageView ChatScreenImage;
    ImageView delete, copy, forward, share, favourite, closeToolbar1;
    ImageView camera, gallery, audio, video, pdf, closeToolbar2;
    ImageButton SendMessage, attachFile, BackButton;
    RecyclerView recyclerView;

    //layouts
    LinearLayout bottomMessageSendLayout;

    //firebase services
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;

    //toolbar
    Toolbar toolbar;
    LinearLayout toolbar1, toolbar2;
    //uri
    Uri pdfUri, audioUri, videoUrl;

    //for checking if user has seen the message or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<Chat> chatList;
    ChatAdapter chatAdapter;

    //Strings
    String receiverUserId;
    String myUid;
    //for image
    String receiverImage;
    String imageFileName;
    String chatScreenImage;
    String typingToWallpaper = "";
    String name;

    private String checkIsBlocked = "";
    private String blockedOrNot = "";
    private String imBlockedOrNot = "";
    private String imBlockedOrNotForChatAdapter = "";

    //progress bar
    ProgressDialog pg;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    Uri image_uri = null;
    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        loadLocal();
        setContentView(R.layout.activity_message);

        //declaring ui ids
        receiverProfile = findViewById(R.id.receiverProfile);
        receiverProfileText = findViewById(R.id.receiver_profile_text);
        messageBox = findViewById(R.id.typing_box);
        receiverName = findViewById(R.id.receiverName);
        lastSeen = findViewById(R.id.lastseen);
        SendMessage = findViewById(R.id.btn_send);
        attachFile = findViewById(R.id.attach_image);
        BackButton = findViewById(R.id.back_btn);
        bottomMessageSendLayout = findViewById(R.id.bottom_linearLayout);
        ChatScreenImage = findViewById(R.id.chat_screen_image);
        copy = findViewById(R.id.copy_message);
        delete = findViewById(R.id.delete_message);
        forward = findViewById(R.id.forward_message);
        share = findViewById(R.id.share_message);
        favourite = findViewById(R.id.favourite);
        closeToolbar1 = findViewById(R.id.back_arrow);
        toolbar1 = findViewById(R.id.linear_toolbar_1);
        toolbar2 = findViewById(R.id.linear_toolbar_2);
        camera = findViewById(R.id.camera);
        gallery = findViewById(R.id.gallery);
        audio = findViewById(R.id.audio);
        video = findViewById(R.id.video);
        pdf = findViewById(R.id.pdf);
        closeToolbar2 = findViewById(R.id.toolbar2_back_arrow);
        recyclerView = findViewById(R.id.chat_recycleView); //recycle view

        //hide toolbar2 and 3
        toolbar1.setVisibility(GONE);
        toolbar2.setVisibility(GONE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        /*//for fullscreen view
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getSupportActionBar().hide();
        test.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                  | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                  | View.SYSTEM_UI_FLAG_FULLSCREEN);*/

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //layout (LinearLayout) for recycleView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recycle view properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //create apiService
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        //get intent
        Intent intent = getIntent();
        receiverUserId = intent.getStringExtra("receiverId");

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        FirebaseUser user1 = user;
        myUid = user1.getUid();

        //getting the receiver details from receiverid
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("Users");

        PRDownloader.initialize(getApplicationContext());

        /*// Enabling database for resume support even after the application is killed:
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

        // Setting timeout globally for the download network requests:
        PRDownloaderConfig config1 = PRDownloaderConfig.newBuilder()
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();
        PRDownloader.initialize(getApplicationContext(), config1);*/

        Query query1 = reference.orderByChild("email").equalTo(user.getEmail());
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    chatScreenImage = "" + ds.child("chatScreenImage").getValue();

                    //set data
                    /*if (chatScreenImage.equals("")) {
                        //if image is received then set this
                        Picasso.get().load(R.mipmap.ic_launcher).into(ChatScreenImage);
                    } else {
                        //if there is any exception while getting this image then set this
                        Picasso.get().load(chatScreenImage).into(ChatScreenImage);
                    }*/

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.child(firebaseAuth.getUid()).child("TypingToWallpaper").orderByChild("uid").equalTo(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    typingToWallpaper = "" + ds.child("typingToWallpaper").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Query query = reference.orderByChild("uid").equalTo(receiverUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check untile required ifo is received
                for (DataSnapshot ds : snapshot.getChildren()) {

                    //get receiver data
                    name = "" + ds.child("name").getValue();
                    receiverImage = "" + ds.child("image").getValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //click send message button to send message
        SendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount()-1);

                notify = true;
                //get text from edit text
                String message = messageBox.getText().toString();
                //check the message box is empty or not
                if (message.isEmpty()) {
                    //if the message box is empty
                    Toast.makeText(MessageActivity.this, getString(R.string.please_enter_message), Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);
                }
                //clear message box after sending message
                messageBox.setText("");
            }
        });

        //on click to make visible toolbar 2
        attachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar1.setVisibility(GONE);

                if (toolbar2.getVisibility() == GONE) {
                    toolbar2.setVisibility(View.VISIBLE);
                } else {
                    toolbar2.setVisibility(GONE);
                }
            }
        });

        //check edit text change listener
        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 0) {
                    checkTypingStatus("noOne");
                } else {
                    checkTypingStatus(receiverUserId); // userid of receiver
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //back to dashboard activity
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //close toolbar 1, if left arrow clicked
        closeToolbar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar1.setVisibility(GONE);
            }
        });

        // if camera icon clicked
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for camera
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromCamera();
                }

                toolbar2.setVisibility(GONE); //close toolbar2
            }
        });

        //if gallery icon clicked
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for gallery
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickFromGallery();
                }

                toolbar2.setVisibility(GONE); //close toolbar2
            }
        });

        // if audio icon clicked
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for sending audio
                if (ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectAudio();
                } else {
                    ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
                }

                toolbar2.setVisibility(GONE); //close toolbar2
            }
        });

        //if video icon clicked
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for sending video
                if (ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectVideo();
                } else {
                    ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
                    //requestStoragePermission();
                }

                toolbar2.setVisibility(GONE); //close toolbar2
            }
        });

        //if pdf icon clicked
        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for sending pdf
                if (ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectPdf();
                } else {
                    ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
                }

                toolbar2.setVisibility(GONE); //close toolbar2
            }
        });

        //close toolbar 2, if left arrow clicked
        closeToolbar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar2.setVisibility(GONE);
            }
        });

        //this is for informing to 'Chat Adapter' i am block or not
        reference.child(receiverUserId).child("BlockedUsers").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.exists()) {
                                imBlockedOrNotForChatAdapter = "true";
                            }
                        }
                        //calling method
                        readMessage(imBlockedOrNotForChatAdapter); //load all chats of this user
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //call methode
        //checkIsBlocked(receiverUserId);
        checkImBlockedOrNot();
        checkIsBlocked();
        seenMessage();

    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chat chat = ds.getValue(Chat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(receiverUserId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("messageSeenOrNot", "true");
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage(String imBlockedOrNotForChatAdapter) {

        chatList = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chat chat = ds.getValue(Chat.class);

                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(receiverUserId) ||
                            chat.getReceiver().equals(receiverUserId) && chat.getSender().equals(myUid)) {
                        chatList.add(chat);
                    }

                    //adapter
                    chatAdapter = new ChatAdapter(MessageActivity.this, chatList, receiverImage, toolbar1, copy, delete, forward, share, favourite, toolbar2, name, imBlockedOrNotForChatAdapter);
                    //set adapter to recycleview
                    recyclerView.setAdapter(chatAdapter);
                    chatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {

        notify = true;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", receiverUserId);
        hashMap.put("message", message);
        hashMap.put("time", timestamp);
        hashMap.put("type", "text");
        hashMap.put("messageSeenOrNot", "false");

        //creating "ChatLists" node in currentUsers details
        reference.child("Chats").push().setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //for chat
                        reference1.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                        reference1.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                        //for group chat
                        reference1.child(user.getUid()).child("GroupChatLists").child(receiverUserId).child("uid").setValue(receiverUserId);
                        reference1.child(receiverUserId).child("GroupChatLists").child(user.getUid()).child("uid").setValue(user.getUid());
                    }
                });

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);

                if (notify) {
                    sendNotification(receiverUserId, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void uploadAudio(Uri audioUri, String audioFileName) {
        pg = new ProgressDialog(this);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setTitle(getString(R.string.sending_audio));
        pg.setProgress(0);
        pg.show();

        notify = true;

        String timestamp = System.currentTimeMillis() + "";
        //StorageReference storageReference = storage.getReference(); //returns root path

        String fileNameAndPath = "ChatAudio/" + myUid + "__" + receiverUserId + "__" + timestamp + "__" + audioFileName;
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);

        ref.putFile(audioUri)
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
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(); //returns the path to root
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                            //setup required data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", myUid);
                            hashMap.put("receiver", receiverUserId);
                            hashMap.put("message", downloadUri);
                            hashMap.put("fileName", audioFileName);
                            hashMap.put("time", timestamp);
                            hashMap.put("type", "audio");
                            hashMap.put("messageSeenOrNot", "false");
                            //put this data to firebase
                            //creating "ChatLists" node in currentUsers details
                            reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //for chat
                                    databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                                    databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);

                                    //for group chat
                                    databaseReference.child(user.getUid()).child("GroupChatLists").child(receiverUserId).child("uid").setValue(receiverUserId);
                                    databaseReference.child(receiverUserId).child("GroupChatLists").child(user.getUid()).child("uid").setValue(user.getUid());

                                    if (task.isSuccessful()) {
                                        pg.dismiss();
                                        Toast.makeText(MessageActivity.this, getString(R.string.file_uploaded), Toast.LENGTH_SHORT).show();
                                    } else {
                                        pg.dismiss();
                                        Toast.makeText(MessageActivity.this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            //send notification
                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                            reference1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Users users = snapshot.getValue(Users.class);

                                    if (notify) {
                                        sendNotification(receiverUserId, users.getName(), getString(R.string.sent_you_a_audio));
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pg.dismiss();
                Toast.makeText(MessageActivity.this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pg.setProgress(currentProgress);
                pg.setCanceledOnTouchOutside(false);
            }
        });
    }

    private void uploadVideo(Uri videoUrl, String videoFileName) {
        pg = new ProgressDialog(this);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setTitle(getString(R.string.sending_video));
        pg.setProgress(0);

        pg.show();

        notify = true;

        String timestamp = System.currentTimeMillis() + "";
        //StorageReference storageReference = storage.getReference(); //returns root path

        String fileNameAndPath = "ChatVideo/" + myUid + "__" + receiverUserId + "__" + timestamp + "__" + videoFileName;
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
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(); //returns the path to root
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                            //setup required data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", myUid);
                            hashMap.put("receiver", receiverUserId);
                            hashMap.put("message", downloadUri);
                            hashMap.put("fileName", videoFileName);
                            hashMap.put("time", timestamp);
                            hashMap.put("type", "video");
                            hashMap.put("messageSeenOrNot", "false");
                            //put this data to firebase
                            //creating "ChatLists" node in currentUsers details
                            reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //for chat
                                    databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                                    databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                                    //for group chat
                                    databaseReference.child(user.getUid()).child("GroupChatLists").child(receiverUserId).child("uid").setValue(receiverUserId);
                                    databaseReference.child(receiverUserId).child("GroupChatLists").child(user.getUid()).child("uid").setValue(user.getUid());

                                    if (task.isSuccessful()) {
                                        pg.dismiss();
                                        Toast.makeText(MessageActivity.this, getString(R.string.file_uploaded), Toast.LENGTH_SHORT).show();
                                    } else {
                                        pg.dismiss();
                                        Toast.makeText(MessageActivity.this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            //send notification
                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                            reference1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Users users = snapshot.getValue(Users.class);

                                    if (notify) {
                                        sendNotification(receiverUserId, users.getName(), getString(R.string.sent_you_a_video));
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pg.dismiss();
                Toast.makeText(MessageActivity.this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pg.setProgress(currentProgress);
                pg.setCanceledOnTouchOutside(false);
            }
        });
    }

    private void uploadPdf(Uri pdfUri, String pdfFileName) {
        pg = new ProgressDialog(this);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setTitle(getString(R.string.sending_pdf));
        pg.setProgress(0);

        pg.show();

        notify = true;

        String timestamp = System.currentTimeMillis() + "";
        //StorageReference storageReference = storage.getReference(); //returns root path

        String fileNameAndPath = "ChatPdf/" + myUid + "__" + receiverUserId + "__" + timestamp + "__" + pdfFileName;
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);

        ref.putFile(pdfUri)
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
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(); //returns the path to root
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                            //setup required data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", myUid);
                            hashMap.put("receiver", receiverUserId);
                            hashMap.put("message", downloadUri);
                            hashMap.put("fileName", pdfFileName);
                            hashMap.put("time", timestamp);
                            hashMap.put("type", "pdf");
                            hashMap.put("messageSeenOrNot", "false");
                            //put this data to firebase
                            //creating "ChatLists" node in currentUsers details
                            reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //for chat
                                    databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                                    databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                                    //for group chat
                                    databaseReference.child(user.getUid()).child("GroupChatLists").child(receiverUserId).child("uid").setValue(receiverUserId);
                                    databaseReference.child(receiverUserId).child("GroupChatLists").child(user.getUid()).child("uid").setValue(user.getUid());

                                    if (task.isSuccessful()) {
                                        pg.dismiss();
                                        Toast.makeText(MessageActivity.this, getString(R.string.file_uploaded), Toast.LENGTH_SHORT).show();
                                    } else {
                                        pg.dismiss();
                                        Toast.makeText(MessageActivity.this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            //send notification
                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                            reference1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Users users = snapshot.getValue(Users.class);

                                    if (notify) {
                                        sendNotification(receiverUserId, users.getName(), getString(R.string.sent_you_a_pdf));
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pg.dismiss();
                Toast.makeText(MessageActivity.this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pg.setProgress(currentProgress);
                pg.setCanceledOnTouchOutside(false);
            }
        });
    }

    private void sendImageMessage(Uri image_uri, String imageFileName) throws IOException {
        notify = true;

        //progress dialog
        pg = new ProgressDialog(this);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setMessage(getString(R.string.sending_image));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        String fileNameAndPath = "ChatImages/" + myUid + "__" + receiverUserId + "__" + timestamp + "__" + imageFileName;
        //Chats node will be created that will contain all image sent via chat
        //get bitmap from image uri

        //try {
        //  Bitmap bitmap = new Compressor(this)
        //        .setMaxHeight(200) //Set height and width
        //      .setMaxWidth(200)
        //    .setQuality(100) // Set Quality
        //  .compressToBitmap(file);
        //  } catch (IOException e) {
        //    e.printStackTrace();
        //}

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
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                    //setup required data
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", myUid);
                    hashMap.put("receiver", receiverUserId);
                    hashMap.put("message", downloadUri);
                    hashMap.put("fileName", imageFileName);
                    hashMap.put("time", timestamp);
                    hashMap.put("type", "image");
                    //hashMap.put("mediaType", "image");
                    hashMap.put("messageSeenOrNot", "false");
                    //put this data to firebase
                    //creating "ChatLists" node in currentUsers details
                    reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //for chat
                            databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                            databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                            //for group chat
                            databaseReference.child(user.getUid()).child("GroupChatLists").child(receiverUserId).child("uid").setValue(receiverUserId);
                            databaseReference.child(receiverUserId).child("GroupChatLists").child(user.getUid()).child("uid").setValue(user.getUid());

                            if (task.isSuccessful()) {
                                pg.dismiss();
                                Toast.makeText(MessageActivity.this, getString(R.string.file_uploaded), Toast.LENGTH_SHORT).show();
                            } else {
                                pg.dismiss();
                                Toast.makeText(MessageActivity.this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    //send notification
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                    reference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Users users = snapshot.getValue(Users.class);

                            if (notify) {
                                sendNotification(receiverUserId, users.getName(), getString(R.string.sent_you_a_photo));
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // if upload failed
                pg.dismiss();
                Toast.makeText(MessageActivity.this, getString(R.string.failed_to_upload) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pg.setProgress(currentProgress);
            }
        });
    }

    private void sendNotification(String receiverUserId, String name, String message) {

        DatabaseReference alltoken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = alltoken.orderByKey().equalTo(receiverUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(myUid, name + ": " + message, getString(R.string.new_message), receiverUserId, R.mipmap.ic_launcher);

                    Sender sender = new Sender(data, token.getToken());


                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void selectPdf() {
        //select a file using file manager
        //we will be using an Intent for this action
        Intent intent = new Intent();
        intent.setType("application/pdf"); //for pdf
        //intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"); //for document
        //intent.setType("audio/*"); //for audio
        //intent.setType("video/*"); //for video
        intent.setAction(Intent.ACTION_GET_CONTENT); //to fetch files
        startActivityForResult(intent, 86);
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

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
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
        //check if storage permissin is enabled or not
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

    private void checkTypingStatus(String typing) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        //update onlineStatus of current user
        reference.updateChildren(hashMap);
    }

    private void currentUser(String receiverUserId) {
        SharedPreferences.Editor editor = getSharedPreferences("SP_USER", MODE_PRIVATE).edit();
        editor.putString("Current_USERID", receiverUserId);
        editor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //for pdf
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectPdf();
        } else {
            Toast.makeText(this, getString(R.string.please_provide_permission_to_access_storage), Toast.LENGTH_SHORT).show();
        }

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

    private void checkImBlockedOrNot() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(receiverUserId).child("BlockedUsers").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.exists()) {
                                bottomMessageSendLayout.setVisibility(GONE);
                                toolbar1.setVisibility(GONE);
                                toolbar2.setVisibility(GONE);
                                lastSeen.setVisibility(GONE);
                                receiverProfile.setVisibility(GONE);
                                receiverProfileText.setVisibility(View.VISIBLE);
                                SendMessage.setVisibility(GONE);
                                messageBox.setEnabled(false);

                                imBlockedOrNot = "true";
                                //calling method
                                loadReceiverDetailsOnBlocked();

                                invalidateOptionsMenu();
                                return;
                            }
                        }

                        bottomMessageSendLayout.setVisibility(View.VISIBLE);

                        imBlockedOrNot = "false";
                        //calling method
                        loadReceiverDetailsOnNotBlocked();

                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadReceiverDetailsOnNotBlocked() {

        Query query = reference.orderByChild("uid").equalTo(receiverUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check untile required ifo is received
                for (DataSnapshot ds : snapshot.getChildren()) {

                    //get receiver data
                    name = "" + ds.child("name").getValue();
                    receiverImage = "" + ds.child("image").getValue();
                    String typingStatus = "" + ds.child("typingTo").getValue();//get value of typing status

                    //set data
                    receiverName.setText(name); //set receiver name

                    if (typingStatus.equals(myUid)) {
                        //set data
                        if (!typingToWallpaper.equals("")) {

                            Picasso.get().load(typingToWallpaper).into(ChatScreenImage);
                        } else {
                            lastSeen.setText(getString(R.string.typing));
                        }

                    } else {
                        //set data
                        if (!chatScreenImage.equals("")) {
                            //if image is received then set this
                            Picasso.get().load(chatScreenImage).into(ChatScreenImage);
                        }

                        //get value of online status
                        String onlineStatus = "" + ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("online")) {
                            lastSeen.setText(R.string.online);
                        } else {
                            //convert timestamp to proper time date
                            //convert timestamp to dd/mm/yyyy hh:mm am/pm
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                            lastSeen.setText(getString(R.string.last_seen) + dateTime); // setting lastSeen time and date
                            lastSeen.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                            lastSeen.setSelected(true);
                        }
                    }

                    if (receiverImage.equals("")) {
                        receiverProfile.setVisibility(GONE);
                        receiverProfileText.setVisibility(View.VISIBLE);

                        String firstLetter = String.valueOf(name.charAt(0)).toLowerCase();

                        Drawable drawable = receiverProfileText.getBackground();
                        drawable = DrawableCompat.wrap(drawable);

                        if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#FF00BCD4")); // color blue

                            receiverProfileText.setBackground(drawable);

                        } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#FFFF5722")); //color red

                            receiverProfileText.setBackground(drawable);

                        } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#bfd200")); //color

                            receiverProfileText.setBackground(drawable);

                        } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#FF9800")); //color orange

                            receiverProfileText.setBackground(drawable);

                        } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#52b788")); //color old: #cccc00

                            receiverProfileText.setBackground(drawable);

                        } else if (firstLetter.equals("z")) {
                            DrawableCompat.setTint(drawable, Color.parseColor("#F33D73")); //color darker pink

                            receiverProfileText.setBackground(drawable);

                        } else {
                            DrawableCompat.setTint(drawable, Color.parseColor("#6c757d")); //color

                            receiverProfileText.setBackground(drawable);
                        }
                        //set first letter of name
                        receiverProfileText.setText(firstLetter);

                    } else {
                        receiverProfileText.setVisibility(GONE);
                        receiverProfile.setVisibility(View.VISIBLE);

                        Picasso.get().load(receiverImage).into(receiverProfile);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadReceiverDetailsOnBlocked() {
        Query query = reference.orderByChild("uid").equalTo(receiverUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check untile required ifo is received
                for (DataSnapshot ds : snapshot.getChildren()) {

                    //get receiver data
                    name = "" + ds.child("name").getValue();

                    //set data
                    receiverName.setText(name); //set receiver name

                    if (!chatScreenImage.equals("")) {
                        //if image is received then set this
                        Picasso.get().load(chatScreenImage).into(ChatScreenImage);
                    }

                    String firstLetter = String.valueOf(name.charAt(0));

                    Drawable drawable = receiverProfileText.getBackground();
                    drawable = DrawableCompat.wrap(drawable);

                    if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#FF00BCD4")); // color blue

                        receiverProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#FFFF5722")); //color red

                        receiverProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#bfd200")); //color

                        receiverProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#FF9800")); //color orange

                        receiverProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#52b788")); //color old: #cccc00

                        receiverProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("z")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#F33D73")); //color darker pink

                        receiverProfileText.setBackground(drawable);

                    } else {
                        DrawableCompat.setTint(drawable, Color.parseColor("#6c757d")); //color

                        receiverProfileText.setBackground(drawable);
                    }
                    //set first letter of name
                    receiverProfileText.setText(firstLetter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkIsBlocked() {
        //check each user, if blocked or not
        //if userid of user exists in "BlockedUsers" then that user is blocked otherwise not

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(receiverUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.exists()) {
                                blockedOrNot = "true";
                                invalidateOptionsMenu();
                                return;
                            }
                        }
                        blockedOrNot = "false";
                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser() {
        //block the user by adding userId to current "users" "BlockedUsers" node

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", receiverUserId);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("BlockedUsers").child(receiverUserId).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //user blocked successfully
                        Toast.makeText(MessageActivity.this, R.string.blocked_successfully, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to block user
                Toast.makeText(MessageActivity.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unBlockUser() {
        //unblock the user by removing the userId from current "users" "BlockedUsers" node
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MessageActivity.this, R.string.unblocked_successfully, Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MessageActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_message_menu, menu);

        if (imBlockedOrNot.equals("true")) {
            menu.findItem(R.id.wallpaper).setVisible(false);
            menu.findItem(R.id.nav_blocking).setVisible(false);
            menu.findItem(R.id.typing_wallpaper).setVisible(false);
            menu.findItem(R.id.nav_media).setVisible(false);

        } else {
            if (blockedOrNot.equals("true")) {
                menu.findItem(R.id.nav_blocking).setTitle(R.string.unblock);
            } else {
                menu.findItem(R.id.nav_blocking).setTitle(R.string.block);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.wallpaper) {
            startActivity(new Intent(MessageActivity.this, EditWallPaperActivity.class));
        }

        if (id == R.id.nav_blocking) {

            if (blockedOrNot.equals("true")) {
                unBlockUser();
            } else {
                blockUser();
            }
        }

        if (id == R.id.nav_media) {

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("receiverId", receiverUserId);

            reference.child(firebaseAuth.getUid()).updateChildren(hashMap);
            Intent intent = new Intent(MessageActivity.this, MediaDashboardActivity.class);
            intent.putExtra("name", name);
            startActivity(intent);
        }

        if (id == R.id.typing_wallpaper) {
            startActivity(new Intent(MessageActivity.this, TypingToWallpaperActivity.class));
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onPause() {
        super.onPause();
        checkTypingStatus("noOne");
        currentUser("None");
        //int downloadId = 0;
        //PRDownloader.pause(downloadId);
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser(receiverUserId);
    }
}