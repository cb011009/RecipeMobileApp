package com.example.recipemobileapp;

import static androidx.camera.core.impl.utils.ContextUtil.getApplicationContext;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;



public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private List<String> friendUids; // List of friend UIDs
    private Context context;
    private DatabaseReference usersRef;

    //orange
    private FirebaseAuth mAuth;
    //orange

    public FriendsAdapter(List<String> friendUids, Context context) {
        this.friendUids = friendUids;
        this.context = context;
        this.usersRef = FirebaseDatabase.getInstance().getReference().child("registered_users");
//orange
        this.mAuth = FirebaseAuth.getInstance();
        //orange

    }

    public void setFriendUids(List<String> friendUids) {
        this.friendUids = friendUids;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String friendUid = friendUids.get(position);

        // Fetch friend's information and bind it to the ViewHolder
        getFriendEmailFromUid(friendUid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String friendEmail = dataSnapshot.child("email").getValue(String.class);
                    holder.textViewFriendEmail.setText(friendEmail);

                    //profile picture
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    // Load and display profile image using Glide
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(context)
                                //.load(profileImageUrl)
                                //.into(holder.profileImageView);
                                .load(profileImageUrl)
                                .placeholder(R.drawable.baseline_person_24) // Set placeholder directly to ImageView
                                .transform(new CircleCrop()) // Apply circular crop transformation
                                .into(holder.profileImageView);
                    }
                    //end of profile picture

                    // Check if the orange circle should be visible
                    checkOrangeCircleVisibility(holder, friendUid);


                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
//orange

        //end of orange

        // Handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to FriendDishesActivity with the selected friend UID
                Intent intent = new Intent(context, FriendDishesActivity.class);
                intent.putExtra("friendUID", friendUid); // Use "friendUID" here
                context.startActivity(intent);

                //orange
                // Set onClick to false under the current user's UID for the selected friend UID
                setOnClickToFalse(friendUid);
                //end of orange
            }
        });

    }
    private void setOnClickToFalse(String friendUid) {
        String currentUserUid = mAuth.getCurrentUser().getUid();
        DatabaseReference isClickedRef = FirebaseDatabase.getInstance().getReference()
                .child("registered_users").child(currentUserUid).child("isClicked").child(friendUid);

        isClickedRef.setValue(false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Debug", "onClick set to false for UID: " + friendUid);

                        //last read
                        // Update Last_read timestamp for the friendUID under the current user's UID
                        updateLastReadTimestamp(currentUserUid, friendUid);
                        //end of last read
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", "Failed to set onClick to false: " + e.getMessage());
                    }
                });
    }

    //LAST READ
    private void updateLastReadTimestamp(String currentUserUid, String friendUid) {
        DatabaseReference registeredUsersRef = FirebaseDatabase.getInstance().getReference()
                .child("registered_users").child(currentUserUid).child("Last_read").child(friendUid);

        // Set the timestamp to the current time
        registeredUsersRef.setValue(ServerValue.TIMESTAMP)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Debug", "Last_read timestamp updated for friendUID: " + friendUid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", "Failed to update Last_read timestamp: " + e.getMessage());
                    }
                });
    }


    //LAST READ


    private void checkOrangeCircleVisibility(ViewHolder holder, String friendUid) {
        String currentUserUid = mAuth.getCurrentUser().getUid();
        DatabaseReference isClickedRef = FirebaseDatabase.getInstance().getReference()
                .child("registered_users").child(currentUserUid).child("isClicked").child(friendUid);

        isClickedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // "isClicked" node exists, proceed to retrieve its value
                    boolean isClicked = dataSnapshot.getValue(Boolean.class);
                    // Set the visibility of the orange circle based on isClicked status
                    holder.orangeCircleImageView.setVisibility(isClicked ? View.VISIBLE : View.GONE);
                } else {
                    // "isClicked" node does not exist, handle the case accordingly
                    Log.d("Debug", "isClicked node does not exist for UID: " + friendUid);
                    // Set the visibility to GONE or handle it as per your requirements
                    holder.orangeCircleImageView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Database error: " + databaseError.getMessage());
            }
        });
    }


    //end of orange

    @Override
    public int getItemCount() {
        return friendUids.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFriendEmail; // Assuming you have a TextView in friend_item.xml

        //TIME STAMP
        ImageView orangeCircleImageView;
        //TIME STAMP END

        //profile
        ImageView profileImageView;
        //profile
        public ViewHolder(View itemView) {
            super(itemView);
            textViewFriendEmail = itemView.findViewById(R.id.textViewFriendEmail); // Adjust accordingly
            //TIME STAMP
            orangeCircleImageView = itemView.findViewById(R.id.orangeCircleImageView);
            //TIME STAMP END

            //profile

            profileImageView = itemView.findViewById(R.id.profileImageView);
            //end of profile
        }
    }

    // Add a method to get the friend's email based on the UID
    private void getFriendEmailFromUid(String friendUid, ValueEventListener valueEventListener) {
        // Implement logic to fetch friend's email from Firebase Realtime Database
        usersRef.child(friendUid).addListenerForSingleValueEvent(valueEventListener);
    }
}
