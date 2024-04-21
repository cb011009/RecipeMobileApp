package com.example.recipemobileapp;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipemobileapp.model.Dish;
import com.squareup.picasso.Picasso;

import java.util.List;

// FriendDishesAdapter.java
// FriendDishesAdapter.java
public class FriendDishesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_LABEL = 0;
    private static final int VIEW_TYPE_DISH = 1;

    private List<Dish> dishes;

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        // Return the view type based on the position in the list
        if (position == 0) {
            return VIEW_TYPE_LABEL;
        } else {
            return VIEW_TYPE_DISH;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a ViewHolder based on the view type
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_LABEL) {
            View view = inflater.inflate(R.layout.item_label, parent, false);
            return new LabelViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_dish, parent, false);
            return new DishViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Bind data to the ViewHolder based on the view type
        if (holder instanceof LabelViewHolder) {
            // Bind label data
            ((LabelViewHolder) holder).bindLabel("Shared by user B");
        } else if (holder instanceof DishViewHolder) {
            // Bind dish data
            Dish dish = dishes.get(position - 1); // Subtract 1 to account for the label
            ((DishViewHolder) holder).bindDish(dish);
        }
    }

    @Override
    public int getItemCount() {
        // Add 1 to the item count to account for the label
        return dishes == null ? 1 : dishes.size() + 1;
    }

    public static class LabelViewHolder extends RecyclerView.ViewHolder {
        private TextView labelTextView;

        public LabelViewHolder(@NonNull View itemView) {
            super(itemView);
            labelTextView = itemView.findViewById(R.id.labelTextView);
        }

        public void bindLabel(String label) {
            labelTextView.setText(label);
        }
    }

    public static class DishViewHolder extends RecyclerView.ViewHolder {
        private ImageView dishImageView;
        private TextView dishNameTextView;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            dishImageView = itemView.findViewById(R.id.item_dish_image);
            dishNameTextView = itemView.findViewById(R.id.item_dish_name);
        }

        public void bindDish(Dish dish) {
            dishNameTextView.setText(dish.getDishName());
            // Load the image using Picasso
            Picasso.get().load(dish.getImageUrl()).into(dishImageView);
        }
    }
}