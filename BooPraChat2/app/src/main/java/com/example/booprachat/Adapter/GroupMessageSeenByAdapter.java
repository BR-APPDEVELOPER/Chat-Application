package com.example.booprachat.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.MessageActivity;
import com.example.booprachat.Model.Users;
import com.example.booprachat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessageSeenByAdapter extends RecyclerView.Adapter<GroupMessageSeenByAdapter.MyHolder> {

    private Context context;
    private ArrayList<Users> usersList;
    private String time;

    public GroupMessageSeenByAdapter(Context context, ArrayList<Users> usersList, String time) {
        this.context = context;
        this.usersList = usersList;
        this.time = time;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_group_message_seen_by, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Users users = usersList.get(position);

        //get data
        String name = users.getName();
        String image = users.getImage();
        String receiverUserId = users.getUid();

        //set data
        holder.userName.setText(name);

        if (image.equals("")) {
            holder.profileImage.setVisibility(View.GONE);
            holder.profileText.setVisibility(View.VISIBLE);

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

            //set first letter of the name
            holder.profileText.setText(firstLetter);

        } else {
            holder.profileText.setVisibility(View.GONE);
            holder.profileImage.setVisibility(View.VISIBLE);

            Picasso.get().load(image).into(holder.profileImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("receiverId", receiverUserId);
                context.startActivity(intent);
                ((Activity) context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        private CircleImageView profileImage;
        private TextView profileText;
        private TextView userName;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //declaring ui ids of row_group_message_seen_by layout
            profileImage = itemView.findViewById(R.id.profile_image);
            profileText = itemView.findViewById(R.id.profile_text);
            userName = itemView.findViewById(R.id.receiver_name);
        }
    }
}
