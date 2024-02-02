package com.example.booprachat.ChatViews;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.booprachat.R;
import com.example.booprachat.Utility.NetworkChangeListener;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PdfViewerPage extends AppCompatActivity {

    PDFView PdfView;
    Toolbar toolbar;

    //for checking internet connection
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer_page);

        //declaring ui ids
        PdfView = findViewById(R.id.pdf_view);
        toolbar = findViewById(R.id.pdf_viewer_toolbar);

        setSupportActionBar(toolbar);
        //enable back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get intent from group chat adapter and chat adapter
        Intent intent = getIntent();
        String pdfUrl = intent.getStringExtra("pdfUrl");
        String fileName = intent.getStringExtra("fileName");

        getSupportActionBar().setTitle(fileName);

        new RetrievePDFStream().execute(pdfUrl);

    }

    class RetrievePDFStream extends AsyncTask<String, Void, InputStream> {

        ProgressDialog pg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pg = new ProgressDialog(PdfViewerPage.this);
            pg.setMessage(getString(R.string.please_wait));
            pg.setCanceledOnTouchOutside(false);
            pg.show();
        }

        @Override
        protected InputStream doInBackground(String... strings) {

            InputStream inputStream = null;

            try {
                URL urlx = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) urlx.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }
            } catch (IOException e) {
                return null;
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            pg.dismiss();
            PdfView.fromStream(inputStream).defaultPage(0)
                    .enableAnnotationRendering(true)
                    .scrollHandle(new DefaultScrollHandle(PdfViewerPage.this))
                    .spacing(2)
                    .load();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previous page
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