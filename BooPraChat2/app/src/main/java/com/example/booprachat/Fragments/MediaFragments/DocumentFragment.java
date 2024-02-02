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
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Adapter.MediaDocumentAdapter;
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

public class DocumentFragment extends Fragment {

    DatabaseReference userReference;
    DatabaseReference chatReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;

    RecyclerView recyclerView;
    TextView noDocumentsFound;
    LinearLayout toolbar1;
    ImageView delete, forward, share, favourite, closeToolbar1;

    ArrayList<Chat> chatList;
    MediaDocumentAdapter mediaDocumentAdapter;

    public DocumentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_document, container, false);

        //declaring ui ids
        recyclerView = view.findViewById(R.id.media_document_recycleview);
        noDocumentsFound = view.findViewById(R.id.no_documents_found);
        toolbar1 = view.findViewById(R.id.linear_toolbar_1);
        delete = view.findViewById(R.id.delete_message);
        forward = view.findViewById(R.id.forward_message);
        share = view.findViewById(R.id.share_message);
        favourite = view.findViewById(R.id.favourite);
        closeToolbar1 = view.findViewById(R.id.down_arrow);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        chatReference = FirebaseDatabase.getInstance().getReference("Chats");
        chatList = new ArrayList<>();

        Query query = userReference.orderByChild("email").equalTo(currentUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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

                                //setup adapter
                                mediaDocumentAdapter = new MediaDocumentAdapter(getActivity(), chatList, noDocumentsFound, recyclerView, toolbar1, delete, forward, share, favourite);
                                recyclerView.setAdapter(mediaDocumentAdapter); // setting adapter
                            }
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

        closeToolbar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar1.setVisibility(View.GONE);
            }
        });

        return view;
    }
}