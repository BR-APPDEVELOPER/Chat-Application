package com.example.booprachat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Model.Users;
import com.example.booprachat.R;
import com.example.booprachat.Settings.PreviewTypingToWallpaperActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class TypingToWallpaperAdapter extends RecyclerView.Adapter<TypingToWallpaperAdapter.MyHolder> {

    Context context;
    ArrayList<Users> usersList;
    private String typingToWallpaper = "";

    public TypingToWallpaperAdapter(Context context, ArrayList<Users> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_add_typing_to_wallpaper, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        Users users = usersList.get(position);

        //get data
        String userName = users.getName();
        String userProfileImage = users.getImage();
        String uid = users.getUid();

        //set data
        holder.userName.setText(userName);
        holder.userName.setEllipsize(TextUtils.TruncateAt.END);

        if (userProfileImage.equals("")) {
            holder.profileImage.setVisibility(View.GONE);
            holder.profileText.setVisibility(View.VISIBLE);

            String name = holder.userName.getText().toString();

            String firstLetter = String.valueOf(name.charAt(0)).toLowerCase();

            Drawable drawable = holder.profileText.getBackground();
            drawable = DrawableCompat.wrap(drawable);

            if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#FF00BCD4")); // color blue

                holder.profileText.setBackground(drawable);

            } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#FFFF5722")); //color red

                holder.profileText.setBackground(drawable);

            } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#bfd200")); //color

                holder.profileText.setBackground(drawable);

            } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#FF9800")); //color orange

                holder.profileText.setBackground(drawable);

            } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#52b788")); //color old: #cccc00

                holder.profileText.setBackground(drawable);

            } else if (firstLetter.equals("z")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#F33D73")); //color darker pink

                holder.profileText.setBackground(drawable);

            } else {
                DrawableCompat.setTint(drawable, Color.parseColor("#6c757d")); //color

                holder.profileText.setBackground(drawable);
            }
            //set first letter of the name text
            holder.profileText.setText(firstLetter);

        } else {
            holder.profileText.setVisibility(View.GONE);
            holder.profileImage.setVisibility(View.VISIBLE);

            Picasso.get().load(userProfileImage).into(holder.profileImage);
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        reference.child(firebaseAuth.getUid()).child("TypingToWallpaper").orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    typingToWallpaper = "" + ds.child("typingToWallpaper").getValue();

                    //set data
                    if (!typingToWallpaper.equals("")) {
                        Picasso.get().load(typingToWallpaper).into(holder.typingToWallpaper);
                    } else {
                        holder.typingToWallpaper.setImageResource(R.drawable.ic_baseline_add_icon);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.typingToWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PreviewTypingToWallpaperActivity.class);
                intent.putExtra("receiverId", uid);
                intent.putExtra("profileImage", userProfileImage);
                context.startActivity(intent);
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
        TextView userName;
        ShapeableImageView typingToWallpaper;
        ImageView addImage;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profile_image);
            profileText = itemView.findViewById(R.id.profile_text);
            userName = itemView.findViewById(R.id.user_name);
            typingToWallpaper = itemView.findViewById(R.id.typing_to_wallpaper);
        }
    }
}
