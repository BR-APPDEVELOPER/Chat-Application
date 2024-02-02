package com.example.booprachat.Fragments.MediaFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.MediaAdapter;
import com.example.booprachat.Model.Chat;
import com.example.booprachat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MediaFragment extends Fragment {

    DatabaseReference userReference;
    DatabaseReference chatReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;

    RecyclerView recyclerView;
    TextView mediaIsEmpty;
    LinearLayout toolbar1;
    ImageView delete, forward, share, favourite, closeToolbar1;

    ArrayList<Chat> chatList;
    MediaAdapter mediaAdapter;

    public MediaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_media, container, false);

        //declaring ui ids
        recyclerView = view.findViewById(R.id.media_recycleview);
        mediaIsEmpty = view.findViewById(R.id.no_media_found);
        toolbar1 = view.findViewById(R.id.linear_toolbar_1);
        delete = view.findViewById(R.id.delete_message);
        forward = view.findViewById(R.id.forward_message);
        share = view.findViewById(R.id.share_message);
        favourite = view.findViewById(R.id.favourite);
        closeToolbar1 = view.findViewById(R.id.down_arrow);

        toolbar1.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        chatReference = FirebaseDatabase.getInstance().getReference("Chats");
        chatList = new ArrayList<>();

        closeToolbar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar1.setVisibility(View.GONE);
            }
        });

        //set Grid layout for recycle view
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);

        Query query = userReference.orderByChild("email").equalTo(currentUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String receiverId = "" + ds.child("receiverId").getValue();

                    chatReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            chatList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                Chat chat = ds.getValue(Chat.class);

                                if (chat.getSender().equals(firebaseAuth.getUid()) && chat.getReceiver().equals(receiverId) ||
                                        chat.getSender().equals(receiverId) && chat.getReceiver().equals(firebaseAuth.getUid())) {

                                    chatList.add(chat);
                                }
                            }
                            //setup adapter
                            mediaAdapter = new MediaAdapter(getActivity(), chatList, recyclerView, mediaIsEmpty, toolbar1, delete, forward, share, favourite);
                            recyclerView.setAdapter(mediaAdapter); // setting adapter
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }
}