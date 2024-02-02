package com.example.booprachat.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.downloader.Status;
import com.example.booprachat.ChatViews.AudioViewPage;
import com.example.booprachat.ChatViews.ImageViewPage;
import com.example.booprachat.ChatViews.PdfViewerPage;
import com.example.booprachat.ChatViews.VideoViewPage;
import com.example.booprachat.GroupChats.GroupMessageSeenByActivity;
import com.example.booprachat.Model.GroupChat;
import com.example.booprachat.ProfileViewActivity;
import com.example.booprachat.R;
import com.example.booprachat.ReceiveDataFromOtherApps.SendToGroupActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.HolderGroupChat> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<GroupChat> groupChats;
    private String groupId;
    private LinearLayout toolbar1;
    private LinearLayout toolbar2;
    private ImageView copy;
    private ImageView delete;
    private ImageView forward;
    private ImageView share;
    private ImageView favourite;
    private ImageView info;

    //firebase services
    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;

    private String favouriteText;
    String secondaryRequestCode = "GCA"; // to find the data is coming from GroupChatAdapter. "GCA" means GroupChatAdapter
    int downloadId;
    ArrayList<String> messageSeenCountList;

    public GroupChatAdapter(Context context, ArrayList<GroupChat> groupChats, String groupId, LinearLayout toolbar1, LinearLayout toolbar2, ImageView copy, ImageView delete, ImageView forward, ImageView share, ImageView favourite, ImageView info) {
        this.context = context;
        this.groupChats = groupChats;
        this.groupId = groupId;
        this.toolbar1 = toolbar1;
        this.toolbar2 = toolbar2;
        this.copy = copy;
        this.delete = delete;
        this.forward = forward;
        this.share = share;
        this.favourite = favourite;
        this.info = info;

        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        messageSeenCountList = new ArrayList<>();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.group_message_item_right, parent, false);
            return new HolderGroupChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.group_message_item_left, parent, false);
            return new HolderGroupChat(view);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {

        PRDownloaderConfig config = PRDownloaderConfig.newBuilder().setDatabaseEnabled(true).setReadTimeout(30_000).setConnectTimeout(30_000).build();
        PRDownloader.initialize(context, config);

        //get data
        GroupChat groupChat = groupChats.get(position);

        String message = groupChat.getMessage();
        String senderId = groupChat.getSender();
        String time = groupChat.getTime();
        String type = groupChat.getType();
        String secondaryMessage = groupChat.getSecondaryMessage();
        String fileName = groupChat.getFileName();

        String requestCode = "GCA"; // this request is sent to files view pages to download file and store to internal storage file name"BooPraGroupChat files". for one to one chat files name as "BooPraChat files"

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(time));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        toolbar2.setVisibility(View.GONE);
        toolbar1.setVisibility(View.GONE);

        //calling method
        loadMessageSeenCount(time, holder, position);
        storeMessageSeenMembers(time);
        setUsername(groupChat, holder);

        //set data
        if (type.equals("image") || type.equals("forwardedImage")) {  // if the message type is image
            holder.imageMessageLayout.setVisibility(View.VISIBLE); //image layout is visible
            holder.textMessageLayout.setVisibility(View.GONE); //text layout is not visible
            holder.leavedFromGroupLayout.setVisibility(View.GONE); //leaved from group layout is not visible
            holder.audioMessageLayout.setVisibility(View.GONE); //audio layout is not visible
            holder.videoMessageLayout.setVisibility(View.GONE); //video layout is not visible
            holder.pdfMessageLayout.setVisibility(View.GONE); //pdf layout is not visible
            holder.imageTextMessageLayout.setVisibility(View.GONE); //image text layout is not visible
            holder.videoTextMessageLayout.setVisibility(View.GONE); //video text layout is not visible

            // set image message
            Glide.with(context)
                    .load(message)
                    .placeholder(R.drawable.progress_animation)
                    .into(holder.showImage);

            holder.imageTime.setText(dateTime); // set image sended time and date

            getImageFileSize(holder, message);

            holder.showImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ImageViewPage.class);
                    intent.putExtra("imageUrl", message);
                    intent.putExtra("imageFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.showImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeImage(groupChat, position, holder);

                    return true;
                }
            });

            //for download image
            holder.downloadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                        return;
                    }

                    downloadFiles(message, fileName, "BooPraChat/BooPraGroupChat Images", holder, dateTime, type);
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

            if (!senderId.equals(firebaseAuth.getUid())) {
                /*checking the image, that user already downloaded or not
                 * if the user already downloaded that image, the download symbol will not be shown
                 * if the user not downloaded that image, the download symbol will be shown*/
                File file = Environment.getExternalStorageDirectory();
                File myDir = new File(file, "BooPraChat/BooPraGroupChat Images");
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
            } else {
                holder.imageDownloadLayout.setVisibility(View.GONE);
            }

        } else if (type.equals("leavedFromGroup")) {   //if the message type is image

            holder.imageMessageLayout.setVisibility(View.GONE); //image layout is not visible
            holder.textMessageLayout.setVisibility(View.GONE); //text layout is visible
            holder.leavedFromGroupLayout.setVisibility(View.VISIBLE); //leaved from group layout is visible
            holder.audioMessageLayout.setVisibility(View.GONE); //audio layout is not visible
            holder.videoMessageLayout.setVisibility(View.GONE); //video layout is not visible
            holder.pdfMessageLayout.setVisibility(View.GONE); //pdf layout is not visible
            holder.imageTextMessageLayout.setVisibility(View.GONE); //image text layout is not visible
            holder.videoTextMessageLayout.setVisibility(View.GONE); //video text layout is not visible

            holder.imageTime.setText(dateTime); // set text sended time and date

        } else if (type.equals("audio") || type.equals("forwardedAudio")) {
            holder.imageMessageLayout.setVisibility(View.GONE); //image layout is not visible
            holder.textMessageLayout.setVisibility(View.GONE); //text layout is visible
            holder.leavedFromGroupLayout.setVisibility(View.GONE); //leaved from group layout is not visible
            holder.audioMessageLayout.setVisibility(View.VISIBLE); //audio layout is visible
            holder.videoMessageLayout.setVisibility(View.GONE); //video layout is not visible
            holder.pdfMessageLayout.setVisibility(View.GONE); //pdf layout is not visible
            holder.imageTextMessageLayout.setVisibility(View.GONE); //image text layout is not visible
            holder.videoTextMessageLayout.setVisibility(View.GONE); //video text layout is not visible

            holder.audioTime.setText(dateTime); // set text sended time and date
            holder.musicFileName.setText(fileName);

            getAudioFileSize(holder, message);

            holder.audioMessageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, AudioViewPage.class);
                    intent.putExtra("audioUrl", message);
                    intent.putExtra("audioFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.audioMessageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeAudio(groupChat, position);

                    return true;
                }
            });

            //for download audio file
            holder.downloadAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                        return;
                    }

                    downloadFiles(message, fileName, "BooPraChat/BooPraGroupChat Audio", holder, dateTime, type);
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

            if (!senderId.equals(firebaseAuth.getUid())) {
                /*checking the audio that user already downloaded or not
                 * if the user already downloaded that audio, the download symbol will not be shown
                 * if the user not downloaded that audio, the download symbol will be shown*/
                File file = Environment.getExternalStorageDirectory();
                File myDir = new File(file, "BooPraChat/BooPraGroupChat Audio");
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
            } else {
                holder.audioDownloadLayout.setVisibility(View.GONE);
            }

        } else if (type.equals("video") || type.equals("forwardedVideo")) {
            holder.imageMessageLayout.setVisibility(View.GONE); //image layout is not visible
            holder.textMessageLayout.setVisibility(View.GONE); //text layout is visible
            holder.leavedFromGroupLayout.setVisibility(View.GONE); //leaved from group layout is not visible
            holder.audioMessageLayout.setVisibility(View.GONE); //audio layout is not visible
            holder.videoMessageLayout.setVisibility(View.VISIBLE); //video layout is visible
            holder.pdfMessageLayout.setVisibility(View.GONE); //pdf layout is not visible
            holder.imageTextMessageLayout.setVisibility(View.GONE); //image text layout is not visible
            holder.videoTextMessageLayout.setVisibility(View.GONE); //video text layout is not visible

            holder.videoTime.setText(dateTime); // set text sended time and date

            getVideoFileSize(holder, message);

            // show video to sender and receiver
            Uri uri = Uri.parse(message);
            holder.showVideo.setVideoURI(uri);

            holder.showVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    holder.showVideo.seekTo(2000);
                    holder.timeDurationForVideo.setText(milliSecondsToTimer(holder.showVideo.getDuration()));
                    holder.preloadProgressBarForVideo.setVisibility(View.GONE);
                }
            });

            holder.showVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, VideoViewPage.class);
                    intent.putExtra("videoUrl", message);
                    intent.putExtra("videoFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.showVideo.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeVideo(groupChat, position);

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
                        Toast.makeText(context, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                        return;
                    }

                    downloadFiles(message, fileName, "BooPraChat/BooPraGroupChat Video", holder, dateTime, type);
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

            if (!senderId.equals(firebaseAuth.getUid())) {
                File file = Environment.getExternalStorageDirectory();
                File myDir = new File(file, "BooPraChat/BooPraGroupChat Video");
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
            } else {
                holder.videoDownloadLayout.setVisibility(View.GONE);
            }

        } else if (type.equals("videoAndText") || type.equals("forwardedVideoAndText")) {
            //image message
            holder.videoTextMessageLayout.setVisibility(View.VISIBLE); //video text layout is visible
            holder.imageTextMessageLayout.setVisibility(View.GONE); //image text layout is visible
            holder.imageMessageLayout.setVisibility(View.GONE); //image layout is not visible
            holder.audioMessageLayout.setVisibility(View.GONE); //audio layout is not visible
            holder.textMessageLayout.setVisibility(View.GONE); //text layout is not visible
            holder.pdfMessageLayout.setVisibility(View.GONE); //pdf layout is not visible
            holder.videoMessageLayout.setVisibility(View.GONE); //video layout is not visible

            holder.timeForVideoAndText.setText(dateTime); // display image time

            getVideoTextFileSize(holder, message);

            holder.showSecondaryVideoText.setText(secondaryMessage);
            //set video
            holder.showSecondaryVideo.setVideoURI(Uri.parse(message));

            holder.showSecondaryVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    holder.showSecondaryVideo.seekTo(2000);
                    holder.timeDurationForVideoText.setText(milliSecondsToTimer(holder.showSecondaryVideo.getDuration()));
                    holder.preloadProgressBarForVideoText.setVisibility(View.GONE);
                }
            });

            holder.showSecondaryVideoText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeVideoAndText(groupChat, position);
                    return true;
                }
            });

            holder.showSecondaryVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, VideoViewPage.class);
                    intent.putExtra("videoUrl", message);
                    intent.putExtra("videoFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.showSecondaryVideo.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeVideoAndText(groupChat, position);
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
                        Toast.makeText(context, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                        return;
                    }

                    downloadFiles(message, fileName, "BooPraChat/BooPraGroupChat Video", holder, dateTime, type);
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

            if (!senderId.equals(firebaseAuth.getUid())) {
                File file = Environment.getExternalStorageDirectory();
                File myDir = new File(file, "BooPraChat/BooPraGroupChat Video");
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
            } else {
                holder.videoTextDownloadLayout.setVisibility(View.GONE);
            }

        } else if (type.equals("pdf") || type.equals("forwardedPdf")) {
            holder.imageMessageLayout.setVisibility(View.GONE); //image layout is not visible
            holder.textMessageLayout.setVisibility(View.GONE); //text layout is visible
            holder.leavedFromGroupLayout.setVisibility(View.GONE); //leaved from group layout is not visible
            holder.audioMessageLayout.setVisibility(View.GONE); //audio layout is not visible
            holder.videoMessageLayout.setVisibility(View.GONE); //video layout is not visible
            holder.pdfMessageLayout.setVisibility(View.VISIBLE); //pdf layout is visible
            holder.imageTextMessageLayout.setVisibility(View.GONE); //image text layout is not visible
            holder.videoTextMessageLayout.setVisibility(View.GONE); //video text layout is not visible

            holder.pdfTime.setText(dateTime); // set text sended time and date
            holder.pdfFileName.setText(fileName);

            getPdfFileSize(holder, message);

            holder.pdfMessageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pdfUrl = message;
                    Intent intent = new Intent(context, PdfViewerPage.class);
                    intent.putExtra("pdfUrl", pdfUrl);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.pdfMessageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typePdf(groupChat, position);

                    return true;
                }
            });

            //for download pdf file
            holder.downloadPdf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                        return;
                    }

                    downloadFiles(message, fileName, "BooPraChat/BooPraGroupChat Pdf", holder, dateTime, type);
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

            if (!senderId.equals(firebaseAuth.getUid())) {
                /*checking the pdf that user already downloaded or not
                 * if the user already downloaded that pdf, the download symbol will not be shown
                 * if the user not downloaded that pdf, the download symbol will be shown*/
                File file = Environment.getExternalStorageDirectory();
                File myDir = new File(file, "BooPraChat/BooPraGroupChat Pdf");
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
            } else {
                holder.pdfDownloadLayout.setVisibility(View.GONE);
            }

        } else if (type.equals("imageAndText") || type.equals("forwardedImageAndText")) {
            //image message
            holder.imageTextMessageLayout.setVisibility(View.VISIBLE); //image text layout is visible
            holder.imageMessageLayout.setVisibility(View.GONE); //image layout is not visible
            holder.audioMessageLayout.setVisibility(View.GONE); //audio layout is not visibili
            holder.textMessageLayout.setVisibility(View.GONE); //text layout is not visibile
            holder.pdfMessageLayout.setVisibility(View.GONE); //pdf layout is not visibili
            holder.videoMessageLayout.setVisibility(View.GONE); //video layout is not visibili
            holder.videoTextMessageLayout.setVisibility(View.GONE); //video text layout is not visible

            holder.timeForImageAndText.setText(dateTime); // display image time

            holder.showSecondaryText.setText(secondaryMessage);

            getImageTextFileSize(holder, message);

            //display image message
            Glide.with(context)
                    .load(message)
                    .placeholder(R.drawable.progress_animation)
                    .into(holder.showSecondaryImage);


            holder.showSecondaryImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ImageViewPage.class);
                    intent.putExtra("imageUrl", message);
                    intent.putExtra("imageFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.showSecondaryImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeImageAndText(groupChat, position, holder);

                    return true;
                }
            });

            holder.showSecondaryText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeImageAndText(groupChat, position, holder);

                    return true;
                }
            });

            //for download image
            holder.downloadImageText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                        Toast.makeText(context, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                        return;
                    }

                    downloadFiles(message, fileName, "BooPraChat/BooPraGroupChat Images", holder, dateTime, type);
                }
            });

            holder.pauseResumeForImageText.setOnClickListener(new View.OnClickListener() {
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

            holder.pauseResumeForImageText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PRDownloader.cancel(downloadId);
                    return true;
                }
            });

            if (!senderId.equals(firebaseAuth.getUid())) {
                /*checking the image that user already downloaded or not
                 * if the user already downloaded that image, the download symbol will not be shown
                 * if the user not downloaded that image, the download symbol will be shown*/
                File file = Environment.getExternalStorageDirectory();
                File myDir = new File(file, "BooPraChat/BooPraGroupChat Images");
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
            } else {
                holder.imageTextDownloadLayout.setVisibility(View.GONE);
            }

        } else if (type.equals("text")) {     //if the message type is text
            //decode the message
            //String decodeAndShowMessage = new String(Base64.decode(message.trim(), Base64.DEFAULT));

            holder.textMessageLayout.setVisibility(View.VISIBLE); //text layout is visible
            holder.imageMessageLayout.setVisibility(View.GONE); //image layout is not visible
            holder.leavedFromGroupLayout.setVisibility(View.GONE); //leaved from group layout is not visible
            holder.imageTextMessageLayout.setVisibility(View.GONE); //image text layout is not visible
            holder.audioMessageLayout.setVisibility(View.GONE); //audio layout is not visible
            holder.videoMessageLayout.setVisibility(View.GONE); //video layout is not visible
            holder.pdfMessageLayout.setVisibility(View.GONE); //pdf layout is not visible
            holder.videoTextMessageLayout.setVisibility(View.GONE); //video text layout is not visible

            holder.showMessage.setText(message);
            holder.messageTime.setText(dateTime); // set message sent time and date

            holder.showMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeText(groupChat, position);

                    return true;
                }
            });

        }
    }

    private void getImageTextFileSize(HolderGroupChat holder, String message) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message);
        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long bytes = storageMetadata.getSizeBytes();
                long kb = bytes / 1024;
                long mb = kb / 1024;

                if (mb >= 1) {
                    holder.imageTextFileSize.setText(mb + "MB");
                } else if (kb >= 1) {
                    holder.imageTextFileSize.setText(kb + "KB");
                } else {
                    holder.imageTextFileSize.setText(bytes + "Bytes");
                }
            }
        });
    }

    private void getPdfFileSize(HolderGroupChat holder, String message) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message);
        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long bytes = storageMetadata.getSizeBytes();
                long kb = bytes / 1024;
                long mb = kb / 1024;

                if (mb >= 1) {
                    holder.pdfFileSize.setText(mb + "MB");
                } else if (kb >= 1) {
                    holder.pdfFileSize.setText(kb + "KB");
                } else {
                    holder.pdfFileSize.setText(bytes + "Bytes");
                }
            }
        });
    }

    private void getVideoTextFileSize(HolderGroupChat holder, String message) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message);
        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long bytes = storageMetadata.getSizeBytes();
                long kb = bytes / 1024;
                long mb = kb / 1024;

                if (mb >= 1) {
                    holder.videoTextFileSize.setText(mb + "MB");
                } else if (kb >= 1) {
                    holder.videoTextFileSize.setText(kb + "KB");
                } else {
                    holder.videoTextFileSize.setText(bytes + "Bytes");
                }
            }
        });
    }

    private void getVideoFileSize(HolderGroupChat holder, String message) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message);
        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long bytes = storageMetadata.getSizeBytes();
                long kb = bytes / 1024;
                long mb = kb / 1024;

                if (mb >= 1) {
                    holder.videoFileSize.setText(mb + "MB");
                } else if (kb >= 1) {
                    holder.videoFileSize.setText(kb + "KB");
                } else {
                    holder.videoFileSize.setText(bytes + "Bytes");
                }
            }
        });
    }

    private void getAudioFileSize(HolderGroupChat holder, String message) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message);
        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long bytes = storageMetadata.getSizeBytes();
                long kb = bytes / 1024;
                long mb = kb / 1024;

                if (mb >= 1) {
                    holder.audioFileSize.setText(mb + "MB");
                } else if (kb >= 1) {
                    holder.audioFileSize.setText(kb + "KB");
                } else {
                    holder.audioFileSize.setText(bytes + "Bytes");
                }
            }
        });
    }

    private void getImageFileSize(HolderGroupChat holder, String message) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message);
        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long bytes = storageMetadata.getSizeBytes();
                long kb = bytes / 1024;
                long mb = kb / 1024;

                if (mb >= 1) {
                    holder.imageFileSize.setText(mb + "MB");
                } else if (kb >= 1) {
                    holder.imageFileSize.setText(kb + "KB");
                } else {
                    holder.imageFileSize.setText(bytes + "Bytes");
                }
            }
        });
    }

    private void loadMessageSeenCount(String time, HolderGroupChat holder, int position) {

        String message = groupChats.get(position).getMessage();
        String fileName = groupChats.get(position).getFileName();
        String secondaryMessage = groupChats.get(position).getSecondaryMessage();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(time).child("messageSeenMembers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageSeenCountList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get user ID
                    String uid = "" + ds.child("uid").getValue();

                    if (!uid.equals(firebaseAuth.getUid())) {
                        messageSeenCountList.add(uid);

                        holder.textMessageSeenCountLayout.setVisibility(View.VISIBLE);// for text
                        holder.textMessageSeenCount.setVisibility(View.VISIBLE);
                        holder.textMessageSeenCount.setText(String.format("%s", messageSeenCountList.size()));
                        //for image
                        holder.eyeIconForImage.setVisibility(View.VISIBLE);
                        holder.imageMessageSeenCount.setVisibility(View.VISIBLE);
                        holder.imageMessageSeenCount.setText(String.format("%s", messageSeenCountList.size()));
                        //for image and text
                        holder.eyeIconForImageText.setVisibility(View.VISIBLE);
                        holder.imageTextMessageSeenCount.setVisibility(View.VISIBLE);
                        holder.imageTextMessageSeenCount.setText(String.format("%s", messageSeenCountList.size()));
                        //for audio
                        holder.eyeIconForAudio.setVisibility(View.VISIBLE);
                        holder.audioMessageSeenCount.setVisibility(View.VISIBLE);
                        holder.audioMessageSeenCount.setText(String.format("%s", messageSeenCountList.size()));
                        //for video
                        holder.eyeIconForVideo.setVisibility(View.VISIBLE);
                        holder.videoMessageSeenCount.setVisibility(View.VISIBLE);
                        holder.videoMessageSeenCount.setText(String.format("%s", messageSeenCountList.size()));
                        //for video and text
                        holder.eyeIconForVideoText.setVisibility(View.VISIBLE);
                        holder.videoTextMessageSeenCount.setVisibility(View.VISIBLE);
                        holder.videoTextMessageSeenCount.setText(String.format("%s", messageSeenCountList.size()));
                        //for pdf
                        holder.eyeIconForPdf.setVisibility(View.VISIBLE);
                        holder.pdfMessageSeenCount.setVisibility(View.VISIBLE);
                        holder.pdfMessageSeenCount.setText(String.format("%s", messageSeenCountList.size()));


                    } else if (messageSeenCountList.size() == 0) {
                        holder.textMessageSeenCount.setVisibility(View.GONE);
                        holder.imageMessageSeenCount.setVisibility(View.GONE);
                        holder.imageTextMessageSeenCount.setVisibility(View.GONE);
                        holder.videoMessageSeenCount.setVisibility(View.GONE);
                        holder.videoTextMessageSeenCount.setVisibility(View.GONE);
                        holder.pdfMessageSeenCount.setVisibility(View.GONE);
                        holder.audioMessageSeenCount.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.textMessageSeenCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                intent.putExtra("time", time);
                intent.putExtra("groupId", groupId);
                intent.putExtra("message", message);
                context.startActivity(intent);
            }
        });

        holder.imageMessageSeenCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                intent.putExtra("time", time);
                intent.putExtra("groupId", groupId);
                intent.putExtra("message", "Image: " + fileName);
                context.startActivity(intent);
            }
        });

        holder.imageTextMessageSeenCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                intent.putExtra("time", time);
                intent.putExtra("groupId", groupId);
                intent.putExtra("message", secondaryMessage);
                context.startActivity(intent);
            }
        });

        holder.videoMessageSeenCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                intent.putExtra("time", time);
                intent.putExtra("groupId", groupId);
                intent.putExtra("message", "Video: " + fileName);
                context.startActivity(intent);
            }
        });

        holder.videoTextMessageSeenCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                intent.putExtra("time", time);
                intent.putExtra("groupId", groupId);
                intent.putExtra("message", "Video: " + fileName);
                context.startActivity(intent);
            }
        });

        holder.audioMessageSeenCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                intent.putExtra("time", time);
                intent.putExtra("groupId", groupId);
                intent.putExtra("message", "Audio: " + fileName);
                context.startActivity(intent);
            }
        });

        holder.pdfMessageSeenCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                intent.putExtra("time", time);
                intent.putExtra("groupId", groupId);
                intent.putExtra("message", "Pdf: " + fileName);
                context.startActivity(intent);
            }
        });

    }

    private void downloadFiles(String message, String fileName, String fileDestination, HolderGroupChat holder, String dateTime, String fileType) {

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
                            getImageFileSize(holder, message);

                        } else if (fileType.equals("imageAndText") || fileType.equals("forwardedImageAndText")) {
                            holder.pauseResumeForImageText.setVisibility(View.GONE);
                            holder.downloadImageText.setVisibility(View.VISIBLE);
                            holder.imageTextDownloadingLevelIndicatingBar.setProgress(100);
                            getImageTextFileSize(holder, message);

                        } else if (fileType.equals("pdf") || fileType.equals("forwardedPdf")) {
                            holder.pauseResumeForPdf.setVisibility(View.GONE);
                            holder.downloadPdf.setVisibility(View.VISIBLE);
                            holder.pdfDownloadingLevelIndicatingBar.setProgress(100);
                            getPdfFileSize(holder, message);

                        } else if (fileType.equals("audio") || fileType.equals("forwardedAudio")) {
                            holder.pauseResumeForAudio.setVisibility(View.GONE);
                            holder.downloadAudio.setVisibility(View.VISIBLE);
                            holder.audioDownloadingLevelIndicatingBar.setProgress(100);
                            getAudioFileSize(holder, message);

                        } else if (fileType.equals("video") || fileType.equals("forwardedVideo")) {
                            holder.pauseResumeForVideo.setVisibility(View.GONE);
                            holder.downloadVideo.setVisibility(View.VISIBLE);
                            holder.videoDownloadingLevelIndicatingBar.setProgress(100);
                            getVideoFileSize(holder, message);

                        } else if (fileType.equals("videoAndText") || fileType.equals("forwardedVideoAndText")) {
                            holder.pauseResumeForVideoText.setVisibility(View.GONE);
                            holder.downloadVideoText.setVisibility(View.VISIBLE);
                            holder.videoTextDownloadingLevelIndicatingBar.setProgress(100);
                            getVideoTextFileSize(holder, message);
                        }

                        Toast.makeText(context, R.string.download_canceled, Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long percentage = progress.currentBytes * 100 / progress.totalBytes;

                        if (fileType.equals("image") || fileType.equals("forwardedImage")) {
                            holder.imageDownloadingLevelIndicatingBar.setProgress((int) percentage);
                            holder.imageFileSize.setText(percentage + "%");

                        } else if (fileType.equals("imageAndText") || fileType.equals("forwardedImageAndText")) {
                            holder.imageTextDownloadingLevelIndicatingBar.setProgress((int) percentage);
                            holder.imageTextFileSize.setText(percentage + "%");

                        } else if (fileType.equals("pdf") || fileType.equals("forwardedPdf")) {
                            holder.pdfDownloadingLevelIndicatingBar.setProgress((int) percentage);
                            holder.pdfFileSize.setText(percentage + "%");

                        } else if (fileType.equals("audio") || fileType.equals("forwardedAudio")) {
                            holder.audioDownloadingLevelIndicatingBar.setProgress((int) percentage);
                            holder.audioFileSize.setText(percentage + "%");

                        } else if (fileType.equals("video") || fileType.equals("forwardedVideo")) {
                            holder.videoDownloadingLevelIndicatingBar.setProgress((int) percentage);
                            holder.videoFileSize.setText(percentage + "%");

                        } else if (fileType.equals("videoAndText") || fileType.equals("forwardedVideoAndText")) {
                            holder.videoDownloadingLevelIndicatingBar.setProgress((int) percentage);
                            holder.videoTextFileSize.setText(percentage + "%");
                        }
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {

                        if (fileType.equals("image") || fileType.equals("forwardedImage")) {
                            holder.imageFileSize.setText(R.string.completed);
                            holder.pauseResumeForImage.setImageResource(R.drawable.downloading_completed);

                        } else if (fileType.equals("imageAndText") || fileType.equals("forwardedImageAndText")) {
                            holder.imageTextFileSize.setText(R.string.completed);
                            holder.pauseResumeForImageText.setImageResource(R.drawable.downloading_completed);

                        } else if (fileType.equals("pdf") || fileType.equals("forwardedPdf")) {
                            holder.pdfFileSize.setText(R.string.completed);
                            holder.pauseResumeForPdf.setImageResource(R.drawable.downloading_completed);

                        } else if (fileType.equals("audio") || fileType.equals("forwardedAudio")) {
                            holder.audioFileSize.setText(R.string.completed);
                            holder.pauseResumeForAudio.setImageResource(R.drawable.downloading_completed);

                        } else if (fileType.equals("video") || fileType.equals("forwardedVideo")) {
                            holder.videoFileSize.setText(R.string.completed);
                            holder.pauseResumeForVideo.setImageResource(R.drawable.downloading_completed);

                        } else if (fileType.equals("videoAndText") || fileType.equals("forwardedVideoAndText")) {
                            holder.videoTextFileSize.setText(R.string.completed);
                            holder.pauseResumeForVideoText.setImageResource(R.drawable.downloading_completed);
                        }

                    }

                    @Override
                    public void onError(Error e) {
                        Toast.makeText(context, R.string.something_went_wrong_please_try_again_later, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void typeVideoAndText(GroupChat groupChat, int position) {
        String message = groupChat.getMessage();
        String senderId = groupChat.getSender();
        String time = groupChat.getTime();
        String fileName = groupChat.getFileName();
        String secondaryMessage = groupChat.getSecondaryMessage();
        String type = groupChat.getType();

        toolbar2.setVisibility(View.GONE);
        toolbar1.setVisibility(View.VISIBLE);

        copy.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        favourite.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);

        Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("VideoMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    favouriteText = "alreadyInFavourite";
                    favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        favouriteText = "";
        favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);

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
                deleteMessage(position);
                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to forward selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = secondaryMessage;
                String videoUri = message;
                String requestCode = "VT";

                Intent intent = new Intent(context, SendToGroupActivity.class);
                intent.putExtra("videoUri", videoUri);
                intent.putExtra("text", text);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                intent.putExtra("fileName", fileName);
                context.startActivity(intent);
                ((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (favouriteText.equals("alreadyInFavourite")) {
                    Toast.makeText(context, R.string.already_in_favourite, Toast.LENGTH_SHORT).show();

                } else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", senderId);
                    hashMap.put("message", message);
                    hashMap.put("secondaryMessage", secondaryMessage);
                    hashMap.put("time", time);
                    hashMap.put("fileName", fileName);
                    hashMap.put("groupIdForFavourite", groupId);
                    hashMap.put("type", type);

                    reference.child(firebaseAuth.getUid()).child("Favourite").child("VideoMessages").child(time).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, R.string.added_to_favourite, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    toolbar1.setVisibility(View.GONE);
                }
            }
        });

        if (senderId.equals(firebaseAuth.getUid())) {
            info.setVisibility(View.VISIBLE);

            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                    intent.putExtra("time", time);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("message", "Video: " + fileName);
                    context.startActivity(intent);

                    toolbar1.setVisibility(View.GONE);
                }
            });

        } else {
            info.setVisibility(View.GONE);
        }
    }

    private void typeVideo(GroupChat groupChat, int position) {

        String message = groupChat.getMessage();
        String senderId = groupChat.getSender();
        String time = groupChat.getTime();
        String fileName = groupChat.getFileName();
        String type = groupChat.getType();

        toolbar2.setVisibility(View.GONE);
        toolbar1.setVisibility(View.VISIBLE);

        copy.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        favourite.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);

        Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("VideoMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    favouriteText = "alreadyInFavourite";
                    favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        favouriteText = "";
        favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);

        //on click to delete selected message
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteMessage(position);
                toolbar1.setVisibility(View.GONE);
            }
        });
        //on click to forward selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String videoUri = message;
                String requestCode = "v";

                Intent intent = new Intent(context, SendToGroupActivity.class);
                intent.putExtra("videoUri", videoUri);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                intent.putExtra("fileName", fileName);
                context.startActivity(intent);
                ((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (favouriteText.equals("alreadyInFavourite")) {
                    Toast.makeText(context, R.string.already_in_favourite, Toast.LENGTH_SHORT).show();

                } else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", senderId);
                    hashMap.put("message", message);
                    hashMap.put("time", time);
                    hashMap.put("fileName", fileName);
                    hashMap.put("groupIdForFavourite", groupId);
                    hashMap.put("type", type);

                    reference.child(firebaseAuth.getUid()).child("Favourite").child("VideoMessages").child(time).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, R.string.added_to_favourite, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    toolbar1.setVisibility(View.GONE);
                }
            }
        });

        if (senderId.equals(firebaseAuth.getUid())) {
            info.setVisibility(View.VISIBLE);

            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                    intent.putExtra("time", time);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("message", "Video: " + fileName);
                    context.startActivity(intent);

                    toolbar1.setVisibility(View.GONE);
                }
            });

        } else {
            info.setVisibility(View.GONE);
        }

    }

    private void typeAudio(GroupChat groupChat, int position) {
        String message = groupChat.getMessage();
        String senderId = groupChat.getSender();
        String time = groupChat.getTime();
        String fileName = groupChat.getFileName();
        String type = groupChat.getType();

        toolbar2.setVisibility(View.GONE);
        toolbar1.setVisibility(View.VISIBLE);

        copy.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("VideoMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    favouriteText = "alreadyInFavourite";
                    favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        favouriteText = "";
        favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);

        //on click to delete selected message
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteMessage(position);
                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to forward selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String audioUri = message;
                String requestCode = "a";

                Intent intent = new Intent(context, SendToGroupActivity.class);
                intent.putExtra("audioUri", audioUri);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                intent.putExtra("fileName", fileName);
                context.startActivity(intent);
                ((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (favouriteText.equals("alreadyInFavourite")) {
                    Toast.makeText(context, R.string.already_in_favourite, Toast.LENGTH_SHORT).show();

                } else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", senderId);
                    hashMap.put("message", message);
                    hashMap.put("time", time);
                    hashMap.put("fileName", fileName);
                    hashMap.put("groupIdForFavourite", groupId);
                    hashMap.put("type", type);

                    reference.child(firebaseAuth.getUid()).child("Favourite").child("VideoMessages").child(time).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, R.string.added_to_favourite, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    toolbar1.setVisibility(View.GONE);
                }
            }
        });

        if (senderId.equals(firebaseAuth.getUid())) {
            info.setVisibility(View.VISIBLE);

            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                    intent.putExtra("time", time);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("message", "Audio: " + fileName);
                    context.startActivity(intent);

                    toolbar1.setVisibility(View.GONE);
                }
            });

        } else {
            info.setVisibility(View.GONE);
        }

    }

    private void typePdf(GroupChat groupChat, int position) {

        String message = groupChat.getMessage();
        String senderId = groupChat.getSender();
        String time = groupChat.getTime();
        String fileName = groupChat.getFileName();
        String type = groupChat.getType();

        toolbar2.setVisibility(View.GONE);
        toolbar1.setVisibility(View.VISIBLE);

        copy.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("PdfMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    favouriteText = "alreadyInFavourite";
                    favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        favouriteText = "";
        favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);

        //on click to delete selected message
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteMessage(position);
                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to forward selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pdfUri = message;
                String requestCode = "p";

                Intent intent = new Intent(context, SendToGroupActivity.class);
                intent.putExtra("pdfUri", pdfUri);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                intent.putExtra("fileName", fileName);
                context.startActivity(intent);
                ((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (favouriteText.equals("alreadyInFavourite")) {
                    Toast.makeText(context, R.string.already_in_favourite, Toast.LENGTH_SHORT).show();

                } else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", senderId);
                    hashMap.put("message", message);
                    hashMap.put("time", time);
                    hashMap.put("fileName", fileName);
                    hashMap.put("groupIdForFavourite", groupId);
                    hashMap.put("type", type);

                    reference.child(firebaseAuth.getUid()).child("Favourite").child("PdfMessages").child(time).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, R.string.added_to_favourite, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    toolbar1.setVisibility(View.GONE);
                }
            }
        });

        if (senderId.equals(firebaseAuth.getUid())) {
            info.setVisibility(View.VISIBLE);

            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                    intent.putExtra("time", time);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("message", "Pdf: " + fileName);
                    context.startActivity(intent);

                    toolbar1.setVisibility(View.GONE);
                }
            });

        } else {
            info.setVisibility(View.GONE);
        }

    }

    private void typeImage(GroupChat groupChat, int position, HolderGroupChat holder) {
        String message = groupChat.getMessage();
        String senderId = groupChat.getSender();
        String time = groupChat.getTime();
        String fileName = groupChat.getFileName();
        String type = groupChat.getType();

        toolbar2.setVisibility(View.GONE);
        toolbar1.setVisibility(View.VISIBLE);

        copy.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.VISIBLE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("ImageMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    favouriteText = "alreadyInFavourite";
                    favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        favouriteText = "";
        favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);

        //on click to delete selected message
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteMessage(position);

                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to forward selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String imageURI = message;
                String text = "";
                String requestCode = "i";

                Intent intent = new Intent(context, SendToGroupActivity.class);
                intent.putExtra("imageUri", imageURI);
                intent.putExtra("text", text);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                intent.putExtra("fileName", fileName);
                context.startActivity(intent);
                ((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.showImage.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                shareImage(bitmap);

                toolbar1.setVisibility(View.GONE);
            }
        });

        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (favouriteText.equals("alreadyInFavourite")) {
                    Toast.makeText(context, R.string.already_in_favourite, Toast.LENGTH_SHORT).show();

                } else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", senderId);
                    hashMap.put("message", message);
                    hashMap.put("time", time);
                    hashMap.put("fileName", fileName);
                    hashMap.put("groupIdForFavourite", groupId);
                    hashMap.put("type", type);

                    reference.child(firebaseAuth.getUid()).child("Favourite").child("ImageMessages").child(time).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, R.string.added_to_favourite, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    toolbar1.setVisibility(View.GONE);
                }
            }
        });

        if (senderId.equals(firebaseAuth.getUid())) {
            info.setVisibility(View.VISIBLE);

            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                    intent.putExtra("time", time);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("message", "Image: " + fileName);
                    context.startActivity(intent);

                    toolbar1.setVisibility(View.GONE);
                }
            });

        } else {
            info.setVisibility(View.GONE);
        }

    }

    private void typeImageAndText(GroupChat groupChat, int position, HolderGroupChat holder) {

        String message = groupChat.getMessage();
        String senderId = groupChat.getSender();
        String time = groupChat.getTime();
        String secondaryMessage = groupChat.getSecondaryMessage();
        String fileName = groupChat.getFileName();
        String type = groupChat.getType();

        toolbar2.setVisibility(View.GONE);
        toolbar1.setVisibility(View.VISIBLE);

        copy.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.VISIBLE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("ImageMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    favouriteText = "alreadyInFavourite";
                    favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        favouriteText = "";
        favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);

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
                deleteMessage(position);
                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to forward selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = secondaryMessage;
                String imageURI = message;
                String requestCode = "it";

                Intent intent = new Intent(context, SendToGroupActivity.class);
                intent.putExtra("text", text);
                intent.putExtra("imageUri", imageURI);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                intent.putExtra("fileName", fileName);
                context.startActivity(intent);
                ((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.showSecondaryImage.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                shareImageWithText(bitmap, secondaryMessage);

                toolbar1.setVisibility(View.GONE);
            }
        });

        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (favouriteText.equals("alreadyInFavourite")) {
                    Toast.makeText(context, R.string.already_in_favourite, Toast.LENGTH_SHORT).show();

                } else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", senderId);
                    hashMap.put("message", message);
                    hashMap.put("secondaryMessage", secondaryMessage);
                    hashMap.put("time", time);
                    hashMap.put("fileName", fileName);
                    hashMap.put("groupIdForFavourite", groupId);
                    hashMap.put("type", type);

                    reference.child(firebaseAuth.getUid()).child("Favourite").child("ImageMessages").child(time).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, R.string.added_to_favourite, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    toolbar1.setVisibility(View.GONE);
                }
            }
        });

        if (senderId.equals(firebaseAuth.getUid())) {
            info.setVisibility(View.VISIBLE);

            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String text = secondaryMessage;

                    Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                    intent.putExtra("time", time);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("message", text);
                    context.startActivity(intent);

                    toolbar1.setVisibility(View.GONE);
                }
            });

        } else {
            info.setVisibility(View.GONE);
        }

    }

    private void typeText(GroupChat groupChat, int position) {

        String message = groupChat.getMessage();
        String senderId = groupChat.getSender();
        String time = groupChat.getTime();

        toolbar2.setVisibility(View.GONE);
        toolbar1.setVisibility(View.VISIBLE);

        copy.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.VISIBLE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("TextMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    favouriteText = "alreadyInFavourite";
                    favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        favouriteText = "";
        favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);

        // on click to copy selected text
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = message;

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
                deleteMessage(position);
                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to forward selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = message;

                Intent intent = new Intent(context, SendToGroupActivity.class);
                intent.putExtra("text", text);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                context.startActivity(intent);
                ((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click add to favourite
        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (favouriteText.equals("alreadyInFavourite")) {
                    Toast.makeText(context, R.string.already_in_favourite, Toast.LENGTH_SHORT).show();

                } else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", senderId);
                    hashMap.put("message", message);
                    hashMap.put("time", time);
                    hashMap.put("groupIdForFavourite", groupId);
                    hashMap.put("type", "text");

                    reference.child(firebaseAuth.getUid()).child("Favourite").child("TextMessages").child(time).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, R.string.added_to_favourite, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    toolbar1.setVisibility(View.GONE);
                }
            }
        });

        if (senderId.equals(firebaseAuth.getUid())) {
            info.setVisibility(View.VISIBLE);

            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, GroupMessageSeenByActivity.class);
                    intent.putExtra("time", time);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("message", message);
                    context.startActivity(intent);

                    toolbar1.setVisibility(View.GONE);
                }
            });

        } else {
            info.setVisibility(View.GONE);
        }

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareOnlyText(message);

                toolbar1.setVisibility(View.GONE);
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

    private void storeMessageSeenMembers(String time) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", firebaseAuth.getUid());

        reference.child(groupId).child("Messages").child(time).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);
    }

    private void deleteMessage(int position) {

        //get time of clicked message
        //compare the time of the clicked message with all messages in chats
        //Where both values matches delete that message;
        String msgtime = groupChats.get(position).getTime();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");

        Query query = reference.child(groupId).child("Messages").orderByChild("time").equalTo(msgtime);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {

                    if (ds.child("sender").getValue().equals(firebaseAuth.getUid())) {
                        //remove the message from current group chat
                        //ds.getRef().removeValue();
                        //ds.getValue();

                        // 2.Change the message that "this message was deleted"
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted");
                        ds.getRef().updateChildren(hashMap);

                    } else {
                        delete.setVisibility(View.GONE);
                        Toast.makeText(context, R.string.you_can_delete_only_your_message, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUsername(GroupChat groupChat, HolderGroupChat holder) {
        //get sender info from uid model
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(groupChat.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get data
                            String name = "" + ds.child("name").getValue();
                            String messageSenderImage = "" + ds.child("image").getValue();
                            String senderId = groupChat.getSender();
                            //set data
                            holder.messageSenderName.setText(name);
                            holder.imageSenderName.setText(name);
                            holder.audioSenderName.setText(name);
                            holder.videoSenderName.setText(name);
                            holder.pdfSenderName.setText(name);
                            holder.imageTextSenderName.setText(name);
                            holder.videoTextSenderName.setText(name);

                            //set messages
                            String type = groupChat.getType();
                            String message = groupChat.getMessage();

                            if (messageSenderImage.equals("")) {

                                String firstLetter = String.valueOf(name.charAt(0)).toLowerCase();

                                if (!senderId.equals(firebaseAuth.getUid())) {

                                    holder.messageSenderProfileImage.setVisibility(View.GONE);
                                    holder.messageSenderProfileText.setVisibility(View.VISIBLE);

                                    Drawable text = holder.messageSenderProfileText.getBackground();
                                    text = DrawableCompat.wrap(text);

                                    if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                                        DrawableCompat.setTint(text, Color.parseColor("#FF00BCD4")); //color blue

                                        holder.messageSenderProfileText.setBackground(text);

                                    } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                                        DrawableCompat.setTint(text, Color.parseColor("#FFFF5722")); //color red

                                        holder.messageSenderProfileText.setBackground(text);

                                    } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                                        DrawableCompat.setTint(text, Color.parseColor("#bfd200")); //color yellow

                                        holder.messageSenderProfileText.setBackground(text);

                                    } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                                        DrawableCompat.setTint(text, Color.parseColor("#FF9800")); //color orange

                                        holder.messageSenderProfileText.setBackground(text);

                                    } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                                        DrawableCompat.setTint(text, Color.parseColor("#52b788")); //color

                                        holder.messageSenderProfileText.setBackground(text);

                                    } else if (firstLetter.equals("z")) {
                                        DrawableCompat.setTint(text, Color.parseColor("#F33D73")); //color darker pink

                                        holder.messageSenderProfileText.setBackground(text);

                                    } else {
                                        DrawableCompat.setTint(text, Color.parseColor("#6c757d")); //color

                                        holder.messageSenderProfileText.setBackground(text);
                                    }

                                    holder.messageSenderProfileText.setText(firstLetter);
                                }

                            } else {

                                holder.messageSenderProfileText.setVisibility(View.GONE);
                                holder.messageSenderProfileImage.setVisibility(View.VISIBLE);
                                Picasso.get().load(messageSenderImage).into(holder.messageSenderProfileImage);
                            }

                            if (type.equals("leavedFromGroup")) {   //if the message type is image
                                holder.leavedFromGroup.setText(message + " " + name + context.getString(R.string.left_from_team));
                            }

                            ///for text
                            holder.messageSenderProfileImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(context, ProfileViewActivity.class);
                                    intent.putExtra("image", messageSenderImage);
                                    intent.putExtra("name", name);
                                    context.startActivity(intent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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

    @Override
    public int getItemCount() {
        return groupChats.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get currently signed in user

        if (groupChats.get(position).getSender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {

        private CircleImageView messageSenderProfileImage;
        private TextView messageSenderProfileText;

        private ImageView showImage;
        private VideoView showVideo;

        private TextView showMessage;
        private TextView messageTime, imageTime, audioTime, videoTime, pdfTime;

        private TextView messageSenderName,
                imageSenderName,
                audioSenderName,
                videoSenderName,
                pdfSenderName;

        private TextView musicFileName, pdfFileName;
        private ImageButton downloadImage, downloadAudio, downloadVideo, downloadPdf;
        private ImageView musicIcon, pdfIcon, videoPlayButton;

        // for layout
        private RelativeLayout textMessageLayout,
                imageMessageLayout,
                audioMessageLayout,
                videoMessageLayout,
                pdfMessageLayout;

        // user leaved from group
        private RelativeLayout leavedFromGroupLayout;
        private TextView leavedFromGroup;
        //for delete message
        private RelativeLayout groupMessageLayout;

        RelativeLayout imageTextMessageLayout;
        ShapeableImageView showSecondaryImage;
        TextView showSecondaryText, timeForImageAndText, imageAndTextMessageSeenOrNot, imageTextSenderName;
        ImageButton downloadImageText;

        RelativeLayout videoTextMessageLayout;
        VideoView showSecondaryVideo;
        TextView showSecondaryVideoText,
                timeForVideoAndText,
                videoAndTextMessageSeenOrNot,
                videoTextSenderName;
        ImageButton downloadVideoText;
        ImageView videoTextPlayButton;

        RelativeLayout videoTextLayout;

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

        //for message seen count
        TextView textMessageSeenCount,
                imageMessageSeenCount,
                imageTextMessageSeenCount,
                audioMessageSeenCount,
                videoMessageSeenCount,
                videoTextMessageSeenCount,
                pdfMessageSeenCount;

        ImageView eyeIconForText,
                eyeIconForImage,
                eyeIconForImageText,
                eyeIconForAudio,
                eyeIconForVideo,
                eyeIconForVideoText,
                eyeIconForPdf;

        //message seen count layout
        LinearLayout textMessageSeenCountLayout,
                imageMessageSeenCountLayout,
                imageTextMessageSeenCountLayout,
                videoMessageSeenCountLayout,
                videoTextMessageSeenCountLayout,
                audioMessageSeenCountLayout,
                pdfMessageSeenCountLayout;

        //for file size
        TextView pdfFileSize, imageFileSize, imageTextFileSize, videoFileSize, videoTextFileSize, audioFileSize;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            //for profile image and profile text
            messageSenderProfileImage = itemView.findViewById(R.id.profile_image);
            messageSenderProfileText = itemView.findViewById(R.id.profile_text);

            //for text message
            showMessage = itemView.findViewById(R.id.show_sender_receiver_Msgs);
            messageTime = itemView.findViewById(R.id.message_time);
            messageSenderName = itemView.findViewById(R.id.message_sender_name);
            textMessageLayout = itemView.findViewById(R.id.text_message_layout);
            eyeIconForText = itemView.findViewById(R.id.eye_icon_for_text);

            //for image message
            showImage = itemView.findViewById(R.id.show_sender_receiver_image);
            imageTime = itemView.findViewById(R.id.time_for_image);
            imageSenderName = itemView.findViewById(R.id.image_sender_name);
            imageMessageLayout = itemView.findViewById(R.id.image_message_layout);
            downloadImage = itemView.findViewById(R.id.download_image);
            eyeIconForImage = itemView.findViewById(R.id.eye_icon_for_image);
            imageFileSize = itemView.findViewById(R.id.image_file_size);

            //for image and text
            timeForImageAndText = itemView.findViewById(R.id.TimeForImageAndText);
            imageAndTextMessageSeenOrNot = itemView.findViewById(R.id.seen_delivered_For_Image_And_Text);
            imageTextMessageLayout = itemView.findViewById(R.id.image_and_text__message_layout);
            showSecondaryImage = itemView.findViewById(R.id.show_Sender_Receiver_Image_And_Text);
            showSecondaryText = itemView.findViewById(R.id.image_text);
            downloadImageText = itemView.findViewById(R.id.download_image_and_text);
            imageTextSenderName = itemView.findViewById(R.id.image_text_sender_name);
            eyeIconForImageText = itemView.findViewById(R.id.eye_icon_for_image_text);
            imageTextFileSize = itemView.findViewById(R.id.image_text_file_size);

            //for audio message
            audioTime = itemView.findViewById(R.id.time_for_audio);
            audioSenderName = itemView.findViewById(R.id.audio_sender_name);
            audioMessageLayout = itemView.findViewById(R.id.audio_message_layout);
            downloadAudio = itemView.findViewById(R.id.download_audio);
            musicFileName = itemView.findViewById(R.id.music_name);
            musicIcon = itemView.findViewById(R.id.music_icon);
            eyeIconForAudio = itemView.findViewById(R.id.eye_icon_for_audio);
            audioFileSize = itemView.findViewById(R.id.audio_file_size);

            //for video message
            videoTime = itemView.findViewById(R.id.time_for_video);
            videoSenderName = itemView.findViewById(R.id.video_sender_name);
            videoMessageLayout = itemView.findViewById(R.id.video_message_layout);
            downloadVideo = itemView.findViewById(R.id.download_video);
            showVideo = itemView.findViewById(R.id.show_video);
            videoPlayButton = itemView.findViewById(R.id.video_play_btn);
            eyeIconForVideo = itemView.findViewById(R.id.eye_icon_for_video);
            videoFileSize = itemView.findViewById(R.id.video_file_size);

            //for video and text
            timeForVideoAndText = itemView.findViewById(R.id.TimeForVideoAndText);
            videoAndTextMessageSeenOrNot = itemView.findViewById(R.id.seen_delivered_For_Video_And_Text);
            videoTextMessageLayout = itemView.findViewById(R.id.video_and_text_message_layout);
            videoTextSenderName = itemView.findViewById(R.id.video_text_sender_name);
            showSecondaryVideo = itemView.findViewById(R.id.show_video_and_text);
            showSecondaryVideoText = itemView.findViewById(R.id.video_text);
            downloadVideoText = itemView.findViewById(R.id.download_video_and_text);
            videoTextPlayButton = itemView.findViewById(R.id.video_and_text_play_btn);
            videoTextLayout = itemView.findViewById(R.id.testing);
            eyeIconForVideoText = itemView.findViewById(R.id.eye_icon_for_video_text);
            videoTextFileSize = itemView.findViewById(R.id.video_text_file_size);

            //for pdf message
            pdfTime = itemView.findViewById(R.id.time_for_pdf);
            pdfSenderName = itemView.findViewById(R.id.pdf_sender_name);
            pdfMessageLayout = itemView.findViewById(R.id.pdf_message_layout);
            downloadPdf = itemView.findViewById(R.id.download_pdf);
            pdfFileName = itemView.findViewById(R.id.pdf_name);
            pdfIcon = itemView.findViewById(R.id.pdf_icon);
            eyeIconForPdf = itemView.findViewById(R.id.eye_icon_for_pdf);
            pdfFileSize = itemView.findViewById(R.id.pdf_file_size);

            //user leaved from group
            leavedFromGroupLayout = itemView.findViewById(R.id.leaved_from_group_layout);
            leavedFromGroup = itemView.findViewById(R.id.leaved_from_group_message);

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

            //for message seen count
            textMessageSeenCount = itemView.findViewById(R.id.text_message_seen_count);
            imageMessageSeenCount = itemView.findViewById(R.id.image_message_seen_count);
            imageTextMessageSeenCount = itemView.findViewById(R.id.image_text_message_seen_count);
            audioMessageSeenCount = itemView.findViewById(R.id.audio_message_seen_count);
            videoMessageSeenCount = itemView.findViewById(R.id.video_message_seen_count);
            videoTextMessageSeenCount = itemView.findViewById(R.id.video_text_message_seen_count);
            pdfMessageSeenCount = itemView.findViewById(R.id.pdf_message_seen_count);

            //message seen count layout
            textMessageSeenCountLayout = itemView.findViewById(R.id.text_message_seen_count_layout);
            imageMessageSeenCountLayout = itemView.findViewById(R.id.image_message_seen_count_layout);
            imageTextMessageSeenCountLayout = itemView.findViewById(R.id.image_text_message_seen_count_layout);
            videoMessageSeenCountLayout = itemView.findViewById(R.id.video_message_seen_count_layout);
            videoTextMessageSeenCountLayout = itemView.findViewById(R.id.video_text_message_seen_count_layout);
            audioMessageSeenCountLayout = itemView.findViewById(R.id.audio_message_seen_count_layout);
            pdfMessageSeenCountLayout = itemView.findViewById(R.id.pdf_message_seen_count_layout);
        }
    }
}
