package com.example.booprachat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class Login_page extends AppCompatActivity {

    EditText Email, Password;
    TextView emailErrorBox, passwordErrorBox, register, Forgotpassword;
    Button Login;
    ProgressDialog pg;
    FirebaseAuth mAuth;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocal();
        setContentView(R.layout.activity_login_page);
        //actionBar.setTitle(R.string.login);

        //declareing ids
        Email = findViewById(R.id.edt_email);
        Password = findViewById(R.id.edt_password);
        emailErrorBox = findViewById(R.id.tv_emailErrorBox);
        passwordErrorBox = findViewById(R.id.tv_passwordErrorBox);
        Login = findViewById(R.id.btn_login);
        register = findViewById(R.id.tv_register);
        Forgotpassword = findViewById(R.id.tv_forgotpaassword);

        mAuth = FirebaseAuth.getInstance();

        pg = new ProgressDialog(this);


        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = Email.getText().toString().trim();
                String password = Password.getText().toString().trim();

                //validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focus to email edittext
                    emailErrorBox.setText(R.string.email_is_required);
                    return;
                } else if (password.isEmpty()) {
                    //set error to password
                    passwordErrorBox.setText(R.string.password_is_required);
                    return;
                } else {
                    // call methode
                    loginUser(email, password); // register user
                }
            }
        });

        // register button click
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login_page.this, Register_page.class));
                finish();
            }
        });

        //recover password
        Forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });

    }

    private void showRecoverPasswordDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(Login_page.this);
        View view = getLayoutInflater().inflate(R.layout.custom_forgot_password_dialog, null);

        EditText edtEmail = (EditText) view.findViewById(R.id.email);
        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        TextView recover = (TextView) view.findViewById(R.id.recover);


        builder.setView(view);
        AlertDialog forgotPasswordDialog = builder.create();
        forgotPasswordDialog.setCancelable(false);
        forgotPasswordDialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPasswordDialog.dismiss();
            }
        });

        recover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    edtEmail.setError("Email is Required");
                    return;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focus to email edittext
                    edtEmail.setError("Please enter in email format");
                    return;
                } else {
                    beginRecovery(email);
                }
            }
        });
    }

    private void beginRecovery(String email) {
        pg.setMessage(getString(R.string.sending_email));
        pg.setCancelable(false);
        pg.show();

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    pg.dismiss();
                    Toast.makeText(Login_page.this, R.string.email_sent, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get and show proper error message
                pg.dismiss();
                Toast.makeText(Login_page.this, R.string.failed, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loginUser(String email, String password) {
        pg.setMessage(getString(R.string.login_user)); //dialog box message
        pg.show(); //show loading bar
        pg.setCancelable(false);

        //signin the user
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // if sign in success
                    pg.dismiss();

                    Toast.makeText(Login_page.this, R.string.welcome_to_booprachat, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Login_page.this, Dashboard_Activity.class));
                    finish();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pg.dismiss();
                Toast.makeText(Login_page.this, getString(R.string.failed) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
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