package com.example.booprachat.Settings;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class EditUserDetailsActivity extends AppCompatActivity {

    Toolbar toolbar;
    FloatingActionButton applyChanges;
    CircleImageView profileImage;
    TextView profileText;
    TextView changeProfileImage;
    EditText userNameEdt;

    ProgressDialog pg;

    //firebase services
    StorageReference storageReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    DatabaseReference reference;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    //uri of picked image
    Uri image_uri = null;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_details);

        //declaring ui ids
        toolbar = findViewById(R.id.toolbar_for_edit_user_details);
        applyChanges = findViewById(R.id.apply_changes);
        profileImage = findViewById(R.id.profile_image);
        profileText = findViewById(R.id.profile_text);
        changeProfileImage = findViewById(R.id.changeProfilePhoto);
        userNameEdt = findViewById(R.id.edt_username);

        //init progress bar
        pg = new ProgressDialog(EditUserDetailsActivity.this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //enable back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calling method
                showImagePictureDialog();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calling method
                showImagePictureDialog();
            }
        });

        profileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calling method
                showImagePictureDialog();
            }
        });

        loadUserDetails();
    }

    private void loadUserDetails() {
        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    name = "" + ds.child("name").getValue();
                    String image = "" + ds.child("image").getValue();

                    //set data
                    userNameEdt.setText(name); //set username

                    applyChanges(name);
                    //set profile image
                    if (image.equals("")) {
                        //if there is any exception while getting this image then set this
                        ///it means if there is no image set this

                        profileImage.setVisibility(View.INVISIBLE);
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
                            DrawableCompat.setTint(drawable, Color.parseColor("#FFC107")); //color yellow

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

                        }
                        profileText.setText(firstLetter);

                    } else {
                        //if image is received then set this
                        //if there is image set that image

                        profileText.setVisibility(View.GONE);
                        profileImage.setVisibility(View.VISIBLE);

                        Picasso.get().load(image).into(profileImage); //for user profile
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void applyChanges(String name) {
        applyChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = userNameEdt.getText().toString();

                if (!name.equals(userName) && image_uri != null) {
                    //calling method
                    uploadProfileCoverPhoto(image_uri); // for changing user profile image
                    changeUserName(); //for changing user name

                } else if (!name.equals(userName) && image_uri == null) {
                    changeUserName(); //for changing user name

                } else if (name.equals(userName) && image_uri != null) {
                    uploadProfileCoverPhoto(image_uri); // for changing user profile image

                } else {
                    Toast.makeText(EditUserDetailsActivity.this, R.string.there_is_no_changes_my_friend, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void changeUserName() {

        String value = userNameEdt.getText().toString();

        if (!TextUtils.isEmpty(value)) {
            pg.setMessage(getString(R.string.applying_changes));
            pg.show();

            HashMap<String, Object> result = new HashMap<>();
            result.put("name", value);

            reference.child(firebaseAuth.getUid()).updateChildren(result)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pg.dismiss();
                            Toast.makeText(EditUserDetailsActivity.this, R.string.changes_applied, Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pg.dismiss();
                    Toast.makeText(EditUserDetailsActivity.this, getString(R.string.failed_to_apply_changes) + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

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
                    pg.setMessage(getString(R.string.updating_profile_picture));

                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                } else if (i == 1) {
                    //if gallery clicked
                    pg.setMessage(getString(R.string.updating_profile_picture));

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
        //check if storage permission is enabled or not
        //return true if enable
        //return false if not enable
        boolean result = ContextCompat.checkSelfPermission(EditUserDetailsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(EditUserDetailsActivity.this, storagePermissions, STORAGE_REQUEST_CODE);
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
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
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

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery, get uri of image
                image_uri = data.getData();
                profileText.setVisibility(View.GONE);
                profileImage.setVisibility(View.VISIBLE);
                profileImage.setImageURI(image_uri);
            }
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                profileText.setVisibility(View.GONE);
                profileImage.setVisibility(View.VISIBLE);
                profileImage.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {

        //show progress
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        //path and name of image to be stored in firebase storage
        String filePathAndName = "UserProfiles/" + "image" + "__" + user.getUid() + "__" + timestamp;

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
                            results.put("image", downloadUri.toString());

                            reference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pg.dismiss();
                                            Toast.makeText(EditUserDetailsActivity.this, R.string.image_updated, Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pg.dismiss();
                                    Toast.makeText(EditUserDetailsActivity.this, R.string.failed_to_update, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            //error
                            pg.dismiss();
                            Toast.makeText(EditUserDetailsActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //there were some error(s) , get and show error message, dismiss progress dialog
                pg.dismiss();
                Toast.makeText(EditUserDetailsActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
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