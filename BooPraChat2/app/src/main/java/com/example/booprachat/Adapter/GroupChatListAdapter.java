package com.example.booprachat.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Dashboard_Activity;
import com.example.booprachat.GroupChats.GroupInfoActivity;
import com.example.booprachat.GroupChats.GroupMessageActivity;
import com.example.booprachat.Model.GroupChat;
import com.example.booprachat.Model.GroupChatList;
import com.example.booprachat.ProfileViewActivity;
import com.example.booprachat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatListAdapter extends RecyclerView.Adapter<GroupChatListAdapter.HolderGroupChatList> {

    private Context context;
    private ArrayList<GroupChatList> groupChatLists;
    private ArrayList<String> unreadMessageCount;

    private FirebaseAuth firebaseAuth;
    private String myGroupRole;

    public GroupChatListAdapter(Context context, ArrayList<GroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;

        firebaseAuth = FirebaseAuth.getInstance();
        unreadMessageCount = new ArrayList<>();
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_list, parent, false);

        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {

        //get data
        GroupChatList groupList = groupChatLists.get(position);

        String groupIcon = groupList.getGroupIcon();
        String groupTitle = groupList.getGroupTitle();
        String groupId = groupList.getGroupId();

        //set data
        holder.groupName.setText(groupTitle);//set group name

        // if there is no message set empty
        holder.senderName.setText("");
        holder.lastMessageTime.setText("");

        if (groupIcon.equals("")) {
            // if there is no group profile image, then set group name starting letter as group profile
            holder.groupProfileImage.setVisibility(View.INVISIBLE);
            holder.groupProfileText.setVisibility(View.VISIBLE);

            String firstLetter = String.valueOf(groupTitle.charAt(0)).toLowerCase();

            Drawable drawable = holder.groupProfileText.getBackground();
            drawable = DrawableCompat.wrap(drawable);

            if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#FF00BCD4")); // color blue

                holder.groupProfileText.setBackground(drawable);

            } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#FFFF5722")); //color red

                holder.groupProfileText.setBackground(drawable);

            } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#bfd200")); //color

                holder.groupProfileText.setBackground(drawable);

            } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#FF9800")); //color orange

                holder.groupProfileText.setBackground(drawable);

            } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#52b788")); //color old: #cccc00

                holder.groupProfileText.setBackground(drawable);

            } else if (firstLetter.equals("z")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#F33D73")); //color darker pink

                holder.groupProfileText.setBackground(drawable);

            } else {
                DrawableCompat.setTint(drawable, Color.parseColor("#6c757d")); //color

                holder.groupProfileText.setBackground(drawable);
            }

            holder.groupProfileText.setText(firstLetter);


        } else {
            //if there is group profile image set that image
            holder.groupProfileImage.setVisibility(View.VISIBLE);
            holder.groupProfileText.setVisibility(View.GONE);

            Picasso.get().load(groupIcon).into(holder.groupProfileImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.selectedItem.getVisibility() == View.VISIBLE) {
                    holder.selectedItem.setVisibility(View.GONE);
                } else {
                    // open group chat activity
                    Intent intent = new Intent(context, GroupMessageActivity.class);
                    intent.putExtra("groupId", groupId);
                    context.startActivity(intent);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                holder.selectedItem.setVisibility(View.VISIBLE);

                holder.groupInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, GroupInfoActivity.class);
                        intent.putExtra("groupId", groupId);
                        context.startActivity(intent);

                        holder.selectedItem.setVisibility(View.GONE);
                    }
                });

                return true;
            }
        });

        holder.linearToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.selectedItem.setVisibility(View.GONE);
            }
        });

        //click to view profile image
        holder.groupProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //start ProfileViewActivity to view the profileImage in fullView
                Intent intent = new Intent(context, ProfileViewActivity.class);
                intent.putExtra("name", groupTitle);
                intent.putExtra("image", groupIcon);
                context.startActivity(intent);
            }
        });

        loadLastMessage(groupList, holder);
        loadUnreadMessageCount(groupId, holder);
        loadMyGroupRole(groupId, holder);

    }

    private void deleteGroup(String groupId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //group deleted successfully
                        Toast.makeText(context, context.getString(R.string.team_deleted_successfully), Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(context, Dashboard_Activity.class));
                        ((Activity) context).finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to delete this group
                        Toast.makeText(context, context.getString(R.string.failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void leaveGroup(String groupId) {

        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", myGroupRole);
        hashMap.put("time", timestamp);
        hashMap.put("type", "leavedFromGroup");

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Groups");
        reference1.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                        reference.child(groupId).child("participants").child(firebaseAuth.getUid())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //if user successfully leave from group
                                        context.startActivity(new Intent(context, Dashboard_Activity.class));
                                        ((Activity) context).finish();
                                        Toast.makeText(context, R.string.successfully_your_leaved_from_team, Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed to leave from this group
                                        Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

    }

    private void loadMyGroupRole(String groupId, HolderGroupChatList holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("participants").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get data
                            myGroupRole = "" + ds.child("role").getValue();
                        }

                        String dialogTitle = "";
                        String dialogDescription = "";
                        String positiveButtonTitle = "";

                        if (myGroupRole.equals("leader")) {
                            holder.exitGroup.setImageResource(R.drawable.ic_baseline_delete_24);
                            dialogTitle = context.getString(R.string.delete_team);
                            dialogDescription = context.getString(R.string.are_you_sure_you_want_to_delete_team);
                            positiveButtonTitle = context.getString(R.string.ok);

                        } else {
                            holder.exitGroup.setImageResource(R.drawable.ic_baseline_group_chat_list_exit);
                            dialogTitle = context.getString(R.string.leave_team);
                            dialogDescription = context.getString(R.string.are_you_sure_you_want_to_leave_from_this_team);
                            positiveButtonTitle = context.getString(R.string.ok);
                        }

                        String finalDialogTitle = dialogTitle;
                        String finalDialogDescription = dialogDescription;
                        String finalPositiveButtonTitle = positiveButtonTitle;

                        //if user is participant/admin leave group
                        //if user is creator delete group
                        holder.exitGroup.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle(finalDialogTitle)
                                        .setMessage(finalDialogDescription)
                                        .setPositiveButton(finalPositiveButtonTitle, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (myGroupRole.equals("leader")) {
                                                    deleteGroup(groupId);
                                                    holder.selectedItem.setVisibility(View.GONE);
                                                } else {
                                                    leaveGroup(groupId);
                                                    holder.selectedItem.setVisibility(View.GONE);
                                                }
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                                holder.selectedItem.setVisibility(View.GONE);
                                            }
                                        }).show();
                            }

                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadUnreadMessageCount(String groupId, HolderGroupChatList holder) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //unreadMessageCount.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {

                    GroupChat groupChat = ds.getValue(GroupChat.class);

                    String messageTime = groupChat.getTime();

                    reference.child(groupId).child("Messages").child(messageTime).child("messageSeenMembers").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot ds : snapshot.getChildren()) {
                                if (ds.exists()) {
                                    holder.unreadMessageCount.setVisibility(View.GONE);
                                    return;
                                }
                            }

                            holder.unreadMessageCount.setVisibility(View.VISIBLE);
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


    }

    private void loadLastMessage(GroupChatList groupList, HolderGroupChatList holder) {
        //get last message from group
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");

        reference.child(groupList.getGroupId()).child("Messages").limitToLast(1) //get last message from child "Messages"
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            // get data
                            String message = "" + ds.child("message").getValue();
                            String sender = "" + ds.child("sender").getValue();
                            String messageTime = "" + ds.child("time").getValue();
                            String type = "" + ds.child("type").getValue();

                            //convert timestamp to dd/mm/yyyy hh:mm am/pm
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(messageTime));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                            //set data
                            holder.lastMessageTime.setText(dateTime); //set last message time

                            if (type.equals("text")) {
                                holder.textLastMessage.setText(message);
                                holder.textLastMessage.setVisibility(View.VISIBLE);
                                holder.audioLastMessage.setVisibility(View.GONE);
                                holder.videoLastMessage.setVisibility(View.GONE);
                                holder.pdfLastMessage.setVisibility(View.GONE);
                                holder.photoLastMessage.setVisibility(View.GONE);

                            } else if (type.equals("audio") || type.equals("forwardedAudio")) {
                                holder.audioLastMessage.setVisibility(View.VISIBLE);
                                holder.videoLastMessage.setVisibility(View.GONE);
                                holder.pdfLastMessage.setVisibility(View.GONE);
                                holder.photoLastMessage.setVisibility(View.GONE);
                                holder.textLastMessage.setVisibility(View.GONE);

                            } else if (type.equals("video") || type.equals("forwardedVideo") || type.equals("videoAndText") || type.equals("forwardedVideoAndText")) {
                                holder.videoLastMessage.setVisibility(View.VISIBLE);
                                holder.pdfLastMessage.setVisibility(View.GONE);
                                holder.photoLastMessage.setVisibility(View.GONE);
                                holder.textLastMessage.setVisibility(View.GONE);
                                holder.audioLastMessage.setVisibility(View.GONE);

                            } else if (type.equals("pdf") || type.equals("forwardedPdf")) {
                                holder.pdfLastMessage.setVisibility(View.VISIBLE);
                                holder.photoLastMessage.setVisibility(View.GONE);
                                holder.textLastMessage.setVisibility(View.GONE);
                                holder.audioLastMessage.setVisibility(View.GONE);
                                holder.videoLastMessage.setVisibility(View.GONE);

                            } else if (type.equals("image") || type.equals("forwardedImage") || type.equals("imageAndText") || type.equals("forwardedImageAndText")) {
                                holder.photoLastMessage.setVisibility(View.VISIBLE);
                                holder.textLastMessage.setVisibility(View.GONE);
                                holder.audioLastMessage.setVisibility(View.GONE);
                                holder.videoLastMessage.setVisibility(View.GONE);
                                holder.pdfLastMessage.setVisibility(View.GONE);

                            }


                            DatabaseReference msgSenderRef = FirebaseDatabase.getInstance().getReference("Users");
                            msgSenderRef.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                //get message sender data
                                                String senderName = "" + ds.child("name").getValue();

                                                //set data
                                                if (type.equals("leavedFromGroup")) {

                                                    holder.textLastMessage.setText(senderName + context.getString(R.string.left_from_team));

                                                    holder.senderName.setVisibility(View.INVISIBLE);
                                                    holder.textLastMessage.setTextColor(Color.parseColor("#A14951"));
                                                    holder.textLastMessage.setVisibility(View.VISIBLE);
                                                    holder.audioLastMessage.setVisibility(View.GONE);
                                                    holder.videoLastMessage.setVisibility(View.GONE);
                                                    holder.pdfLastMessage.setVisibility(View.GONE);
                                                    holder.photoLastMessage.setVisibility(View.GONE);

                                                } else {
                                                    holder.senderName.setText(senderName + ": "); //set last message sender name
                                                }
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
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }


    class HolderGroupChatList extends RecyclerView.ViewHolder {

        private TextView textLastMessage, senderName, groupName, lastMessageTime;
        private TextView groupProfileText;
        private CircleImageView groupProfileImage;
        private LinearLayout photoLastMessage, audioLastMessage, videoLastMessage, pdfLastMessage;
        private CardView unreadMessageCount;

        //for group chat list toolbar
        private RelativeLayout selectedItem;
        private ImageView groupInfo, exitGroup;
        private LinearLayout linearToolbar;

        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);

            //declaring ids
            senderName = itemView.findViewById(R.id.last_message_sender_name);
            groupName = itemView.findViewById(R.id.group_chat_name);
            groupProfileImage = itemView.findViewById(R.id.group_chat_profile_image);
            groupProfileText = itemView.findViewById(R.id.group_chat_profile_text);

            //for unread message count
            unreadMessageCount = itemView.findViewById(R.id.unread_message_count_for_group_chat);

            //for last message
            textLastMessage = itemView.findViewById(R.id.gc_text_last_message);
            audioLastMessage = itemView.findViewById(R.id.gc_audio_last_message);
            videoLastMessage = itemView.findViewById(R.id.gc_video_last_message);
            pdfLastMessage = itemView.findViewById(R.id.gc_pdf_last_message);
            photoLastMessage = itemView.findViewById(R.id.gc_photo_last_message);
            lastMessageTime = itemView.findViewById(R.id.last_message_time); //for last message time

            //for group chat list toolbar
            selectedItem = itemView.findViewById(R.id.selected_item);
            groupInfo = itemView.findViewById(R.id.group_info);
            exitGroup = itemView.findViewById(R.id.exit_group);
            linearToolbar = itemView.findViewById(R.id.linear_toolbar);

        }
    }
}
