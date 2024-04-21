package com.example.recipemobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.recipemobileapp.model.Dish;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PosterActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private RecyclerView posterDishesRecyclerView;
    private DishAdapter posterDishAdapter;
    private List<Dish> posterDishList;
    private String posterUid; // The UID of the user whose dishes are being displayed

    private ImageView ProfileImageView;
    private TextView userDetails;

    private ImageView ownerprofileImageView; // Initialize this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Get the UID from the Intent
        Intent intent = getIntent();
        posterUid = intent.getStringExtra("posterUid");

        //profile
        ProfileImageView = findViewById(R.id.profileImageView);
        userDetails = findViewById(R.id.user_details);
        //end of profile

        posterDishesRecyclerView = findViewById(R.id.posterDishesRecyclerView);
        posterDishList = new ArrayList<>();
        posterDishAdapter = new DishAdapter(posterDishList, this,true,false);
        posterDishesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        posterDishesRecyclerView.setAdapter(posterDishAdapter);

        displayPosterDishData();

        //profile
        loadProfilePicture();
        // Retrieve and display user details
        retrieveAndDisplayUserDetails();
        //end of profile

        //profile button

        ownerprofileImageView = findViewById(R.id.ownerprofileImageView);
        ProfileUtils profileUtils = new ProfileUtils();
        profileUtils.loadProfileImage(PosterActivity.this, ownerprofileImageView);
        profileUtils.setProfileImageClickListener(PosterActivity.this, ownerprofileImageView);

        //end of profile button
    }

    private void displayPosterDishData() {
        DatabaseReference posterDishesRef = FirebaseDatabase.getInstance().getReference("users").child(posterUid);

        posterDishesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posterDishList.clear(); // Clear existing data to avoid duplicates

                for (DataSnapshot dishSnapshot : dataSnapshot.getChildren()) {
                    Dish dish = dishSnapshot.getValue(Dish.class);
                    if (dish != null) {
                        posterDishList.add(dish);
                    }
                }

                // Notify the adapter that the data has changed
                posterDishAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.handleNavigationItemSelected(this, item);
    }
    //profile
    private void loadProfilePicture() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("registered_users");

        // Retrieve the profile image URL of the poster from Firebase Realtime Database
        usersRef.child(posterUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the profile image URL
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    if (profileImageUrl != null) {
                        // Load the profile image using Glide
                        Glide.with(PosterActivity.this)
                                /*.load(profileImageUrl)
                                .placeholder(R.drawable.default_profile_image) // Placeholder image while loading
                                .error(R.drawable.default_profile_image) // Image to display in case of error
                                .into(ProfileImageView);*/
                                .load(profileImageUrl)
                                .placeholder(R.drawable.baseline_person_24) // Set placeholder directly to ImageView
                                .transform(new CircleCrop()) // Apply circular crop transformation
                                .into(ProfileImageView);
                    } else {
                        // If no profile image URL is available, load a default image
                        ProfileImageView.setImageResource(R.drawable.baseline_person_24);
                    }
                } else {
                    // If the user data does not exist, load a default image
                    ProfileImageView.setImageResource(R.drawable.baseline_person_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(PosterActivity.this, "Failed to load profile picture: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retrieveAndDisplayUserDetails() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("registered_users");
        usersRef.child(posterUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        userDetails.setText(email);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
    //end of profile
}
