package com.example.booprachat.FavouriteMessages;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.FavouriteMessagesAdapter;
import com.example.booprachat.Model.Chat;
import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavouritePdfActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Chat> chatList;
    Toolbar toolbar;
    ImageView delete, copy, forward, share, sendMessage, closeToolbar1;
    LinearLayout toolbar1;
    FavouriteMessagesAdapter favouriteMessagesAdapter;

    //for database access
    FirebaseAuth firebaseAuth;
    DatabaseReference reference;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_pdf);

        //declaring ui ids
        recyclerView = findViewById(R.id.favourite_pdf_recycleView);
        toolbar = findViewById(R.id.favourite_pdf_toolbar);
        copy = findViewById(R.id.copy_message);
        delete = findViewById(R.id.delete_message);
        forward = findViewById(R.id.forward_message);
        share = findViewById(R.id.share_message);
        sendMessage = findViewById(R.id.send_message);
        closeToolbar1 = findViewById(R.id.back_arrow);
        toolbar1 = findViewById(R.id.linear_toolbar_1);

        toolbar1.setVisibility(View.GONE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        chatList = new ArrayList<>();

        //close toolbar 1, if left arrow clicked
        closeToolbar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar1.setVisibility(View.GONE);
            }
        });

        reference.child(firebaseAuth.getUid()).child("Favourite").child("PdfMessages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chat chat = ds.getValue(Chat.class);

                    chatList.add(chat);
                }

                //adapter
                favouriteMessagesAdapter = new FavouriteMessagesAdapter(FavouritePdfActivity.this, chatList, copy, delete, forward, share, sendMessage, toolbar1);
                //set adapter to recycleview
                recyclerView.setAdapter(favouriteMessagesAdapter);
                favouriteMessagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favourite_messages, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.clear_all) {

            if (chatList.size() == 0) {
                Toast.makeText(this, "There is no pdf file to clear", Toast.LENGTH_SHORT).show();
            } else {
                reference.child(firebaseAuth.getUid()).child("Favourite").child("PdfMessages").removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(FavouritePdfActivity.this, "successfully removed all from favourite", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }

        return super.onOptionsItemSelected(item);
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