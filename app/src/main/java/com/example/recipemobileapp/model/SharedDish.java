package com.example.recipemobileapp.model;


public class SharedDish {
    private Dish dish;
    private String senderUID;
    private String recipientUID;

    public SharedDish() {
        // Default constructor required for Firebase
    }

    public SharedDish(Dish dish, String senderUID, String recipientUID) {
        this.dish = dish;
        this.senderUID = senderUID;
        this.recipientUID = recipientUID;
    }

    public Dish getDish() {
        return dish;
    }

    public String getSenderUID() {
        return senderUID;
    }

    public String getRecipientUID() {
        return recipientUID;
    }


}

//OLD



/*public class SharedDish {
    private Dish dish;
    private String senderUID;



    public SharedDish() {
        // Default constructor required for Firebase
    }

    public SharedDish(Dish dish, String senderUID) {
        this.dish = dish;
        this.senderUID = senderUID;
    }

    public Dish getDish() {
        return dish;
    }

    public String getSenderUID() {
        return senderUID;
    }
}*/

/*
Original
public class SharedDish {
    private String dishName;
    private String imageUrl;
    private String senderUid;

    // Empty constructor (required by Firebase)
    public SharedDish() {
    }

    public SharedDish(String dishName, String imageUrl, String senderUid) {
        this.dishName = dishName;
        this.imageUrl = imageUrl;
        this.senderUid = senderUid;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }
}*/

/*public class SharedDish {
    private String dishName;
    private String imageUrl;
    private String senderUid;

    // Empty constructor (required by Firebase)
    public SharedDish() {
    }

    public SharedDish(String dishName, String imageUrl, String senderUid) {
        this.dishName = dishName;
        this.imageUrl = imageUrl;
        this.senderUid = senderUid;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }
}*/


/*public class SharedDish {
    private String dishName;
    private String recipientUid;

    // Required default constructor for Firebase
    public SharedDish() {
    }

    public SharedDish(String dishName, String recipientUid) {
        this.dishName = dishName;
        this.recipientUid = recipientUid;
    }

    public String getDishName() {
        return dishName;
    }

    public String getRecipientUid() {
        return recipientUid;
    }
}*/
