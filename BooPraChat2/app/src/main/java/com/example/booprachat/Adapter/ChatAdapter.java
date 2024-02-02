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
import com.example.booprachat.Model.Chat;
import com.example.booprachat.R;
import com.example.booprachat.ReceiveDataFromOtherApps.SendToActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    Context context;
    List<Chat> chatList;
    String imageUrl;
    LinearLayout toolbar1;
    ImageView copy;
    ImageView delete;
    ImageView forward;
    ImageView share;
    ImageView favourite;
    LinearLayout toolbar2;
    String name;
    String imBlockedOrNot;

    //firebase services;
    FirebaseUser fUser;
    DatabaseReference reference;
    private String favouriteText;
    private String checkImBlockedOrNot = "";
    String secondaryRequestCode = "CA"; // to find where the data is coming. "CA" means ChatAdapter
    String timeStamp;
    int downloadId;
    private String name1;

    public ChatAdapter(Context context, List<Chat> chatList, String imageUrl, LinearLayout toolbar1, ImageView copy, ImageView delete, ImageView forward, ImageView share, ImageView favourite, LinearLayout toolbar2, String name, String imBlockedOrNot) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
        this.toolbar1 = toolbar1;
        this.copy = copy;
        this.delete = delete;
        this.forward = forward;
        this.share = share;
        this.favourite = favourite;
        this.toolbar2 = toolbar2;
        this.name = name;
        this.imBlockedOrNot = imBlockedOrNot;

        reference = FirebaseDatabase.getInstance().getReference("Users");
        timeStamp = "" + System.currentTimeMillis();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.message_item_right, parent, false);
            return new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.message_item_left, parent, false);
            return new MyHolder(view);
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {

        /*holder.imageDownloadLayout.setVisibility(View.GONE);
        holder.imageTextDownloadLayout.setVisibility(View.GONE);
        holder.audioDownloadLayout.setVisibility(View.GONE);
        holder.pdfDownloadLayout.setVisibility(View.GONE);
        holder.videoDownloadLayout.setVisibility(View.GONE);
        holder.videoTextDownloadLayout.setVisibility(View.GONE);*/

        PRDownloaderConfig config = PRDownloaderConfig.newBuilder().setDatabaseEnabled(true).setReadTimeout(30_000).setConnectTimeout(30_000).build();
        PRDownloader.initialize(context, config);

        //get data
        Chat chat = chatList.get(position);

        String message = chat.getMessage();
        String time = chat.getTime();
        String type = chat.getType();
        String fileName = chat.getFileName();
        String senderId = chat.getSender();
        String receiverId = chat.getReceiver();
        String secondaryMessage = chat.getSecondaryMessage();
        String messageSeenOrNot = chat.getMessageSeenOrNot();

        String requestCode = "CA"; // this request is sent to files view pages to download file and store to internal storage file name"BooPraChat files". for group chat files name as "BooPraGroupChat files"

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(time));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        if (imBlockedOrNot.equals("true")) {
            ifImBlocked(holder, senderId);
        } else {
            ifImNotBlocked(holder, senderId, type);
        }

        if (type.equals("text")) {
            //text message
            holder.textMessageLayout.setVisibility(View.VISIBLE); //text layout is visibile
            holder.imageMessageLayout.setVisibility(View.GONE); //image layout is not visibili
            holder.pdfMessageLayout.setVisibility(View.GONE); //pdf layout is not visibili
            holder.audioMessageLayout.setVisibility(View.GONE); //audio layout is not visibili
            holder.videoMessageLayout.setVisibility(View.GONE); //video layout is not visibili
            holder.imageTextMessageLayout.setVisibility(View.GONE); //image text layout is not visible
            holder.videoTextMessageLayout.setVisibility(View.GONE); //video text layout is not visible

            holder.showMessage.setText(message); //display message
            holder.messageTime.setText(dateTime); //set message time

            holder.showMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (imBlockedOrNot.equals("true")) {
                        toolbar1.setVisibility(View.GONE);
                    } else {
                        //calling method
                        typeText(chat, position);
                    }
                    return true;
                }
            });

            if (chatList.get(position).getMessageSeenOrNot().equals("true")) {
                holder.Message_seen_or_not.setImageResource(R.drawable.eye_opened);
            } else {
                holder.Message_seen_or_not.setImageResource(R.drawable.eye_closed);
            }

        } else if (type.equals("pdf") || type.equals("forwardedPdf")) {
            //pdf message
            holder.videoMessageLayout.setVisibility(View.GONE); //video layout is not visibili
            holder.audioMessageLayout.setVisibility(View.GONE); //audio layout is not visibili
            holder.textMessageLayout.setVisibility(View.GONE); //text layout is not visibile
            holder.imageMessageLayout.setVisibility(View.GONE); //image layout is not visibili
            holder.imageTextMessageLayout.setVisibility(View.GONE); //image text layout is not visible
            holder.pdfMessageLayout.setVisibility(View.VISIBLE); //pdf layout is  visibili
            holder.videoTextMessageLayout.setVisibility(View.GONE); //video text layout is not visible

            holder.pdfTime.setText(dateTime); // display image time
            holder.pdfFileName.setText(fileName); //display file name

            getPdfFileSize(message, holder);

            if (chatList.get(position).getMessageSeenOrNot().equals("true")) {
                holder.Pdf_Message__seen_or_not.setImageResource(R.drawable.eye_opened);
            } else {
                holder.Pdf_Message__seen_or_not.setImageResource(R.drawable.eye_closed);
            }

            holder.PdfFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // this 'if' for if any item is selected, for disable that selected item
                    if (holder.pdfSelected.getVisibility() == View.VISIBLE) {
                        toolbar1.setVisibility(View.GONE);
                        holder.pdfSelected.setVisibility(View.GONE);
                        return;
                    }

                    Intent intent = new Intent(context, PdfViewerPage.class);
                    intent.putExtra("pdfUrl", message);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.PdfFile.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (imBlockedOrNot.equals("true")) {
                        toolbar1.setVisibility(View.GONE);
                    } else {
                        //calling method
                        typePdf(chat, position, holder);
                    }

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


            if (!senderId.equals(fUser.getUid())) {
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
            } else {
                holder.pdfDownloadLayout.setVisibility(View.GONE);
            }

        } else if (type.equals("audio") || type.equals("forwardedAudio")) {
            //image message
            holder.audioMessageLayout.setVisibility(View.VISIBLE); //audio layout is visibili
            holder.textMessageLayout.setVisibility(View.GONE); //text layout is not visibile
            holder.imageMessageLayout.setVisibility(View.GONE); //image layout is not visibili
            holder.pdfMessageLayout.setVisibility(View.GONE); //pdf layout is not visibili
            holder.imageTextMessageLayout.setVisibility(View.GONE); //image text layout is not visible
            holder.videoMessageLayout.setVisibility(View.GONE); //video layout is not visibili
            holder.videoTextMessageLayout.setVisibility(View.GONE); //video text layout is not visible

            holder.audioTime.setText(dateTime); // display image time
            holder.audioFileName.setText(fileName); //display audio file name

            getAudioFileSize(message, holder);

            if (chatList.get(position).getMessageSeenOrNot().equals("true")) {
                holder.Audio_Message_seen_or_not.setImageResource(R.drawable.eye_opened);
            } else {
                holder.Audio_Message_seen_or_not.setImageResource(R.drawable.eye_closed);
            }

            holder.audioMessageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, AudioViewPage.class);
                    intent.putExtra("audioUrl", message);
                    intent.putExtra("audioFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    intent.putExtra("receiverId", receiverId);
                    intent.putExtra("senderId", senderId);
                    intent.putExtra("time", time);
                    intent.putExtra("type", type);
                    intent.putExtra("messageSeenOrNot", messageSeenOrNot);
                    context.startActivity(intent);
                }
            });

            holder.audioMessageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (imBlockedOrNot.equals("true")) {
                        toolbar1.setVisibility(View.GONE);
                    } else {
                        //calling method
                        typeAudio(chat, position);
                    }

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

            if (!senderId.equals(fUser.getUid())) {
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
            } else {
                holder.audioDownloadLayout.setVisibility(View.GONE);
            }

        } else if (type.equals("video") || type.equals("forwardedVideo")) {
            //video message
            holder.audioMessageLayout.setVisibility(View.GONE); //audio layout is visibili
            holder.textMessageLayout.setVisibility(View.GONE); //text layout is not visibile
            holder.imageMessageLayout.setVisibility(View.GONE); //image layout is not visibili
            holder.pdfMessageLayout.setVisibility(View.GONE); //pdf layout is not visibili
            holder.imageTextMessageLayout.setVisibility(View.GONE); //image text layout is not visible
            holder.videoMessageLayout.setVisibility(View.VISIBLE); //video layout is not visibili
            holder.videoTextMessageLayout.setVisibility(View.GONE); //video text layout is not visible

            holder.videoTime.setText(dateTime); // display image time

            getVideoFileSize(message, holder);

            // show video to sender and receiver
            Uri uri = Uri.parse(message);
            holder.showVideo.setVideoURI(uri);
            //set on prepared listener
            holder.showVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    holder.showVideo.seekTo(2000);
                    holder.preloadProgressBarForVideo.setVisibility(View.GONE);
                    //for set the total time duration of video
                    String totalDuration = milliSecondsToTimer(holder.showVideo.getDuration());
                    holder.timeDurationForVideo.setText(totalDuration);
                }
            });

            holder.videoMessageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    typeVideo(chat, position);
                    return true;
                }
            });

            if (chatList.get(position).getMessageSeenOrNot().equals("true")) {
                holder.Video_message_seen_or_not.setImageResource(R.drawable.eye_opened);
            } else {
                holder.Video_message_seen_or_not.setImageResource(R.drawable.eye_closed);
            }

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

                    if (imBlockedOrNot.equals("true")) {
                        toolbar1.setVisibility(View.GONE);
                    } else {
                        //calling method
                        typeVideo(chat, position);
                    }

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

            if (!senderId.equals(fUser.getUid())) {
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

            getVideoTextFileSize(message, holder);

            holder.showSecondaryVideoText.setText(secondaryMessage);
            //set video
            holder.showSecondaryVideo.setVideoURI(Uri.parse(message));
            //set on prepared listener
            holder.showSecondaryVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    holder.preloadProgressBarForVideoText.setVisibility(View.GONE);
                    holder.showSecondaryVideo.seekTo(2000);
                    //for set the total time duration of video
                    String totalDuration = milliSecondsToTimer(holder.showSecondaryVideo.getDuration());
                    holder.timeDurationForVideoText.setText(totalDuration);

                }
            });

            if (chatList.get(position).getMessageSeenOrNot().equals("true")) {
                holder.videoAndTextMessageSeenOrNot.setImageResource(R.drawable.eye_opened);
            } else {
                holder.videoAndTextMessageSeenOrNot.setImageResource(R.drawable.eye_closed);
            }

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

                    if (imBlockedOrNot.equals("true")) {
                        toolbar1.setVisibility(View.GONE);
                    } else {
                        //calling method
                        typeVideoText(chat, position);
                    }

                    return true;
                }
            });

            holder.showSecondaryVideoText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (imBlockedOrNot.equals("true")) {
                        toolbar1.setVisibility(View.GONE);
                    } else {
                        //calling method
                        typeVideoText(chat, position);
                    }

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

            if (!senderId.equals(fUser.getUid())) {
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
            } else {
                holder.videoTextDownloadLayout.setVisibility(View.GONE);
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
            holder.showSecondaryImageText.setText(secondaryMessage);

            getImageTextFileSize(message, holder);

            //display image message
            Glide.with(context)
                    .load(message)
                    .placeholder(R.drawable.progress_animation)
                    .into(holder.showSecondaryImage);

            if (chatList.get(position).getMessageSeenOrNot().equals("true")) {
                holder.imageAndTextMessageSeenOrNot.setImageResource(R.drawable.eye_opened);
            } else {
                holder.imageAndTextMessageSeenOrNot.setImageResource(R.drawable.eye_closed);
            }

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

                    if (imBlockedOrNot.equals("true")) {
                        toolbar1.setVisibility(View.GONE);
                    } else {
                        //calling method
                        typeImageText(chat, position, holder);
                    }


                    return true;
                }
            });

            holder.showSecondaryImageText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (imBlockedOrNot.equals("true")) {
                        toolbar1.setVisibility(View.GONE);
                    } else {
                        //calling method
                        typeImageText(chat, position, holder);
                    }

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

            if (!senderId.equals(fUser.getUid())) {
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
            } else {
                holder.imageTextDownloadLayout.setVisibility(View.GONE);
            }

        } else if (type.equals("image") || type.equals("forwardedImage")) {
            //image message
            holder.imageMessageLayout.setVisibility(View.VISIBLE); //image layout is visibili
            holder.audioMessageLayout.setVisibility(View.GONE); //audio layout is not visibili
            holder.textMessageLayout.setVisibility(View.GONE); //text layout is not visibile
            holder.pdfMessageLayout.setVisibility(View.GONE); //pdf layout is not visibili
            holder.imageTextMessageLayout.setVisibility(View.GONE); //image text layout is not visible
            holder.videoMessageLayout.setVisibility(View.GONE); //video layout is not visibili
            holder.videoTextMessageLayout.setVisibility(View.GONE); //video text layout is not visible

            holder.imageTime.setText(dateTime); // display image time

            getImageFileSize(message, holder);

            //display the image message
            Glide.with(context)
                    .load(message)
                    .placeholder(R.drawable.progress_animation)
                    .into(holder.showImage);

            if (chatList.get(position).getMessageSeenOrNot().equals("true")) {
                holder.Message_seen_or_not_Image.setImageResource(R.drawable.eye_opened);
            } else {
                holder.Message_seen_or_not_Image.setImageResource(R.drawable.eye_closed);
            }

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

                    if (imBlockedOrNot.equals("true")) {
                        toolbar1.setVisibility(View.GONE);
                    } else {
                        //calling method
                        typeImage(chat, position, holder);
                    }

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

            if (senderId.equals(fUser.getUid())) {
                holder.imageDownloadLayout.setVisibility(View.GONE);
            }
        }

    }

    private void getImageFileSize(String message, MyHolder holder) {

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

    private void getImageTextFileSize(String message, MyHolder holder) {
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

    private void getVideoTextFileSize(String message, MyHolder holder) {
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

    private void getVideoFileSize(String message, MyHolder holder) {
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

    private void getAudioFileSize(String message, MyHolder holder) {
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

    private void getPdfFileSize(String message, MyHolder holder) {
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
                            getImageFileSize(message, holder);

                        } else if (fileType.equals("imageAndText") || fileType.equals("forwardedImageAndText")) {
                            holder.pauseResumeForImageText.setVisibility(View.GONE);
                            holder.downloadImageText.setVisibility(View.VISIBLE);
                            holder.imageTextDownloadingLevelIndicatingBar.setProgress(100);
                            getImageTextFileSize(message, holder);

                        } else if (fileType.equals("pdf") || fileType.equals("forwardedPdf")) {
                            holder.pauseResumeForPdf.setVisibility(View.GONE);
                            holder.downloadPdf.setVisibility(View.VISIBLE);
                            holder.pdfDownloadingLevelIndicatingBar.setProgress(100);
                            getPdfFileSize(message, holder);

                        } else if (fileType.equals("audio") || fileType.equals("forwardedAudio")) {
                            holder.pauseResumeForAudio.setVisibility(View.GONE);
                            holder.downloadAudio.setVisibility(View.VISIBLE);
                            holder.audioDownloadingLevelIndicatingBar.setProgress(100);
                            getAudioFileSize(message, holder);

                        } else if (fileType.equals("video") || fileType.equals("forwardedVideo")) {
                            holder.pauseResumeForVideo.setVisibility(View.GONE);
                            holder.downloadVideo.setVisibility(View.VISIBLE);
                            holder.videoDownloadingLevelIndicatingBar.setProgress(100);
                            getVideoFileSize(message, holder);

                        } else if (fileType.equals("videoAndText") || fileType.equals("forwardedVideoAndText")) {
                            holder.pauseResumeForVideoText.setVisibility(View.GONE);
                            holder.downloadVideoText.setVisibility(View.VISIBLE);
                            holder.videoTextDownloadingLevelIndicatingBar.setProgress(100);
                            getVideoTextFileSize(message, holder);
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
                            holder.videoTextDownloadingLevelIndicatingBar.setProgress((int) percentage);
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

    private void ifImNotBlocked(MyHolder holder, String senderId, String type) {

        if (imageUrl.equals("")) {
            //disable profile image view
            //enable the profile text view
            String firstLetter = String.valueOf(name.charAt(0)).toLowerCase();

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if (!senderId.equals(firebaseAuth.getUid())) {

                holder.receiverProfileImage.setVisibility(View.GONE);
                holder.receiverProfileText.setVisibility(View.VISIBLE);

                Drawable text = holder.receiverProfileText.getBackground();
                text = DrawableCompat.wrap(text);

                if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                    DrawableCompat.setTint(text, Color.parseColor("#FF00BCD4")); // color blue

                    holder.receiverProfileText.setBackground(text);

                } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                    DrawableCompat.setTint(text, Color.parseColor("#FFFF5722")); //color red

                    holder.receiverProfileText.setBackground(text);

                } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                    DrawableCompat.setTint(text, Color.parseColor("#bfd200")); //color

                    holder.receiverProfileText.setBackground(text);

                } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                    DrawableCompat.setTint(text, Color.parseColor("#FF9800")); //color orange

                    holder.receiverProfileText.setBackground(text);

                } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                    DrawableCompat.setTint(text, Color.parseColor("#52b788")); //color old: #cccc00

                    holder.receiverProfileText.setBackground(text);

                } else if (firstLetter.equals("z")) {
                    DrawableCompat.setTint(text, Color.parseColor("#F33D73")); //color darker pink

                    holder.receiverProfileText.setBackground(text);

                } else {
                    DrawableCompat.setTint(text, Color.parseColor("#6c757d")); //color

                    holder.receiverProfileText.setBackground(text);
                }

                holder.receiverProfileText.setText(firstLetter);
            }

        } else {
            //enable profile image view
            //disable profile text view

            holder.receiverProfileText.setVisibility(View.GONE);
            holder.receiverProfileImage.setVisibility(View.VISIBLE);

            Picasso.get().load(imageUrl).into(holder.receiverProfileImage);
        }
    }

    private void ifImBlocked(MyHolder holder, String senderId) {
        String firstLetter = String.valueOf(name.charAt(0)).toLowerCase();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (!senderId.equals(firebaseAuth.getUid())) {

            holder.receiverProfileImage.setVisibility(View.GONE);
            holder.receiverProfileText.setVisibility(View.VISIBLE);

            Drawable text = holder.receiverProfileText.getBackground();
            text = DrawableCompat.wrap(text);


            if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                DrawableCompat.setTint(text, Color.parseColor("#FF00BCD4")); // color blue

                holder.receiverProfileText.setBackground(text);

            } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                DrawableCompat.setTint(text, Color.parseColor("#FFFF5722")); //color red

                holder.receiverProfileText.setBackground(text);

            } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                DrawableCompat.setTint(text, Color.parseColor("#bfd200")); //color

                holder.receiverProfileText.setBackground(text);

            } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                DrawableCompat.setTint(text, Color.parseColor("#FF9800")); //color orange

                holder.receiverProfileText.setBackground(text);

            } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                DrawableCompat.setTint(text, Color.parseColor("#52b788")); //color old: #cccc00

                holder.receiverProfileText.setBackground(text);

            } else if (firstLetter.equals("z")) {
                DrawableCompat.setTint(text, Color.parseColor("#F33D73")); //color darker pink

                holder.receiverProfileText.setBackground(text);

            } else {
                DrawableCompat.setTint(text, Color.parseColor("#6c757d")); //color

                holder.receiverProfileText.setBackground(text);
            }

            holder.receiverProfileText.setText(firstLetter);
        }

    }

    private void typeAudio(Chat chat, int position) {

        String message = chat.getMessage();
        String time = chat.getTime();
        String type = chat.getType();
        String senderId = chat.getSender();
        String receiverId = chat.getReceiver();
        String fileName = chat.getFileName();
        String messageSeenOrNot = chat.getMessageSeenOrNot();

        toolbar1.setVisibility(View.VISIBLE);
        toolbar2.setVisibility(View.GONE);

        copy.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(fUser.getUid()).child("Favourite").child("VideoMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        favouriteText = "alreadyInFavourite";
                        favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                        return;
                    }
                }

                favouriteText = "";
                favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        //on click to share selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String audioUri = chatList.get(position).getMessage();
                String requestCode = "a";

                Intent intent = new Intent(context, SendToActivity.class);
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
                    //removeFromFavourite(message);
                    Toast.makeText(context, R.string.already_in_favourite, Toast.LENGTH_SHORT).show();
                } else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", senderId);
                    hashMap.put("receiver", receiverId);
                    hashMap.put("message", message);
                    hashMap.put("fileName", fileName);
                    hashMap.put("time", time);
                    hashMap.put("type", type);
                    hashMap.put("messageSeenOrNot", messageSeenOrNot);

                    reference.child(fUser.getUid()).child("Favourite").child("VideoMessages").child(time).setValue(hashMap)
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
                }
                toolbar1.setVisibility(View.GONE);
            }
        });
    }

    private void typePdf(Chat chat, int position, MyHolder holder) {

        String message = chat.getMessage();
        String time = chat.getTime();
        String type = chat.getType();
        String senderId = chat.getSender();
        String receiverId = chat.getReceiver();
        String fileName = chat.getFileName();
        String messageSeenOrNot = chat.getMessageSeenOrNot();

        holder.pdfSelected.setVisibility(View.VISIBLE);// if user selected any item, to show to user selected this item
        toolbar1.setVisibility(View.VISIBLE);// this to show toolbar, tools like delete, copy etc..
        toolbar2.setVisibility(View.GONE);

        copy.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(fUser.getUid()).child("Favourite").child("PdfMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        favouriteText = "alreadyInFavourite";
                        favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                        return;
                    }
                }

                favouriteText = "";
                favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //on click to delete selected message
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteMessage(position);
                toolbar1.setVisibility(View.GONE);
                holder.pdfSelected.setVisibility(View.GONE);
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
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                intent.putExtra("fileName", fileName);
                context.startActivity(intent);
                ((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
                holder.pdfSelected.setVisibility(View.GONE);
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
                    hashMap.put("receiver", receiverId);
                    hashMap.put("message", message);
                    hashMap.put("fileName", fileName);
                    hashMap.put("time", time);
                    hashMap.put("type", type);
                    hashMap.put("messageSeenOrNot", messageSeenOrNot);

                    reference.child(fUser.getUid()).child("Favourite").child("PdfMessages").child(time).setValue(hashMap)
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
                }
                toolbar1.setVisibility(View.GONE);
                holder.pdfSelected.setVisibility(View.GONE);
            }
        });
    }

    private void typeVideo(Chat chat, int position) {

        String message = chat.getMessage();
        String time = chat.getTime();
        String type = chat.getType();
        String senderId = chat.getSender();
        String receiverId = chat.getReceiver();
        String fileName = chat.getFileName();
        String messageSeenOrNot = chat.getMessageSeenOrNot();

        toolbar1.setVisibility(View.VISIBLE);
        toolbar2.setVisibility(View.GONE);

        copy.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(fUser.getUid()).child("Favourite").child("VideoMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {

                    if (ds.exists()) {
                        favouriteText = "alreadyInFavourite";
                        favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                        return;
                    }
                }

                favouriteText = "";
                favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        //on click to share selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String videoUri = chatList.get(position).getMessage();
                String requestCode = "v";

                Intent intent = new Intent(context, SendToActivity.class);
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
                    hashMap.put("receiver", receiverId);
                    hashMap.put("message", message);
                    hashMap.put("time", time);
                    hashMap.put("fileName", fileName);
                    hashMap.put("type", type);
                    hashMap.put("messageSeenOrNot", messageSeenOrNot);

                    reference.child(fUser.getUid()).child("Favourite").child("VideoMessages").child(time).setValue(hashMap)
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
                }
                toolbar1.setVisibility(View.GONE);
            }
        });
    }

    private void typeVideoText(Chat chat, int position) {

        String message = chat.getMessage();
        String time = chat.getTime();
        String type = chat.getType();
        String senderId = chat.getSender();
        String receiverId = chat.getReceiver();
        String messageSeenOrNot = chat.getMessageSeenOrNot();
        String fileName = chat.getFileName();
        String secondaryMessage = chat.getSecondaryMessage();

        toolbar1.setVisibility(View.VISIBLE);
        toolbar2.setVisibility(View.GONE);

        copy.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(fUser.getUid()).child("Favourite").child("VideoMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        favouriteText = "alreadyInFavourite";
                        favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                        return;
                    }
                }

                favouriteText = "";
                favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                    hashMap.put("receiver", receiverId);
                    hashMap.put("message", message);
                    hashMap.put("secondaryMessage", secondaryMessage);
                    hashMap.put("time", time);
                    hashMap.put("fileName", fileName);
                    hashMap.put("type", type);
                    hashMap.put("messageSeenOrNot", messageSeenOrNot);

                    reference.child(fUser.getUid()).child("Favourite").child("VideoMessages").child(time).setValue(hashMap)
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
                }
                toolbar1.setVisibility(View.GONE);
            }
        });
    }

    private void typeImageText(Chat chat, int position, MyHolder holder) {

        String message = chat.getMessage();
        String time = chat.getTime();
        String type = chat.getType();
        String senderId = chat.getSender();
        String receiverId = chat.getReceiver();
        String messageSeenOrNot = chat.getMessageSeenOrNot();
        String fileName = chat.getFileName();
        String secondaryMessage = chat.getSecondaryMessage();

        toolbar1.setVisibility(View.VISIBLE);
        toolbar2.setVisibility(View.GONE);

        copy.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.VISIBLE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(fUser.getUid()).child("Favourite").child("ImageMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        favouriteText = "alreadyInFavourite";
                        favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                        return;
                    }
                }

                favouriteText = "";
                favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                deleteMessage(position);
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
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                intent.putExtra("fileName", fileName);
                context.startActivity(intent);
                ((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to share selected message to other apps
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = chatList.get(position).getSecondaryMessage();

                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.showSecondaryImage.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                shareImageWithText(bitmap, text);

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
                    hashMap.put("receiver", receiverId);
                    hashMap.put("message", message);
                    hashMap.put("secondaryMessage", secondaryMessage);
                    hashMap.put("time", time);
                    hashMap.put("fileName", fileName);
                    hashMap.put("type", type);
                    hashMap.put("messageSeenOrNot", messageSeenOrNot);

                    reference.child(fUser.getUid()).child("Favourite").child("ImageMessages").child(time).setValue(hashMap)
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
                }
                toolbar1.setVisibility(View.GONE);
            }
        });


    }

    private void typeImage(Chat chat, int position, MyHolder holder) {

        String message = chat.getMessage();
        String time = chat.getTime();
        String type = chat.getType();
        String senderId = chat.getSender();
        String receiverId = chat.getReceiver();
        String fileName = chat.getFileName();
        String messageSeenOrNot = chat.getMessageSeenOrNot();

        toolbar1.setVisibility(View.VISIBLE);
        toolbar2.setVisibility(View.GONE);

        copy.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.VISIBLE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(fUser.getUid()).child("Favourite").child("ImageMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        favouriteText = "alreadyInFavourite";
                        favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                        return;
                    }
                }

                favouriteText = "";
                favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        //on click to share selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String imageURI = chatList.get(position).getMessage();
                String text = "";
                String requestCode = "i"; // "i" means image

                Intent intent = new Intent(context, SendToActivity.class);
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

        //on click to share selected message to other apps
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
                    hashMap.put("receiver", receiverId);
                    hashMap.put("message", message);
                    hashMap.put("time", time);
                    hashMap.put("fileName", fileName);
                    hashMap.put("type", type);
                    hashMap.put("messageSeenOrNot", messageSeenOrNot);

                    reference.child(fUser.getUid()).child("Favourite").child("ImageMessages").child(time).setValue(hashMap)
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

                }
                toolbar1.setVisibility(View.GONE);
            }
        });
    }

    private void typeText(Chat chat, int position) {

        String message = chat.getMessage();
        String time = chat.getTime();
        String type = chat.getType();
        String senderId = chat.getSender();
        String receiverId = chat.getReceiver();
        String messageSeenOrNot = chat.getMessageSeenOrNot();

        toolbar1.setVisibility(View.VISIBLE);
        toolbar2.setVisibility(View.GONE);

        copy.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.VISIBLE);
        favourite.setVisibility(View.VISIBLE);

        //get message
        String text = chatList.get(position).getMessage();

        Query query = reference.child(fUser.getUid()).child("Favourite").child("TextMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        favouriteText = "alreadyInFavourite";
                        favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                        return;
                    }
                }

                favouriteText = "";
                favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                deleteMessage(position);
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
                ((Activity) context).finish();

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

        //on click add to favourite
        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (favouriteText.equals("alreadyInFavourite")) {
                    //removeFromFavourite(message);
                    Toast.makeText(context, R.string.already_in_favourite, Toast.LENGTH_SHORT).show();
                } else {

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", senderId);
                    hashMap.put("receiver", receiverId);
                    hashMap.put("message", message);
                    hashMap.put("time", time);
                    hashMap.put("type", "text");
                    hashMap.put("messageSeenOrNot", messageSeenOrNot);

                    reference.child(fUser.getUid()).child("Favourite").child("TextMessages").child(time).setValue(hashMap)
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

                }

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

    private void deleteMessage(int position) {

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //get time of clicked message
        //compare the time of the clicked message with all messages in chats
        //Where both values matches delete that message;
        String msgtime = chatList.get(position).getTime();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = reference.orderByChild("time").equalTo(msgtime);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {

                    if (ds.child("sender").getValue().equals(myUID)) {
                        //1.remove the message from chats
                        ds.getRef().removeValue();

                        // 2.Change the message that "this message was deleted"
                        /*HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted");
                        ds.getRef().updateChildren(hashMap);*/
                    } else {
                        Toast.makeText(context, R.string.you_can_delete_only_your_message, Toast.LENGTH_SHORT).show();
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
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get currently signed in user

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //for profile image
        CircleImageView receiverProfileImage;

        //for profile text
        TextView receiverProfileText;

        TextView showMessage,
                messageTime,
                imageTime,
                pdfTime,
                audioTime;

        TextView videoTime;

        RelativeLayout messageLayout,
                textMessageLayout,
                imageMessageLayout,
                pdfMessageLayout,
                audioMessageLayout,
                videoMessageLayout;

        ImageView Message_seen_or_not,
                Message_seen_or_not_Image,
                Pdf_Message__seen_or_not,
                Audio_Message_seen_or_not,
                Video_message_seen_or_not;

        // textview for file name
        TextView pdfFileName, audioFileName;

        //ShapeableImageView showImage;
        ImageView showImage;
        ImageView videoPlayButton;
        VideoView showVideo;

        //for image text layout
        RelativeLayout imageTextMessageLayout;
        ShapeableImageView showSecondaryImage;
        TextView showSecondaryImageText, timeForImageAndText;
        ImageButton downloadImageText;
        ImageView imageAndTextMessageSeenOrNot;

        //for video text layout
        RelativeLayout videoTextMessageLayout;
        VideoView showSecondaryVideo;
        TextView showSecondaryVideoText, timeForVideoAndText;
        ImageButton downloadVideoText;
        ImageView videoTextPlayButton, videoAndTextMessageSeenOrNot;

        // relative layout for open delete, copy toolbar
        RelativeLayout audioFile, PdfFile;

        //for selected item ui design
        LinearLayout pdfSelected;

        //download button for download files
        ImageButton downloadPdf, downloadImage, downloadAudio, downloadVideo;

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
        ProgressBar preloadProgressBarForVideoText,
                preloadProgressBarForVideo;

        //for file size
        TextView pdfFileSize, imageFileSize, imageTextFileSize, videoFileSize, videoTextFileSize, audioFileSize;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //for profile image and text type profile
            receiverProfileImage = itemView.findViewById(R.id.profile_image);
            receiverProfileText = itemView.findViewById(R.id.profile_text);

            //for text message
            showMessage = itemView.findViewById(R.id.showSenderReceiver_Msgs);
            messageTime = itemView.findViewById(R.id.Time);
            Message_seen_or_not = itemView.findViewById(R.id.seen_delivered);
            textMessageLayout = itemView.findViewById(R.id.text_message_layout);

            //for image message
            showImage = itemView.findViewById(R.id.show_Sender_Receiver_Image);
            imageTime = itemView.findViewById(R.id.TimeForImage);
            Message_seen_or_not_Image = itemView.findViewById(R.id.seen_delivered_For_Image);
            imageMessageLayout = itemView.findViewById(R.id.image_message_layout);
            downloadImage = itemView.findViewById(R.id.download_image);
            imageFileSize = itemView.findViewById(R.id.image_file_size);

            //for deleting the message
            messageLayout = itemView.findViewById(R.id.messageLayout);

            //for pdf message
            pdfTime = itemView.findViewById(R.id.TimeForPdf);
            Pdf_Message__seen_or_not = itemView.findViewById(R.id.seen_delivered_For_Pdf);
            pdfMessageLayout = itemView.findViewById(R.id.pdf_message_layout);
            downloadPdf = itemView.findViewById(R.id.download_pdf);
            PdfFile = itemView.findViewById(R.id.pdf_file);
            pdfFileName = itemView.findViewById(R.id.pdf_file_name);
            pdfFileSize = itemView.findViewById(R.id.pdf_file_size);

            // for audio message
            audioTime = itemView.findViewById(R.id.TimeForAudio);
            Audio_Message_seen_or_not = itemView.findViewById(R.id.seen_delivered_For_Audio);
            audioMessageLayout = itemView.findViewById(R.id.audio_message_layout);
            downloadAudio = itemView.findViewById(R.id.download_audio);
            audioFileName = itemView.findViewById(R.id.audio_file_name);
            audioFileSize = itemView.findViewById(R.id.audio_file_size);

            //for video message
            videoTime = itemView.findViewById(R.id.TimeForVideo);
            Video_message_seen_or_not = itemView.findViewById(R.id.seen_delivered_For_Video);
            videoMessageLayout = itemView.findViewById(R.id.video_message_layout);
            showVideo = itemView.findViewById(R.id.show_video);
            downloadVideo = itemView.findViewById(R.id.download_video);
            videoPlayButton = itemView.findViewById(R.id.video_play_btn);
            videoFileSize = itemView.findViewById(R.id.video_file_size);

            //for video and text
            timeForVideoAndText = itemView.findViewById(R.id.TimeForVideoAndText);
            videoAndTextMessageSeenOrNot = itemView.findViewById(R.id.seen_delivered_For_Video_And_Text);
            videoTextMessageLayout = itemView.findViewById(R.id.video_and_text__message_layout);
            showSecondaryVideo = itemView.findViewById(R.id.show_video_and_text);
            showSecondaryVideoText = itemView.findViewById(R.id.video_text);
            downloadVideoText = itemView.findViewById(R.id.download_video_and_text);
            videoTextPlayButton = itemView.findViewById(R.id.video_and_text_play_btn);
            videoTextFileSize = itemView.findViewById(R.id.video_text_file_size);

            //for image and text
            timeForImageAndText = itemView.findViewById(R.id.TimeForImageAndText);
            imageAndTextMessageSeenOrNot = itemView.findViewById(R.id.seen_delivered_For_Image_And_Text);
            imageTextMessageLayout = itemView.findViewById(R.id.image_and_text__message_layout);
            showSecondaryImage = itemView.findViewById(R.id.show_Sender_Receiver_Image_And_Text);
            showSecondaryImageText = itemView.findViewById(R.id.image_text);
            downloadImageText = itemView.findViewById(R.id.download_image_and_text);
            imageTextFileSize = itemView.findViewById(R.id.image_text_file_size);

            //for selected items ui design
            pdfSelected = itemView.findViewById(R.id.selected_item);

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