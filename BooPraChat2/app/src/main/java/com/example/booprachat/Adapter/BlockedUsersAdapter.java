package com.example.booprachat.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.booprachat.Model.Users;
import com.example.booprachat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlockedUsersAdapter extends RecyclerView.Adapter<BlockedUsersAdapter.MyHolder> {

    Context context;
    ArrayList<Users> usersList;

    public BlockedUsersAdapter(Context context, ArrayList<Users> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //get data
        String receiverName = usersList.get(position).getName();
        String profileImage = usersList.get(position).getImage();
        String receiverId = usersList.get(position).getUid();


        //set data
        holder.receiverName.setText(receiverName);

        if (profileImage.equals("")) {
            // if user does not have profile image

            holder.profileImage.setVisibility(View.GONE);
            holder.profileText.setVisibility(View.VISIBLE);

            String firstLetter = String.valueOf(receiverName.charAt(0)).toLowerCase();

            Drawable drawable = holder.profileText.getBackground();
            drawable = DrawableCompat.wrap(drawable);

            if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#FF00BCD4")); // color blue

                holder.profileText.setBackground(drawable);
                holder.profileText.setText(firstLetter);

            } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#FFFF5722")); //color red

                holder.profileText.setBackground(drawable);
                holder.profileText.setText(firstLetter);

            } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#bfd200")); //color yellow

                holder.profileText.setBackground(drawable);
                holder.profileText.setText(firstLetter);

            } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#FF9800")); //color orange

                holder.profileText.setBackground(drawable);
                holder.profileText.setText(firstLetter);

            } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#52b788")); //color old: #cccc00

                holder.profileText.setBackground(drawable);
                holder.profileText.setText(firstLetter);

            } else if (firstLetter.equals("z")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#F33D73")); //color darker pink

                holder.profileText.setBackground(drawable);
                holder.profileText.setText(firstLetter);
            } else {
                DrawableCompat.setTint(drawable, Color.parseColor("#6c757d")); //color

                holder.profileText.setBackground(drawable);
                holder.profileText.setText(firstLetter);
            }

        } else {
            // if user having profile image

            holder.profileImage.setVisibility(View.VISIBLE);
            holder.profileText.setVisibility(View.GONE);

            Glide.with(context).load(profileImage).into(holder.profileImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                String[] options = {"Unblock " + receiverName};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            unBlockUser(receiverId);
                        }
                    }
                }).show();

            }
        });
    }

    private void unBlockUser(String receiverId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        reference.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, R.string.unblocked_successfully, Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView profileText;
        TextView receiverName;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //declaring ui ids
            profileImage = itemView.findViewById(R.id.profile_image);
            profileText = itemView.findViewById(R.id.profile_text);
            receiverName = itemView.findViewById(R.id.receiverName);
        }
    }
}
