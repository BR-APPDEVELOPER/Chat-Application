package com.example.booprachat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register_page extends AppCompatActivity {

    EditText Username, Email, Password;
    CircleImageView ProfileImage;
    TextView usernameErrorBox, emailErrorBox, passwordErrorBox, alreadyHaveAccount;
    Button Register;
    ProgressDialog pg;
    private FirebaseAuth mAuth;

    //permissions cons tants
    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_GALLERY_CODE = 200;
    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    Uri image_uri = null;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);
        //actionBar.setTitle(R.string.create_account);

        //declareing the ids
        Username = findViewById(R.id.edt_username);
        Email = findViewById(R.id.edt_email);
        Password = findViewById(R.id.edt_password);
        ProfileImage = findViewById(R.id.profile_image);
        Register = findViewById(R.id.btn_register);
        usernameErrorBox = findViewById(R.id.tv_usernameErrorBox);
        emailErrorBox = findViewById(R.id.tv_emailErrorBox);
        passwordErrorBox = findViewById(R.id.tv_passwordErrorBox);
        alreadyHaveAccount = findViewById(R.id.txtalready);

        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        mAuth = FirebaseAuth.getInstance();
        pg = new ProgressDialog(this);
        pg.setMessage(getString(R.string.registering_user));
        pg.setCancelable(false);


        //register btn click
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //input username, email, password
                String username = Username.getText().toString().trim();
                String email = Email.getText().toString().trim();
                String password = Password.getText().toString().trim();

                if (!username.isEmpty()) {
                    usernameErrorBox.setText("");

                } else if (!email.isEmpty()) {
                    emailErrorBox.setText("");

                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focus to email edittext
                    emailErrorBox.setText("");

                } else if (!password.isEmpty()) {
                    //set error to password
                    passwordErrorBox.setText("");

                } else if (password.length() > 9) {
                    passwordErrorBox.setText("");
                }

                //validate
                if (username.isEmpty()) {
                    usernameErrorBox.setText(R.string.username_is_required);
                    return;

                } else if (email.isEmpty()) {
                    emailErrorBox.setText(R.string.email_is_required);
                    return;

                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focus to email edittext
                    emailErrorBox.setText(R.string.you_entered_email_is_in_invalid_format);
                    return;

                } else if (password.isEmpty()) {
                    //set error to password
                    passwordErrorBox.setText(R.string.password_is_required);
                    return;

                } else if (password.length() <= 9) {
                    passwordErrorBox.setText(R.string.password_must_be_greater_than_9_characters);
                    return;

                } else {

                    if (image_uri == null) {
                        registerUserWithOutImage(email, password);
                    } else {
                        registerUserWithImage(email, password);
                    }
                }
            }
        });

        // already have account button click
        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register_page.this, Login_page.class));
                finish();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
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

    }

    private void registerUserWithOutImage(String email, String password) {
        pg.show();

        /*int count = 5;
        for (int i = 0; i < 15; i++) {
            String email1 = "boopra" + count + "@gmail.com";
            String username1 = "" + count;
            String password1 = "1234567890";
        count++;}*/

        mAuth.createUserWithEmailAndPassword(email, password).

                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // sign in success
                            FirebaseUser user = mAuth.getCurrentUser();

                            //getting user email and userid from auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            String username = Username.getText().toString();
                            String password = Password.getText().toString();

                            // when user is register store user info in firebase realtime database too
                            //using hashmap for store user details
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", username);
                            hashMap.put("password", password);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "onOne");
                            hashMap.put("image", "");
                            hashMap.put("homeScreenImage", "");
                            hashMap.put("chatScreenImage", "");
                            //firebase database instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //path to store user data named "Users"
                            DatabaseReference reference = database.getReference("Users");
                            //put data with in hashmap in database
                            reference.child(uid).setValue(hashMap);

                            pg.dismiss(); // dismiss the loading
                            //opening the content page
                            Toast.makeText(Register_page.this, R.string.your_account_registered_successfully, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Register_page.this, Dashboard_Activity.class));
                            finish();
                        } else {
                            //if sgn in fails
                            Toast.makeText(Register_page.this, R.string.failed + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                }).

                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pg.dismiss();
                        Toast.makeText(Register_page.this, R.string.failed + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUserWithImage(String email, String password) {
        pg.show();

        String timestamp = "" + System.currentTimeMillis();
        String filePathAndName = "UserProfiles/" + "image" + "__" + mAuth.getUid() + "__" + timestamp;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // sign in success
                    FirebaseUser user = mAuth.getCurrentUser();

                    //geting user email and userid from auth
                    String email = user.getEmail();
                    String uid = user.getUid();
                    String username = Username.getText().toString();
                    String password = Password.getText().toString();

                    // when user is register store user info in firebase realtime database too
                    //using hashmap for store user details
                    HashMap<Object, String> hashMap = new HashMap<>();
                    hashMap.put("email", email);
                    hashMap.put("uid", uid);
                    hashMap.put("name", username);
                    hashMap.put("password", password);
                    hashMap.put("onlineStatus", "online");
                    hashMap.put("typingTo", "onOne");
                    hashMap.put("image", "");
                    hashMap.put("homeScreenImage", "");
                    hashMap.put("chatScreenImage", "");
                    //firebase database instance
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    //path to store user data named "Users"
                    DatabaseReference reference = database.getReference("Users");
                    //put data with in hashmap in database

                    reference.child(uid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            storageReference.putFile(image_uri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            //image uploaded get image url
                                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                            while (!uriTask.isSuccessful()) ;
                                            String downloadUri = uriTask.getResult().toString();

                                            if (uriTask.isSuccessful()) {

                                                //storing profile image of user, after storing users details
                                                HashMap<String, Object> updateImageHashMap = new HashMap<>();
                                                updateImageHashMap.put("image", downloadUri);
                                                reference.child(uid).updateChildren(updateImageHashMap);

                                                pg.dismiss();
                                                //opening the content page
                                                Toast.makeText(Register_page.this, "Your Account Register Successfully", Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(Register_page.this, Dashboard_Activity.class));
                                                finish();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pg.dismiss();
                                            Toast.makeText(Register_page.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });

                } else {
                    //if sgn in fails
                    pg.dismiss();
                    Toast.makeText(Register_page.this, "Error !" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pg.dismiss();
                Toast.makeText(Register_page.this, "Error !" + e.getMessage(), Toast.LENGTH_LONG).show();
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
                        //Toast.makeText(this, R.string.please_provide_permission_to_access_storage, Toast.LENGTH_SHORT).show();
                        showPermissionDialogBox();
                    }
                } else {

                }

            }
            break;
        }


    }

    private void showPermissionDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Register_page.this);
        builder.setTitle("Notice");
        builder.setMessage("Please give permission to access storage, without storage permission the app will not work, Don't worry about your data, we will access only this app files.");
        builder.setCancelable(false);
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.requestPermissions(Register_page.this, storagePermissions, STORAGE_REQUEST_CODE);
            }
        }).setNegativeButton("Open", new DialogInterface.OnClickListener() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery, get uri of image
                image_uri = data.getData();
                //set image to preview
                ProfileImage.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // goto previous activity
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