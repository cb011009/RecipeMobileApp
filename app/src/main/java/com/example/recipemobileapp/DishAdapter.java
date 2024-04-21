package com.example.recipemobileapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.example.recipemobileapp.model.SharedDish;
import com.example.recipemobileapp.model.SharedItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.proto.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.recipemobileapp.model.Dish;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class DishAdapter extends RecyclerView.Adapter<DishAdapter.ViewHolder>  {
    private List<Dish> dishList;
    private Context context;
    //Button shareButton;
    //String recipientEmail;

    //private static final int CONTACTS_PERMISSION_REQUEST = 1001;
    private static final int READ_CONTACTS_PERMISSION_REQUEST = 1001;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public void setData(List<Dish> dishList) {
        this.dishList = dishList;
    }
    private boolean isEditMode;
    private boolean isSaveMode;

    public DishAdapter(List<Dish> dishList, Context context, boolean isSaveMode, boolean isEditMode) {
        this.dishList = dishList;
        this.context = context;
        this.isSaveMode = isSaveMode;
        this.isEditMode = isEditMode;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dish, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Dish dish = dishList.get(position);
        holder.dishNameTextView.setText(dish.getDishName());
        Picasso.get().load(dish.getImageUrl()).into(holder.dishImageView);

        holder.itemView.setOnClickListener(view -> {
            navigateToDetailedView(dish);
        });

        holder.shareButton.setOnClickListener(view -> {
            checkAndRequestPermission(position);
        });

        holder.saveButton.setVisibility(isSaveMode ? View.VISIBLE : View.GONE);
        holder.saveButton.setOnClickListener(view -> syncContactsAndShowDialog(position));

        holder.saveButton.setOnClickListener(view -> {
            saveDishToFirebase(dish);
        });

        holder.checkBox.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        holder.checkBox.setChecked(dish.isSelected());
        holder.checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            dish.setSelected(isChecked);
        });

    }



    //REQUEST USER PERSMISSION TO SYNC CONTACT
    private void checkAndRequestPermission(int position) {
        // Check if the app has permission to read contacts
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted, proceed with syncing contacts and showing the dialog
            syncContactsAndShowDialog(position);
        } else {
            // Request permission from the user
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    READ_CONTACTS_PERMISSION_REQUEST);
        }
    }
    //END OF REQUEST USER PERMISSION TO SYNC CONTACT


    //save




    private void saveDishToFirebase(Dish dish) {
        // Get the current user ID
        String userId = mAuth.getCurrentUser().getUid();

        // Get a reference to the "saved_dishes" node for the current user
        DatabaseReference savedDishesRef = FirebaseDatabase.getInstance().getReference().child("saved_dishes").child(userId);

        // Generate a new key for the saved dish
        String savedDishKey = savedDishesRef.push().getKey();

        // Set the dish key to the new key
        dish.setDishKey(savedDishKey);

        // Save the dish under the "saved_dishes" node
        savedDishesRef.child(savedDishKey).setValue(dish)
                .addOnSuccessListener(aVoid -> {
                    // Handle successful save
                    Toast.makeText(context, "Dish saved successfully!", Toast.LENGTH_SHORT).show();

                    // Redirect the user to PosterActivity
                    Intent intent = new Intent(context, SavedDishesActivity.class);
                    context.startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    // Handle failed save
                    Toast.makeText(context, "Failed to save dish. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
    //end of save

    //share

    private List<String> getUserContactPhoneNumbers() {
        List<String> contactPhoneNumbers = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            int phoneNumberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            while (cursor.moveToNext()) {
                // Check if the column index is valid
                if (phoneNumberColumnIndex >= 0) {
                    String phoneNumber = cursor.getString(phoneNumberColumnIndex);
                    contactPhoneNumbers.add(phoneNumber);
                } else {
                    // Handle the case where the column index is invalid
                    Log.e("Error", "Invalid column index for phone number");
                }
            }

            cursor.close();
        } else {
            // Handle the case where the cursor is null
            Log.e("Error", "Contacts cursor is null");
        }

        return contactPhoneNumbers;
    }

    //start of dialogue box with emails new

    private void syncContactsAndShowDialog(int position) {
        DatabaseReference registeredUsersRef = FirebaseDatabase.getInstance().getReference().child("registered_users");
        registeredUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> registeredPhoneNumbers = new ArrayList<>();
                    List<String> registeredEmails = new ArrayList<>();

                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String phoneNumber = userSnapshot.child("phoneNumber").getValue(String.class);
                        String email = userSnapshot.child("email").getValue(String.class);

                        if (phoneNumber != null && email != null) {
                            registeredPhoneNumbers.add(phoneNumber);
                            registeredEmails.add(email);
                        }
                    }

                    List<String> userContactPhoneNumbers = getUserContactPhoneNumbers();
                    List<String> commonPhoneNumbers = new ArrayList<>(registeredPhoneNumbers);
                    commonPhoneNumbers.retainAll(userContactPhoneNumbers);

                    List<String> commonEmails = new ArrayList<>();
                    for (String phoneNumber : commonPhoneNumbers) {
                        int index = registeredPhoneNumbers.indexOf(phoneNumber);
                        if (index != -1) {
                            commonEmails.add(registeredEmails.get(index));
                        }
                    }

                    showSuggestedUsersDialog(commonEmails, position);
                } else {
                    Toast.makeText(context, "No registered users in Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Database error: " + databaseError.getMessage());
            }
        });
    }



    private void showSuggestedUsersDialog(List<String> suggestedEmails, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Suggested Users");

      
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_search_users, null);
        builder.setView(dialogView);


        AutoCompleteTextView searchEditText = dialogView.findViewById(R.id.searchEditText);
        ListView suggestedUsersListView = dialogView.findViewById(R.id.suggestedUsersListView);


        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, suggestedEmails);
        suggestedUsersListView.setAdapter(listViewAdapter);

        // Set the item click listener for the ListView
        suggestedUsersListView.setOnItemClickListener((parent, view, which, id) -> {
            String selectedEmail = suggestedEmails.get(which);
            saveSharedDishDetails(selectedEmail, position);
        });

        // Set up the adapter for the AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, suggestedEmails);
        searchEditText.setAdapter(adapter);
        searchEditText.setThreshold(1); // Show suggestions after the first character

        // Set the item click listener for the AutoCompleteTextView
        searchEditText.setOnItemClickListener((parent, view, pos, id) -> {
            String selectedEmail = (String) parent.getItemAtPosition(pos);
            saveSharedDishDetails(selectedEmail, position);
        });

        // Set the text change listener for the AutoCompleteTextView
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Filter and update suggestions based on the user's input
                filterAndDisplaySuggestions(charSequence.toString(), adapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        builder.setPositiveButton("Cancel", (dialogInterface, i) -> {
            // Handle cancel button click if needed
        });

        builder.create().show();
    }

    private void filterAndDisplaySuggestions(String query, ArrayAdapter<String> adapter) {
        // Fetch and filter suggestions from the registered_users node
        DatabaseReference registeredUsersRef = FirebaseDatabase.getInstance().getReference().child("registered_users");
        registeredUsersRef.orderByChild("email").startAt(query).endAt(query + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> filteredEmails = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String email = userSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        filteredEmails.add(email);
                    }
                }
                adapter.clear();
                adapter.addAll(filteredEmails);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Database error: " + databaseError.getMessage());
            }
        });
    }


    private void saveSharedDishDetails(String recipientEmail, int position) {
        Dish selectedDish = getSelectedDish(position);

        if (selectedDish != null) {
            DatabaseReference referenceRegisteredUsers = FirebaseDatabase.getInstance().getReference().child("registered_users");
            referenceRegisteredUsers.orderByChild("email").equalTo(recipientEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String recipientUID = dataSnapshot.getChildren().iterator().next().getKey();
                        String senderUID = mAuth.getCurrentUser().getUid();

                        DatabaseReference sharedItemsRef = FirebaseDatabase.getInstance().getReference().child("shared_items");

                        // Create a unique timestamp for each shared item
                        String timestamp = String.valueOf(System.currentTimeMillis());

                        // Create a SharedItem for shared dish
                        SharedItem sharedDishItem = new SharedItem("dish", selectedDish, senderUID, recipientUID);

                        // Save the shared dish item to the database under both sender's and recipient's UIDs
                        DatabaseReference senderRef = sharedItemsRef.child(senderUID).child(timestamp);
                        senderRef.setValue(sharedDishItem);

                        DatabaseReference recipientRef = sharedItemsRef.child(recipientUID).child(timestamp);
                        recipientRef.setValue(sharedDishItem);

                        // Update sender's node with friend information
                        DatabaseReference senderFriendRef = referenceRegisteredUsers.child(senderUID).child("friends").child(recipientUID);
                        senderFriendRef.setValue(true);

                        // Update recipient's node with friend information
                        DatabaseReference recipientFriendRef = referenceRegisteredUsers.child(recipientUID).child("friends").child(senderUID);
                        recipientFriendRef.setValue(true);

                        // Update "Last_sent_time" for both sender and recipient
                        updateLastSharedTime(senderUID, recipientUID,timestamp);
                        updateLastSharedTime(recipientUID, senderUID,timestamp);

                        Toast.makeText(context, "Dish shared with " + recipientEmail, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "User is not registered.", Toast.LENGTH_SHORT).show();
                        Log.d("Debug", "User is not registered.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Error", "Database error: " + databaseError.getMessage());
                }
            });
        }
    }

    private void updateLastSharedTime(String userUID, String friendUID,String sharedItemTimestamp) {
        DatabaseReference registeredUsersRef = FirebaseDatabase.getInstance().getReference().child("registered_users").child(userUID);
        DatabaseReference lastSharedRef = registeredUsersRef.child("Last_shared").child(friendUID);
        lastSharedRef.setValue(ServerValue.TIMESTAMP);



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
    }







//TIME STAMP END





    //shared save 2



    //END OF NEW SAVED SHARED DISH

    private Dish getSelectedDish(int position) {
        if (position >= 0 && position < dishList.size()) {
            return dishList.get(position);
        } else {
            Log.e("Error", "Invalid position for selected dish");
            return null;
        }
    }
    //share
    private void navigateToDetailedView(Dish dish) {
        Intent intent = new Intent(context, DetailedViewActivity.class);
        intent.putExtra("dish", dish);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return dishList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView dishImageView;
        TextView dishNameTextView;
        Button shareButton;
        Button saveButton;
        CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dishImageView = itemView.findViewById(R.id.item_dish_image);
            dishNameTextView = itemView.findViewById(R.id.item_dish_name);
            shareButton = itemView.findViewById(R.id.btnShare);
            saveButton = itemView.findViewById(R.id.saveButton);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
        }
    }



