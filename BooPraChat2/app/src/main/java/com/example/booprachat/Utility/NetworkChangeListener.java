package com.example.booprachat.Utility;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import com.example.booprachat.R;

public class NetworkChangeListener extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Common.isConnectedToInternet(context)){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View layout_view = LayoutInflater.from(context).inflate(R.layout.check_internet_conneciton_dialog, null);
            builder.setView(layout_view);

            AppCompatButton retry = layout_view.findViewById(R.id.retry);
            // show dialog
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setCancelable(false);

            dialog.getWindow().setGravity(Gravity.CENTER);


            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    onReceive(context, intent);
                }
            });
        }

    }
}
