package com.example.recipemobileapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.recipemobileapp.model.Dish;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import java.util.List;
import java.util.Locale;



public class DetailedViewActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener  {
    private DatabaseReference dishesRef;
    private FirebaseAuth mAuth;
    private String dishKey;

    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        mAuth = FirebaseAuth.getInstance();
        dishesRef = FirebaseDatabase.getInstance().getReference().child("dishes");

        // Retrieve data passed from the adapter
        Intent intent = getIntent();
        Dish dish = (Dish) intent.getSerializableExtra("dish");
        dishKey = dish.getDishKey(); // Replace with the actual method to get the dish key

        // Update the UI with the dish details
        profileImageView = findViewById(R.id.profileImageView);
        TextView dishNameTextView = findViewById(R.id.detailed_dish_name);
        ImageView dishImageView = findViewById(R.id.detailed_dish_image);


        dishNameTextView.setText(dish.getDishName());
        Picasso.get().load(dish.getImageUrl()).into(dishImageView);


        /*Set existing rating and reviews
        setExistingRating();

        // Set listener for submitReviewButton
        setSubmitReviewButtonListener();*/

        // Vist Poster

        /*TextView userTextView = findViewById(R.id.userTextView); // Replace with your actual TextView ID

        Set the user details and make it clickable
        userTextView.setText("Posted by: " + dish.getUid()); // You might want to display username or email here

        userTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to UserDishesActivity with the user's UID
                Intent userDishesIntent = new Intent(DetailedViewActivity.this, PosterActivity.class);
                userDishesIntent.putExtra("posterUid", dish.getUid());
                startActivity(userDishesIntent);
            }
        });*/

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("registered_users");

        TextView userTextView = findViewById(R.id.userTextView);
        userTextView.setText("Loading..."); // Show a loading message while fetching the email

        usersRef.child(dish.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if the user exists
                if (dataSnapshot.exists()) {
                    // Get the email from the dataSnapshot
                    String email = dataSnapshot.child("email").getValue(String.class);


                    // Display the email in the userTextView
                    userTextView.setText("Posted by: " + email);

                    //profile picture
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(getApplicationContext())
                                //.load(profileImageUrl)
                                //.into(profileImageView);
                                .load(profileImageUrl)
                                .placeholder(R.drawable.baseline_person_24) // Set placeholder directly to ImageView
                                .transform(new CircleCrop()) // Apply circular crop transformation
                                .into(profileImageView);
                    } else {
                        // Handle the case where profile image URL is empty or null
                        // For example, you can set a placeholder image or hide the ImageView
                        profileImageView.setVisibility(View.GONE);
                    }
                    //end of profile picture
                } else {
                    // User does not exist
                    userTextView.setText("User not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                userTextView.setText("Error fetching user details");
            }
        });

        userTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to PosterActivity with the user's UID
                Intent posterIntent = new Intent(DetailedViewActivity.this, PosterActivity.class);
                posterIntent.putExtra("posterUid", dish.getUid());
                startActivity(posterIntent);
            }
        });



        //visit Uploader


        //pageview
        String ingredients = dish.getIngredients();
        String preparationSteps = dish.getPreparationSteps();
        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        setupViewPager(viewPager, ingredients, preparationSteps);
        tabLayout.setupWithViewPager(viewPager);

        //end of pageview


    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.handleNavigationItemSelected(this, item);
    }


    //pageview
    // Inside your DetailedViewActivity
    private void setupViewPager(ViewPager viewPager, String ingredients, String preparationSteps) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add fragments to the adapter
        adapter.addFragment(IngredientsFragment.newInstance(ingredients), "Ingredients");
        adapter.addFragment(PreparationStepsFragment.newInstance(preparationSteps), "Preparation Steps");
        adapter.addFragment(ReviewsFragment.newInstance(dishKey), "Reviews");
        // Add other fragments as needed

        // Set the adapter to the ViewPager
        viewPager.setAdapter(adapter);
    }

}
/*


* DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("registered_users");

        TextView userTextView = findViewById(R.id.userTextView);
        userTextView.setText("Loading..."); // Show a loading message while fetching the email

        usersRef.child(dish.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if the user exists
                if (dataSnapshot.exists()) {
                    // Get the email from the dataSnapshot
                    String email = dataSnapshot.child("email").getValue(String.class);


                    // Display the email in the userTextView
                    userTextView.setText("Posted by: " + email);

                    //profile picture
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(getApplicationContext())
                                //.load(profileImageUrl)
                                //.into(profileImageView);
                                .load(profileImageUrl)
                                .placeholder(R.drawable.baseline_person_24) // Set placeholder directly to ImageView
                                .transform(new CircleCrop()) // Apply circular crop transformation
                                .into(profileImageView);
                    } else {
                        // Handle the case where profile image URL is empty or null
                        // For example, you can set a placeholder image or hide the ImageView
                        profileImageView.setVisibility(View.GONE);
                    }
                    //end of profile picture
                } else {
                    // User does not exist
                    userTextView.setText("User not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                userTextView.setText("Error fetching user details");
            }
        });

        userTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to PosterActivity with the user's UID
                Intent posterIntent = new Intent(DetailedViewActivity.this, PosterActivity.class);
                posterIntent.putExtra("posterUid", dish.getUid());
                startActivity(posterIntent);
            }
        });
*/



