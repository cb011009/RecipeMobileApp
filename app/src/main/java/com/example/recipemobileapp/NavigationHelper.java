package com.example.recipemobileapp;
//import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
public class NavigationHelper {

    public static boolean handleNavigationItemSelected(@NonNull Context context, @NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_home) {
            // Handle Home item click
            startActivity(context, MainActivity.class);
            return true;
        } else if (itemId == R.id.action_search) {
            // Handle Search item click
            startActivity(context, SearchActivity.class);
            return true;
        } else if (itemId == R.id.action_user_dishes) {
            // Handle User Dishes item click
            startActivity(context, UserDishesActivity.class);
            return true;
        }else if (itemId == R.id.action_friends) {
            // Handle User Dishes item click
            startActivity(context, FriendsActivity.class);
            return true;
        }
        else if (itemId == R.id.action_saved) {
            // Handle User Dishes item click
            startActivity(context, SavedDishesActivity.class);
            return true;
        }



        return false;
    }

    private static void startActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }
}
