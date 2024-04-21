package com.example.recipemobileapp.model;


public class SharedItem {
    private String type;         // "dish" or "message"
    private Dish dish;           // Details of the shared dish
    private String message;      // Text of the shared message
    private String senderUID;    // UID of the sender
    private String recipientUID; // UID of the recipient

    //delete message
    private String timestamp; // Add this field
    //end of delete message



    // Required empty constructor for Firebase
    public SharedItem() {
    }

    // Constructor for shared dish
    /*public SharedItem(String type, Dish dish, String senderUID, String recipientUID) {
        this.type = type;
        this.dish = dish;
        this.senderUID = senderUID;
        this.recipientUID = recipientUID;
    }

    // Constructor for shared message
    public SharedItem(String type, String message, String senderUID, String recipientUID) {
        this.type = type;
        this.message = message;
        this.senderUID = senderUID;
        this.recipientUID = recipientUID;
    }*/

    // Constructor for message
    public SharedItem(String type, String message, String senderUID, String recipientUID) {
        this.type = type;
        this.message = message;
        this.senderUID = senderUID;
        this.recipientUID = recipientUID;
        //delete message
        this.timestamp = String.valueOf(System.currentTimeMillis());
        //end of delete message

    }

    // Constructor for dish
    public SharedItem(String type, Dish dish, String senderUID, String recipientUID) {
        this.type = type;
        this.dish = dish;
        this.senderUID = senderUID;
        this.recipientUID = recipientUID;
        //delete message
        this.timestamp = String.valueOf(System.currentTimeMillis());
        //end of delete message

    }

    public String getRecipientUID() {
        return recipientUID;
    }

    public String getMessage() {
        return message;
    }

    public Dish getDish() {
        return dish;
    }

    public String getType() {
        return type;
    }

    public String getSenderUID() {
        return senderUID;
    }

    public void setSenderUID(String senderUID) {
        this.senderUID = senderUID;
    }



    public void setRecipientUID(String recipientUID) {
        this.recipientUID = recipientUID;
    }

    public String getTimestamp() {
        return timestamp;
    }


}
