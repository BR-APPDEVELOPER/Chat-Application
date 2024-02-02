package com.example.booprachat.ChatViews;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.downloader.Status;
import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.squareup.picasso.Picasso;
import com.zolad.zoominimageview.ZoomInImageView;

import java.io.File;

public class ImageViewPage extends AppCompatActivity {

    Toolbar toolbar;
    ZoomInImageView imageView;
    ImageView downloadImage;

    LinearLayout downloadingLayout;
    TextView downloadingPercentage;
    ImageButton pauseResumeForImage, cancelDownload;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    int downloadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_page);

        //declaring ids
        imageView = findViewById(R.id.image_view);
        toolbar = findViewById(R.id.image_view_toolbar);
        downloadImage = findViewById(R.id.download_icon);
        downloadingLayout = findViewById(R.id.image_downloading_layout);
        downloadingPercentage = findViewById(R.id.percentage);
        pauseResumeForImage = findViewById(R.id.pause_resume_for_image);
        cancelDownload = findViewById(R.id.cancel_download);

        //action toolbar
        setSupportActionBar(toolbar);
        //enable back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");
        String imageFileName = intent.getStringExtra("imageFileName");
        String requestCode = intent.getStringExtra("requestCode");
        // if requestCode is "CA" means the data is coming from ChatAdapter.
        // if requestCode is "GCA" means the data is coming from GroupChatAdapter.
        getSupportActionBar().setTitle(imageFileName);

        Picasso.get().load(imageUrl).into(imageView);

        PRDownloaderConfig config = PRDownloaderConfig.newBuilder().setDatabaseEnabled(true).setReadTimeout(30_000).setConnectTimeout(30_000).build();
        PRDownloader.initialize(ImageViewPage.this, config);

        if (requestCode.equals("CA") || requestCode.equals("FMA") || requestCode.equals("MA")) {
            // this is for 1 to 1 chat
            File file = Environment.getExternalStorageDirectory();
            File myDir = new File(file, "BooPraChat/BooPraChat Images");
            if (myDir.exists()) {
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        String name = f.getName();

                        if (name.equals(imageFileName)) {
                            downloadImage.setVisibility(View.GONE);
                        } else {
                            downloadImage.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

        } else {
            // this is for group chat
            File file = Environment.getExternalStorageDirectory();
            File myDir = new File(file, "BooPraChat/BooPraGroupChat Images");
            if (myDir.exists()) {
                for (File f : myDir.listFiles()) {
                    if (f.isFile()) {
                        String name = f.getName();

                        if (name.equals(imageFileName)) {
                            downloadImage.setVisibility(View.GONE);
                        } else {
                            downloadImage.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }

        downloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestCode.equals("CA") || requestCode.equals("FMA") || requestCode.equals("MA")) {
                    // this is for 1 to 1 chat
                    downloadImage(imageUrl, imageFileName, "BooPraChat/BooPraChat Images");
                } else {
                    // this is for group chat
                    downloadImage(imageUrl, imageFileName, "BooPraChat/BooPraGroupChat Images");
                }
            }
        });

        pauseResumeForImage.setOnClickListener(new View.OnClickListener() {
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

    private void downloadImage(String imageUrl, String imageFileName, String fileDestination) {

        File file = Environment.getExternalStoragePublicDirectory(fileDestination);
        downloadId = PRDownloader.download(imageUrl, file.getPath(), imageFileName)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        downloadImage.setVisibility(View.GONE);
                        downloadingLayout.setVisibility(View.VISIBLE);
                        pauseResumeForImage.setImageResource(R.drawable.pause_downloading);
                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                        pauseResumeForImage.setImageResource(R.drawable.resume_downloading);
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        downloadingLayout.setVisibility(View.GONE);
                        downloadingPercentage.setText("");
                        downloadImage.setVisibility(View.VISIBLE);
                        Toast.makeText(ImageViewPage.this, R.string.download_canceled, Toast.LENGTH_SHORT).show();
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
                        pauseResumeForImage.setVisibility(View.GONE);
                        cancelDownload.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Error error) {
                        Toast.makeText(ImageViewPage.this, R.string.something_went_wrong_please_try_again_later, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto to previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        //for checking internet connetion
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, intentFilter);
        super.onStart();
    }
}