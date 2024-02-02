package com.example.booprachat.Settings;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PreviewTypingToWallpaperActivity extends AppCompatActivity {

    CircleImageView profileImage;
    ImageView previewTypingToWallpaper;
    ImageButton openGallery;
    TextView uploadImage;
    Toolbar toolbar;
    String receiverId;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    //permissions constants
    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_GALLERY_CODE = 200;
    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    Uri image_uri = null;

    //firebase services
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_typing_to_wallpaper);

        //declaring ui ids
        profileImage = findViewById(R.id.profile_image);
        previewTypingToWallpaper = findViewById(R.id.preview_typing_to_wallpaper);
        uploadImage = findViewById(R.id.upload_image);
        openGallery = findViewById(R.id.open_gallery);
        toolbar = findViewById(R.id.wallpaper_preview_toolbar);

        //set action toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();

        //get intent from typing to wallpaper adapter
        Intent intent = getIntent();
        receiverId = intent.getStringExtra("receiverId");
        String ProfileImage = intent.getStringExtra("profileImage");

        //for load profile image
        if (ProfileImage.equals("")) {
            profileImage.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.get().load(ProfileImage).into(profileImage);
        }

        //for gallery
        if (!checkStoragePermission()) {
            requestStoragePermission();
        } else {
            pickFromGallery();
        }


        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set to image view
                if (image_uri != null) {
                    uploadImage.setEnabled(true);
                    try {
                        sendImageMessage(image_uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(PreviewTypingToWallpaperActivity.this, R.string.there_is_no_changes_my_friend, Toast.LENGTH_SHORT).show();
                }
            }
        });

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for gallery
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickFromGallery();
                }
            }
        });
        //calling method
        loadTypingTo();
    }

    private void loadTypingTo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("TypingToWallpaper").orderByChild("uid").equalTo(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String typingToWallpaper = "" + ds.child("typingToWallpaper").getValue();

                    //set data
                    if (!typingToWallpaper.equals("")) {
                        Picasso.get().load(typingToWallpaper).into(previewTypingToWallpaper);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    private void sendImageMessage(Uri image_uri) throws IOException {
        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.uploading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();

        String fileNameAndPath = "TypingToImages/" + firebaseAuth.getUid() + "__" + receiverId + "__" + timestamp;

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
                progressDialog.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {
                    //add image uri nad other info to database
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("typingToWallpaper", downloadUri);
                    hashMap.put("uid", receiverId);

                    reference.child(firebaseAuth.getUid()).child("TypingToWallpaper").child(receiverId).setValue(hashMap);
                    Toast.makeText(PreviewTypingToWallpaperActivity.this, R.string.uploaded_successfully_please_wait_to_load, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // if upload failed
                progressDialog.dismiss();
                Toast.makeText(PreviewTypingToWallpaperActivity.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
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
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this, R.string.please_provide_permission_to_access_storage, Toast.LENGTH_SHORT).show();
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
                //set image to preview
                previewTypingToWallpaper.setImageURI(image_uri);
                profileImage.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previous activity
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