package com.example.booprachat.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.MessageActivity;
import com.example.booprachat.Model.Chat;
import com.example.booprachat.Model.Users;
import com.example.booprachat.ProfileViewActivity;
import com.example.booprachat.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatlistAdapter extends RecyclerView.Adapter<ChatlistAdapter.MyHolder> {

    Context context;
    private ArrayList<String> namesList, IdsList;

    private ArrayList<Chat> chats;
    String theLastMessage;

    // for firebase services
    FirebaseAuth firebaseAuth;
    DatabaseReference reference;

    public ChatlistAdapter(Context context, ArrayList<String> namesList, ArrayList<String> IdsList) {
        this.context = context;
        this.namesList = namesList;
        this.IdsList = IdsList;

        // for firebase services
        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        holder.ProfileText.setVisibility(View.GONE);
        holder.ProfileImage.setVisibility(View.GONE);
        //get data
        //holder.receiverName.setText(namesList.get(position));
        String receiverUserId = IdsList.get(position);

        //calling method
        loadUserDetails(receiverUserId, holder);

        chats = new ArrayList<>();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get value
                    Chat chat = ds.getValue(Chat.class);

                    String unreadMessageCount = "" + ds.child("messageSeenOrNot").getValue();

                    if (chat.getReceiver().equals(firebaseAuth.getUid()) && chat.getSender().equals(receiverUserId)) {
                        if (unreadMessageCount.equals("false")) {
                            chats.add(chat);
                        }
                    }

                    String messageCount = String.valueOf(chats.size());
                    int count = Integer.parseInt(messageCount);

                    if (messageCount.equals("0")) {
                        holder.unreadMessageCountLayout.setVisibility(View.GONE);

                    } else if (count > 5000) {
                        holder.unreadMessageCountLayout.setVisibility(View.VISIBLE);
                        holder.unreadMessageCount.setText("5000+");

                    } else {
                        holder.unreadMessageCountLayout.setVisibility(View.VISIBLE);
                        holder.unreadMessageCount.setText(messageCount);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.selectedItem.getVisibility() == View.VISIBLE) {
                    holder.selectedItem.setVisibility(View.GONE);

                } else {
                    //start messageActivity with the selected user
                    Intent intent = new Intent(context, MessageActivity.class);
                    intent.putExtra("receiverId", receiverUserId);
                    context.startActivity(intent);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                holder.selectedItem.setVisibility(View.VISIBLE);

                holder.deleteUserFromChatList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDeleteDialog(position, receiverUserId, holder);
                    }
                });

                return true;
            }
        });

    }

    private void loadUserDetails(String receiverUserId, MyHolder holder) {
        reference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);

                String receiverName = users.getName();
                String receiverImage = users.getImage();
                String onlineStatus = users.getOnlineStatus();

                lastMessage(users.getUid(), holder);

                holder.receiverName.setText(receiverName);

                if (receiverImage.equals("")) {
                    holder.ProfileImage.setVisibility(View.GONE);
                    holder.ProfileText.setVisibility(View.VISIBLE);

                    //char firstLetterLetter = name.charAt(0);
                    String firstLetter = String.valueOf(receiverName.charAt(0)).toLowerCase();

                    Drawable drawable = holder.ProfileText.getBackground();
                    drawable = DrawableCompat.wrap(drawable);

                    if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#FF00BCD4")); // color blue

                        holder.ProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#FFFF5722")); //color red

                        holder.ProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#bfd200")); //color

                        holder.ProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#FF9800")); //color orange

                        holder.ProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#52b788")); //color old: #cccc00

                        holder.ProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("z")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#F33D73")); //color darker pink

                        holder.ProfileText.setBackground(drawable);

                    } else {
                        DrawableCompat.setTint(drawable, Color.parseColor("#6c757d")); //color

                        holder.ProfileText.setBackground(drawable);
                    }

                    //set text
                    holder.ProfileText.setText(firstLetter);

                } else {
                    holder.ProfileImage.setVisibility(View.VISIBLE);
                    holder.ProfileText.setVisibility(View.GONE);

                    Picasso.get().load(receiverImage).into(holder.ProfileImage);
                }

                if (onlineStatus.equals("online")) {
                    holder.onlineOrOfflineLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.onlineOrOfflineLayout.setVisibility(View.GONE);
                }

                holder.ProfileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //start ProfileViewActivity to view the profileImage in fullView
                        Intent intent = new Intent(context, ProfileViewActivity.class);
                        intent.putExtra("name", receiverName);
                        intent.putExtra("image", receiverImage);
                        context.startActivity(intent);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showDeleteDialog(int position, String receiverUserId, MyHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.confirm);
        builder.setMessage(context.getString(R.string.do_you_want_to_delete) + " " + namesList.get(position) + " " + context.getString(R.string.chat_question_mark));

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");

        builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                reference1.child(firebaseAuth.getUid()).child("ChatLists").child(receiverUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        holder.selectedItem.setVisibility(View.GONE);
                        Toast.makeText(context, "Successfully deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                holder.selectedItem.setVisibility(View.GONE);
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void lastMessage(final String userId, MyHolder holder) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);

                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())) {

                        //instead of displaying url in message show
                        if (chat.getType().equals("image") || chat.getType().equals("forwardedImage") || chat.getType().equals("forwardedImageAndText") || chat.getType().equals("imageAndText")) {
                            //for image types
                            holder.PhotoLastMessage.setVisibility(View.VISIBLE);
                            holder.TextLastMessage.setVisibility(View.GONE);
                            holder.AudioLastMessage.setVisibility(View.GONE);
                            holder.VideoLastMessage.setVisibility(View.GONE);
                            holder.PdfLastMessage.setVisibility(View.GONE);

                        } else if (chat.getType().equals("audio") || chat.getType().equals("forwardedAudio")) {
                            //for audio
                            holder.AudioLastMessage.setVisibility(View.VISIBLE);
                            holder.PhotoLastMessage.setVisibility(View.GONE);
                            holder.TextLastMessage.setVisibility(View.GONE);
                            holder.VideoLastMessage.setVisibility(View.GONE);
                            holder.PdfLastMessage.setVisibility(View.GONE);

                        } else if (chat.getType().equals("video") || chat.getType().equals("forwardedVideo") || chat.getType().equals("videoAndText") || chat.getType().equals("forwardedVideoAndText")) {
                            //for video
                            holder.VideoLastMessage.setVisibility(View.VISIBLE);
                            holder.AudioLastMessage.setVisibility(View.GONE);
                            holder.PhotoLastMessage.setVisibility(View.GONE);
                            holder.TextLastMessage.setVisibility(View.GONE);
                            holder.PdfLastMessage.setVisibility(View.GONE);

                        } else if (chat.getType().equals("pdf") || chat.getType().equals("forwardedPdf")) {
                            //for pdf
                            holder.PdfLastMessage.setVisibility(View.VISIBLE);
                            holder.VideoLastMessage.setVisibility(View.GONE);
                            holder.AudioLastMessage.setVisibility(View.GONE);
                            holder.PhotoLastMessage.setVisibility(View.GONE);
                            holder.TextLastMessage.setVisibility(View.GONE);

                        } else if (chat.getType().equals("text")) {
                            //for text
                            holder.TextLastMessage.setVisibility(View.VISIBLE);
                            holder.TextLastMessage.setText(chat.getMessage());

                            holder.PhotoLastMessage.setVisibility(View.GONE);
                            holder.AudioLastMessage.setVisibility(View.GONE);
                            holder.VideoLastMessage.setVisibility(View.GONE);
                            holder.PdfLastMessage.setVisibility(View.GONE);

                        }
                    }
                }
                theLastMessage = "default";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return namesList.size(); //size of the list
    }

    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView ProfileImage;
        TextView receiverName, TextLastMessage, unreadMessageCount;
        TextView ProfileText;
        LinearLayout onlineOrOfflineLayout;

        RelativeLayout unreadMessageCountLayout, selectedItem;
        LinearLayout PhotoLastMessage, AudioLastMessage, VideoLastMessage, PdfLastMessage;
        CardView deleteUserFromChatList;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //declaring ui ids
            //for user details
            ProfileImage = itemView.findViewById(R.id.profile_image);
            ProfileText = itemView.findViewById(R.id.profile_text);
            receiverName = itemView.findViewById(R.id.receiver_name);

            //for last message layout
            TextLastMessage = itemView.findViewById(R.id.text_last_message);
            PhotoLastMessage = itemView.findViewById(R.id.photo_last_message);
            AudioLastMessage = itemView.findViewById(R.id.audio_last_message);
            VideoLastMessage = itemView.findViewById(R.id.video_last_message);
            PdfLastMessage = itemView.findViewById(R.id.pdf_last_message);

            //for unread message
            unreadMessageCount = itemView.findViewById(R.id.unread_message_count);
            unreadMessageCountLayout = itemView.findViewById(R.id.unread_message_count_layout);

            // online or offline
            onlineOrOfflineLayout = itemView.findViewById(R.id.online_or_offline_layout);

            //delete user from chat list
            deleteUserFromChatList = itemView.findViewById(R.id.delete_user_from_chat);
            selectedItem = itemView.findViewById(R.id.selected_item);
        }
    }
}
