package com.example.booprachat.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.AudioFullScreenActivity;
import com.example.booprachat.ChatViews.ImageViewPage;
import com.example.booprachat.ChatViews.VideoViewPage;
import com.example.booprachat.Model.Chat;
import com.example.booprachat.R;
import com.example.booprachat.ReceiveDataFromOtherApps.SendToActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MyHolder> {

    Context context;
    ArrayList<Chat> chatList;
    RecyclerView recyclerView;
    TextView mediaIsEmpty;
    LinearLayout toolbar1;
    ImageView delete, forward, share, favourite;

    MediaPlayer mediaPlayer;
    FirebaseUser fUser;
    DatabaseReference reference;
    String favouriteText;
    String timeStamp;
    String secondaryRequestCode = "MA"; // to find where the data is coming. "MA" means MediaAdapter

    public MediaAdapter(Context context, ArrayList<Chat> chatList, RecyclerView recyclerView, TextView mediaIsEmpty, LinearLayout toolbar1, ImageView delete, ImageView forward, ImageView share, ImageView favourite) {
        this.context = context;
        this.chatList = chatList;
        this.recyclerView = recyclerView;
        this.mediaIsEmpty = mediaIsEmpty;
        this.toolbar1 = toolbar1;
        this.delete = delete;
        this.forward = forward;
        this.share = share;
        this.favourite = favourite;

        reference = FirebaseDatabase.getInstance().getReference("Users");
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        timeStamp = "" + System.currentTimeMillis();
        mediaPlayer = new MediaPlayer();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_media, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Chat chat = chatList.get(position);
        //String mediaType = chat.getMediaType();
        String type = chat.getType();
        String message = chat.getMessage();
        String fileName = chat.getFileName();
        String requestCode = "MA";

        if (type.equals("image") || type.equals("forwardedImage") || type.equals("imageAndText") || type.equals("forwardedImageAndText")) {
            recyclerView.setVisibility(View.VISIBLE);
            mediaIsEmpty.setVisibility(View.GONE);
            holder.imageAndAudioLayout.setVisibility(View.VISIBLE);
            holder.mediaVideoLayout.setVisibility(View.GONE);
            holder.totalAudioTime.setVisibility(View.GONE);

            Picasso.get().load(message).into(holder.imageAndAudio);

            holder.imageAndAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ImageViewPage.class);
                    intent.putExtra("imageUrl", message);
                    intent.putExtra("imageFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.imageAndAudio.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeImage(chat, position, holder);

                    return true;
                }
            });

        } else if (type.equals("audio") || type.equals("forwardedAudio")) {
            recyclerView.setVisibility(View.VISIBLE);
            mediaIsEmpty.setVisibility(View.GONE);
            holder.imageAndAudioLayout.setVisibility(View.VISIBLE);
            holder.mediaVideoLayout.setVisibility(View.GONE);
            holder.totalAudioTime.setVisibility(View.VISIBLE);

            holder.imageAndAudio.setImageResource(R.drawable.ic_baseline_music_note_24);
            //calling method
            prepareMediaPlayer(holder, message);

            holder.imageAndAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, AudioFullScreenActivity.class);
                    intent.putExtra("audioUrl", message);
                    intent.putExtra("audioFileName", fileName);
                    intent.putExtra("requestCode", requestCode);
                    context.startActivity(intent);
                }
            });

            holder.imageAndAudio.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeAudio(chat, position);

                    return true;
                }
            });

        } else if (type.equals("video") || type.equals("forwardedVideo") || type.equals("videoAndText") || type.equals("forwardedVideoAndText")) {
            recyclerView.setVisibility(View.VISIBLE);
            mediaIsEmpty.setVisibility(View.GONE);
            holder.mediaVideoLayout.setVisibility(View.VISIBLE);
            holder.imageAndAudioLayout.setVisibility(View.GONE);
            holder.totalAudioTime.setVisibility(View.GONE);

            //set data
            holder.video.setVideoURI(Uri.parse(message));
            holder.video.seekTo(2000);

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

            holder.videoPlayButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeVideo(chat, position);

                    return true;
                }
            });

            holder.mediaVideoLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typeVideo(chat, position);

                    return true;
                }
            });

        }

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

        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(fUser.getUid()).child("Favourite").child("VideoMessages").orderByChild("message").equalTo(message);
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

                    reference.child(fUser.getUid()).child("Favourite").child("VideoMessages").child(timeStamp).setValue(hashMap)
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

    private void typeAudio(Chat chat, int position) {

        String message = chat.getMessage();
        String time = chat.getTime();
        String type = chat.getType();
        String senderId = chat.getSender();
        String receiverId = chat.getReceiver();
        String fileName = chat.getFileName();
        String messageSeenOrNot = chat.getMessageSeenOrNot();

        toolbar1.setVisibility(View.VISIBLE);

        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(fUser.getUid()).child("Favourite").child("VideoMessages").orderByChild("message").equalTo(message);
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

                    reference.child(fUser.getUid()).child("Favourite").child("VideoMessages").child(timeStamp).setValue(hashMap)
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

        delete.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        share.setVisibility(View.VISIBLE);
        favourite.setVisibility(View.VISIBLE);

        Query query = reference.child(fUser.getUid()).child("Favourite").child("ImageMessages").orderByChild("message").equalTo(message);
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
                String requestCode = "i";

                Intent intent = new Intent(context, SendToActivity.class);
                intent.putExtra("imageUri", imageURI);
                intent.putExtra("text", text);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                intent.putExtra("fileName", fileName);
                context.startActivity(intent);
                //((Activity) context).finish();

                toolbar1.setVisibility(View.GONE);
            }
        });

        //on click to share selected message to other apps
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.imageAndAudio.getDrawable();
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

                    reference.child(fUser.getUid()).child("Favourite").child("ImageMessages").child(timeStamp).setValue(hashMap)
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

    private void prepareMediaPlayer(MyHolder holder, String message) {
        try {
            mediaPlayer.setDataSource(message);
            mediaPlayer.prepare();
            holder.totalAudioTime.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
        } catch (Exception e) {

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

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView imageAndAudio;
        VideoView video;
        ImageView videoPlayButton;
        TextView totalAudioTime;

        RelativeLayout mediaVideoLayout, imageAndAudioLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //declaring ui ids
            //for image
            imageAndAudio = itemView.findViewById(R.id.media_image_and_audio);
            imageAndAudioLayout = itemView.findViewById(R.id.media_image_and_audio_layout);

            //for video
            video = itemView.findViewById(R.id.media_video);
            videoPlayButton = itemView.findViewById(R.id.play_button);
            mediaVideoLayout = itemView.findViewById(R.id.media_video_layout);

            //for audio
            totalAudioTime = itemView.findViewById(R.id.total_audio_time);
        }
    }
}
