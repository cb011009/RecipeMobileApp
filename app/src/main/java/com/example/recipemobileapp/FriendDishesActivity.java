package com.example.recipemobileapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.recipemobileapp.model.SharedDish;
import com.example.recipemobileapp.model.SharedItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class FriendDishesActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener  {

    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private List<SharedItem> sharedItemList;
    private SharedItemAdapter adapter;

    //heading
    private DatabaseReference usersRef;
    private TextView headingTextView;
    //end of heading

    //profile
    private ImageView friendProfileImageView;
    //end of profile

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_dishes);

        //profile

        //profile

        mAuth = FirebaseAuth.getInstance();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        EditText editTextMessage = findViewById(R.id.editTextMessage);
        Button buttonSend = findViewById(R.id.buttonSend);
        recyclerView = findViewById(R.id.recyclerView);

        sharedItemList = new ArrayList<>();
        adapter = new SharedItemAdapter(sharedItemList, mAuth,this);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        String friendUID = getIntent().getStringExtra("friendUID");
        String currentUserUID = mAuth.getCurrentUser().getUid();

        // Set up click listener for the send button
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = editTextMessage.getText().toString().trim();

                if (!messageText.isEmpty()) {
                    //saveSharedMessageDetails(currentUserUID, friendUID, messageText);
                    saveSharedMessageDetails(friendUID, messageText);
                    editTextMessage.setText(""); // Clear the EditText after sending
                } else {
                    Toast.makeText(FriendDishesActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /* Load shared items for both sender and recipient
        loadSharedItems(currentUserUID, friendUID);*/

        loadSharedItems(currentUserUID, friendUID);

        //profile
        friendProfileImageView = findViewById(R.id.FriendProfileImageView);
        loadFriendProfilePicture(friendUID);
        //profile

//DELETE MESSAGE
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // Handle item click here
            }

            @Override
            public void onLongItemClick(View view, int position) {
                // Show a confirmation dialog for deletion
                showDeleteConfirmationDialog(position);
            }
        }));



        //DELETE MESSAGE


        //heading
        usersRef = FirebaseDatabase.getInstance().getReference().child("registered_users"); // Reference to registered_users node// Initialize views
        headingTextView = findViewById(R.id.headingTextView);

        // Set OnClickListener to headingTextView
        headingTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to the new activity when headingTextView is clicked
                Intent intent = new Intent(FriendDishesActivity.this, PosterActivity.class);
                intent.putExtra("posterUid", friendUID); // Assuming friendUID is the UID you want to pass to the next activity
                startActivity(intent);
            }
        });

        usersRef.child(friendUID).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String friendEmail = dataSnapshot.getValue(String.class);
                if (friendEmail != null) {
                    headingTextView.setText(": " + friendEmail);
                    headingTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
        //end of heading


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.handleNavigationItemSelected(this, item);
    }

    //profile
    private void loadFriendProfilePicture(String friendUID) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("registered_users");

        // Retrieve the profile image URL from Firebase Realtime Database
        usersRef.child(friendUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the profile image URL
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    if (profileImageUrl != null) {
                        // Load the profile image using Glide
                        Glide.with(FriendDishesActivity.this)
                                //.load(profileImageUrl)
                              //.into(friendProfileImageView);
                                .load(profileImageUrl)
                                .placeholder(R.drawable.baseline_person_24) // Set placeholder directly to ImageView
                                .transform(new CircleCrop()) // Apply circular crop transformation
                                .into(friendProfileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(FriendDishesActivity.this, "Failed to load profile picture: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //end of profile

    //DELETE MESSAGE

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Delete the item
                deleteSharedItem(position);
            }
        });
        builder.setNegativeButton("No", null);
        builder.create().show();
    }

    private void deleteSharedItem(int position) {
        // Get the current user UID
        String currentUserUID = mAuth.getCurrentUser().getUid();

        // Call the deleteItem method in the adapter
        adapter.deleteItem(position, currentUserUID);
    }
    //DELETE MESSAGE


    /*100% WORKS COMMENTED ON 16TH DEC
    private void saveSharedMessageDetails(String recipientUID, String messageText) {
        String senderUID = mAuth.getCurrentUser().getUid();

        DatabaseReference sharedItemsRef = FirebaseDatabase.getInstance().getReference().child("shared_items");


        String timestamp = String.valueOf(System.currentTimeMillis());

        // Create a SharedItem for shared message
        SharedItem sharedMessageItem = new SharedItem("message", messageText, senderUID, recipientUID);

        // Save the shared message item to the database under both sender's and recipient's UIDs
        DatabaseReference senderRef = sharedItemsRef.child(senderUID).child(timestamp);
        senderRef.setValue(sharedMessageItem);

        DatabaseReference recipientRef = sharedItemsRef.child(recipientUID).child(timestamp);
        recipientRef.setValue(sharedMessageItem);

        Toast.makeText(this, "Message sent to friend", Toast.LENGTH_SHORT).show();
    }*/


    //TIME STAMP
    /*private void saveSharedMessageDetails(String recipientUID, String messageText) {
        String senderUID = mAuth.getCurrentUser().getUid();
        DatabaseReference sharedItemsRef = FirebaseDatabase.getInstance().getReference().child("shared_items");

        // Create a unique timestamp for each shared item
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Create a SharedItem for shared message
        SharedItem sharedMessageItem = new SharedItem("message", messageText, senderUID, recipientUID);

        // Save the shared message item to the database under both sender's and recipient's UIDs
        DatabaseReference senderRef = sharedItemsRef.child(senderUID).child(timestamp);
        senderRef.setValue(sharedMessageItem);

        DatabaseReference recipientRef = sharedItemsRef.child(recipientUID).child(timestamp);
        recipientRef.setValue(sharedMessageItem);

        // Update "Last_sent_time" for both sender and recipient
        updateLastSentTime(senderUID);
        updateLastSentTime(recipientUID);

        Toast.makeText(this, "Message sent to friend", Toast.LENGTH_SHORT).show();
    }

    private void updateLastSentTime(String uid) {
        DatabaseReference registeredUsersRef = FirebaseDatabase.getInstance().getReference().child("registered_users").child(uid);
        registeredUsersRef.child("Last_sent_time").setValue(ServerValue.TIMESTAMP);
    }
*/
    private void saveSharedMessageDetails(String recipientUID, String messageText) {
        String senderUID = mAuth.getCurrentUser().getUid();

        DatabaseReference sharedItemsRef = FirebaseDatabase.getInstance().getReference().child("shared_items");

        // Create a unique timestamp for each shared item
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Create a SharedItem for shared message
        SharedItem sharedMessageItem = new SharedItem("message", messageText, senderUID, recipientUID);

        // Save the shared message item to the database under both sender's and recipient's UIDs
        DatabaseReference senderRef = sharedItemsRef.child(senderUID).child(timestamp);
        senderRef.setValue(sharedMessageItem);

        DatabaseReference recipientRef = sharedItemsRef.child(recipientUID).child(timestamp);
        recipientRef.setValue(sharedMessageItem);

        Toast.makeText(this, "Message sent to friend", Toast.LENGTH_SHORT).show();

        //Update "Last_shared" for both sender and recipient
        updateLastSharedTime(senderUID, recipientUID,timestamp);
        updateLastSharedTime(recipientUID, senderUID,timestamp);

    }

    private void updateLastSharedTime(String userUID, String friendUID,String sharedItemTimestamp) {
        DatabaseReference registeredUsersRef = FirebaseDatabase.getInstance().getReference().child("registered_users").child(userUID);

        // Update "Last shared" value for the friend
        DatabaseReference lastSharedRef = registeredUsersRef.child("Last_shared").child(friendUID);
        lastSharedRef.setValue(ServerValue.TIMESTAMP);

        //ONCLICK START

        // Update "onClick" value for the friend
       /* DatabaseReference isClickedRef = registeredUsersRef.child("isClicked").child(friendUID);
        isClickedRef.setValue(true);*/

        // Check if the current user is the recipient of the shared item
        DatabaseReference sharedItemRef = FirebaseDatabase.getInstance().getReference().child("shared_items").child(userUID).child(sharedItemTimestamp);
        sharedItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> sharedItemData = (Map<String, Object>) dataSnapshot.getValue();

                    // Check if the current user is the recipient
                    if (sharedItemData != null && userUID.equals(sharedItemData.get("recipientUID"))) {
                        // Update "onClick" value for the friend only if the current user is the recipient
                        DatabaseReference isClickedRef = registeredUsersRef.child("isClicked").child(friendUID);
                        isClickedRef.setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Database error: " + databaseError.getMessage());
            }
        });

        //END OF ONCLICK START

    }



    //TIME STAMP


    private void loadSharedItems(String currentUserUID, String friendUID) {
        DatabaseReference currentUserSharedItemsRef = FirebaseDatabase.getInstance().getReference().child("shared_items").child(currentUserUID);
        DatabaseReference friendSharedItemsRef = FirebaseDatabase.getInstance().getReference().child("shared_items").child(friendUID);

        // Load shared items for the current user
        currentUserSharedItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sharedItemList.clear();

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    SharedItem sharedItem = itemSnapshot.getValue(SharedItem.class);

                    // Only add items related to the specified friendUID
                    if (sharedItem != null && friendUID.equals(sharedItem.getRecipientUID()) || friendUID.equals(sharedItem.getSenderUID())) {
                        sharedItemList.add(sharedItem);
                    }
                }

                // Notify adapter of data change for the current user's items
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Database error: " + databaseError.getMessage());
            }
        });

        // Load shared items for the friend
        friendSharedItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sharedItemList.clear();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    SharedItem sharedItem = itemSnapshot.getValue(SharedItem.class);

                    // Add logic to handle friend's shared items
                    if (sharedItem != null && (currentUserUID.equals(sharedItem.getRecipientUID()) || currentUserUID.equals(sharedItem.getSenderUID()))) {
                        // You can add the shared item to the friend's list or perform any other logic
                        // For example, you might want to update the UI or add the item to a list
                        sharedItemList.add(sharedItem);
                    }
                }

                // Notify adapter of data change for the friend's items
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Database error: " + databaseError.getMessage());
            }
        });
    }







}







