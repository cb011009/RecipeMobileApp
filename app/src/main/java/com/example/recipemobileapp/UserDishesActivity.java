package com.example.recipemobileapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemobileapp.model.Dish;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class UserDishesActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    private RecyclerView dishRecyclerView;
    private DishAdapter dishAdapter;
    private List<Dish> dishList;

    //PROFILE
    private ImageView profileImageView;

    //END OF PROFILE


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dishes);

        dishRecyclerView = findViewById(R.id.dishRecyclerView);
        dishList = new ArrayList<>();
        dishAdapter = new DishAdapter(dishList, this, false,true);
        dishRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dishRecyclerView.setAdapter(dishAdapter);
        displayUserDishData();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        //profile button

        profileImageView = findViewById(R.id.profileImageView);
        ProfileUtils profileUtils = new ProfileUtils();
        profileUtils.loadProfileImage(UserDishesActivity.this, profileImageView);
        profileUtils.setProfileImageClickListener(UserDishesActivity.this, profileImageView);

        //end of profile button

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.handleNavigationItemSelected(this, item);
    }




    private void displayUserDishData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userDishesRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userDishesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dishList.clear(); // Clear existing data to avoid duplicates

                for (DataSnapshot dishSnapshot : dataSnapshot.getChildren()) {
                    Dish dish = dishSnapshot.getValue(Dish.class);
                    if (dish != null) {
                        dishList.add(dish);
                    }
                }

                // Notify the adapter that the data has changed
                dishAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }




        });

        //delete

        // Add a click listener to your delete button (replace R.id.btnDelete with the actual ID)
        Button deleteButton = findViewById(R.id.btnDelete);
        deleteButton.setOnClickListener(view -> {
            // Collect selected dish keys
            List<String> selectedDishKeys = new ArrayList<>();
            for (Dish dish : dishList) {
                if (dish.isSelected()) {
                    selectedDishKeys.add(dish.getDishKey());
                }
            }

            // Delete selected dishes from Firebase Database
            deleteSelectedDishes(selectedDishKeys);
        });

    }

    private void deleteSelectedDishes(List<String> selectedDishKeys) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userDishesRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        DatabaseReference sharedDishesRef = FirebaseDatabase.getInstance().getReference("shared_dishes");
        DatabaseReference savedDishesRef = FirebaseDatabase.getInstance().getReference("saved_dishes");
        DatabaseReference dishesRef = FirebaseDatabase.getInstance().getReference("dishes");

        for (String dishKey : selectedDishKeys) {
            // Remove from users node
            userDishesRef.child(dishKey).removeValue();

            // Remove from shared_dishes node
            sharedDishesRef.child(dishKey).removeValue();

            // Remove from saved_dishes node
            savedDishesRef.child(uid).child(dishKey).removeValue();

            // Remove from dishes node
            dishesRef.child(dishKey).removeValue();
        }

        // Clear the selected state in the dish objects
        for (Dish dish : dishList) {
            dish.setSelected(false);
        }

        // Notify the adapter that the data has changed
        dishAdapter.notifyDataSetChanged();
    }



}
