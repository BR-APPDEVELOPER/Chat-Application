package com.example.booprachat.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.Model.Users;
import com.example.booprachat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SendToAdapter extends RecyclerView.Adapter<SendToAdapter.MyHolder> {

    Context context;
    private ArrayList<String> namesList, IdsList;
    String imageUri;
    String text;
    String requestCode;
    String pdfUri;
    String audioUri;
    String videoUri;
    String fileName;
    ArrayList<Uri> multipleImageUris;

    int uploadCount = 0;
    //String shareText;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    public SendToAdapter(Context context, ArrayList<String> namesList, ArrayList<String> idsList, String imageUri, String text, String requestCode, String pdfUri, String audioUri, String videoUri, String fileName, ArrayList<Uri> multipleImageUris) {
        this.context = context;
        this.namesList = namesList;
        IdsList = idsList;
        this.imageUri = imageUri;
        this.text = text;
        this.requestCode = requestCode;
        this.pdfUri = pdfUri;
        this.audioUri = audioUri;
        this.videoUri = videoUri;
        this.fileName = fileName;
        this.multipleImageUris = multipleImageUris;

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.send_to_list, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data and set data
        String userName = namesList.get(position);

        holder.Username.setText(userName);
        String receiverUserId = IdsList.get(position);

        holder.reference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);

                String receiverImage = users.getImage();

                if (receiverImage.equals("")) {
                    holder.ProfileImage.setVisibility(View.GONE);
                    holder.ProfileText.setVisibility(View.VISIBLE);

                    //char firstLetterLetter = name.charAt(0);
                    String firstLetter = String.valueOf(userName.charAt(0)).toLowerCase();

                    Drawable drawable = holder.ProfileText.getBackground();
                    drawable = DrawableCompat.wrap(drawable);

                    if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#FF00BCD4")); // color blue

                        holder.ProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#FFFF5722")); //color red

                        holder.ProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#bfd200")); //color

                        holder.ProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#FF9800")); //color orange

                        holder.ProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#52b788")); //color old: #cccc00

                        holder.ProfileText.setBackground(drawable);

                    } else if (firstLetter.equals("z")) {
                        DrawableCompat.setTint(drawable, Color.parseColor("#F33D73")); //color darker pink

                        holder.ProfileText.setBackground(drawable);

                    } else {
                        DrawableCompat.setTint(drawable, Color.parseColor("#6c757d")); //color

                        holder.ProfileText.setBackground(drawable);
                    }

                    //set text
                    holder.ProfileText.setText(firstLetter);

                } else {
                    holder.ProfileText.setVisibility(View.GONE);
                    holder.ProfileImage.setVisibility(View.VISIBLE);

                    Picasso.get().load(receiverImage).into(holder.ProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (pdfUri != null && requestCode.equals("p")) {
                    forwardPdf(receiverUserId);

                } else if (audioUri != null && requestCode.equals("a")) {
                    forwardAudio(receiverUserId);

                } else if (videoUri != null && requestCode.equals("v")) {
                    forwardVideo(receiverUserId);

                } else if (imageUri != null && text != null && requestCode.equals("it")) {
                    forwardImageText(receiverUserId);

                } else if (imageUri != null && !text.isEmpty() && requestCode.equals("IRAI")) {
                    sendImageAndText(receiverUserId);

                } else if (imageUri != null && text.isEmpty() && requestCode.equals("IRAI")) {
                    sendImage(receiverUserId);

                } else if (imageUri != null && text.equals("") && requestCode.equals("i")) {
                    forwardImage(receiverUserId);

                } else if (videoUri != null && text.isEmpty() && requestCode.equals("IRAV")) {
                    sendVideo(receiverUserId);

                } else if (videoUri != null && !text.isEmpty() && requestCode.equals("IRAV")) {
                    sendVideoAndText(receiverUserId);

                } else if (videoUri != null && text != null && requestCode.equals("VT")) {
                    forwardVideoAndText(receiverUserId);

                } else if (multipleImageUris != null && text.isEmpty() && requestCode.equals("IRAMI")) {
                    sendMultipleImages(receiverUserId);

                } else {
                    forwardOrSendText(receiverUserId);
                }

            }
        });
    }

    private void sendMultipleImages(String receiverUserId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.sending_images));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        for (uploadCount = 0; uploadCount < multipleImageUris.size(); uploadCount++) {
            Uri ImagesUris = multipleImageUris.get(uploadCount);

            String timestamp = "" + System.currentTimeMillis();

            String fileNameAndPath = "ChatImages/" + "MultipleImages/" + firebaseAuth.getUid() + "__" + receiverUserId + "__" + timestamp + fileName;
            //Chats node will be created that will contain all image sent via chat
            final StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);

            ref.putFile(ImagesUris).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //image uploaded
                    //get uri of uploaded image
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String downloadUri = uriTask.getResult().toString();

                    if (uriTask.isSuccessful()) {
                        //add image uri nad other info to database
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                        //setup required data
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", firebaseAuth.getUid());
                        hashMap.put("receiver", receiverUserId);
                        hashMap.put("message", downloadUri);
                        hashMap.put("fileName", fileName);
                        hashMap.put("time", timestamp);
                        hashMap.put("type", "image");
                        hashMap.put("messageSeenOrNot", "false");
                        //put this data to firebase
                        //creating "ChatLists" node in currentUsers details
                        reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                pg.dismiss();
                                databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                                databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // if upload failed
                    pg.dismiss();
                    Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    pg.setProgress(currentProgress);
                }
            });
        }
    }

    private void forwardVideoAndText(String receiverUserId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.forwarding_video));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        //add image uri nad other info to database
        String timestamp = "" + System.currentTimeMillis();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //setup required data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("receiver", receiverUserId);
        hashMap.put("message", videoUri);
        hashMap.put("fileName", fileName);
        hashMap.put("secondaryMessage", text);
        hashMap.put("time", timestamp);
        hashMap.put("type", "forwardedVideoAndText");
        hashMap.put("messageSeenOrNot", "false");
        //put this data to firebase
        //creating "ChatLists" node in currentUsers details
        reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                pg.dismiss();
                Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendVideo(String receiverUserId) {
        ProgressDialog pg = new ProgressDialog(context);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setTitle(R.string.sending_video);
        pg.setProgress(0);

        pg.show();

        String timestamp = System.currentTimeMillis() + "";

        String fileNameAndPath = "ChatVideo/" + firebaseAuth.getUid() + "__" + receiverUserId + "__" + timestamp + "__" + fileName;
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);

        ref.putFile(Uri.parse(videoUri))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(); //returns the path to root
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                            //setup required data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", firebaseAuth.getUid());
                            hashMap.put("receiver", receiverUserId);
                            hashMap.put("message", downloadUri);
                            hashMap.put("fileName", fileName);
                            hashMap.put("time", timestamp);
                            hashMap.put("type", "video");
                            hashMap.put("messageSeenOrNot", "false");
                            //put this data to firebase
                            //creating "ChatLists" node in currentUsers details
                            reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //for chat
                                    databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                                    databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);

                                    if (task.isSuccessful()) {
                                        pg.dismiss();
                                        Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                                    } else {
                                        pg.dismiss();
                                        Toast.makeText(context, R.string.failed_to_upload, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pg.dismiss();
                Toast.makeText(context, R.string.failed_to_upload, Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pg.setProgress(currentProgress);
                pg.setCanceledOnTouchOutside(false);
            }
        });
    }

    private void sendVideoAndText(String receiverUserId) {
        ProgressDialog pg = new ProgressDialog(context);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setTitle(R.string.sending_video);
        pg.setProgress(0);

        pg.show();

        String timestamp = System.currentTimeMillis() + "";


        String fileNameAndPath = "ChatVideo/" + firebaseAuth.getUid() + "__" + receiverUserId + "__" + timestamp + "__" + fileName;
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);

        ref.putFile(Uri.parse(videoUri))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(); //returns the path to root
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                            //setup required data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", firebaseAuth.getUid());
                            hashMap.put("receiver", receiverUserId);
                            hashMap.put("message", downloadUri);
                            hashMap.put("fileName", fileName);
                            hashMap.put("secondaryMessage", text);
                            hashMap.put("time", timestamp);
                            hashMap.put("type", "videoAndText");
                            hashMap.put("messageSeenOrNot", "false");
                            //put this data to firebase
                            //creating "ChatLists" node in currentUsers details
                            reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //for chat
                                    databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                                    databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);

                                    if (task.isSuccessful()) {
                                        pg.dismiss();
                                        Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                                    } else {
                                        pg.dismiss();
                                        Toast.makeText(context, R.string.failed_to_upload, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pg.dismiss();
                Toast.makeText(context, R.string.failed_to_upload, Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pg.setProgress(currentProgress);
                pg.setCanceledOnTouchOutside(false);
            }
        });
    }

    private void forwardImageText(String receiverUserId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.forwarding_image));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        //add image uri nad other info to database
        String timestamp = "" + System.currentTimeMillis();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //setup required data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("receiver", receiverUserId);
        hashMap.put("message", imageUri);
        hashMap.put("fileName", fileName);
        hashMap.put("secondaryMessage", text);
        hashMap.put("time", timestamp);
        hashMap.put("type", "forwardedImageAndText");
        hashMap.put("messageSeenOrNot", "false");
        //put this data to firebase
        //creating "ChatLists" node in currentUsers details
        reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                pg.dismiss();
                Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void forwardVideo(String receiverUserId) {
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.forwarding_video));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(); //returns the path to root
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //setup required data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("receiver", receiverUserId);
        hashMap.put("message", videoUri);
        hashMap.put("fileName", fileName);
        hashMap.put("time", timestamp);
        hashMap.put("type", "forwardedVideo");
        hashMap.put("messageSeenOrNot", "false");
        //put this data to firebase
        //creating "ChatLists" node in currentUsers details
        reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                pg.dismiss();
                Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void forwardAudio(String receiverUserId) {
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.forwarding_audio));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = System.currentTimeMillis() + "";

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(); //returns the path to root
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //setup required data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("receiver", receiverUserId);
        hashMap.put("message", audioUri);
        hashMap.put("fileName", fileName);
        hashMap.put("time", timestamp);
        hashMap.put("type", "forwardedAudio");
        hashMap.put("messageSeenOrNot", "false");
        //put this data to firebase
        //creating "ChatLists" node in currentUsers details
        reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                pg.dismiss();
                Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void forwardPdf(String receiverUserId) {
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.forwarding_pdf));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = System.currentTimeMillis() + "";

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(); //returns the path to root
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //setup required data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("receiver", receiverUserId);
        hashMap.put("message", pdfUri);
        hashMap.put("fileName", fileName);
        hashMap.put("time", timestamp);
        hashMap.put("type", "forwardedPdf");
        hashMap.put("messageSeenOrNot", "false");

        //put this data to firebase
        //creating "ChatLists" node in currentUsers details
        reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                pg.dismiss();
                Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void forwardImage(String receiverUserId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.forwarding_image));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = "" + System.currentTimeMillis();
        //add image uri nad other info to database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //setup required data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("receiver", receiverUserId);
        hashMap.put("message", imageUri);
        hashMap.put("fileName", fileName);
        hashMap.put("time", timestamp);
        hashMap.put("type", "forwardedImage");
        hashMap.put("messageSeenOrNot", "false");
        //put this data to firebase
        //creating "ChatLists" node in currentUsers details
        reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                pg.dismiss();
                Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendImageAndText(String receiverUserId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setMessage(context.getString(R.string.sending_image));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        String fileNameAndPath = "ChatImages/" + firebaseAuth.getUid() + "__" + receiverUserId + "__" + timestamp + "__" + fileName;
        //Chats node will be created that will contain all image sent via chat
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);

        ref.putFile(Uri.parse(imageUri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image uploaded
                //get uri of uploaded image
                pg.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {
                    //add image uri nad other info to database
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                    //setup required data
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", firebaseAuth.getUid());
                    hashMap.put("receiver", receiverUserId);
                    hashMap.put("message", downloadUri);
                    hashMap.put("fileName", fileName);
                    hashMap.put("secondaryMessage", text);
                    hashMap.put("time", timestamp);
                    hashMap.put("type", "imageAndText");
                    hashMap.put("messageSeenOrNot", "false");
                    //put this data to firebase
                    //creating "ChatLists" node in currentUsers details
                    reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                            databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                            Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // if upload failed
                pg.dismiss();
                Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pg.setProgress(currentProgress);
            }
        });
    }

    private void sendImage(String receiverUserId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setMessage(context.getString(R.string.sending_image));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        String fileNameAndPath = "ChatImages/" + firebaseAuth.getUid() + "__" + receiverUserId + "__" + timestamp + fileName;
        //Chats node will be created that will contain all image sent via chat
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);

        ref.putFile(Uri.parse(imageUri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image uploaded
                //get uri of uploaded image
                pg.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {
                    //add image uri nad other info to database
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                    //setup required data
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", firebaseAuth.getUid());
                    hashMap.put("receiver", receiverUserId);
                    hashMap.put("message", downloadUri);
                    hashMap.put("fileName", fileName);
                    hashMap.put("time", timestamp);
                    hashMap.put("type", "image");
                    hashMap.put("messageSeenOrNot", "false");
                    //put this data to firebase
                    //creating "ChatLists" node in currentUsers details
                    reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            databaseReference.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                            databaseReference.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                            Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // if upload failed
                pg.dismiss();
                Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pg.setProgress(currentProgress);
            }
        });
    }

    private void forwardOrSendText(String receiverUserId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.sending_text));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("receiver", receiverUserId);
        hashMap.put("message", text);
        hashMap.put("time", timestamp);
        hashMap.put("type", "text");
        hashMap.put("messageSeenOrNot", "false");

        //creating "ChatLists" node in currentUsers details
        reference.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                reference1.child(user.getUid()).child("ChatLists").child(receiverUserId).setValue(ServerValue.TIMESTAMP);
                reference1.child(receiverUserId).child("ChatLists").child(user.getUid()).setValue(ServerValue.TIMESTAMP);
                pg.dismiss();
                Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return namesList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView ProfileImage;
        TextView Username;
        TextView ProfileText;
        DatabaseReference reference;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //declaring ids
            ProfileImage = itemView.findViewById(R.id.profile_image);
            ProfileText = itemView.findViewById(R.id.profile_text);
            Username = itemView.findViewById(R.id.user_name);

            //firebase services
            reference = FirebaseDatabase.getInstance().getReference("Users");
        }
    }

}
