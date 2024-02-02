package com.example.booprachat.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.GroupChatListAdapter;
import com.example.booprachat.GroupChats.GroupCreateActivity;
import com.example.booprachat.Model.GroupChatList;
import com.example.booprachat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class GroupChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ArrayList<GroupChatList> groupChatLists;
    private ArrayList<String> unreadMessageCount;
    private GroupChatListAdapter groupChatListAdapter;
    private ImageView HomeScreenImage;
    private String groupId;

    public GroupChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        loadLocal();
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);

        recyclerView = view.findViewById(R.id.group_chat_recycleview);
        HomeScreenImage = view.findViewById(R.id.home_screen_wallpaper);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        loadGroupChatList();
        loadHomeScreenImage();


        return view;
    }


    private void loadHomeScreenImage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String homeScreenImage = "" + ds.child("homeScreenImage").getValue();

                    if (!homeScreenImage.equals("")) {
                        Picasso.get().load(homeScreenImage).into(HomeScreenImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupChatList() {
        groupChatLists = new ArrayList<>();
        //unreadMessageCount = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    //if current user, uid exists in Participants list of group then show that group
                    if (ds.child("participants").child(firebaseAuth.getUid()).exists()) {
                        GroupChatList modelGroupChatList = ds.getValue(GroupChatList.class);

                        groupChatLists.add(modelGroupChatList);
                        groupId = modelGroupChatList.getGroupId();
                    }

                    /*reference.child(groupId).child("Messages").limitToLast(1).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            unreadMessageCount.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {

                                GroupChat groupChat = ds.getValue(GroupChat.class);

                                messageTime = groupChat.getTime();

                            }

                            /*reference.child(groupId).child("Messages").child(message).child("messageSeenMembers").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    unreadMessageCount.clear();
                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        //get user ID
                                        String uid = "" + ds.child("uid").getValue();

                                        Toast.makeText(getActivity(), " " + uid, Toast.LENGTH_SHORT).show();
                                        unreadMessageCount.add(uid);

                                        if (uid.equals(firebaseAuth.getUid())){
                                            Toast.makeText(getActivity(), "1:" + unreadMessageCount.size(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), "0:" + unreadMessageCount.size(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            reference.child(groupId).child("Messages").child(messageTime).child("messageSeenMembers").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        //get user ID
                                        String uid = "" + ds.child("uid").getValue();

                                        if (!uid.equals(firebaseAuth.getUid())){
                                            Toast.makeText(getActivity(), "1", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), "0", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });*/
                }
                groupChatListAdapter = new GroupChatListAdapter(getContext(), groupChatLists);
                recyclerView.setAdapter(groupChatListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void searchGroupChatList(String query) {
        groupChatLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    //if current user, uid exists in Participants list of group then show that group
                    if (ds.child("participants").child(firebaseAuth.getUid()).exists()) {

                        //search by group name
                        if (ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())) {
                            GroupChatList modelGroupChatList = ds.getValue(GroupChatList.class);
                            groupChatLists.add(modelGroupChatList);
                        }

                    }
                }
                groupChatListAdapter = new GroupChatListAdapter(getContext(), groupChatLists);
                recyclerView.setAdapter(groupChatListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    private void setLocal(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getActivity().getResources().updateConfiguration(configuration, getActivity().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_language", language);
        editor.apply();
    }

    public void loadLocal() {
        SharedPreferences editor = getActivity().getSharedPreferences("Settings", MODE_PRIVATE);
        String lan = editor.getString("My_language", "");
        setLocal(lan);
    }

    //for profile option mmenu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.main_menu, menu);

        //hide
        menu.findItem(R.id.logout).setVisible(false);
        menu.findItem(R.id.settings).setVisible(false);
        menu.findItem(R.id.favourite).setVisible(false);
        menu.findItem(R.id.exit).setVisible(false);

        //search view
        MenuItem item = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user pressed search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim())) {
                    //search text contains text, search it
                    searchGroupChatList(s);
                } else {
                    //search text empty, get all users
                    loadGroupChatList();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called when user pressed search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim())) {
                    //search text contains text, search it
                    searchGroupChatList(s);
                } else {
                    //search text empty, get all users
                    loadGroupChatList();
                }

                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    //handling the menu item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item ids
        int id = item.getItemId();

        if (id == R.id.create_group) {
            //for create new group
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}