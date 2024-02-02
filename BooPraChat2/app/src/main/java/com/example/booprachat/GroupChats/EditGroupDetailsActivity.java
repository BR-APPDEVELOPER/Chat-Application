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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditGroupDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    //strings
    private String groupId;

    private FirebaseAuth firebaseAuth;

    private CircleImageView groupIcon;
    private EditText groupTitleEdt, groupDescriptionEdt;
    private FloatingActionButton updateGroup;

    ProgressDialog progressDialog;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_detailes);

        //declaring ids
        groupIcon = findViewById(R.id.change_group_profile_image);
        groupTitleEdt = findViewById(R.id.change_group_title);
        groupDescriptionEdt = findViewById(R.id.change_group_description);
        updateGroup = findViewById(R.id.update_group);
        toolbar = findViewById(R.id.edit_group_toolbar);

        //set action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.modify_team);
        toolbar.setTitleTextColor(Color.WHITE);
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //get intent from group info activity
        groupId = getIntent().getStringExtra("groupId");

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        //calling method
        loadGroupInfo();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        //set group icon
        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePictureDialog();
            }
        });

        //on click listener for createGroup
        updateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUpdatingGroup();
            }
        });
    }

    private void startUpdatingGroup() {
        progressDialog.setMessage(getString(R.string.updating_team_info));
        progressDialog.show();

        //get input data
        String groupTitle = groupTitleEdt.getText().toString().trim();
        String groupDescription = groupDescriptionEdt.getText().toString().trim();

        if (image_uri == null) {
            //update group with out icon
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupTitle", groupTitle);
            hashMap.put("groupDescription", groupDescription);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
            reference.child(groupId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // successfully done means
                            progressDialog.dismiss();
                            Toast.makeText(EditGroupDetailsActivity.this, R.string.team_info_updated, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed
                            progressDialog.dismiss();
                            Toast.makeText(EditGroupDetailsActivity.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            //update group with icon

            String timestamp = "" + System.currentTimeMillis();

            String fileNameAndPath = "Group_Images/" + "image" + timestamp;

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

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("groupTitle", groupTitle);
                                hashMap.put("groupDescription", groupDescription);
                                hashMap.put("groupIcon", "" + downloadUri);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                                reference.child(groupId).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // successfully done means
                                                progressDialog.dismiss();
                                                Toast.makeText(EditGroupDetailsActivity.this, R.string.team_info_updated, Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed
                                                progressDialog.dismiss();
                                                Toast.makeText(EditGroupDetailsActivity.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditGroupDetailsActivity.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            //get data
                            String GroupTitle = "" + ds.child("groupTitle").getValue();
                            String GroupDescription = "" + ds.child("groupDescription").getValue();
                            String GroupIcon = "" + ds.child("groupIcon").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String GroupCreatedBy = "" + ds.child("groupCreatedBy").getValue();

                            //set data
                            groupTitleEdt.setText(GroupTitle);
                            groupDescriptionEdt.setText(GroupDescription);

                            try {
                                //if there is a group icon set that icon
                                Picasso.get().load(GroupIcon).into(groupIcon);
                            } catch (Exception e) {
                                // if there is no icon set this icon to that group
                                groupIcon.setImageResource(R.drawable.ic_baseline_group_24);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showImagePictureDialog() {
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