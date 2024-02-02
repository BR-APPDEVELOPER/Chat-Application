package com.example.booprachat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.ChatViews.PdfViewerPage;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MediaDocumentAdapter extends RecyclerView.Adapter<MediaDocumentAdapter.MyHolder> {

    Context context;
    ArrayList<Chat> chatList;
    TextView noDocumentsFound;
    RecyclerView recyclerView;
    LinearLayout toolbar1;
    ImageView delete, forward, share, favourite;

    FirebaseUser fUser;
    DatabaseReference reference;
    String favouriteText;
    String secondaryRequestCode = "MDA"; // to find where the data is coming. "MDA" means MediaDocumentAdapter

    public MediaDocumentAdapter(Context context, ArrayList<Chat> chatList, TextView noDocumentsFound, RecyclerView recyclerView, LinearLayout toolbar1, ImageView delete, ImageView forward, ImageView share, ImageView favourite) {
        this.context = context;
        this.chatList = chatList;
        this.noDocumentsFound = noDocumentsFound;
        this.recyclerView = recyclerView;
        this.toolbar1 = toolbar1;
        this.delete = delete;
        this.forward = forward;
        this.share = share;
        this.favourite = favourite;

        reference = FirebaseDatabase.getInstance().getReference("Users");
        fUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_media_document, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Chat chat = chatList.get(position);
        //String mediaType = chat.getMediaType();
        String type = chat.getType();
        String message = chat.getMessage();
        String fileName = chat.getFileName();
        String time = chat.getTime();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(time));
        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();

        if (type.equals("pdf") || type.equals("forwardedPdf")) {
            holder.pdfFileLayout.setVisibility(View.VISIBLE);

            holder.pdfFileName.setText(fileName);
            holder.pdfFileDate.setText(date);

            holder.pdfFileLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PdfViewerPage.class);
                    intent.putExtra("pdfUrl", message);
                    intent.putExtra("fileName", fileName);
                    context.startActivity(intent);
                }
            });

            holder.pdfFileLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //calling method
                    typePdf(chat, position);

                    return true;
                }
            });
        } else {
            holder.pdfFileLayout.setVisibility(View.GONE);
        }
    }

    private void typePdf(Chat chat, int position) {

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

        Query query = reference.child(fUser.getUid()).child("Favourite").child("PdfMessages").orderByChild("message").equalTo(message);
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
                String pdfUri = chatList.get(position).getMessage();
                String requestCode = "p";

                Intent intent = new Intent(context, SendToActivity.class);
                intent.putExtra("pdfUri", pdfUri);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("secondaryRequestCode", secondaryRequestCode);
                intent.putExtra("fileName", fileName);
                context.startActivity(intent);

                toolbar1.setVisibility(View.GONE);
            }
        });

        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String timeStamp = "" + System.currentTimeMillis();

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

                    reference.child(fUser.getUid()).child("Favourite").child("PdfMessages").child(timeStamp).setValue(hashMap)
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

    class MyHolder extends RecyclerView.ViewHolder {

        TextView pdfFileDate;
        TextView pdfFileName;
        TextView noDocuments;

        RelativeLayout pdfFileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //declaring ui ids
            pdfFileDate = itemView.findViewById(R.id.media_pdf_file_date);
            pdfFileName = itemView.findViewById(R.id.media_pdf_file_name);
            pdfFileLayout = itemView.findViewById(R.id.media_pdf_file_layout);
            noDocuments = itemView.findViewById(R.id.no_documents_found);
        }
    }
}
