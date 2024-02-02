package com.example.booprachat.GroupChats;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupCreateActivity extends AppCompatActivity {

    //action bar
    private Toolbar toolbar;

    private FirebaseAuth firebaseAuth;

    private CircleImageView groupIcon;
    private EditText groupTitleEdt, groupDescriptionEdt;
    private FloatingActionButton createGroup;

    ProgressDialog progressDialog;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    //arrays of permissions to be requested
    private String cameraPermissions[];
    private String storagePermissions[];

    //uri of picked image
    private Uri image_uri = null;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);

        // declaring action bar
        toolbar = findViewById(R.id.create_group_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.create_team));
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFFFF"));
        //set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //declaring ids
        groupIcon = findViewById(R.id.group_profile_image);
        groupTitleEdt = findViewById(R.id.group_title);
        groupDescriptionEdt = findViewById(R.id.group_description);
        createGroup = findViewById(R.id.create_group);

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();

        //set group icon
        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePictureDialog();
            }
        });

        //on click listener for createGroup
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreatingGroup();
            }
        });
    }

    private void startCreatingGroup() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.creating_team));
        progressDialog.setCancelable(false);

        // get title and description
        String groupTitle = groupTitleEdt.getText().toString().trim();
        String groupDescription = groupDescriptionEdt.getText().toString().trim();

        // if the edtbox is empty
        if (TextUtils.isEmpty(groupTitle)) {
            Toast.makeText(this, R.string.please_enter_team_name, Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        String groupTimestamp = "" + System.currentTimeMillis();
        if (image_uri == null) {
            //create group without group icon
            createGroupWithoutIcon(
                    "" + groupTimestamp,
                    "" + groupTitle,
                    "" + groupDescription,
                    "");
        } else {
            //create group with group icon
            //upload image

            String fileNameAndPath = "Group_Images/" + "image" + groupTimestamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);

            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //image uploaded get image url
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            String downloadUri = uriTask.getResult().toString();

                            if (uriTask.isSuccessful()) {

                                createGroupWithoutIcon(
                                        "" + groupTimestamp,
                                        "" + groupTitle,
                                        "" + groupDescription,
                                        "" + downloadUri);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupCreateActivity.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void createGroupWithoutIcon(String groupTimestamp, String groupTitle, String groupDescription, String groupIcon) {

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("groupId", "" + groupTimestamp);
        hashMap.put("groupTitle", "" + groupTitle);
        hashMap.put("groupDescription", "" + groupDescription);
        hashMap.put("groupIcon", "" + groupIcon);
        hashMap.put("timestamp", "" + groupTimestamp);
        hashMap.put("groupCreatedBy", "" + firebaseAuth.getUid());
        hashMap.put("theMessageCanSendBy", "" + "all");

        //create group
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");

        reference.child(groupTimestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //if group created successfully

                        //setup member info (add current user in groups participants list)
                        HashMap<String, String> hashMap1 = new HashMap<>();
                        hashMap1.put("uid", firebaseAuth.getUid());
                        hashMap1.put("role", "leader");
                        hashMap1.put("timestamp", groupTimestamp);

                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Groups");

                        reference1.child(groupTimestamp).child("participants").child(firebaseAuth.getUid()).setValue(hashMap1)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // if participants added successfully
                                        progressDialog.dismiss();
                                        finish();
                                        Toast.makeText(GroupCreateActivity.this, R.string.team_created_successfully, Toast.LENGTH_LONG).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // if participants failed to add
                                        progressDialog.dismiss();
                                        Toast.makeText(GroupCreateActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        progressDialog.dismiss();
                        Toast.makeText(GroupCreateActivity.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showImagePictureDialog() {
        //show camera and gallery option
        //options to show dialog
        String option[] = {getString(R.string.camera), getString(R.string.gallery)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set title
        builder.setTitle(getString(R.string.pick_image_from));
        //set items to dialog
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle dialog item clicks
                if (i == 0) {
                    //if camera clicked

                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                } else if (i == 1) {
                    //if gallery clicked

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

    private void pickFromCamera() {
        //Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Group Image Icon Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Icon Description");
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

    private void requestStoragePermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        //check if storage permission is enabled or not
        //return true if enable
        //return false if not enable
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

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
                        Toast.makeText(this, R.string.please_enable_camera_and_storage_permission, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, R.string.please_provide_permission_to_access_storage, Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery, get uri of image
                image_uri = data.getData();
                groupIcon.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                groupIcon.setImageURI(image_uri);
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