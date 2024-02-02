package com.example.booprachat.ChatViews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;

import java.io.File;

public class VideoViewPage extends AppCompatActivity {

    VideoView videoView;
    MediaController mediaController;
    Toolbar toolbar;
    ImageView downloadVideo;

    Handler handler = new Handler();

    LinearLayout downloadingLayout;
    TextView downloadingPercentage;
    ImageButton cancelDownload, pauseResumeForDownloading;
    ImageView pausePlayButtonForVideo;
    RelativeLayout mediaControllerForVideo;
    TextView timeDurationForVideo;
    ProgressBar preloadProgressBar;
    SeekBar seekBar;
    CountDownTimer countDownTimer;
    long timeLeftInMillieSeconds = 800; // half second

    int downloadId; // download id for download file

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view_page);

        //declaring ids
        videoView = findViewById(R.id.video_view);
        mediaController = new MediaController(this);
        toolbar = findViewById(R.id.video_view_toolbar);
        downloadVideo = findViewById(R.id.download_icon);
        downloadingLayout = findViewById(R.id.video_downloading_layout);
        downloadingPercentage = findViewById(R.id.percentage);
        cancelDownload = findViewById(R.id.cancel_download);
        pauseResumeForDownloading = findViewById(R.id.pause_resume_for_download);
        pausePlayButtonForVideo = findViewById(R.id.pause_play_button_for_video);
        mediaControllerForVideo = findViewById(R.id.media_controller_for_video);
        timeDurationForVideo = findViewById(R.id.time_duration_for_video);
        preloadProgressBar = findViewById(R.id.preload_progress_bar);
        seekBar = findViewById(R.id.seek_bar);
        seekBar.setMax(100);

        setSupportActionBar(toolbar);
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        String videoUrl = intent.getStringExtra("videoUrl");
        String videoFileName = intent.getStringExtra("videoFileName");
        String requestCode = intent.getStringExtra("requestCode");
        // if requestCode is "CA" means the data is coming from ChatAdapter.
        // if requestCode is "GCA" means the data is coming from GroupChatAdapter.

        getSupportActionBar().setTitle(videoFileName);

        //hide components
        pausePlayButtonForVideo.setVisibility(View.GONE);
        mediaControllerForVideo.setVisibility(View.GONE);

        /*while downloading file, now the app was killed by user then the downloading file will be paused and continue while the user download that file again.
        for this process we want to enable the database using this statement*/
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder().setDatabaseEnabled(true).setReadTimeout(30_000).setConnectTimeout(30_000).build();
        PRDownloader.initialize(VideoViewPage.this, config);

        if (requestCode.equals("CA") || requestCode.equals("FMA") || requestCode.equals("MA")) {
            // this is for 1 to 1 chat
            File file = Environment.getExternalStorageDirectory();
            File myDir = new File(file, "BooPraChat/BooPraChat Video");
            if (myDir.exists()) {
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        String name = f.getName();

                        if (name.equals(videoFileName)) {
                            downloadVideo.setVisibility(View.GONE);
                        } else {
                            downloadVideo.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

        } else {
            // this is for group chat
            File file = Environment.getExternalStorageDirectory();
            File myDir = new File(file, "BooPraChat/BooPraGroupChat Video");
            if (myDir.exists()) {
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        String name = f.getName();

                        if (name.equals(videoFileName)) {
                            if (name.equals(videoFileName)) {
                                downloadVideo.setVisibility(View.GONE);
                            } else {
                                downloadVideo.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        }

        //uri of video and setting the uri to video
        Uri uri = Uri.parse(videoUrl);
        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.start();
                startTimer();
                updateSeekBar();
                preloadProgressBar.setVisibility(View.GONE);
                pausePlayButtonForVideo.setImageResource(R.drawable.video_pause_button);
                pausePlayButtonForVideo.setVisibility(View.VISIBLE);
                mediaControllerForVideo.setVisibility(View.VISIBLE);

                videoView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            if (pausePlayButtonForVideo.getVisibility() == View.VISIBLE) {
                                pausePlayButtonForVideo.setVisibility(View.INVISIBLE);
                                mediaControllerForVideo.setVisibility(View.GONE);
                            } else {
                                pausePlayButtonForVideo.setVisibility(View.VISIBLE);
                                mediaControllerForVideo.setVisibility(View.VISIBLE);
                            }
                        }
                        return true;
                    }
                });

            }
        });

        //for pause and resume for video
        pausePlayButtonForVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoView.isPlaying()) {
                    handler.removeCallbacks(updater);
                    videoView.pause();
                    countDownTimer.cancel();
                    pausePlayButtonForVideo.setImageResource(R.drawable.video_play_button);
                } else {
                    videoView.start();
                    pausePlayButtonForVideo.setImageResource(R.drawable.video_pause_button);
                    updateSeekBar();
                }
            }
        });

        //on touch listener
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SeekBar seekBar = (SeekBar) view;
                int playPosition = (videoView.getDuration() / 100) * seekBar.getProgress();
                videoView.seekTo(playPosition);
                String currentDuration = milliSecondsToTimer(videoView.getCurrentPosition());
                String totalDuration = milliSecondsToTimer(videoView.getDuration());
                timeDurationForVideo.setText(String.format("%s/%s", currentDuration, totalDuration));
                countDownTimer.cancel();
                return false;
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.seekTo(0);
                seekBar.setProgress(0);
                pausePlayButtonForVideo.setVisibility(View.VISIBLE);
                pausePlayButtonForVideo.setImageResource(R.drawable.video_play_button);
            }
        });

        //for downloading video
        downloadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestCode.equals("CA") || requestCode.equals("FMA") || requestCode.equals("MA")) {
                    // this is for 1 to 1 chat
                    downloadVideo(videoUrl, videoFileName, "BooPraChat/BooPraChat Video");
                } else {
                    // this is for group chat
                    downloadVideo(videoUrl, videoFileName, "BooPraChat/BooPraGroupChat Video");
                }
            }
        });

        pauseResumeForDownloading.setOnClickListener(new View.OnClickListener() {
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

        cancelDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PRDownloader.cancel(downloadId);
            }
        });

    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillieSeconds, 1) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                pausePlayButtonForVideo.setVisibility(View.GONE);
                mediaControllerForVideo.setVisibility(View.GONE);
            }
        }.start();
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            String currentDuration = milliSecondsToTimer(videoView.getCurrentPosition());
            String totalDuration = milliSecondsToTimer(videoView.getDuration());
            timeDurationForVideo.setText(String.format("%s/%s", currentDuration, totalDuration));

            if (videoView.isPlaying()) {
            /*for, when the pause play button is visible to user,
              then we want to hide the play button after half a second*/
                if (pausePlayButtonForVideo.getVisibility() == View.VISIBLE) {
                    startTimer();
                }
            }

        }
    };

    private void updateSeekBar() {
        if (videoView.isPlaying()) {
            seekBar.setProgress((int) (((float) videoView.getCurrentPosition() / videoView.getDuration()) * 100));
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

    private void downloadVideo(String videoUrl, String videoFileName, String fileDestination) {

        File file = Environment.getExternalStoragePublicDirectory(fileDestination);
        downloadId = PRDownloader.download(videoUrl, file.getPath(), videoFileName)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        downloadVideo.setVisibility(View.GONE);
                        downloadingLayout.setVisibility(View.VISIBLE);
                        pauseResumeForDownloading.setImageResource(R.drawable.pause_downloading);
                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                        pauseResumeForDownloading.setImageResource(R.drawable.resume_downloading);
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        downloadingLayout.setVisibility(View.GONE);
                        downloadingPercentage.setText("");
                        downloadVideo.setVisibility(View.VISIBLE);
                        Toast.makeText(VideoViewPage.this, R.string.download_canceled, Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long percentage = progress.currentBytes * 100 / progress.totalBytes;
                        downloadingPercentage.setText(getString(R.string.downloading) + percentage + "%");
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        downloadingPercentage.setText(R.string.download_completed);
                        pauseResumeForDownloading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Error error) {
                        Toast.makeText(VideoViewPage.this, R.string.something_went_wrong_please_try_again_later, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // goto previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {

        //for checking internet connection
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, intentFilter);

        super.onStart();
    }
}