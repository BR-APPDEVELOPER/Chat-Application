package com.example.booprachat.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.UserAdapter;
import com.example.booprachat.Model.Users;
import com.example.booprachat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class Users_Fragment extends Fragment {

    RecyclerView recyclerView;
    EditText searchBox;

    UserAdapter userAdapter;
    List<Users> usersList;
    FirebaseAuth firebaseAuth;

    public Users_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        //declaring ids
        recyclerView = view.findViewById(R.id.users_recycleview);
        searchBox = view.findViewById(R.id.search_box);
        //set its properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //init user list
        usersList = new ArrayList<>();


        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsers(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        getAllUsers(); //calling the getAllUsers method;

        return view;
    }

    private void getAllUsers() {
        //get current user
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        //get the path of "Users"
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from the "Users" path
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Users users = ds.getValue(Users.class);
                    //get all users except currently signed in user
                    if (!users.getUid().equals(fuser.getUid())) {
                        usersList.add(users);
                    }
                    //set adapter
                    userAdapter = new UserAdapter(getActivity(), usersList);
                    //set adapter to recycleview
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(String s) {
        //get current user
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        //get the path of "Users"
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from the "Users" path
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Users users = ds.getValue(Users.class);

                    //get all searched users except currently signed in user
                    if (!users.getUid().equals(fuser.getUid())) {
                        if (users.getName().toLowerCase().contains(s.toLowerCase()) ||
                                users.getEmail().toLowerCase().contains(s.toLowerCase())) {
                            usersList.add(users);
                        }

                    }
                    //set adapter
                    userAdapter = new UserAdapter(getActivity(), usersList);
                    userAdapter.notifyDataSetChanged(); //refresh adapter
                    //set adapter to recycleview
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}