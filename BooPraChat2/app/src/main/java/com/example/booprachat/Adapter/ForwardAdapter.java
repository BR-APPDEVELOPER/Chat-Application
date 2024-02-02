package com.example.booprachat.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Model.Users;
import com.example.booprachat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ForwardAdapter extends RecyclerView.Adapter<ForwardAdapter.MyHolder>{

    Context context;
    private ArrayList<String> namesList, IdsList;
    String imageUri;
    String text;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    public ForwardAdapter(Context context, ArrayList<String> namesList, ArrayList<String> idsList, String imageUri, String text) {
        this.context = context;
        this.namesList = namesList;
        IdsList = idsList;
        this.imageUri = imageUri;
        this.text = text;

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.send_to_list, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //get data and set data
        holder.Username.setText(namesList.get(position));
        String receiverUserId = IdsList.get(position);

        holder.reference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);

                String receiverImage = users.getImage();

                if (receiverImage.equals("")) {
                    holder.ProfileImage.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Picasso.get().load(receiverImage).into(holder.ProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forwardImageText(receiverUserId);
            }
        });
    }


    private void forwardImageText(String receiverUserId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage("Sending...");
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        //add image uri nad other info to database
        String timestamp = "" + System.currentTimeMillis();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //setup required data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("receiver", receiverUserId);
        hashMap.put("message", imageUri);
        hashMap.put("secondaryMessage", text);
        hashMap.put("time", timestamp);
        hashMap.put("type", "forwardedImageAndText");
        hashMap.put("isSeen", false);
        //put this data to firebase
        //creating "ChatLists" node in currentUsers details
        reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                pg.dismiss();
                Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public int getItemCount() {
        return namesList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        CircleImageView ProfileImage;
        TextView Username;
        TextView ProfileText;
        DatabaseReference reference;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //declaring ids
            ProfileImage = itemView.findViewById(R.id.profile_image);
            ProfileText = itemView.findViewById(R.id.profile_text);
            Username = itemView.findViewById(R.id.user_name);

            //firebase services
            reference = FirebaseDatabase.getInstance().getReference("Users");
        }
    }
}
