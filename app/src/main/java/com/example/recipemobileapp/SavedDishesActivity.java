package com.example.recipemobileapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;


import com.example.recipemobileapp.model.Dish;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SavedDishesActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener  {

    private RecyclerView recyclerView;
    private DishAdapter dishAdapter;
    private List<Dish> savedDishesList;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference savedDishesRef;

    //PROFILE
    private ImageView profileImageView;

    //END OF PROFILE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_dishes);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        savedDishesRef = FirebaseDatabase.getInstance().getReference().child("saved_dishes").child(userId);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.savedDishesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Use the existing DishAdapter
        savedDishesList = new ArrayList<>();
        dishAdapter = new DishAdapter(savedDishesList, this, false,false); // Set isEditMode to false
        recyclerView.setAdapter(dishAdapter);

        // Load saved dishes
        loadSavedDishes();

        //profile button

        profileImageView = findViewById(R.id.profileImageView);
        ProfileUtils profileUtils = new ProfileUtils();
        profileUtils.loadProfileImage(SavedDishesActivity.this, profileImageView);
        profileUtils.setProfileImageClickListener(SavedDishesActivity.this, profileImageView);

        //end of profile button


    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.handleNavigationItemSelected(this, item);
    }


    //load saved dishes from firebase

    private void loadSavedDishes() {
        savedDishesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                savedDishesList.clear(); // Clear existing data to avoid duplicates

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Dish savedDish = snapshot.getValue(Dish.class);
                    if (savedDish != null) {
                        savedDishesList.add(savedDish);
                    }
                }

                // Notify the adapter that the data has changed
                //dishAdapter.notifyDataSetChanged();
                // Set the updated data to the adapter
                dishAdapter.setData(savedDishesList);
                dishAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    //end of load saved dishes from firebase




}
