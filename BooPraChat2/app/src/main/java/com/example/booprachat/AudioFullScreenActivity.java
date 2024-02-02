package com.example.booprachat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.Status;
import com.example.booprachat.Utility.NetworkChangeListener;

import java.io.File;

public class AudioFullScreenActivity extends AppCompatActivity {

    Toolbar toolbar;
    MediaPlayer mediaPlayer;
    SeekBar seekBar;
    TextView audioPlayingTime, totalTimeOfAudio, audioName;
    Handler handler = new Handler();
    ImageView pausePlayButton;
    ImageButton downloadAudio, pauseResumeForAudio;
    RelativeLayout audioDownloadLayout;
    ProgressBar audioDownloadingLevelIndicatingBar;
    String audioUrl;
    int downloadId;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_full_screen);

        //declaring ui ids
        toolbar = findViewById(R.id.audio_full_screen_toolbar);
        mediaPlayer = new MediaPlayer();
        audioPlayingTime = findViewById(R.id.audio_playing_time);
        audioName = findViewById(R.id.audio_name);
        totalTimeOfAudio = findViewById(R.id.total_time_of_audio);
        pausePlayButton = findViewById(R.id.pause_play_button);
        pauseResumeForAudio = findViewById(R.id.pause_resume_for_audio);
        downloadAudio = findViewById(R.id.download_audio);
        audioDownloadLayout = findViewById(R.id.audio_download_layout);
        audioDownloadingLevelIndicatingBar = findViewById(R.id.audio_downloading_circle_progress_bar);
        seekBar = findViewById(R.id.seek_bar);
        seekBar.setMax(100);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        audioUrl = intent.getStringExtra("audioUrl");
        String audioFileName = intent.getStringExtra("audioFileName");

        audioName.setText(audioFileName);
        audioName.setSelected(true);
        audioName.setEllipsize(TextUtils.TruncateAt.MARQUEE);

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

        //for download audio file
        downloadAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                    Toast.makeText(AudioFullScreenActivity.this, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                    return;
                }

                if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                    Toast.makeText(AudioFullScreenActivity.this, R.string.at_a_time_you_can_download_only_one_file, Toast.LENGTH_LONG).show();
                    return;
                }

                downloadFiles(audioFileName, "BooPraChat/BooPraChat Audio");
            }
        });

        pauseResumeForAudio.setOnClickListener(new View.OnClickListener() {
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

        pauseResumeForAudio.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PRDownloader.cancel(downloadId);
                return true;
            }
        });

        /*checking the audio that user already downloaded or not
         * if the user already downloaded that audio, the download symbol will not be shown
         * if the user not downloaded that audio, the download symbol will be shown*/
        File file = Environment.getExternalStorageDirectory();
        File myDir = new File(file, "BooPraChat/BooPraChat Audio");
        if (myDir.exists()) {
            for (File f : myDir.listFiles()) {
                if (f.isFile()) {
                    String name = f.getName();
                    if (name.equals(audioFileName)) {
                        audioDownloadLayout.setVisibility(View.GONE);
                    } else {
                        audioDownloadLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    private void downloadFiles(String fileName, String fileDestination) {

        File file = Environment.getExternalStoragePublicDirectory(fileDestination);

        downloadId = PRDownloader.download(audioUrl, file.getPath(), fileName)
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

                        Toast.makeText(AudioFullScreenActivity.this, R.string.download_canceled, Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long percentage = progress.currentBytes * 100 / progress.totalBytes;

                        audioDownloadingLevelIndicatingBar.setProgress((int) percentage);

                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        pauseResumeForAudio.setImageResource(R.drawable.downloading_completed);

                    }

                    @Override
                    public void onError(Error e) {
                        Toast.makeText(AudioFullScreenActivity.this, R.string.something_went_wrong_please_try_again_later, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void prepareMediaPlayer() {
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            totalTimeOfAudio.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
        } catch (Exception e) {
            Toast.makeText(AudioFullScreenActivity.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        ///prepareMediaPlayer();

        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // goto previous page
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStop() {
        //for checking internet connetion
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

}