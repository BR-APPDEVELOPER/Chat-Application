package com.example.booprachat.Adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.Status;
import com.example.booprachat.ChatViews.AudioViewPage;
import com.example.booprachat.ChatViews.ImageViewPage;
import com.example.booprachat.ChatViews.PdfViewerPage;
import com.example.booprachat.ChatViews.VideoViewPage;
import com.example.booprachat.GroupChats.GroupMessageActivity;
import com.example.booprachat.MessageActivity;
import com.example.booprachat.Model.Chat;
import com.example.booprachat.R;
import com.example.booprachat.ReceiveDataFromOtherApps.SendToActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class FavouriteMessagesAdapter extends RecyclerView.Adapter<FavouriteMessagesAdapter.MyHolder> {

    Context context;
    List<Chat> chatList;
    ImageView copy, delete, forward, share, sendMessage;
    LinearLayout toolbar1;

    String senderName;
    private String senderImage;
    FirebaseAuth firebaseAuth;
    DatabaseReference reference;
    String requestCode = "FMA";
    private String secondaryRequestCode = "favourite";
    int downloadId;

    public FavouriteMessagesAdapter(Context context, List<Chat> chatList, ImageView copy, ImageView delete, ImageView forward, ImageView share, ImageView sendMessage, LinearLayout toolbar1) {
        this.context = context;
        this.chatList = chatList;
        this.copy = copy;
        this.delete = delete;
        this.forward = forward;
        this.share = share;
        this.sendMessage = sendMessage;
        this.toolbar1 = toolbar1;

        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_favourite_messages, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        Chat chat = chatList.get(position);

        String senderId = chat.getSender();
        String receiverId = chat.getReceiver();
        String time = chat.getTime();
        String message = chat.getMessage();
        String type = chat.getType();
        String messageSeenOrNot = chat.getMessageSeenOrNot();
        String secondaryMessage = chat.getSecondaryMessage();
        String fileName = chat.getFileName();
        String groupIdForFavourite = chat.getGroupIdForFavourite();

        //convert timestamp to proper time date
        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(time));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        Query query = reference.orderByChild("uid").equalTo(senderId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required ifo is received
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get receiver data
                    senderName = "" + ds.child("name").getValue();
                    senderImage = "" + ds.child("image").getValue();
                }


                if (senderImage.equals("")) {
                    holder.profileImage.setVisibility(View.INVISIBLE);
                    holder.profileText.setVisibility(View.VISIBLE);
                    String firstLetter = String.valueOf(senderName.charAt(0)).toLowerCase();

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
                    //set first letter of name
                    holder.profileText.setText(firstLetter);

                } else {
                    holder.profileImage.setVisibility(View.VISIBLE);
                    holder.profileText.setVisibility(View.GONE);
                    Picasso.get().load(senderImage).into(holder.profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Query query1 = reference.orderByChild("uid").equalTo(receiverId);
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check untile required ifo is received
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get receiver data
                    String receiverName = "" + ds.child("name").getValue();

                    //set data
                    if (groupIdForFavourite == null) {
                        if (senderId.equals(firebaseAuth.getUid())) {
                            holder.senderReceiverName.setText(context.getString(R.string.conscience) + "  ☞  " + receiverName);
                        } else {
                            holder.senderReceiverName.setText(senderName + "  ☞  " + context.getString(R.string.conscience));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Groups");

        reference1.orderByChild("groupId").equalTo(groupIdForFavourite).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //set data
                    String groupTitle = "" + ds.child("groupTitle").getValue();

                    if (groupIdForFavourite != null) {
                        if (senderId.equals(firebaseAuth.getUid())) {
                            //➠ ➳ ➸ ➻ ☛ ☞ ➢ ➤
                            holder.senderReceiverName.setText(context.getString(R.string.conscience) + "  ☞  " + groupTitle);
                        } else {
                            holder.senderReceiverName.setText(senderName + "  ☞  " + groupTitle);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        if (type.equals("text")) {
            // for favourite text layout
            holder.imageMessageLayout.setVisibility(View.GONE);
            holder.videoMessageLayout.setVisibility(View.GONE);
            holder.audioMessageLayout.setVisibility(View.GONE);
            holder.pdfMessageLayout.setVisibility(View.GONE);
            holder.imageTextMessageLayout.setVisibility(View.GONE);
            holder.videoTextMessageLayout.setVisibility(View.GONE);
            holder.textMessageLayout.setVisibility(View.VISIBLE);

            //set data
            holder.timeForText.setText(dateTime);
            holder.textMessage.setText(message);

            if (groupIdForFavourite == null) {
                if (messageSeenOrNot.equals("true")) {
                    holder.messageSeenOrNot.setImageResource(R.drawable.eye_opened);
                } else {
                    holder.messageSeenOrNot.setImageResource(R.drawable.eye_closed);
                }
            } else {
                holder.messageSeenOrNot.setVisibility(View.GONE);
            }

            holder.cardViewLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeText(position);

                    return true;
                }
            });

        } else if (type.equals("image") || type.equals("forwardedImage")) {
            // for favourite image layout
            holder.textMessageLayout.setVisibility(View.GONE);
            holder.videoMessageLayout.setVisibility(View.GONE);
            holder.audioMessageLayout.setVisibility(View.GONE);
            holder.pdfMessageLayout.setVisibility(View.GONE);
            holder.videoTextMessageLayout.setVisibility(View.GONE);
            holder.imageMessageLayout.setVisibility(View.VISIBLE);
            holder.imageTextMessageLayout.setVisibility(View.GONE);

            //set data
            holder.timeForImage.setText(dateTime);
            Glide.with(context)
                    .load(message)
                    .placeholder(R.drawable.progress_animation)
                    .into(holder.imageMessage);

            if (groupIdForFavourite == null) {
                if (messageSeenOrNot.equals("true")) {
                    holder.imageSeenOrNot.setImageResource(R.drawable.eye_opened);
                } else {
                    holder.imageSeenOrNot.setImageResource(R.drawable.eye_closed);
                }
            } else {
                holder.messageSeenOrNot.setVisibility(View.GONE);
            }

            holder.imageMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ImageViewPage.class);
                    intent.putExtra("imageUrl", message);
                    intent.putExtra("imageFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.imageMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeImage(position, holder);

                    return true;
                }
            });

            //for download image
            holder.downloadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, "At a time you can download only one file OR cancel the downloading file then download this file", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, "At a time you can download only one file OR cancel the downloading file then download this file", Toast.LENGTH_LONG).show();
                        return;
                    }

                    downloadFiles(message, fileName, "BooPraChat/BooPraChat Images", holder, dateTime, type);
                }
            });

            holder.pauseResumeForImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        PRDownloader.pause(downloadId);

                    } else if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        PRDownloader.resume(downloadId);

                    }
                }
            });

            holder.pauseResumeForImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PRDownloader.cancel(downloadId);
                    return true;
                }
            });

            /*checking the image that user already downloaded or not
             * if the user already downloaded that image, the download symbol will not be shown
             * if the user not downloaded that image, the download symbol will be shown*/
            File file = Environment.getExternalStorageDirectory();
            File myDir = new File(file, "BooPraChat/BooPraChat Images");
            if (myDir.exists()) {
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        String name = f.getName();
                        if (name.equals(fileName)) {
                            holder.imageDownloadLayout.setVisibility(View.GONE);
                        } else {
                            holder.imageDownloadLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

        } else if (type.equals("imageAndText") || type.equals("forwardedImageAndText")) {
            // for favourite image layout
            holder.textMessageLayout.setVisibility(View.GONE);
            holder.imageMessageLayout.setVisibility(View.GONE);
            holder.videoMessageLayout.setVisibility(View.GONE);
            holder.audioMessageLayout.setVisibility(View.GONE);
            holder.pdfMessageLayout.setVisibility(View.GONE);
            holder.videoTextMessageLayout.setVisibility(View.GONE);
            holder.imageTextMessageLayout.setVisibility(View.VISIBLE);

            //set data
            holder.timeForImageText.setText(dateTime);
            holder.imageText.setText(secondaryMessage);

            Glide.with(context).load(message).placeholder(R.drawable.progress_animation).into(holder.imageTextMessage);

            if (groupIdForFavourite == null) {
                if (messageSeenOrNot.equals("true")) {
                    holder.imageTextSeenOrNot.setImageResource(R.drawable.eye_opened);
                } else {
                    holder.imageTextSeenOrNot.setImageResource(R.drawable.eye_closed);
                }
            } else {
                holder.messageSeenOrNot.setVisibility(View.GONE);
            }

            holder.imageTextMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ImageViewPage.class);
                    intent.putExtra("imageUrl", message);
                    intent.putExtra("imageFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.imageTextMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeImageAndText(chat, position, holder);

                    return true;
                }
            });

            holder.imageText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeImageAndText(chat, position, holder);

                    return true;
                }
            });

            //for download image
            holder.downloadImageText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, "At a time you can download only one file OR cancel the downloading file then download this file", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, "At a time you can download only one file OR cancel the downloading file then download this file", Toast.LENGTH_LONG).show();
                        return;
                    }

                    downloadFiles(message, fileName, "BooPraChat/BooPraChat Images", holder, dateTime, type);
                }
            });

            holder.pauseResumeForImageText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        PRDownloader.pause(downloadId);

                    } else if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        PRDownloader.resume(downloadId);
                    }
                }
            });

            holder.pauseResumeForImageText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PRDownloader.cancel(downloadId);
                    return true;
                }
            });

            /*checking the image that user already downloaded or not
             * if the user already downloaded that image, the download symbol will not be shown
             * if the user not downloaded that image, the download symbol will be shown*/
            File file = Environment.getExternalStorageDirectory();
            File myDir = new File(file, "BooPraChat/BooPraChat Images");
            if (myDir.exists()) {
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        String name = f.getName();

                        if (name.equals(fileName)) {
                            holder.imageTextDownloadLayout.setVisibility(View.GONE);
                        } else {
                            holder.imageTextDownloadLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

        } else if (type.equals("video") || type.equals("forwardedVideo")) {
            // for favourite image layout
            holder.textMessageLayout.setVisibility(View.GONE);
            holder.imageMessageLayout.setVisibility(View.GONE);
            holder.audioMessageLayout.setVisibility(View.GONE);
            holder.pdfMessageLayout.setVisibility(View.GONE);
            holder.imageTextMessageLayout.setVisibility(View.GONE);
            holder.videoTextMessageLayout.setVisibility(View.GONE);
            holder.videoMessageLayout.setVisibility(View.VISIBLE);

            //set data
            holder.timeForVideo.setText(dateTime);

            holder.videoMessage.setVideoURI(Uri.parse(message));

            holder.videoMessage.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    holder.preloadProgressBarForVideo.setVisibility(View.GONE);
                    holder.videoMessage.seekTo(2000);
                    String totalDuration = milliSecondsToTimer(holder.videoMessage.getDuration());
                    holder.timeDurationForVideo.setText(totalDuration);
                }
            });

            if (groupIdForFavourite == null) {
                if (messageSeenOrNot.equals("true")) {
                    holder.videoSeenOrNot.setImageResource(R.drawable.eye_opened);
                } else {
                    holder.videoSeenOrNot.setImageResource(R.drawable.eye_closed);
                }
            } else {
                holder.messageSeenOrNot.setVisibility(View.GONE);
            }

            holder.videoMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, VideoViewPage.class);
                    intent.putExtra("videoUrl", message);
                    intent.putExtra("videoFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.videoMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeVideo(position);

                    return true;
                }
            });

            holder.videoPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, VideoViewPage.class);
                    intent.putExtra("videoUrl", message);
                    intent.putExtra("videoFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            //for download video file
            holder.downloadVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, "At a time you can download only one file OR cancel the downloading file then download this file", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, "At a time you can download only one file OR cancel the downloading file then download this file", Toast.LENGTH_LONG).show();
                        return;
                    }

                    downloadFiles(message, fileName, "BooPraChat/BooPraChat Video", holder, dateTime, type);
                }
            });

            holder.pauseResumeForVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        PRDownloader.pause(downloadId);
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        PRDownloader.resume(downloadId);
                    }
                }
            });

            holder.pauseResumeForVideo.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PRDownloader.cancel(downloadId);
                    return true;
                }
            });

            File file = Environment.getExternalStorageDirectory();
            File myDir = new File(file, "BooPraChat/BooPraChat Video");
            if (myDir.exists()) {
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        String name = f.getName();

                        if (name.equals(fileName)) {
                            holder.videoDownloadLayout.setVisibility(View.GONE);
                        } else {
                            holder.videoDownloadLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

        } else if (type.equals("videoAndText") || type.equals("forwardedVideoAndText")) {
            // for favourite image layout
            holder.textMessageLayout.setVisibility(View.GONE);
            holder.imageMessageLayout.setVisibility(View.GONE);
            holder.imageTextMessageLayout.setVisibility(View.GONE);
            holder.videoMessageLayout.setVisibility(View.GONE);
            holder.pdfMessageLayout.setVisibility(View.GONE);
            holder.audioMessageLayout.setVisibility(View.GONE);
            holder.videoTextMessageLayout.setVisibility(View.VISIBLE);

            //set data
            holder.timeForVideoText.setText(dateTime);
            holder.videoText.setText(secondaryMessage);

            holder.videoTextMessage.setVideoURI(Uri.parse(message));

            holder.videoTextMessage.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    holder.preloadProgressBarForVideoText.setVisibility(View.GONE);
                    holder.videoTextMessage.seekTo(2000);
                    String totalDuration = milliSecondsToTimer(holder.videoTextMessage.getDuration());
                    holder.timeDurationForVideoText.setText(totalDuration);
                }
            });

            if (groupIdForFavourite == null) {
                if (messageSeenOrNot.equals("true")) {
                    holder.videoTextSeenOrNot.setImageResource(R.drawable.eye_opened);
                } else {
                    holder.videoTextSeenOrNot.setImageResource(R.drawable.eye_closed);
                }
            } else {
                holder.messageSeenOrNot.setVisibility(View.GONE);
            }

            holder.videoTextMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, VideoViewPage.class);
                    intent.putExtra("videoUrl", message);
                    intent.putExtra("videoFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.videoTextMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeVideoAndText(position);

                    return true;
                }
            });

            holder.videoText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeVideoAndText(position);

                    return true;
                }
            });

            holder.videoTextPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, VideoViewPage.class);
                    intent.putExtra("videoUrl", message);
                    intent.putExtra("videoFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            //for download video file
            holder.downloadVideoText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, "At a time you can download only one file OR cancel the downloading file then download this file", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, "At a time you can download only one file OR cancel the downloading file then download this file", Toast.LENGTH_LONG).show();
                        return;
                    }

                    downloadFiles(message, fileName, "BooPraChat/BooPraChat Video", holder, dateTime, type);
                }
            });

            holder.pauseResumeForVideoText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        PRDownloader.pause(downloadId);
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        PRDownloader.resume(downloadId);
                    }
                }
            });

            holder.pauseResumeForVideoText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PRDownloader.cancel(downloadId);
                    return true;
                }
            });

            File file = Environment.getExternalStorageDirectory();
            File myDir = new File(file, "BooPraChat/BooPraChat Video");
            if (myDir.exists()) {
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        String name = f.getName();

                        if (name.equals(fileName)) {
                            holder.videoTextDownloadLayout.setVisibility(View.GONE);
                        } else {
                            holder.videoTextDownloadLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

        } else if (type.equals("audio") || type.equals("forwardedAudio")) {
            // for favourite image layout
            holder.textMessageLayout.setVisibility(View.GONE);
            holder.imageMessageLayout.setVisibility(View.GONE);
            holder.imageTextMessageLayout.setVisibility(View.GONE);
            holder.pdfMessageLayout.setVisibility(View.GONE);
            holder.videoMessageLayout.setVisibility(View.GONE);
            holder.videoTextMessageLayout.setVisibility(View.GONE);
            holder.audioMessageLayout.setVisibility(View.VISIBLE);

            //set data
            holder.timeForAudio.setText(dateTime);
            holder.audioFileName.setText(fileName);

            if (groupIdForFavourite == null) {
                if (messageSeenOrNot.equals("true")) {
                    holder.audioSeenOrNot.setImageResource(R.drawable.eye_opened);
                } else {
                    holder.audioSeenOrNot.setImageResource(R.drawable.eye_closed);
                }
            } else {
                holder.messageSeenOrNot.setVisibility(View.GONE);
            }

            holder.audioMessageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, AudioViewPage.class);
                    intent.putExtra("audioUrl", message);
                    intent.putExtra("audioFileName", fileName);
                    intent.putExtra("requestCode", "FMA"); //"FMA" means the Favourite Messages Adapter
                    context.startActivity(intent);
                }
            });

            holder.audioMessageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeAudio(position);

                    return true;
                }
            });

            //for download audio file
            holder.downloadAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, "At a time you can download only one file OR cancel the downloading file then download this file", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, "At a time you can download only one file OR cancel the downloading file then download this file", Toast.LENGTH_LONG).show();
                        return;
                    }

                    downloadFiles(message, fileName, "BooPraChat/BooPraChat Audio", holder, dateTime, type);
                }
            });

            holder.pauseResumeForAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        PRDownloader.pause(downloadId);
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        PRDownloader.resume(downloadId);
                    }

                }
            });

            holder.pauseResumeForAudio.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PRDownloader.cancel(downloadId);
                    return true;
                }
            });

            /*checking the image that user already downloaded or not
             * if the user already downloaded that image, the download symbol will not be shown
             * if the user not downloaded that image, the download symbol will be shown*/
            File file = Environment.getExternalStorageDirectory();
            File myDir = new File(file, "BooPraChat/BooPraChat Audio");
            if (myDir.exists()) {
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        String name = f.getName();

                        if (name.equals(fileName)) {
                            holder.audioDownloadLayout.setVisibility(View.GONE);
                        } else {
                            holder.audioDownloadLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }


        } else if (type.equals("pdf") || type.equals("forwardedPdf")) {
            // for favourite pdf layout
            holder.textMessageLayout.setVisibility(View.GONE);
            holder.imageMessageLayout.setVisibility(View.GONE);
            holder.imageTextMessageLayout.setVisibility(View.GONE);
            holder.videoMessageLayout.setVisibility(View.GONE);
            holder.videoTextMessageLayout.setVisibility(View.GONE);
            holder.audioMessageLayout.setVisibility(View.GONE);
            holder.pdfMessageLayout.setVisibility(View.VISIBLE);

            //set data
            holder.timeForPdf.setText(dateTime);
            holder.pdfFileName.setText(fileName);

            if (groupIdForFavourite == null) {
                if (messageSeenOrNot.equals("true")) {
                    holder.pdfSeenOrNot.setImageResource(R.drawable.eye_opened);
                } else {
                    holder.pdfSeenOrNot.setImageResource(R.drawable.eye_opened);
                }
            } else {
                holder.messageSeenOrNot.setVisibility(View.GONE);
            }

            holder.pdfMessageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PdfViewerPage.class);
                    intent.putExtra("pdfUrl", message);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.pdfMessageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typePdf(position);

                    return true;
                }
            });

            //for download pdf file
            holder.downloadPdf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, "At a time you can download only one file OR cancel the downloading file then download this file", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, "At a time you can download only one file OR cancel the downloading file then download this file", Toast.LENGTH_LONG).show();
                        return;
                    }

                    downloadFiles(message, fileName, "BooPraChat/BooPraChat Pdf", holder, dateTime, type);
                }
            });

            holder.pauseResumeForPdf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        PRDownloader.pause(downloadId);
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        PRDownloader.resume(downloadId);
                    }

                }
            });

            holder.pauseResumeForPdf.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PRDownloader.cancel(downloadId);
                    return true;
                }
            });

            /*checking the image that user already downloaded or not
             * if the user already downloaded that image, the download symbol will not be shown
             * if the user not downloaded that image, the download symbol will be shown*/
            File file = Environment.getExternalStorageDirectory();
            File myDir = new File(file, "BooPraChat/BooPraChat Pdf");
            if (myDir.exists()) {
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        String name = f.getName();

                        if (name.equals(fileName)) {
                            holder.pdfDownloadLayout.setVisibility(View.GONE);
                        } else {
                            holder.pdfDownloadLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }


        }

    }

    private String milliSecondsToTimer(long milliSeconds) {
        String timerString = "";
        String secondsString;

        int hours = (int) (milliSeconds / (1000 * 60 * 60));
        int minutes = (int) (milliSeconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            timerString = hours + ":";
        }
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        timerString = timerString + minutes + ":" + secondsString;

        return timerString;
    }

    private void downloadFiles(String message, String fileName, String fileDestination, MyHolder holder, String dateTime, String fileType) {

        File file = Environment.getExternalStoragePublicDirectory(fileDestination);

        downloadId = PRDownloader.download(message, file.getPath(), fileName)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                        if (fileType.equals("image") || fileType.equals("forwardedImage")) {
                            holder.downloadImage.setVisibility(View.GONE);
                            holder.pauseResumeForImage.setVisibility(View.VISIBLE);
                            holder.pauseResumeForImage.setImageResource(R.drawable.pause_downloading);

                        } else if (fileType.equals("imageAndText") || fileType.equals("forwardedImageAndText")) {
                            holder.downloadImageText.setVisibility(View.GONE);
                            holder.pauseResumeForImageText.setVisibility(View.VISIBLE);
                            holder.pauseResumeForImageText.setImageResource(R.drawable.pause_downloading);

                        } else if (fileType.equals("pdf") || fileType.equals("forwardedPdf")) {
                            holder.downloadPdf.setVisibility(View.GONE);
                            holder.pauseResumeForPdf.setVisibility(View.VISIBLE);
                            holder.pauseResumeForPdf.setImageResource(R.drawable.pause_downloading);

                        } else if (fileType.equals("audio") || fileType.equals("forwardedAudio")) {
                            holder.downloadAudio.setVisibility(View.GONE);
                            holder.pauseResumeForAudio.setVisibility(View.VISIBLE);
                            holder.pauseResumeForAudio.setImageResource(R.drawable.pause_downloading);

                        } else if (fileType.equals("video") || fileType.equals("forwardedVideo")) {
                            holder.downloadVideo.setVisibility(View.GONE);
                            holder.pauseResumeForVideo.setVisibility(View.VISIBLE);
                            holder.pauseResumeForVideo.setImageResource(R.drawable.pause_downloading);

                        } else if (fileType.equals("videoAndText") || fileType.equals("forwardedVideoAndText")) {
                            holder.downloadVideoText.setVisibility(View.GONE);
                            holder.pauseResumeForVideoText.setVisibility(View.VISIBLE);
                            holder.pauseResumeForVideoText.setImageResource(R.drawable.pause_downloading);
                        }
                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                        if (fileType.equals("image") || fileType.equals("forwardedImage")) {
                            holder.pauseResumeForImage.setImageResource(R.drawable.resume_downloading);

                        } else if (fileType.equals("imageAndText") || fileType.equals("forwardedImageAndText")) {
                            holder.pauseResumeForImageText.setImageResource(R.drawable.resume_downloading);

                        } else if (fileType.equals("pdf") || fileType.equals("forwardedPdf")) {
                            holder.pauseResumeForPdf.setImageResource(R.drawable.resume_downloading);

                        } else if (fileType.equals("audio") || fileType.equals("forwardedAudio")) {
                            holder.pauseResumeForAudio.setImageResource(R.drawable.resume_downloading);

                        } else if (fileType.equals("video") || fileType.equals("forwardedVideo")) {
                            holder.pauseResumeForVideo.setImageResource(R.drawable.resume_downloading);

                        } else if (fileType.equals("videoAndText") || fileType.equals("forwardedVideoAndText")) {
                            holder.pauseResumeForVideoText.setImageResource(R.drawable.resume_downloading);
                        }
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        if (fileType.equals("image") || fileType.equals("forwardedImage")) {
                            holder.pauseResumeForImage.setVisibility(View.GONE);
                            holder.downloadImage.setVisibility(View.VISIBLE);
                            holder.imageDownloadingLevelIndicatingBar.setProgress(100);
                            holder.timeForImage.setText(dateTime);

                        } else if (fileType.equals("imageAndText") || fileType.equals("forwardedImageAndText")) {
                            holder.pauseResumeForImageText.setVisibility(View.GONE);
                            holder.downloadImageText.setVisibility(View.VISIBLE);
                            holder.imageTextDownloadingLevelIndicatingBar.setProgress(100);
                            holder.timeForImageText.setText(dateTime);

                        } else if (fileType.equals("pdf") || fileType.equals("forwardedPdf")) {
                            holder.pauseResumeForPdf.setVisibility(View.GONE);
                            holder.downloadPdf.setVisibility(View.VISIBLE);
                            holder.pdfDownloadingLevelIndicatingBar.setProgress(100);
                            holder.timeForPdf.setText(dateTime);

                        } else if (fileType.equals("audio") || fileType.equals("forwardedAudio")) {
                            holder.pauseResumeForAudio.setVisibility(View.GONE);
                            holder.downloadAudio.setVisibility(View.VISIBLE);
                            holder.audioDownloadingLevelIndicatingBar.setProgress(100);
                            holder.timeForAudio.setText(dateTime);

                        } else if (fileType.equals("video") || fileType.equals("forwardedVideo")) {
                            holder.pauseResumeForVideo.setVisibility(View.GONE);
                            holder.downloadVideo.setVisibility(View.VISIBLE);
                            holder.videoDownloadingLevelIndicatingBar.setProgress(100);
                            holder.timeForVideo.setText(dateTime);

                        } else if (fileType.equals("videoAndText") || fileType.equals("forwardedVideoAndText")) {
                            holder.pauseResumeForVideoText.setVisibility(View.GONE);
                            holder.downloadVideoText.setVisibility(View.VISIBLE);
                            holder.videoTextDownloadingLevelIndicatingBar.setProgress(100);
                            holder.timeForVideoText.setText(dateTime);
                        }

                        Toast.makeText(context, "Download canceled", Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long percentage = progress.currentBytes * 100 / progress.totalBytes;

                        if (fileType.equals("image") || fileType.equals("forwardedImage")) {
                            holder.imageDownloadingLevelIndicatingBar.setProgress((int) percentage);
                            holder.timeForImage.setText(percentage + "%");

                        } else if (fileType.equals("imageAndText") || fileType.equals("forwardedImageAndText")) {
                            holder.imageTextDownloadingLevelIndicatingBar.setProgress((int) percentage);
                            holder.timeForImageText.setText(percentage + "%");

                        } else if (fileType.equals("pdf") || fileType.equals("forwardedPdf")) {
                            holder.pdfDownloadingLevelIndicatingBar.setProgress((int) percentage);
                            holder.timeForPdf.setText(percentage + "%");

                        } else if (fileType.equals("audio") || fileType.equals("forwardedAudio")) {
                            holder.audioDownloadingLevelIndicatingBar.setProgress((int) percentage);
                            holder.timeForAudio.setText(percentage + "%");

                        } else if (fileType.equals("video") || fileType.equals("forwardedVideo")) {
                            holder.videoDownloadingLevelIndicatingBar.setProgress((int) percentage);
                            holder.timeForVideo.setText(percentage + "%");

                        } else if (fileType.equals("videoAndText") || fileType.equals("forwardedVideoAndText")) {
                            holder.videoTextDownloadingLevelIndicatingBar.setProgress((int) percentage);
                            holder.timeForVideoText.setText(percentage + "%");
                        }
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {

                        if (fileType.equals("image") || fileType.equals("forwardedImage")) {
                            holder.timeForImage.setText(dateTime);
                            holder.pauseResumeForImage.setImageResource(R.drawable.downloading_completed);

                        } else if (fileType.equals("imageAndText") || fileType.equals("forwardedImageAndText")) {
                            holder.timeForImageText.setText(dateTime);
                            holder.pauseResumeForImageText.setImageResource(R.drawable.downloading_completed);

                        } else if (fileType.equals("pdf") || fileType.equals("forwardedPdf")) {
                            holder.timeForPdf.setText(dateTime);
                            holder.pauseResumeForPdf.setImageResource(R.drawable.downloading_completed);

                        } else if (fileType.equals("audio") || fileType.equals("forwardedAudio")) {
                            holder.timeForAudio.setText(dateTime);
                            holder.pauseResumeForAudio.setImageResource(R.drawable.downloading_completed);

                        } else if (fileType.equals("video") || fileType.equals("forwardedVideo")) {
                            holder.timeForVideo.setText(dateTime);
                            holder.pauseResumeForVideo.setImageResource(R.drawable.downloading_completed);

                        } else if (fileType.equals("videoAndText") || fileType.equals("forwardedVideoAndText")) {
                            holder.timeForVideoText.setText(dateTime);
                            holder.pauseResumeForVideoText.setImageResource(R.drawable.downloading_completed);
                        }

                    }

                    @Override
                    public void onError(Error e) {
                        Toast.makeText(context, R.string.something_went_wrong_please_try_again_later, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void typeVideoAndText(int position) {

        toolbar1.setVisibility(View.VISIBLE);

        String message = chatList.get(position).getMessage();
        String fileName = chatList.get(position).getFileName();
        String secondaryMessage = chatList.get(position).getSecondaryMessage();
        String receiverId = chatList.get(position).getReceiver();
        String senderId = chatList.get(position).getSender();
        String groupId = chatList.get(position).getGroupIdForFavourite();

        copy.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        sendMessage.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);

        // on click to copy selected text
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = secondaryMessage;

                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("String", text);
                clipboardManager.setPrimaryClip(clipData);
                clipData.getDescription();
                //give toast message
                Toast.makeText(context, R.string.text_copied, Toast.LENGTH_SHORT).show();

                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to delete selected message
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeVideoAudioFromFavourite(message);
                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to share selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = secondaryMessage;
                String videoURI = message;
                String requestCode = "VT"; //VT means for video text

                Intent intent = new Intent(context, SendToActivity.class);
                intent.putExtra("text", text);
                intent.putExtra("videoUri", videoURI);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("fileName", fileName);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                context.startActivity(intent);

                toolbar1.setVisibility(View.GONE);
            }
        });

        //send personal message to that user
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupId == null) {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", receiverId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                } else {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, GroupMessageActivity.class);
                        intent.putExtra("groupId", groupId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                }
                toolbar1.setVisibility(View.GONE);
            }
        });
    }

    private void typeVideo(int position) {

        toolbar1.setVisibility(View.VISIBLE);

        String message = chatList.get(position).getMessage();
        String fileName = chatList.get(position).getFileName();
        String receiverId = chatList.get(position).getReceiver();
        String senderId = chatList.get(position).getSender();
        String groupId = chatList.get(position).getGroupIdForFavourite();

        copy.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        sendMessage.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);

        //on click to delete selected message
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                removeVideoAudioFromFavourite(message);
                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to share selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String videoUri = chatList.get(position).getMessage();
                String requestCode = "v";

                Intent intent = new Intent(context, SendToActivity.class);
                intent.putExtra("videoUri", videoUri);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("fileName", fileName);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                context.startActivity(intent);

                toolbar1.setVisibility(View.GONE);
            }
        });

        //send personal message to that user
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupId == null) {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", receiverId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                } else {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, GroupMessageActivity.class);
                        intent.putExtra("groupId", groupId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                }
                toolbar1.setVisibility(View.GONE);
            }
        });
    }

    private void typeAudio(int position) {

        toolbar1.setVisibility(View.VISIBLE);

        String message = chatList.get(position).getMessage();
        String fileName = chatList.get(position).getFileName();
        String receiverId = chatList.get(position).getReceiver();
        String senderId = chatList.get(position).getSender();
        String groupId = chatList.get(position).getGroupIdForFavourite();

        copy.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        sendMessage.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);

        //on click to delete selected message
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                removeVideoAudioFromFavourite(message);
                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to share selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String audioUri = chatList.get(position).getMessage();
                String requestCode = "a";

                Intent intent = new Intent(context, SendToActivity.class);
                intent.putExtra("audioUri", audioUri);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("fileName", fileName);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                context.startActivity(intent);
                //((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        //send personal message to that user
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupId == null) {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", receiverId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                } else {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, GroupMessageActivity.class);
                        intent.putExtra("groupId", groupId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                }
                toolbar1.setVisibility(View.GONE);
            }
        });
    }

    private void typePdf(int position) {

        toolbar1.setVisibility(View.VISIBLE);

        String message = chatList.get(position).getMessage();
        String fileName = chatList.get(position).getFileName();
        String receiverId = chatList.get(position).getReceiver();
        String senderId = chatList.get(position).getSender();
        String groupId = chatList.get(position).getGroupIdForFavourite();

        copy.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        sendMessage.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);

        //on click to delete selected message
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("PdfMessages").orderByChild("message").equalTo(message);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().removeValue();
                            Toast.makeText(context, R.string.removed_from_favourite, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to share selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pdfUri = chatList.get(position).getMessage();
                String requestCode = "p";

                Intent intent = new Intent(context, SendToActivity.class);
                intent.putExtra("pdfUri", pdfUri);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("fileName", fileName);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                context.startActivity(intent);
                //((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        //send personal message to that user
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupId == null) {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", receiverId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                } else {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, GroupMessageActivity.class);
                        intent.putExtra("groupId", groupId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                }
                toolbar1.setVisibility(View.GONE);
            }
        });
    }

    private void typeImage(int position, MyHolder holder) {

        toolbar1.setVisibility(View.VISIBLE);

        String message = chatList.get(position).getMessage();
        String fileName = chatList.get(position).getFileName();
        String receiverId = chatList.get(position).getReceiver();
        String senderId = chatList.get(position).getSender();
        String groupId = chatList.get(position).getGroupIdForFavourite();

        copy.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        sendMessage.setVisibility(View.VISIBLE);
        share.setVisibility(View.VISIBLE);

        //on click to delete selected message
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeImageFromFavourite(message);
                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to share selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String imageURI = chatList.get(position).getMessage();
                String text = "";
                String requestCode = "i";

                Intent intent = new Intent(context, SendToActivity.class);
                intent.putExtra("imageUri", imageURI);
                intent.putExtra("text", text);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("fileName", fileName);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                context.startActivity(intent);
                //((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to share selected message to other apps
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.imageMessage.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                shareImage(bitmap);

                toolbar1.setVisibility(View.GONE);
            }
        });

        //send personal message to that user
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupId == null) {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", receiverId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                } else {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, GroupMessageActivity.class);
                        intent.putExtra("groupId", groupId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                }
                toolbar1.setVisibility(View.GONE);
            }
        });
    }

    private void typeImageAndText(Chat chat, int position, MyHolder holder) {

        toolbar1.setVisibility(View.VISIBLE);

        String fileName = chat.getFileName();
        String message = chat.getMessage();
        String receiverId = chat.getReceiver();
        String senderId = chat.getSender();
        String groupId = chat.getGroupIdForFavourite();

        copy.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.VISIBLE);
        sendMessage.setVisibility(View.VISIBLE);

        // on click to copy selected text
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = chatList.get(position).getSecondaryMessage();

                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("String", text);
                clipboardManager.setPrimaryClip(clipData);
                clipData.getDescription();
                //give toast message
                Toast.makeText(context, R.string.text_copied, Toast.LENGTH_SHORT).show();

                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to delete selected message
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeImageFromFavourite(message);
                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to share selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = chatList.get(position).getSecondaryMessage();
                String imageURI = chatList.get(position).getMessage();
                String requestCode = "it";

                Intent intent = new Intent(context, SendToActivity.class);
                intent.putExtra("text", text);
                intent.putExtra("imageUri", imageURI);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("fileName", fileName);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                context.startActivity(intent);
                //((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to share selected message to other apps
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = chatList.get(position).getSecondaryMessage();

                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.imageTextMessage.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                shareImageWithText(bitmap, text);

                toolbar1.setVisibility(View.GONE);
            }
        });

        //send personal message to that user
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupId == null) {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", receiverId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                } else {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, GroupMessageActivity.class);
                        intent.putExtra("groupId", groupId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                }
                toolbar1.setVisibility(View.GONE);
            }
        });
    }

    private void typeText(int position) {

        toolbar1.setVisibility(View.VISIBLE);

        String message = chatList.get(position).getMessage();
        String receiverId = chatList.get(position).getReceiver();
        String senderId = chatList.get(position).getSender();
        String groupId = chatList.get(position).getGroupIdForFavourite();

        copy.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.VISIBLE);
        sendMessage.setVisibility(View.VISIBLE);

        //get message
        String text = chatList.get(position).getMessage();

        // on click to copy selected text
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("String", text);
                clipboardManager.setPrimaryClip(clipData);
                clipData.getDescription();

                //give toast message
                Toast.makeText(context, R.string.text_copied, Toast.LENGTH_SHORT).show();

                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to delete selected message
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("TextMessages").orderByChild("message").equalTo(message);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().removeValue();
                            Toast.makeText(context, R.string.removed_from_favourite, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to forward selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get message
                Intent intent = new Intent(context, SendToActivity.class);
                intent.putExtra("text", text);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                context.startActivity(intent);

                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to share selected message to other apps
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get message
                shareOnlyText(text);
                toolbar1.setVisibility(View.GONE);
            }
        });

        //send personal message to that user
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupId == null) {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", receiverId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                } else {
                    if (senderId.equals(firebaseAuth.getUid())) {
                        Intent intent = new Intent(context, GroupMessageActivity.class);
                        intent.putExtra("groupId", groupId);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("receiverId", senderId);
                        context.startActivity(intent);
                    }
                }
                toolbar1.setVisibility(View.GONE);
            }
        });
    }

    private void removeVideoAudioFromFavourite(String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("VideoMessages").orderByChild("message").equalTo(message);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();
                    Toast.makeText(context, R.string.removed_from_favourite, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removeImageFromFavourite(String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("ImageMessages").orderByChild("message").equalTo(message);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();
                    Toast.makeText(context, R.string.removed_from_favourite, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void shareImage(Bitmap bitmap) {
        // first we will save the image in cache and we will get that saved image
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, uri); //in case you share via an email app
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); //in case you share via an email app
        context.startActivity(Intent.createChooser(intent, "Share Via")); //message to show in share dialog

    }

    private void shareImageWithText(Bitmap bitmap, String text) {
        // first we will save the image in cache and we will get that saved image
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, uri); //in case you share via an email app
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); //in case you share via an email app
        context.startActivity(Intent.createChooser(intent, "Share Via")); //message to show in share dialog

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;

        try {
            imageFolder.mkdir();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            uri = FileProvider.getUriForFile(context, "com.example.booprachat.fileprovider", file);


        } catch (Exception e) {
            Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return uri;
    }

    private void shareOnlyText(String text) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); //in case you share via an email app
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, "Share Via")); //message to show in share dialog

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView profileText;
        TextView senderReceiverName;

        //for set message
        ImageView imageMessage, imageTextMessage;
        TextView textMessage, imageText, videoText;
        TextView audioFileName, pdfFileName;
        VideoView videoMessage, videoTextMessage;

        ImageView messageSeenOrNot,
                imageSeenOrNot,
                imageTextSeenOrNot,
                videoSeenOrNot,
                videoPlayButton,
                videoTextSeenOrNot,
                videoTextPlayButton,
                audioSeenOrNot,
                pdfSeenOrNot;

        ImageButton downloadImage,
                downloadImageText,
                downloadVideo,
                downloadVideoText,
                downloadAudio,
                downloadPdf;

        TextView timeForText,
                timeForImage,
                timeForImageText,
                timeForVideo,
                timeForVideoText,
                timeForAudio,
                timeForPdf;

        RelativeLayout textMessageLayout,
                imageMessageLayout,
                imageTextMessageLayout,
                videoMessageLayout,
                videoTextMessageLayout,
                audioMessageLayout,
                pdfMessageLayout;

        //cardview for selecting
        CardView cardViewLayout;

        //for indicating the downloading level
        ProgressBar imageDownloadingLevelIndicatingBar,
                imageTextDownloadingLevelIndicatingBar,
                pdfDownloadingLevelIndicatingBar,
                audioDownloadingLevelIndicatingBar,
                videoDownloadingLevelIndicatingBar,
                videoTextDownloadingLevelIndicatingBar;

        //for pause and resume the downloading files
        ImageButton pauseResumeForImage,
                pauseResumeForImageText,
                pauseResumeForPdf,
                pauseResumeForAudio,
                pauseResumeForVideo,
                pauseResumeForVideoText;

        //this layout for visible the download button
        RelativeLayout imageDownloadLayout,
                imageTextDownloadLayout,
                pdfDownloadLayout,
                audioDownloadLayout,
                videoDownloadLayout,
                videoTextDownloadLayout;

        //text view video duration
        TextView timeDurationForVideo, timeDurationForVideoText;

        //preload progress for fro videos
        ProgressBar preloadProgressBarForVideoText, preloadProgressBarForVideo;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profile_image);
            profileText = itemView.findViewById(R.id.profile_text);
            senderReceiverName = itemView.findViewById(R.id.sender_receiver_name);
            //  favourite = itemView.findViewById(R.id.favourite_layout);
            //for text
            textMessageLayout = itemView.findViewById(R.id.text_message_layout);
            textMessage = itemView.findViewById(R.id.favourite_message);
            timeForText = itemView.findViewById(R.id.time);
            messageSeenOrNot = itemView.findViewById(R.id.seen_delivered_for_text);

            //for image
            imageMessageLayout = itemView.findViewById(R.id.image_message_layout);
            timeForImage = itemView.findViewById(R.id.time_for_image);
            imageMessage = itemView.findViewById(R.id.favourite_image);
            imageSeenOrNot = itemView.findViewById(R.id.seen_delivered_for_image);
            downloadImage = itemView.findViewById(R.id.download_image);

            //for image and text
            imageTextMessageLayout = itemView.findViewById(R.id.image_and_text_message_layout);
            timeForImageText = itemView.findViewById(R.id.time_for_image_and_text);
            imageTextMessage = itemView.findViewById(R.id.favourite_image_text);
            imageText = itemView.findViewById(R.id.image_text);
            imageTextSeenOrNot = itemView.findViewById(R.id.seen_delivered_for_image_and_text);
            downloadImageText = itemView.findViewById(R.id.download_image_and_text);

            //for video
            videoMessageLayout = itemView.findViewById(R.id.video_message_layout);
            timeForVideo = itemView.findViewById(R.id.time_for_video);
            videoMessage = itemView.findViewById(R.id.favourite_video);
            videoSeenOrNot = itemView.findViewById(R.id.seen_delivered_for_video);
            downloadVideo = itemView.findViewById(R.id.download_video);
            videoPlayButton = itemView.findViewById(R.id.video_play_btn);

            //for video and text
            videoTextMessageLayout = itemView.findViewById(R.id.video_and_text_message_layout);
            timeForVideoText = itemView.findViewById(R.id.time_for_video_and_text);
            videoTextMessage = itemView.findViewById(R.id.favourite_video_and_text);
            videoText = itemView.findViewById(R.id.video_text);
            videoTextSeenOrNot = itemView.findViewById(R.id.seen_delivered_for_video_and_text);
            downloadVideoText = itemView.findViewById(R.id.download_video_and_text);
            videoTextPlayButton = itemView.findViewById(R.id.video_and_text_play_btn);

            //for audio
            audioMessageLayout = itemView.findViewById(R.id.audio_message_layout);
            timeForAudio = itemView.findViewById(R.id.time_for_audio);
            audioSeenOrNot = itemView.findViewById(R.id.seen_delivered_for_audio);
            downloadAudio = itemView.findViewById(R.id.download_audio);
            audioFileName = itemView.findViewById(R.id.audio_file_name);

            //for pdf
            pdfMessageLayout = itemView.findViewById(R.id.pdf_message_layout);
            timeForPdf = itemView.findViewById(R.id.time_for_pdf);
            pdfSeenOrNot = itemView.findViewById(R.id.seen_delivered_for_pdf);
            downloadPdf = itemView.findViewById(R.id.download_pdf);
            pdfFileName = itemView.findViewById(R.id.pdf_file_name);

            //cardview for selecting
            cardViewLayout = itemView.findViewById(R.id.favourite_card_view_layout);

            //for indicating the downloading level
            imageDownloadingLevelIndicatingBar = itemView.findViewById(R.id.image_downloading_circle_progress_bar);
            imageTextDownloadingLevelIndicatingBar = itemView.findViewById(R.id.image_and_text_downloading_circle_progress_bar);
            pdfDownloadingLevelIndicatingBar = itemView.findViewById(R.id.pdf_downloading_circle_progress_bar);
            audioDownloadingLevelIndicatingBar = itemView.findViewById(R.id.audio_downloading_circle_progress_bar);
            videoDownloadingLevelIndicatingBar = itemView.findViewById(R.id.video_downloading_circle_progress_bar);
            videoTextDownloadingLevelIndicatingBar = itemView.findViewById(R.id.video_and_text_downloading_circle_progress_bar);

            //for pause and resume the downloading files
            pauseResumeForImage = itemView.findViewById(R.id.pause_resume_for_image);
            pauseResumeForImageText = itemView.findViewById(R.id.pause_resume_for_image_and_text);
            pauseResumeForPdf = itemView.findViewById(R.id.pause_resume_for_pdf);
            pauseResumeForAudio = itemView.findViewById(R.id.pause_resume_for_audio);
            pauseResumeForVideo = itemView.findViewById(R.id.pause_resume_for_video);
            pauseResumeForVideoText = itemView.findViewById(R.id.pause_resume_for_video_and_text);

            //this layout for visible the download button
            imageDownloadLayout = itemView.findViewById(R.id.image_download_layout);
            imageTextDownloadLayout = itemView.findViewById(R.id.image_and_text_download_layout);
            pdfDownloadLayout = itemView.findViewById(R.id.pdf_download_layout);
            audioDownloadLayout = itemView.findViewById(R.id.audio_download_layout);
            videoDownloadLayout = itemView.findViewById(R.id.video_download_layout);
            videoTextDownloadLayout = itemView.findViewById(R.id.video_and_text_download_layout);

            //for video duration
            timeDurationForVideo = itemView.findViewById(R.id.video_duration);
            timeDurationForVideoText = itemView.findViewById(R.id.video_text_duration);

            //for before loading the video
            preloadProgressBarForVideoText = itemView.findViewById(R.id.preload_progress_bar_for_video_text);
            preloadProgressBarForVideo = itemView.findViewById(R.id.preload_progress_bar_for_video);
        }
    }
}
