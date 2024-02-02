package com.example.booprachat.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booprachat.MessageActivity;
import com.example.booprachat.Model.Users;
import com.example.booprachat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParticipantsAddAdapter extends RecyclerView.Adapter<ParticipantsAddAdapter.HolderParticipantsAdd> {

    private Context context;
    private ArrayList<Users> usersList;
    private String groupId, myGroupRole; //creator|admin|participants
    private FirebaseAuth firebaseAuth;
    private ArrayList<String> namesList, IdsList;

    public ParticipantsAddAdapter(Context context, ArrayList<Users> usersList, String groupId, String myGroupRole) {
        this.context = context;
        this.usersList = usersList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;

        firebaseAuth = FirebaseAuth.getInstance();
    }


    @NonNull
    @Override
    public HolderParticipantsAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_add_participants, parent, false);
        return new HolderParticipantsAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantsAdd holder, int position) {
        //get data
        Users users = usersList.get(position);

        String Username = users.getName();
        String ProfileImage = users.getImage();
        String uid = users.getUid();

        //set data
        if (uid.equals(firebaseAuth.getUid())) {
            holder.Username.setText("Conscience (மனசாட்சி)");
        } else {
            holder.Username.setText(Username);
        }

        if (ProfileImage.equals("")) {
            holder.ProfileImage.setVisibility(View.GONE);
            holder.ProfileText.setVisibility(View.VISIBLE);

            String name = holder.Username.getText().toString();

            //char firstLetterLetter = name.charAt(0);
            String firstLetter = String.valueOf(name.charAt(0)).toLowerCase();

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

            Picasso.get().load(ProfileImage).into(holder.ProfileImage);
        }

        //if item on click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*check if user already add or not
                 * if added : show remove-participants/make-admin/remove-admin option (admin will not able to change role of creator)
                 * if not added : show add participants option*/

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                reference.child(groupId).child("participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    // user exists/participant
                                    String hisPreviousRole = "" + snapshot.child("role").getValue();

                                    //options to show in dialog
                                    String[] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle(R.string.choose_option);

                                    if (myGroupRole.equals("leader")) {
                                        if (hisPreviousRole.equals("commander")) {
                                            options = new String[]{context.getString(R.string.remove) + " " + Username + " " + context.getString(R.string.from_commander_role), context.getString(R.string.remove_commander) + " " + Username + " " + context.getString(R.string.from_team)};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    if (i == 0) {
                                                        //remove admin clicked
                                                        removeAdmin(users);
                                                    } else {
                                                        //remove user clicked
                                                        removeParticipant(users);
                                                    }
                                                }
                                            }).show();
                                        } else if (hisPreviousRole.equals("soldier")) {
                                            options = new String[]{context.getString(R.string.make_soldier) + " " + Username + " " + context.getString(R.string.to_commander_role), context.getString(R.string.remove_soldier) + " " + Username + " " + context.getString(R.string.from_team)};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    if (i == 0) {
                                                        //remove admin clicked
                                                        MakeAdmin(users);
                                                    } else {
                                                        //remove user clicked
                                                        removeParticipant(users);
                                                    }
                                                }
                                            }).show();
                                        }
                                    } else if (myGroupRole.equals("commander")) {
                                        if (hisPreviousRole.equals("leader")) {
                                            // iam admin , he is creator
                                            Toast.makeText(context, R.string.leader_of_team, Toast.LENGTH_SHORT).show();

                                        } else if (hisPreviousRole.equals("commander")) {
                                            //iam admin he admin too
                                            options = new String[]{context.getString(R.string.remove) + " " + Username + " " + context.getString(R.string.from_commander_role), context.getString(R.string.remove_commander) + " " + Username + " " + context.getString(R.string.from_team)};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    if (i == 0) {
                                                        //remove admin clicked
                                                        removeAdmin(users);
                                                    } else {
                                                        //remove user clicked
                                                        removeParticipant(users);
                                                    }
                                                }
                                            }).show();
                                        } else if (hisPreviousRole.equals("soldier")) {
                                            options = new String[]{context.getString(R.string.make_soldier) + " " + Username + " " + context.getString(R.string.to_commander_role), context.getString(R.string.remove_soldier) + " " + Username + " " + context.getString(R.string.from_team)};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    if (i == 0) {
                                                        //remove admin clicked
                                                        MakeAdmin(users);
                                                    } else {
                                                        //remove user clicked
                                                        removeParticipant(users);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }

                                } else {

                                    DatabaseReference checkUserBlockedYouOrNotReference = FirebaseDatabase.getInstance().getReference("Users");
                                    checkUserBlockedYouOrNotReference.child(uid).child("BlockedUsers").orderByChild("uid").equalTo(firebaseAuth.getUid())
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                                        if (ds.exists()) {
                                                            Toast.makeText(context, "You are not allow to add this solider", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }
                                                    }

                                                    //user doesn't exists// not participant: add
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                    builder.setTitle(R.string.add_soldier)
                                                            .setMessage(R.string.add_this_soldier_in_this_team)
                                                            .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    //add participant
                                                                    addParticipant(users);
                                                                }
                                                            })
                                                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    dialogInterface.dismiss();
                                                                }
                                                            }).show();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }
        });

        //if item on long clicked
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (uid.equals(firebaseAuth.getUid())) {
                    return true;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                String[] options = {context.getString(R.string.message) + " " + Username};

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            Intent intent = new Intent(context, MessageActivity.class);
                            intent.putExtra("receiverId", uid);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                        }
                    }
                }).show();

                return true;
            }
        });
        //calling method
        checkIfAlreadyExists(users, holder);
    }

    private void addParticipant(Users users) {
        //setup user data
        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", users.getUid());
        hashMap.put("role", "soldier");
        hashMap.put("time", "" + timestamp);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("participants").child(users.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //participant added successfully
                        Toast.makeText(context, R.string.soldier_added_successfully, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to add participant
                        Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void MakeAdmin(Users users) {
        //update user data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "commander");

        //update role in db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("participants").child(users.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //successfully updated from  participant to admin
                        Toast.makeText(context, R.string.now_this_soldier_promoted_to_commander, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to updated participant to admin
                        Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeAdmin(Users users) {
        //update user data from admin to participant

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "soldier");

        //update role in db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("participants").child(users.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //successfully updated from  participant to admin
                        Toast.makeText(context, R.string.now_this_commander_changed_to_soldier_role, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to updated participant to admin
                        Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeParticipant(Users users) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("participants").child(users.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //  user successfully removed from group
                        Toast.makeText(context, R.string.successfully_removed_from_team, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //  user failed removed from group
                        Toast.makeText(context, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfAlreadyExists(Users users, HolderParticipantsAdd holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("participants").child(users.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // already exixts
                            String hisRole = "" + snapshot.child("role").getValue();
                            //set data
                            if (hisRole.equals("leader")) {
                                holder.ParticipantsStatus.setText("leader (தலைவன்)");

                            } else if (hisRole.equals("commander")) {
                                holder.ParticipantsStatus.setText("commander (படைத்தலைவன்)");

                            } else {
                                holder.ParticipantsStatus.setText("soldier (சிப்பாய்)");
                            }


                        } else {
                            //doesn't exists
                            holder.ParticipantsStatus.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }


    class HolderParticipantsAdd extends RecyclerView.ViewHolder {

        private CircleImageView ProfileImage;
        private TextView ProfileText;
        private TextView Username, ParticipantsStatus;

        public HolderParticipantsAdd(@NonNull View itemView) {
            super(itemView);

            ProfileImage = itemView.findViewById(R.id.profile_image);
            ProfileText = itemView.findViewById(R.id.profile_text);
            Username = itemView.findViewById(R.id.username);
            ParticipantsStatus = itemView.findViewById(R.id.participants_status);

        }
    }
}
