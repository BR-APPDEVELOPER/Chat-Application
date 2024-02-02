package com.example.booprachat.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.ChatlistAdapter;
import com.example.booprachat.FavouriteMessages.FavouriteDashboardActivity;
import com.example.booprachat.MainActivity;
import com.example.booprachat.R;
import com.example.booprachat.Settings.Settings_Activity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class Home_Fragment extends Fragment {

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseUser currentUser;
    RecyclerView recyclerView;

    private ArrayList<String> namesList, IdsList;

    ChatlistAdapter chatlistAdapter;

    ImageView HomeScreenWallpaper;

    public Home_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_, container, false);

        //declaring ids
        HomeScreenWallpaper = view.findViewById(R.id.home_screen_wallpaper);
        recyclerView = view.findViewById(R.id.chatlist_RecycleView);
        namesList = new ArrayList<>();
        IdsList = new ArrayList<>();


        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        //recyclerView.setLayoutManager(linearLayoutManager);

        chatlistAdapter = new ChatlistAdapter(getContext(), namesList, IdsList);
        recyclerView.setAdapter(chatlistAdapter);

        Query query = databaseReference.orderByChild("email").equalTo(currentUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String homeScreenImage = "" + ds.child("homeScreenImage").getValue();

                    if (!homeScreenImage.equals("")) {
                        //if image is received then set this
                        //if there is image set that image
                        Picasso.get().load(homeScreenImage).into(HomeScreenWallpaper); //setting homeScreenWallpaper
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //calling method
        loadChatLists();

        return view;
    }

    private void loadChatLists() {

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        //for getting chatlist of current user
        userRef.child(currentUser.getUid()).child("ChatLists")
                .orderByValue().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    String userNameId = snapshot.getKey();
                    userRef.child(userNameId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            if (snapshot2.exists()) {
                                namesList.add(0, snapshot2.child("name").getValue().toString());
                                IdsList.add(0, snapshot2.getKey());
                                chatlistAdapter.notifyDataSetChanged();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    final String userNameId = snapshot.getKey();
                    userRef.child(userNameId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            if (snapshot2.exists()) {
                                int index = IdsList.indexOf(userNameId);
                                namesList.remove(index);
                                IdsList.remove(index);

                                namesList.add(0, snapshot2.child("name").getValue().toString());
                                IdsList.add(0, snapshot2.getKey());
                                chatlistAdapter.notifyDataSetChanged();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {// if user signed in already

        } else {
            //if user is not signed in goto mainActivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    //for profile option mmenu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.main_menu, menu);

        //hide logout
        menu.findItem(R.id.create_group).setVisible(false);
        menu.findItem(R.id.search).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    //handling the menu item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item ids
        int id = item.getItemId();

        if (id == R.id.logout) {
            logoutDialog();
        }
        if (id == R.id.settings) {
            startActivity(new Intent(getActivity(), Settings_Activity.class));
        }
        if (id == R.id.favourite) {
            startActivity(new Intent(getActivity(), FavouriteDashboardActivity.class));
        }
        if (id == R.id.exit) {
            //getActivity().finish();
            System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

    private void logoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirm);
        builder.setMessage(R.string.do_you_want_to_logout);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //get timestamp
                String timestamp = String.valueOf(System.currentTimeMillis());
                //set offline with last seen
                checkOnlineStatus(timestamp);

            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update onlineStatus of current user
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                firebaseAuth.signOut();
                checkUserStatus();
            }
        });
    }
}
