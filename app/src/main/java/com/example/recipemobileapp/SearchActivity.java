// SearchActivity.java
package com.example.recipemobileapp;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.recipemobileapp.model.Dish;
import com.example.recipemobileapp.model.SharedDish;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.view.MenuItem;
import android.content.Intent;
import android.widget.Toast;

public class SearchActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private List<Dish> searchList;
    private DishAdapter searchAdapter;
    private String searchQuery = "";

    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        SearchView searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText.trim().toLowerCase();
                loadAllRecipes();
                return true;
            }
        });

        RecyclerView searchRecyclerView = findViewById(R.id.searchRecyclerView);
        searchList = new ArrayList<>();
        searchAdapter = new DishAdapter(searchList, this, true,false);
        searchRecyclerView.setAdapter(searchAdapter);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadAllRecipes();

        //PROFILE
        profileImageView = findViewById(R.id.profileImageView);
        ProfileUtils profileUtils = new ProfileUtils();
        profileUtils.loadProfileImage(SearchActivity.this, profileImageView);
        profileUtils.setProfileImageClickListener(SearchActivity.this, profileImageView);
        //END OF PROFILE



    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.handleNavigationItemSelected(this, item);
    }
    private void loadAllRecipes() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference dishesRef = FirebaseDatabase.getInstance().getReference("dishes");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Dish> searchList = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot dishSnapshot : userSnapshot.getChildren()) {
                        Dish dish = dishSnapshot.getValue(Dish.class);

                        DatabaseReference singleDishRef = dishesRef.child(dish.getDishKey());
                        singleDishRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    dish.setAverageRating(getAverageRating(snapshot));
                                }

                                if (dishMatchesSearchQuery(dish, searchQuery)) {
                                    searchList.add(dish);
                                    Collections.sort(searchList, (dish1, dish2) -> {
                                        if (dish1.getAverageRating() > dish2.getAverageRating()) {
                                            return -1;
                                        } else if (dish1.getAverageRating() < dish2.getAverageRating()) {
                                            return 1;
                                        } else {
                                            return dish2.getAverageRating() == 0 ? 1 : 0;
                                        }
                                    });
                                    searchAdapter.setData(searchList);
                                    searchAdapter.notifyDataSetChanged();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private boolean dishMatchesSearchQuery(Dish dish, String searchQuery) {
        String dishName = dish.getDishName().toLowerCase();
        return dishName.contains(searchQuery);
    }

    private float getAverageRating(DataSnapshot dataSnapshot) {
        float totalRatings = 0;
        int numUsers = 0;

        for (DataSnapshot userRatingSnapshot : dataSnapshot.child("ratings").getChildren()) {
            Float userRating = userRatingSnapshot.getValue(Float.class);
            if (userRating != null) {
                totalRatings += userRating;
                numUsers++;
            }
        }
        return numUsers > 0 ? totalRatings / numUsers : 0;
    }


}

