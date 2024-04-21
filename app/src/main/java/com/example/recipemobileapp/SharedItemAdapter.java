package com.example.recipemobileapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipemobileapp.model.SharedItem;
import com.example.recipemobileapp.model.Dish;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import android.Manifest;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SharedItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SharedItem> sharedItemList;

    //UI
    private FirebaseAuth mAuth;

    //delete mesage
    private DatabaseReference sharedItemsRef;
    //end of delete message

    //END OF UI

    // Define view types
    private static final int VIEW_TYPE_MESSAGE = 1;
    private static final int VIEW_TYPE_DISH = 2;

    private static final int READ_CONTACTS_PERMISSION_REQUEST = 1001;
    /*ORIGINAL
    public SharedItemAdapter(List<SharedItem> sharedItemList) {
        this.sharedItemList = sharedItemList;
    }*/


    //BUTTON CLICK
    private DishAdapter dishAdapter;
    private Context context;

    private Dish clickedDish;

    //BUTTON CLICK



    public SharedItemAdapter(List<SharedItem> sharedItemList, FirebaseAuth mAuth,Context context) {
        this.sharedItemList = sharedItemList;
        this.mAuth = mAuth;  // Initialize mAuth
        //delete message
        this.sharedItemsRef = FirebaseDatabase.getInstance().getReference().child("shared_items");
        //delete message
        this.context = context; // Initialize the context

    }

    //BUTTON CLICK
    public Dish getSelectedDish(int position) {
        if (position != RecyclerView.NO_POSITION && position < sharedItemList.size()) {
            return sharedItemList.get(position).getDish();
        }
        return null; // or throw an exception or handle it based on your requirements
    }
    //BUTTON CLICK

    //delete message
    public void deleteItem(int position, String currentUserUID) {
        SharedItem deletedItem = sharedItemList.get(position);

        // Remove the item from the local list
        sharedItemList.remove(position);

        // Notify the adapter that the item has been removed
        notifyItemRemoved(position);

        // Check if the current user is the sender of the shared item
        if (currentUserUID.equals(deletedItem.getSenderUID())) {
            // If the current user is the sender, delete the item from both local list and Firebase
            deleteItemFromDatabase(deletedItem, currentUserUID);
        }
        // If the current user is the recipient, do not delete the item from the Firebase Realtime Database
    }

    private void deleteItemFromDatabase(SharedItem item, String currentUserUID) {
        // Get references to the shared item in Firebase
        DatabaseReference senderRef = sharedItemsRef.child(currentUserUID).child(item.getTimestamp());
        DatabaseReference recipientRef = sharedItemsRef.child(item.getRecipientUID()).child(item.getTimestamp());

        // Remove the shared item from Firebase
        senderRef.removeValue();
        recipientRef.removeValue();
    }


    /*public void deleteItem(int position, String currentUserUID) {
        SharedItem deletedItem = sharedItemList.get(position);

        // Remove the item from the local list
        sharedItemList.remove(position);

        // Notify the adapter that the item has been removed
        notifyItemRemoved(position);

        // Remove the item from the Firebase Realtime Database
        DatabaseReference itemRef;
        if (currentUserUID.equals(deletedItem.getSenderUID())) {
            itemRef = sharedItemsRef.child(currentUserUID).child(deletedItem.getTimestamp());
        } else {
            itemRef = sharedItemsRef.child(deletedItem.getRecipientUID()).child(deletedItem.getTimestamp());
        }

        itemRef.removeValue();
    }*/
    //delete message

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate different layouts based on view type
        if (viewType == VIEW_TYPE_MESSAGE) {
            View view = inflater.inflate(R.layout.item_message, parent, false);
            return new MessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_DISH) {

            View view = inflater.inflate(R.layout.item_dish, parent, false);
            return new DishViewHolder(view);

            



        }

        throw new IllegalArgumentException("Invalid view type");

    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SharedItem sharedItem = sharedItemList.get(position);

        /* Bind data based on view type orignal
        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE) {
            ((MessageViewHolder) holder).bind(sharedItem.getMessage());
        } else if (holder.getItemViewType() == VIEW_TYPE_DISH) {
            ((DishViewHolder) holder).bind(sharedItem.getDish());
        }*/

        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE) {
            ((MessageViewHolder) holder).bind(sharedItem.getMessage(), mAuth.getCurrentUser().getUid(), sharedItem.getSenderUID());

        } else if (holder.getItemViewType() == VIEW_TYPE_DISH) {

            ((DishViewHolder) holder).bind(sharedItem.getDish());




        }

    }

    //BUTTON CLICKS

    //BUTTON CLICKS

    @Override
    public int getItemCount() {
        return sharedItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        SharedItem sharedItem = sharedItemList.get(position);

        // Return different view types based on the type of shared item
        if ("message".equals(sharedItem.getType())) {
            return VIEW_TYPE_MESSAGE;
        } else if ("dish".equals(sharedItem.getType())) {
            return VIEW_TYPE_DISH;
        }

        throw new IllegalArgumentException("Invalid shared item type");
    }



    // ViewHolder for messages
    private static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;

        private LinearLayout messageContainer; // Add a LinearLayout for message layout

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.messageTextView);
            messageContainer = itemView.findViewById(R.id.messageContainer);


        }

        /*ORIGNAL
         public void bind(String message) {
            textViewMessage.setText(message);
        }*/

        public void bind(String message, String currentUserUID, String senderUID) {
            // Bind message text
            textViewMessage.setText(message);

            // Adjust layout based on the sender
            if (currentUserUID.equals(senderUID)) {
                // Current user's message, align to the right
                messageContainer.setGravity(Gravity.END);
            } else {
                // Friend's message, align to the left
                messageContainer.setGravity(Gravity.START);
            }
        }

    }

    // ViewHolder for dishes



    /*private static class DishViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewDish;
        private TextView textViewDishName;
        private Button btnShare;
        private Button btnEdit;
        private Button btnSave;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewDish = itemView.findViewById(R.id.item_dish_image);
            textViewDishName = itemView.findViewById(R.id.item_dish_name);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnSave = itemView.findViewById(R.id.saveButton);






        }

        public void bind(Dish dish) {
            // Bind dish details to the corresponding views
            // For example:

            textViewDishName.setText(dish.getDishName());

            // Load the image using Picasso
            Picasso.get().load(dish.getImageUrl()).into(imageViewDish);




            // Set onClickListeners for buttons if needed
            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle share button click
                    // You can access the position using getAdapterPosition() if needed
                }
            });

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle edit button click
                    // You can access the position using getAdapterPosition() if needed
                }
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle save button click
                    // You can access the position using getAdapterPosition() if needed
                }
            });
        }
    }
*/


    private class DishViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewDish;
        private TextView textViewDishName;
        private Button btnShare;
        private Button btnEdit;
        private Button btnSave;

        private CheckBox checkBox;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewDish = itemView.findViewById(R.id.item_dish_image);
            textViewDishName = itemView.findViewById(R.id.item_dish_name);
            btnShare = itemView.findViewById(R.id.btnShare);
            checkBox = itemView.findViewById(R.id.checkBox);
            checkBox.setVisibility(View.GONE);
            btnSave = itemView.findViewById(R.id.saveButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickedDish = sharedItemList.get(position).getDish();
                        navigateToDetailedView(clickedDish);
                    }
                }
            });

            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickedDish = sharedItemList.get(position).getDish();
                        checkAndRequestPermission(position);
                    }
                }
            });

            //save button
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Dish clickedDish = sharedItemList.get(position).getDish();
                        saveDishToFirebase(clickedDish);
                    }
                }
            });
            //save button
        }

        public void bind(Dish dish) {

            textViewDishName.setText(dish.getDishName());

            Picasso.get().load(dish.getImageUrl()).into(imageViewDish);


        }




    }


    //save
    private void saveDishToFirebase(Dish dish) {
        // Get the current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
    //save


    private void navigateToDetailedView(Dish dish) {
        Intent intent = new Intent(context, DetailedViewActivity.class);
        intent.putExtra("dish", dish);
        context.startActivity(intent);
    }
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

        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_search_users, null);
        builder.setView(dialogView);

        // Get references to the views in the custom layout
        AutoCompleteTextView searchEditText = dialogView.findViewById(R.id.searchEditText);
        ListView suggestedUsersListView = dialogView.findViewById(R.id.suggestedUsersListView);

        // Set the adapter for the ListView
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












}
