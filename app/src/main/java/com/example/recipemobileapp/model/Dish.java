package com.example.recipemobileapp.model;


import java.util.List;

import java.io.Serializable;

public class Dish implements Serializable{
    private String dishName;
    private String imageUrl;

    private String ingredients;
    private String preparationSteps;

    //private List<String> tagsList;

    private String dishKey;

    private String uid;

    //delete
    private boolean selected;
//delete



    // Add constructor and getter methods here
    public Dish() {
    }


    public Dish(String dishKey, String uid, String dishName, String imageUrl, String ingredients, String preparationSteps) {
        this.dishKey = dishKey;
        this.uid = uid;
        this.dishName = dishName;
        this.imageUrl = imageUrl;

        this.ingredients = ingredients;
        this.preparationSteps = preparationSteps;
        //this.tagsList = tagsList;
    }

    // Getter methods
    public String getDishName() {
        return dishName;
    }

    public String getImageUrl() {
        return imageUrl;
    }



    public String getIngredients() {
        return ingredients;
    }

    public String getPreparationSteps() {
        return preparationSteps;
    }

    /* Getter and setter methods for tagsList
    public List<String> getTagsList() {
        return tagsList;
    }

    public void setTagsList(List<String> tagsList) {
        this.tagsList = tagsList;
    }*/


    public String getDishKey() {
        return dishKey;
    }
    // Add a method to set the key

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public void setPreparationSteps(String preparationSteps) {
        this.preparationSteps = preparationSteps;
    }

    public void setDishKey(String dishKey) {
        this.dishKey = dishKey;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    //delete
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
//delete

    private float averageRating;


    // Other existing getters and setters

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }


}


