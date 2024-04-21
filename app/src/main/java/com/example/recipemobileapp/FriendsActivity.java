package com.example.recipemobileapp;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class FriendsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener  {
    private DatabaseReference usersRef;
    private String uid;
    private RecyclerView recyclerView;
    private FriendsAdapter friendsAdapter; // You need to create this adapter

    //PROFILE
    private ImageView profileImageView;
    //END OF PROFLE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("registered_users");

        recyclerView = findViewById(R.id.recyclerFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize and set the adapter with an empty list initially
        friendsAdapter = new FriendsAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(friendsAdapter);

        // Retrieve friends data and populate the UI
        retrieveFriends();

        //profile
        profileImageView = findViewById(R.id.profileImageView);
        ProfileUtils profileUtils = new ProfileUtils();
        profileUtils.loadProfileImage(FriendsActivity.this, profileImageView);
        profileUtils.setProfileImageClickListener(FriendsActivity.this, profileImageView);
        //profile
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.handleNavigationItemSelected(this, item);
    }


    private void retrieveFriends() {
        DatabaseReference currentUserRef = usersRef.child(uid);

        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the last_shared node
                    DataSnapshot lastSharedSnapshot = dataSnapshot.child("Last_shared");

                    // Create a list to store friend UIDs in descending order
                    List<String> friendUids = new ArrayList<>();

                    // Populate the friend UIDs list
                    for (DataSnapshot friendSnapshot : lastSharedSnapshot.getChildren()) {
                        String friendUid = friendSnapshot.getKey();
                        friendUids.add(friendUid);
                    }

                    // Sort friend UIDs based on the timestamp in descending order
                    Collections.sort(friendUids, (friendUid1, friendUid2) -> {
                        Long timestamp1 = (Long) lastSharedSnapshot.child(friendUid1).getValue();
                        Long timestamp2 = (Long) lastSharedSnapshot.child(friendUid2).getValue();

                        if (timestamp1 != null && timestamp2 != null) {
                            return timestamp2.compareTo(timestamp1);
                        } else {
                            return 0;
                        }
                    });

                    // Update the adapter with the sorted list of friend UIDs
                    friendsAdapter.setFriendUids(friendUids);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }




}
