package com.example.recipemobileapp;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Button;

import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
//
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.recipemobileapp.model.Dish;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import com.example.recipemobileapp.R;



public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener  {

    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;
    //
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    Button submitButton;

    private ImageView imageView;
    private String currentPhotoPath;

    private RecyclerView dishRecyclerView;
    private DishAdapter dishAdapter;
    private List<Dish> dishList;

    // Newly Added Feilds
    private EditText aboutEditText, ingredientsEditText, preparationStepsEditText, tagsEditText;
    private Button addTagButton;
    private List<String> tagsList;

    Button takePhotoButton;
    // End of newly added feilds

    //PROFILE
    private DatabaseReference usersRef;

    private StorageReference storageRef;

    private ImageView profileImageView; // Initialize this

    private ProfileUtils profileUtils;
    //END OF PROFILE
//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);



        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        preparationStepsEditText = findViewById(R.id.preparationStepsEditText);
        imageView = findViewById(R.id.imageView);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        takePhotoButton.setOnClickListener(view -> dispatchTakePictureIntent());
        submitButton = findViewById(R.id.submitButton);
        // Get dish details from EditText fields
        submitButton.setOnClickListener(view -> {
            EditText dishNameEditText = findViewById(R.id.dishNameEditText);
            String dishName = dishNameEditText.getText().toString();

            EditText ingredientsEditText = findViewById(R.id.ingredientsEditText);
            String ingredients = ingredientsEditText.getText().toString();
            EditText preparationStepsEditText = findViewById(R.id.preparationStepsEditText);
            String preparationSteps = preparationStepsEditText.getText().toString();
            saveDishToFirebase(dishName, ingredients, preparationSteps, currentPhotoPath);
            // Redirect to UserDishesActivity
            Intent intent = new Intent(MainActivity.this, UserDishesActivity.class);
            startActivity(intent);
        });


        /*Button btnFindRecipes = findViewById(R.id.btnFindRecipes);
        btnFindRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });*/

        auth = FirebaseAuth.getInstance();
        //log out moved to userdishespage
        /*button = findViewById(R.id.btn_logOut);
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
        });*/


//NOTFICATION SECTION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "default_channel_id",
                    "Default Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }




        //profile button

        profileImageView = findViewById(R.id.profileImageView);
        ProfileUtils profileUtils = new ProfileUtils();
        profileUtils.loadProfileImage(MainActivity.this, profileImageView);
        profileUtils.setProfileImageClickListener(MainActivity.this, profileImageView);

        //end of profile button

    }





    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.handleNavigationItemSelected(this, item);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(
                        this,
                        "com.example.recipemobileapp.fileprovider",
                        photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            imageFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            currentPhotoPath = imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }
    

//Placement Of Button
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Image captured, set it to the ImageView
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            imageView.setImageBitmap(bitmap);


            // Remove the text on the button
            takePhotoButton.setText("");

            // Style the button as a circle
            takePhotoButton.setBackgroundResource(R.drawable.circle_button);

            // Set the width and height of the button to be the same
            int buttonSize = getResources().getDimensionPixelSize(R.dimen.button_size); // Define in res/values/dimens.xml
            takePhotoButton.getLayoutParams().width = buttonSize;
            takePhotoButton.getLayoutParams().height = buttonSize;


            // Move the button to the bottom right corner
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) takePhotoButton.getLayoutParams();
            layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
            layoutParams.topMargin = 0; // Remove any top margin
            layoutParams.leftMargin = 0; // Remove any left margin
            takePhotoButton.setLayoutParams(layoutParams);

            // Remove default padding inside the button
            takePhotoButton.setPadding(24, 0, 0, 0);
            // Set drawableLeft and center it inside the button







            }
    }


    private void saveDishToFirebase(String dishName,  String ingredients, String preparationSteps, String photoPath) {
        // Get the current user's UID
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a unique key for the dish
        String dishKey = FirebaseDatabase.getInstance().getReference("users").child(uid).push().getKey();

        // Get a reference to the Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to the image file in Firebase Storage
        StorageReference imageRef = storageRef.child("images/" + dishKey + ".jpg");

        // Upload the image file to Firebase Storage
        Uri photoUri = Uri.fromFile(new File(photoPath));
        imageRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully, get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Create a Dish object with dish details including UID and tagsList
                        Dish dish = new Dish(dishKey, uid, dishName, uri.toString(), ingredients, preparationSteps);

                        // Save dish details to the Realtime Database
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                        databaseReference.child(uid).child(dishKey).setValue(dish);


                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error uploading image: " + e.getMessage());

                });

    }

}