/*public class FriendDishesActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private List<SharedItem> sharedItemList;
    private SharedItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_dishes);

        mAuth = FirebaseAuth.getInstance();

        // Assuming you have references to the EditText, Button, and RecyclerView in your layout
        EditText editTextMessage = findViewById(R.id.editTextMessage);
        Button buttonSend = findViewById(R.id.buttonSend);
        recyclerView = findViewById(R.id.recyclerView);

        sharedItemList = new ArrayList<>();
        adapter = new SharedItemAdapter(sharedItemList);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Assuming you have friendUID from Intent
        String friendUID = getIntent().getStringExtra("friendUID");

        // Set up click listener for the send button
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = editTextMessage.getText().toString().trim();

                if (!messageText.isEmpty()) {
                    saveSharedMessageDetails(friendUID, messageText);
                    editTextMessage.setText(""); // Clear the EditText after sending
                } else {
                    Toast.makeText(FriendDishesActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load shared items for the current user and friendUID
        loadSharedItems(friendUID);
    }

    private void saveSharedMessageDetails(String friendUID, String messageText) {
        String senderUID = mAuth.getCurrentUser().getUid();

        DatabaseReference sharedItemsRef = FirebaseDatabase.getInstance().getReference().child("shared_items").child(senderUID);

        // Create a unique timestamp for each shared item
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Create a SharedItem for shared message
        SharedItem sharedMessageItem = new SharedItem("message", messageText, senderUID, friendUID);

        // Save the shared message item to the database
        sharedItemsRef.child(timestamp).setValue(sharedMessageItem);

        // Notify adapter of data change
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Message sent to friend", Toast.LENGTH_SHORT).show();
    }

    private void loadSharedItems(String friendUID) {
        String currentUserUID = mAuth.getCurrentUser().getUid();

        DatabaseReference sharedItemsRef = FirebaseDatabase.getInstance().getReference().child("shared_items").child(currentUserUID);

        sharedItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sharedItemList.clear();

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    SharedItem sharedItem = itemSnapshot.getValue(SharedItem.class);

                    // Only add items related to the specified friendUID
                    if (sharedItem != null && friendUID.equals(sharedItem.getRecipientUID())) {
                        sharedItemList.add(sharedItem);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Database error: " + databaseError.getMessage());
            }
        });
    }
}*/


 /*public class FriendDishesActivity extends AppCompatActivity {

    private RecyclerView friendDishesRecyclerView;
    private DishAdapter dishAdapter;
    private List<Dish> friendDishList;

    //message

    //message

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_dishes);

        friendDishesRecyclerView = findViewById(R.id.friendDishesRecyclerView);
        friendDishList = new ArrayList<>();
        //ORIGINAL dishAdapter = new DishAdapter(friendDishList, this);
        //EDIT
        //DishAdapter userDishesAdapter = new DishAdapter(friendDishList , this, false);
        dishAdapter = new DishAdapter(friendDishList, this, false,true);
        //END OF EDIT
        friendDishesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendDishesRecyclerView.setAdapter(dishAdapter);

        // Retrieve friend UID from the Intent
        String friendUID = getIntent().getStringExtra("friendUID");

        // Call method to display shared dishes for both sender and receiver
        displayFriendDishes(friendUID);

        //message

        //message
    }

    //message

    //message

    private void displayFriendDishes(String friendUID) {
        DatabaseReference senderDishesRef = FirebaseDatabase.getInstance().getReference("shared_dishes")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(friendUID);

        DatabaseReference receiverDishesRef = FirebaseDatabase.getInstance().getReference("shared_dishes")
                .child(friendUID)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        senderDishesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendDishList.clear(); // Clear existing data to avoid duplicates

                for (DataSnapshot friendDishSnapshot : dataSnapshot.getChildren()) {
                    SharedDish sharedDish = friendDishSnapshot.getValue(SharedDish.class);
                    if (sharedDish != null) {
                        Dish dish = sharedDish.getDish();
                        friendDishList.add(dish);
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

        receiverDishesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot friendDishSnapshot : dataSnapshot.getChildren()) {
                    SharedDish sharedDish = friendDishSnapshot.getValue(SharedDish.class);
                    if (sharedDish != null) {
                        Dish dish = sharedDish.getDish();
                        friendDishList.add(dish);
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
    }
}*/

