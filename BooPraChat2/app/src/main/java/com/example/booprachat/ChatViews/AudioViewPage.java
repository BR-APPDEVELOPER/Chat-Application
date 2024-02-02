package com.example.booprachat.ChatViews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.Status;
import com.example.booprachat.R;
import com.example.booprachat.ReceiveDataFromOtherApps.SendToActivity;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.HashMap;

public class AudioViewPage extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    SeekBar seekBar;
    TextView audioPlayingTime, totalTimeOfAudio, audioName, downloadingPercentage;
    Handler handler = new Handler();
    ImageView pausePlayButton, downloadAudio;
    ImageView favourite, forward;
    String audioUrl;
    boolean favouriteText;

    FirebaseAuth firebaseAuth;
    DatabaseReference reference;

    //for indicating the downloading level
    ProgressBar audioDownloadingLevelIndicatingBar;

    //for pause and resume the downloading files
    ImageButton pauseResumeForAudio;
    //this layout for visible the download button
    RelativeLayout audioDownloadLayout;

    int downloadId;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_view_page);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        getWindow().setGravity(Gravity.CENTER);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .9), (int) (height * .25));


        //declaring ids
        mediaPlayer = new MediaPlayer();
        audioPlayingTime = findViewById(R.id.audio_playing_time);
        totalTimeOfAudio = findViewById(R.id.total_time_of_audio);
        pausePlayButton = findViewById(R.id.pause_play_button);
        favourite = findViewById(R.id.favourite);
        forward = findViewById(R.id.forward);
        audioName = findViewById(R.id.audio_name);
        downloadAudio = findViewById(R.id.download_audio);
        audioDownloadingLevelIndicatingBar = findViewById(R.id.audio_downloading_circle_progress_bar);
        audioDownloadLayout = findViewById(R.id.audio_download_layout);
        pauseResumeForAudio = findViewById(R.id.pause_resume_for_audio);
        downloadingPercentage = findViewById(R.id.percentage);
        seekBar = findViewById(R.id.seek_bar);
        seekBar.setMax(100);

        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        Intent intent = getIntent();
        audioUrl = intent.getStringExtra("audioUrl");
        String audioFileName = intent.getStringExtra("audioFileName");
        String receiverId = intent.getStringExtra("receiverId");
        String senderId = intent.getStringExtra("senderId");
        String time = intent.getStringExtra("time");
        String type = intent.getStringExtra("audioFileName");
        String messageSeenOrNot = intent.getStringExtra("messageSeenOrNot");
        String requestCode = intent.getStringExtra("requestCode");
        // if requestCode is "CA" means the data is coming from ChatAdapter.
        // if requestCode is "GCA" means the data is coming from GroupChatAdapter.
        // if requestCode is "FMA" means the data is coming from Favourite Message Adapter.

        audioName.setText(audioFileName);
        audioName.setSelected(true);
        audioName.setEllipsize(TextUtils.TruncateAt.MARQUEE);

        if (requestCode.equals("CA") || requestCode.equals("FMA") || requestCode.equals("MA")) {
            // this is for 1 to 1 chat
            File file = Environment.getExternalStorageDirectory();
            File myDir = new File(file, "BooPraChat/BooPraChat Audio");
            if (myDir.exists()) {
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        String name = f.getName();

                        if (name.equals(audioFileName)) {
                            audioDownloadLayout.setVisibility(View.GONE);
                            downloadingPercentage.setVisibility(View.GONE);
                        } else {
                            audioDownloadLayout.setVisibility(View.VISIBLE);
                            downloadingPercentage.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

        } else {
            // this is for group chat
            File file = Environment.getExternalStorageDirectory();
            File myDir = new File(file, "BooPraChat/BooPraGroupChat Audio");
            if (myDir.exists()) {
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        String name = f.getName();

                        if (name.equals(audioFileName)) {
                            audioDownloadLayout.setVisibility(View.GONE);
                            downloadingPercentage.setVisibility(View.GONE);
                        } else {
                            audioDownloadLayout.setVisibility(View.VISIBLE);
                            downloadingPercentage.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }

        pausePlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    handler.removeCallbacks(updater);
                    mediaPlayer.pause();
                    pausePlayButton.setImageResource(R.drawable.ic_baseline_play_icon);
                } else {
                    mediaPlayer.start();
                    pausePlayButton.setImageResource(R.drawable.ic_baseline_pause_icon);
                    updateSeekBar();
                }
            }
        });

        //calling method
        prepareMediaPlayer();

        //on touch listener
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SeekBar seekBar = (SeekBar) view;
                int playPosition = (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
                mediaPlayer.seekTo(playPosition);
                audioPlayingTime.setText(milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
                return false;
            }
        });

        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                seekBar.setSecondaryProgress(i);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                seekBar.setProgress(0);
                pausePlayButton.setImageResource(R.drawable.ic_baseline_play_icon);
                audioPlayingTime.setText(R.string.zero);
                totalTimeOfAudio.setText(R.string.zero);
                mediaPlayer.reset();
                prepareMediaPlayer();
            }
        });

        downloadAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestCode.equals("CA") || requestCode.equals("FMA") || requestCode.equals("MA")) {
                    // this is for 1 to 1 chat
                    downloadAudio(audioFileName, "BooPraChat/BooPraChat Audio");

                } else {
                    // this is for group chat
                    downloadAudio(audioFileName, "BooPraChat/BooPraGroupChat Audio");
                }
            }
        });

        pauseResumeForAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                    PRDownloader.pause(downloadId);

                } else if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                    PRDownloader.resume(downloadId);

                }
            }
        });

        pauseResumeForAudio.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PRDownloader.cancel(downloadId);
                return true;
            }
        });


        pauseResumeForAudio.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PRDownloader.cancel(downloadId);
                return true;
            }
        });

        //on click to share selected message
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String requestCode = "a"; //"a" means the audio .mp3 file
                String secondaryRequestCode = "CA"; // to find where the data is coming. "CA" means ChatAdapter

                Intent intent = new Intent(AudioViewPage.this, SendToActivity.class);
                intent.putExtra("audioUri", audioUrl);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                intent.putExtra("fileName", audioFileName);
                startActivity(intent);
            }
        });

        if (requestCode.equals("FMA")) {
            favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
            favourite.setEnabled(false);

        } else {
            Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("VideoMessages").orderByChild("time").equalTo(time);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (ds.exists()) {
                            favouriteText = true;
                            favourite.setImageResource(R.drawable.ic_baseline_already_in_favorite);
                            return;
                        }
                    }

                    favouriteText = false;
                    favourite.setImageResource(R.drawable.ic_baseline_add_to_favorite);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            favourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (favouriteText == true) {
                        //removeFromFavourite(time);
                        //return;
                        Toast.makeText(AudioViewPage.this, R.string.already_in_favourite, Toast.LENGTH_SHORT).show();
                    } else {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", senderId);
                        hashMap.put("receiver", receiverId);
                        hashMap.put("message", audioUrl);
                        hashMap.put("fileName", audioFileName);
                        hashMap.put("time", time);
                        hashMap.put("type", type);
                        hashMap.put("messageSeenOrNot", messageSeenOrNot);

                        reference.child(firebaseAuth.getUid()).child("Favourite").child("VideoMessages").child(time).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(AudioViewPage.this, R.string.added_to_favourite, Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AudioViewPage.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

    private void removeFromFavourite(String time) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference.child(firebaseAuth.getUid()).child("Favourite").child("VideoMessages").orderByChild("time").equalTo(time);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AudioViewPage.this, "Removed from favourite", Toast.LENGTH_SHORT).show();
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

    private void downloadAudio(String audioFileName, String fileDestination) {

        File file = Environment.getExternalStoragePublicDirectory(fileDestination);

        downloadId = PRDownloader.download(audioUrl, file.getPath(), audioFileName)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        downloadAudio.setVisibility(View.GONE);
                        pauseResumeForAudio.setVisibility(View.VISIBLE);
                        pauseResumeForAudio.setImageResource(R.drawable.pause_downloading);

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                        pauseResumeForAudio.setImageResource(R.drawable.resume_downloading);
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        pauseResumeForAudio.setVisibility(View.GONE);
                        downloadAudio.setVisibility(View.VISIBLE);
                        audioDownloadingLevelIndicatingBar.setProgress(100);

                        Toast.makeText(AudioViewPage.this, R.string.download_canceled, Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long percentage = progress.currentBytes * 100 / progress.totalBytes;
                        audioDownloadingLevelIndicatingBar.setProgress((int) percentage);
                        downloadingPercentage.setText(percentage + "%");
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        downloadingPercentage.setText(R.string.completed);
                        pauseResumeForAudio.setImageResource(R.drawable.downloading_completed);

                    }

                    @Override
                    public void onError(Error e) {
                        Toast.makeText(AudioViewPage.this, R.string.something_went_wrong_please_try_again_later, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void prepareMediaPlayer() {
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            totalTimeOfAudio.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
        } catch (Exception e) {
            Toast.makeText(AudioViewPage.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            long currentDuration = mediaPlayer.getCurrentPosition();
            audioPlayingTime.setText(milliSecondsToTimer(currentDuration));
        }
    };

    private void updateSeekBar() {
        if (mediaPlayer.isPlaying()) {
            seekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));
            handler.postDelayed(updater, 1000);
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
    protected void onStart() {
        if (mediaPlayer.isPlaying()) {
            handler.removeCallbacks(updater);
            mediaPlayer.pause();
            pausePlayButton.setImageResource(R.drawable.ic_baseline_play_icon);
        } else {
            mediaPlayer.start();
            pausePlayButton.setImageResource(R.drawable.ic_baseline_pause_icon);
            updateSeekBar();
        }

        //for checking internet connetion
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, intentFilter);

        super.onStart();
    }

    @Override
    protected void onDestroy() {
        seekBar.setProgress(0);
        pausePlayButton.setImageResource(R.drawable.ic_baseline_play_icon);
        audioPlayingTime.setText(R.string.zero);
        totalTimeOfAudio.setText(R.string.zero);
        mediaPlayer.reset();
        //prepareMediaPlayer();

        super.onDestroy();
    }
}