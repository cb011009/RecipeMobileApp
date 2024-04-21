package com.example.recipemobileapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.provider.MediaStore;
public class UserProfileActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

       //light and dark mode

        private Button themeModeToggle;
        private TextView themeModeText;
        //end of light and dark mode

        //logout
        FirebaseAuth auth;
        Button button;
        TextView textView;
        FirebaseUser user;
        //logout


    //profile picture
    private DatabaseReference usersRef;

    private StorageReference storageRef;


    private Button uploadImageButton; // Initialize this
    private ImageView profileImageView; // Initialize this

    //private static String profilePictureUrl;

    //profile picture

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_profile);

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
            bottomNavigationView.setOnNavigationItemSelectedListener(this);

            //light and dark mode

            //100% Works no issue when navigating between pages and coming back to set mode on and off
            themeModeToggle = findViewById(R.id.themeModeToggle);
            themeModeText = findViewById(R.id.themeModeText);

            // Set initial text based on current mode
            updateThemeText();

            themeModeToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleDarkMode();
                }
            });


            //light and dark mode


            //logout

            auth = FirebaseAuth.getInstance();
            button = findViewById(R.id.btn_logOut);
            textView = findViewById(R.id.user_details);
            user = auth.getCurrentUser();
            if (user == null) {
                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();
            }

            else {
                textView.setText(user.getEmail());
            }


            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), login.class);
                    startActivity(intent);
                    finish();
                }
            });

            //logout


            //profile picture

            auth = FirebaseAuth.getInstance();
            usersRef = FirebaseDatabase.getInstance().getReference("registered_users");
            storageRef = FirebaseStorage.getInstance().getReference();

            // Initialize UI components
            uploadImageButton = findViewById(R.id.uploadImageButton);
            profileImageView = findViewById(R.id.profileImageView);


            uploadImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open gallery to select an image
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 1);
                }
            });


            loadProfileImage();

            profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to UserProfileActivity
                    startActivity(new Intent(UserProfileActivity.this, UserProfileActivity.class));
                }
            });

            //profile picture




        }

        //navigation

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.handleNavigationItemSelected(this, item);
    }

    //navigation


        // light and dark mode
       private void toggleDarkMode() {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            int newMode = (currentMode == AppCompatDelegate.MODE_NIGHT_YES) ?
                    AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;

            AppCompatDelegate.setDefaultNightMode(newMode);

            // Update the text based on the new mode
            updateThemeText();
        }

        // Update the text based on the current mode
        private void updateThemeText() {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                themeModeText.setText("Dark Mode");
            } else {
                themeModeText.setText("Light Mode");
            }
        }
        // end light and dark mode


    //profile picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // Get the image URI from the intent
            Uri imageUri = data.getData();

            // Upload the image to Firebase Storage
            uploadImageToStorage(imageUri);
        }
    }

    private void uploadImageToStorage(Uri imageUri) {
        // Create a reference to the location where the profile picture will be stored
        final StorageReference profileImageRef = storageRef.child("profile_images/" + user.getUid() + ".jpg");

        // Upload the image to Firebase Storage
        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image uploaded successfully, get the download URL
                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Update the user's profile information in the Realtime Database
                                usersRef.child(user.getUid()).child("profileImageUrl").setValue(uri.toString());

                                // Load the uploaded image into ImageView
                                Glide.with(UserProfileActivity.this)
                                        //.load(uri)
                                        //.into(profileImageView);
                                        .load(uri)
                                        .placeholder(R.drawable.baseline_person_24) // Set placeholder directly to ImageView
                                        .transform(new CircleCrop()) // Apply circular crop transformation
                                        .into(profileImageView);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle errors
                        Toast.makeText(UserProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadProfileImage() {
        // Get the currently logged-in user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Get the UID of the currently logged-in user
            String uid = currentUser.getUid();

            // Retrieve the profile image URL from the database
            usersRef.child(uid).child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Check if the profile image URL exists
                    if (dataSnapshot.exists()) {
                        // Get the profile image URL
                        String profileImageUrl = dataSnapshot.getValue(String.class);

                        // Load the profile image into the ImageView using Glide library
                        Glide.with(UserProfileActivity.this)
                                //.load(profileImageUrl)
                                //.into(profileImageView);
                                .load(profileImageUrl)
                                .placeholder(R.drawable.baseline_person_24) // Set placeholder directly to ImageView
                                .transform(new CircleCrop()) // Apply circular crop transformation
                                .into(profileImageView);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors
                    Toast.makeText(UserProfileActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                }
            });
        }}
    //profile picture




}
