package com.example.booprachat.ReceiveDataFromOtherApps;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AppCompatActivity;

import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;

import java.io.File;
import java.util.ArrayList;

public class ImageReceiveActivity extends AppCompatActivity {

    ImageView Image;
    VideoView Video;
    ImageSwitcher multipleImageView;
    ImageView leftButton;
    ImageView rightButton;
    EditText Text;
    ImageView Send;
    RelativeLayout multipleImagesLayout;

    CountDownTimer countDownTimer;
    ImageView pausePlayButtonForVideo;
    TextView playTimeDuration, totalVideoDuration;
    SeekBar seekBar;
    RelativeLayout mediaController;
    Handler handler = new Handler();
    long timeLeftInMillieSeconds = 800; // half second

    //uri
    Uri imageUri = null;
    Uri videoUri = null;
    ArrayList<Uri> ImagesUris = null;
    //string
    String imageFileName = null;
    String videoFileName = null;
    int position;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private String secondaryRequestCode = "IRA"; // "IRA" means Image Receiver Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_receive);

        //declaring ui ids
        Image = findViewById(R.id.image);
        Video = findViewById(R.id.video);
        pausePlayButtonForVideo = findViewById(R.id.video_play_btn);
        playTimeDuration = findViewById(R.id.play_time_duration);
        totalVideoDuration = findViewById(R.id.total_video_duration);
        mediaController = findViewById(R.id.media_controller_for_video);
        seekBar = findViewById(R.id.seek_bar);
        seekBar.setMax(100);
        multipleImageView = findViewById(R.id.multiple_images_view);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);
        Text = findViewById(R.id.text);
        Send = findViewById(R.id.send);
        multipleImagesLayout = findViewById(R.id.multiple_images_view_layout);

        //get intent
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        multipleImageView.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                ViewGroup.LayoutParams params = new ImageSwitcher.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                imageView.setLayoutParams(params);

                return imageView;
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position < ImagesUris.size() - 1) {
                    position++;
                    multipleImageView.setImageURI(ImagesUris.get(position));
                } else {
                    Toast.makeText(ImageReceiveActivity.this, R.string.no_more_images, Toast.LENGTH_SHORT).show();
                }
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position > 0) {
                    position--;
                    multipleImageView.setImageURI(ImagesUris.get(position));
                } else {
                    Toast.makeText(ImageReceiveActivity.this, R.string.no_previous_images, Toast.LENGTH_SHORT).show();
                }
            }
        });


        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                //receive text from other apps
                receiveText(intent);

                Video.setVisibility(View.GONE);
                mediaController.setVisibility(View.GONE);
                pausePlayButtonForVideo.setVisibility(View.GONE);
                multipleImagesLayout.setVisibility(View.GONE);
                Image.setVisibility(View.GONE);

            } else if (type.startsWith("image")) {
                //receive image from other apps
                Video.setVisibility(View.GONE);
                mediaController.setVisibility(View.GONE);
                pausePlayButtonForVideo.setVisibility(View.GONE);
                multipleImagesLayout.setVisibility(View.GONE);
                Image.setVisibility(View.VISIBLE);

                receiveImage(intent);

            } else if (type.startsWith("video")) {
                //receive video from other apps
                Image.setVisibility(View.GONE);
                multipleImagesLayout.setVisibility(View.GONE);
                pausePlayButtonForVideo.setVisibility(View.VISIBLE);
                Video.setVisibility(View.VISIBLE);
                mediaController.setVisibility(View.VISIBLE);

                receiveVideo(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {

            if (type.startsWith("image")) {

                Image.setVisibility(View.GONE);
                Video.setVisibility(View.GONE);
                mediaController.setVisibility(View.GONE);
                pausePlayButtonForVideo.setVisibility(View.GONE);
                Text.setVisibility(View.INVISIBLE);
                multipleImagesLayout.setVisibility(View.VISIBLE);

                handleMultipleImages(intent);
            }
        }

        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get data
                if (imageUri != null) {
                    String text = Text.getText().toString();
                    String requestCode = "IRAI"; //IRAI means request code from ImageReceiverActivity (type) Image
                    final String imageURI = imageUri != null ? imageUri.toString() : null;

                    //start new activity with data
                    Intent intent1 = new Intent(ImageReceiveActivity.this, SendToActivity.class);
                    intent1.putExtra("imageUri", imageURI);
                    intent1.putExtra("text", text);
                    intent1.putExtra("requestCode", requestCode);
                    intent1.putExtra("secondaryRequestCode", secondaryRequestCode);
                    intent1.putExtra("fileName", imageFileName);
                    startActivity(intent1);
                    finish();

                } else if (ImagesUris != null) {
                    String text = Text.getText().toString();
                    String requestCode = "IRAMI"; //IRAMI means request code from ImageReceiverActivity (type) Multiple Image
                    //final ArrayList multipleImageURI = ImagesUris != null ? ImagesUris.toString() : null;

                    //start new activity with data
                    Intent intent1 = new Intent(ImageReceiveActivity.this, SendToActivity.class);
                    intent1.putExtra("multipleImageUris", ImagesUris);
                    intent1.putExtra("text", text);
                    intent1.putExtra("requestCode", requestCode);
                    intent1.putExtra("secondaryRequestCode", secondaryRequestCode);
                    intent1.putExtra("fileName", imageFileName);
                    startActivity(intent1);
                    finish();

                } else {
                    String text = Text.getText().toString();
                    String requestCode = "IRAV"; //IRA means request code from ImageReceiverActivity (type) Video
                    final String videoURI = videoUri != null ? videoUri.toString() : null;

                    //start new activity with data
                    Intent intent1 = new Intent(ImageReceiveActivity.this, SendToActivity.class);
                    intent1.putExtra("videoUri", videoURI);
                    intent1.putExtra("text", text);
                    intent1.putExtra("requestCode", requestCode);
                    intent1.putExtra("secondaryRequestCode", secondaryRequestCode);
                    intent1.putExtra("fileName", videoFileName);
                    startActivity(intent1);
                    finish();
                }
            }
        });

    }

    private void handleMultipleImages(Intent intent) {
        ArrayList<Uri> imagesUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imagesUris != null) {
            ImagesUris = imagesUris;
            multipleImageView.setImageURI(imagesUris.get(0));
            position = 0;
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void receiveVideo(Intent intent) {
        Uri videoURI = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (videoURI != null) {
            videoUri = videoURI;

            String VideoUri = videoUri.toString(); //convert uri to String

            File myFile = new File(VideoUri);
            String path = myFile.getAbsolutePath();

            if (VideoUri.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(videoUri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        videoFileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (VideoUri.startsWith("file://")) {
                videoFileName = myFile.getName();
            }

            Uri uri = Uri.parse(String.valueOf(videoUri));

            //set image
            Video.setVideoURI(uri);

            Video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    String totalDuration = milliSecondsToTimer(Video.getDuration());
                    totalVideoDuration.setText(totalDuration);
                    pausePlayButtonForVideo.setImageResource(R.drawable.video_pause_button);
                    Video.start();
                    updateSeekBar();
                    startTimer();
                }
            });

            Video.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (pausePlayButtonForVideo.getVisibility() == View.VISIBLE) {
                            pausePlayButtonForVideo.setVisibility(View.INVISIBLE);
                        } else {
                            pausePlayButtonForVideo.setVisibility(View.VISIBLE);
                        }
                    }
                    return true;
                }
            });

            Video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Video.seekTo(0);
                    seekBar.setProgress(0);
                    playTimeDuration.setText("0:00");
                    countDownTimer.cancel();
                    pausePlayButtonForVideo.setVisibility(View.VISIBLE);
                    pausePlayButtonForVideo.setImageResource(R.drawable.video_play_button);
                }
            });

            //on touch listener
            seekBar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    SeekBar seekBar = (SeekBar) view;
                    int playPosition = (Video.getDuration() / 100) * seekBar.getProgress();
                    Video.seekTo(playPosition);
                    String currentDuration = milliSecondsToTimer(Video.getCurrentPosition());
                    playTimeDuration.setText(String.format("%s", currentDuration));
                    return false;
                }
            });

            pausePlayButtonForVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Video.isPlaying()) {
                        Video.pause();
                        countDownTimer.cancel();
                        pausePlayButtonForVideo.setImageResource(R.drawable.video_play_button);
                    } else {
                        Video.start();
                        pausePlayButtonForVideo.setImageResource(R.drawable.video_pause_button);
                        updateSeekBar();
                    }
                }
            });
        }
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            String currentDuration = milliSecondsToTimer(Video.getCurrentPosition());
            playTimeDuration.setText(String.format("%s", currentDuration));

            if (Video.isPlaying()) {
            /*for, when the pause play button is visible to user,
              then we want to hide the play button after half a second*/
                if (pausePlayButtonForVideo.getVisibility() == View.VISIBLE) {
                    startTimer();
                }
            }

        }
    };

    private void updateSeekBar() {
        if (Video.isPlaying()) {
            seekBar.setProgress((int) (((float) Video.getCurrentPosition() / Video.getDuration()) * 100));
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

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillieSeconds, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                pausePlayButtonForVideo.setVisibility(View.GONE);
            }
        }.start();
    }

    private void receiveText(Intent intent) {
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (text != null) {
            Text.setText(text);

        }
    }

    private void receiveImage(Intent intent) {
        Uri imageURI = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageURI != null) {
            imageUri = imageURI;

            String ImageUri = imageUri.toString(); //convert uri to String

            File myFile = new File(ImageUri);
            String path = myFile.getAbsolutePath();

            if (ImageUri.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(imageUri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        imageFileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (ImageUri.startsWith("file://")) {
                imageFileName = myFile.getName();
            }

            //set image
            Image.setImageURI(imageUri);

        }
    }

    @Override
    protected void onStart() {
        //for checking internet connection
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, intentFilter);

        super.onStart();
    }

    @Override
    protected void onStop() {
        //for checking internet connection
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}