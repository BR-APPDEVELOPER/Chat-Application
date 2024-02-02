package com.example.booprachat.Settings;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditWallPaperActivity extends AppCompatActivity {

    ShapeableImageView homeScreenImage, chatScreenImage;
    Toolbar toolbar;

    //progress
    ProgressDialog pg;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    //for checking profile and photo
    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];
    String ProfileOrCoverPhoto;
    String fileName;

    //uri of picked image
    Uri image_uri;

    //firebase services
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    DatabaseReference reference;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_wall_paper);

        //declaring ui ids
        toolbar = findViewById(R.id.edit_wallpaper_toolbar);
        homeScreenImage = findViewById(R.id.home_screen_wallpaper);
        chatScreenImage = findViewById(R.id.chat_screen_wallpaper);

        pg = new ProgressDialog(EditWallPaperActivity.this);

        //set action toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        homeScreenImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calling method
                showHomeScreenImageDialog(); //for changing home screen image dialog
            }
        });

        chatScreenImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calling method
                showChatScreenImageDialog(); //for changing chat screen image dialog
            }
        });

        loadHomeAndChatScreenWallpaper();
    }

    private void loadHomeAndChatScreenWallpaper() {

        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {

                    //get data
                    String ChatScreenImage = "" + ds.child("chatScreenImage").getValue();
                    String HomeScreenImage = "" + ds.child("homeScreenImage").getValue();

                    if (ChatScreenImage.equals("")) {
                        chatScreenImage.setImageResource(R.drawable.ic_baseline_add_icon);
                    } else {
                        Picasso.get().load(ChatScreenImage).into(chatScreenImage);
                    }

                    if (HomeScreenImage.equals("")) {
                        homeScreenImage.setImageResource(R.drawable.ic_baseline_add_icon);
                    } else {
                        Picasso.get().load(HomeScreenImage).into(homeScreenImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showHomeScreenImageDialog() {
        //show camera and gallery option
        //options to show dialog
        String option[] = {getString(R.string.camera), getString(R.string.gallery)};
        AlertDialog.Builder builder = new AlertDialog.Builder(EditWallPaperActivity.this);
        //set title
        builder.setTitle(getString(R.string.pick_image_from));
        //set items to dialog
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle dialog item clicks
                if (i == 0) {
                    //if camera clicked
                    pg.setMessage(getString(R.string.updating_home_screen_picture));
                    fileName = "HomeScreenImage/";
                    ProfileOrCoverPhoto = "homeScreenImage";

                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                } else if (i == 1) {
                    //if gallery clicked
                    pg.setMessage(getString(R.string.updating_home_screen_picture));
                    fileName = "HomeScreenImage/";
                    ProfileOrCoverPhoto = "homeScreenImage";

                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }

                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showChatScreenImageDialog() {
        //show camera and gallery option
        //options to show dialog
        String option[] = {getString(R.string.camera), getString(R.string.gallery)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set title
        builder.setTitle(R.string.pick_image_from);
        //set items to dialog
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle dialog item clicks
                if (i == 0) {
                    //if camera clicked
                    pg.setMessage(getString(R.string.updating_chat_screen_picture));
                    fileName = "ChatScreenImage/";
                    ProfileOrCoverPhoto = "chatScreenImage";

                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                } else if (i == 1) {
                    //if gallery clicked
                    pg.setMessage(getString(R.string.updating_chat_screen_picture));
                    fileName = "ChatScreenImage/";
                    ProfileOrCoverPhoto = "chatScreenImage";

                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }

                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private boolean checkStoragePermission() {
        //check if storage permissin is enabled or not
        //return true if enable
        //return false if not enable
        boolean result = ContextCompat.checkSelfPermission(EditWallPaperActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(EditWallPaperActivity.this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //check if storage permission is enabled or not
        //return true if enable
        //return false if not enable
        boolean result = ContextCompat.checkSelfPermission(EditWallPaperActivity.this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(EditWallPaperActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(EditWallPaperActivity.this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                        Toast.makeText(EditWallPaperActivity.this, R.string.please_enable_camera_and_storage_permission, Toast.LENGTH_SHORT).show();
                    }
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
                        Toast.makeText(EditWallPaperActivity.this, R.string.please_provide_permission_to_access_storage, Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery, get uri of image
                image_uri = data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                uploadProfileCoverPhoto(image_uri);
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {

        //show progress
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        //path and name of image to be stored in firebase storage
        String filePathAndName = fileName + ProfileOrCoverPhoto + "__" + user.getUid() + "__" + timestamp;

        StorageReference storageReference1 = storageReference.child(filePathAndName);
        storageReference1.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage now get its uri and store in users database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();

                        //check if image is uploaded or not and uri is received
                        if (uriTask.isSuccessful()) {
                            //image uploaded
                            //add/updata uri in user database
                            HashMap<String, Object> results = new HashMap<>();
                            results.put(ProfileOrCoverPhoto, downloadUri.toString());

                            reference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pg.dismiss();
                                            Toast.makeText(EditWallPaperActivity.this, R.string.image_updated, Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pg.dismiss();
                                    Toast.makeText(EditWallPaperActivity.this, R.string.failed_to_update, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            //error
                            pg.dismiss();
                            Toast.makeText(EditWallPaperActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //there were some error(s) , get and show error message, dismiss progress dialog
                pg.dismiss();
                Toast.makeText(EditWallPaperActivity.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickFromCamera() {
        //Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp description");
        //put image url
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
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