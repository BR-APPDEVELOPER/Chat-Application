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

import com.example.booprachat.Model.GroupChatList;
import com.example.booprachat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SendToGroupAdapter extends RecyclerView.Adapter<SendToGroupAdapter.MyHolder> {

    private Context context;
    private ArrayList<GroupChatList> groupChatLists;
    private String text;
    private String requestCode;
    private String pdfUri;
    private String imageUri;
    private String audioUri;
    private String videoUri;
    private String fileName;
    private ArrayList<Uri> multipleImageUris;

    private int uploadCount = 0;
    private FirebaseAuth firebaseAuth;

    public SendToGroupAdapter(Context context, ArrayList<GroupChatList> groupChatLists, String text, String requestCode, String pdfUri, String imageUri, String audioUri, String videoUri, String fileName, ArrayList<Uri> multipleImageUris) {
        this.context = context;
        this.groupChatLists = groupChatLists;
        this.text = text;
        this.requestCode = requestCode;
        this.pdfUri = pdfUri;
        this.imageUri = imageUri;
        this.audioUri = audioUri;
        this.videoUri = videoUri;
        this.fileName = fileName;
        this.multipleImageUris = multipleImageUris;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.send_to_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        GroupChatList groupList = groupChatLists.get(position);
        String groupIcon = groupList.getGroupIcon();
        String groupTitle = groupList.getGroupTitle();
        String groupId = groupList.getGroupId();

        //set data
        holder.groupName.setText(groupTitle);//set group name

        try {
            //if there is a group icon set that icon
            holder.groupIcon.setVisibility(View.VISIBLE);
            holder.profileText.setVisibility(View.GONE);

            Picasso.get().load(groupIcon).into(holder.groupIcon);

        } catch (Exception e) {
            // if there is no icon set first letter of group name
            holder.groupIcon.setVisibility(View.GONE);
            holder.profileText.setVisibility(View.VISIBLE);

            String firstLetter = String.valueOf(groupTitle.charAt(0)).toLowerCase();

            Drawable drawable = holder.profileText.getBackground();
            drawable = DrawableCompat.wrap(drawable);

            if (firstLetter.equals("a") || firstLetter.equals("f") || firstLetter.equals("k") || firstLetter.equals("p") || firstLetter.equals("u")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#FF00BCD4")); // color blue

                holder.profileText.setBackground(drawable);

            } else if (firstLetter.equals("b") || firstLetter.equals("g") || firstLetter.equals("l") || firstLetter.equals("q") || firstLetter.equals("v")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#FFFF5722")); //color red

                holder.profileText.setBackground(drawable);

            } else if (firstLetter.equals("c") || firstLetter.equals("h") || firstLetter.equals("m") || firstLetter.equals("r") || firstLetter.equals("w")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#bfd200")); //color

                holder.profileText.setBackground(drawable);

            } else if (firstLetter.equals("d") || firstLetter.equals("i") || firstLetter.equals("n") || firstLetter.equals("s") || firstLetter.equals("x")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#FF9800")); //color orange

                holder.profileText.setBackground(drawable);

            } else if (firstLetter.equals("e") || firstLetter.equals("j") || firstLetter.equals("o") || firstLetter.equals("t") || firstLetter.equals("y")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#52b788")); //color old: #cccc00

                holder.profileText.setBackground(drawable);

            } else if (firstLetter.equals("z")) {
                DrawableCompat.setTint(drawable, Color.parseColor("#F33D73")); //color darker pink

                holder.profileText.setBackground(drawable);

            } else {
                DrawableCompat.setTint(drawable, Color.parseColor("#6c757d")); //color

                holder.profileText.setBackground(drawable);
            }

            //set text
            holder.profileText.setText(firstLetter);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (pdfUri != null && requestCode.equals("p")) {
                    forwardPdf(groupId);

                } else if (audioUri != null && requestCode.equals("a")) {
                    forwardAudio(groupId);

                } else if (videoUri != null && requestCode.equals("v")) {
                    forwardVideo(groupId);

                } else if (imageUri != null && text != null && requestCode.equals("it")) {
                    forwardImageText(groupId);

                } else if (imageUri != null && !text.isEmpty() && requestCode.equals("IRAI")) {
                    sendImageAndText(groupId);

                } else if (imageUri != null && text.isEmpty() && requestCode.equals("IRAI")) {
                    sendImage(groupId);

                } else if (imageUri != null && text.equals("") && requestCode.equals("i")) {
                    forwardImage(groupId);

                } else if (videoUri != null && text.isEmpty() && requestCode.equals("IRAV")) {
                    sendVideo(groupId);

                } else if (videoUri != null && !text.isEmpty() && requestCode.equals("IRAV")) {
                    sendVideoAndText(groupId);

                } else if (videoUri != null && text != null && requestCode.equals("VT")) {
                    forwardVideoAndText(groupId);

                } else if (multipleImageUris != null && text.isEmpty() && requestCode.equals("IRAMI")) {
                    sendMultipleImages(groupId);

                } else {
                    forwardOrSendText(groupId);
                }
            }
        });

    }

    private void sendMultipleImages(String groupId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.sending_images));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        for (uploadCount = 0; uploadCount < multipleImageUris.size(); uploadCount++) {

            Uri ImagesUris = multipleImageUris.get(uploadCount);

            String timestamp = "" + System.currentTimeMillis();

            String fileNameAndPath = "GroupChatImages/" + "MultipleImages" + "image_" + groupId + "_time_" + timestamp + "__" + fileName;
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
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", firebaseAuth.getUid());
                        hashMap.put("message", downloadUri);
                        hashMap.put("time", timestamp);
                        hashMap.put("type", "image");
                        hashMap.put("fileName", fileName);

                        pg.dismiss();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                        reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //store, the message is seen by sender by default
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid", firebaseAuth.getUid());
                                reference.child(groupId).child("Messages").child(timestamp).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pg.dismiss();
                                Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            });
        }
    }

    private void forwardVideoAndText(String groupId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.forwarding_video));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        //add image uri nad other info to database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", videoUri);
        hashMap.put("time", timestamp);
        hashMap.put("type", "forwardedVideoAndText");
        hashMap.put("secondaryMessage", text);
        hashMap.put("fileName", fileName);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //store, the message is seen by sender by default
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", firebaseAuth.getUid());
                        reference.child(groupId).child("Messages").child(timestamp).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);

                        pg.dismiss();
                        Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pg.dismiss();
                        Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void sendVideoAndText(String groupId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setTitle(R.string.sending_video);
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        String fileNameAndPath = "GroupChatVideo/" + groupId + "_time_" + timestamp + "_" + fileName;

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putFile(Uri.parse(videoUri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", firebaseAuth.getUid());
                    hashMap.put("message", downloadUri);
                    hashMap.put("time", timestamp);
                    hashMap.put("type", "videoAndText");
                    hashMap.put("secondaryMessage", text);
                    hashMap.put("fileName", fileName);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                    reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //store, the message is seen by sender by default
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("uid", firebaseAuth.getUid());
                                    reference.child(groupId).child("Messages").child(timestamp).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);

                                    Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void sendVideo(String groupId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setCanceledOnTouchOutside(false);
        pg.setTitle(R.string.sending_video);
        pg.setProgress(0);

        pg.show();

        String timestamp = System.currentTimeMillis() + "";

        String fileNameAndPath = "GroupChatVideo/" + groupId + "__" + timestamp + "__" + fileName;
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);

        ref.putFile(Uri.parse(videoUri))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", firebaseAuth.getUid());
                            hashMap.put("message", downloadUri);
                            hashMap.put("time", timestamp);
                            hashMap.put("type", "video");
                            hashMap.put("fileName", fileName);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                            reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //store, the message is seen by sender by default
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("uid", firebaseAuth.getUid());
                                            reference.child(groupId).child("Messages").child(timestamp).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);

                                            pg.dismiss();
                                            Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pg.dismiss();
                                            Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pg.dismiss();
                Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pg.setProgress(currentProgress);
            }
        });
    }

    private void forwardImageText(String groupId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.forwarding_image));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        //add image uri nad other info to database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", imageUri);
        hashMap.put("time", timestamp);
        hashMap.put("type", "imageAndText");
        hashMap.put("secondaryMessage", text);
        hashMap.put("fileName", fileName);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //store, the message is seen by sender by default
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", firebaseAuth.getUid());
                        reference.child(groupId).child("Messages").child(timestamp).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);

                        pg.dismiss();
                        Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pg.dismiss();
                        Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void forwardVideo(String groupId) {
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.forwarding_video));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", videoUri);
        hashMap.put("time", timestamp);
        hashMap.put("type", "forwardedVideo");
        hashMap.put("fileName", fileName);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //store, the message is seen by sender by default
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", firebaseAuth.getUid());
                        reference.child(groupId).child("Messages").child(timestamp).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);

                        pg.dismiss();
                        Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pg.dismiss();
                        Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void forwardAudio(String groupId) {
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.forwarding_audio));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = System.currentTimeMillis() + "";

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", audioUri);
        hashMap.put("time", timestamp);
        hashMap.put("type", "forwardedAudio");
        hashMap.put("fileName", fileName);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //store, the message is seen by sender by default
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", firebaseAuth.getUid());
                        reference.child(groupId).child("Messages").child(timestamp).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);

                        pg.dismiss();
                        Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pg.dismiss();
                        Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void forwardPdf(String groupId) {
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.forwarding_pdf));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = System.currentTimeMillis() + "";

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", pdfUri);
        hashMap.put("time", timestamp);
        hashMap.put("type", "forwardedPdf");
        hashMap.put("fileName", fileName);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //store, the message is seen by sender by default
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", firebaseAuth.getUid());
                        reference.child(groupId).child("Messages").child(timestamp).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);

                        pg.dismiss();
                        Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pg.dismiss();
                        Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void forwardImage(String groupId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.forwarding_image));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", imageUri);
        hashMap.put("time", timestamp);
        hashMap.put("type", "forwardedImage");
        hashMap.put("fileName", fileName);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {//store, the message is seen by sender by default
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", firebaseAuth.getUid());
                        reference.child(groupId).child("Messages").child(timestamp).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);

                        pg.dismiss();
                        Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pg.dismiss();
                        Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendImageAndText(String groupId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setTitle(R.string.sending_image);
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        String fileNameAndPath = "GroupChatImages/" + "image_" + groupId + "_time_" + timestamp;

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
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", firebaseAuth.getUid());
                    hashMap.put("message", downloadUri);
                    hashMap.put("time", timestamp);
                    hashMap.put("type", "imageAndText");
                    hashMap.put("secondaryMessage", text);
                    hashMap.put("fileName", fileName);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                    reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("uid", firebaseAuth.getUid());
                                    reference.child(groupId).child("Messages").child(timestamp).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);

                                    Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void sendImage(String groupId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pg.setMessage(context.getString(R.string.sending_image));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = "" + System.currentTimeMillis();

        String fileNameAndPath = "GroupChatImages/" + "image_" + groupId + "_time_" + timestamp + "__" + fileName;

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
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", firebaseAuth.getUid());
                    hashMap.put("message", downloadUri);
                    hashMap.put("time", timestamp);
                    hashMap.put("type", "image");
                    hashMap.put("fileName", fileName);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                    reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //store, the message is seen by sender by default
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("uid", firebaseAuth.getUid());
                                    reference.child(groupId).child("Messages").child(timestamp).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);

                                    Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        ;
    }

    private void forwardOrSendText(String groupId) {
        //progress dialog
        ProgressDialog pg = new ProgressDialog(context);
        pg.setMessage(context.getString(R.string.sending_text));
        pg.setCanceledOnTouchOutside(false);
        pg.show();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", text);
        hashMap.put("time", timestamp);
        hashMap.put("type", "text");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //store, the message is seen by sender by default
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", firebaseAuth.getUid());
                        reference.child(groupId).child("Messages").child(timestamp).child("messageSeenMembers").child(firebaseAuth.getUid()).setValue(hashMap);

                        pg.dismiss();
                        Toast.makeText(context, R.string.successfully_send, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pg.dismiss();
                        Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        private TextView groupName;
        private TextView profileText;
        private CircleImageView groupIcon;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            groupName = itemView.findViewById(R.id.user_name);
            groupIcon = itemView.findViewById(R.id.profile_image);
            profileText = itemView.findViewById(R.id.profile_text);
        }
    }
}
