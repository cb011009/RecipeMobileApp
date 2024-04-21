package com.example.recipemobileapp;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;


public class ReviewsFragment extends Fragment {

    private static final String ARG_DISH_KEY = "dishKey";

    private DatabaseReference dishesRef;
    private FirebaseAuth mAuth;
    private String dishKey;

    public ReviewsFragment() {
        // Required empty public constructor
    }

    public static ReviewsFragment newInstance(String dishKey) {
        ReviewsFragment fragment = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DISH_KEY, dishKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase and get dishKey
        dishesRef = FirebaseDatabase.getInstance().getReference().child("dishes");
        mAuth = FirebaseAuth.getInstance();

        Bundle arguments = getArguments();
        if (arguments != null) {
            dishKey = arguments.getString(ARG_DISH_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);

        // Set up UI components
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        TextView reviewEditText = view.findViewById(R.id.reviewEditText);
        Button submitReviewButton = view.findViewById(R.id.submitReviewButton);
        TextView averageRatingTextView = view.findViewById(R.id.averageRatingTextView);
        //TextView reviewsTextView = view.findViewById(R.id.reviewsTextView);
        LinearLayout reviewsContainer = view.findViewById(R.id.reviewsContainer);

        // Set existing rating and reviews
        setExistingRating(ratingBar, averageRatingTextView);
        //ORGINAL setExistingReviews(reviewsTextView);
        setExistingReviews(reviewsContainer);

        // Set listener for submitReviewButton
        setSubmitReviewButtonListener(submitReviewButton, ratingBar, reviewEditText);

        return view;


    }

    private void setExistingRating(RatingBar ratingBar, TextView averageRatingTextView) {
        DatabaseReference ratingsRef = dishesRef.child(dishKey).child("ratings");

        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    float totalRatings = 0;
                    int numUsers = 0;

                    for (DataSnapshot userRatingSnapshot : snapshot.getChildren()) {
                        Float userRating = userRatingSnapshot.getValue(Float.class);
                        if (userRating != null) {
                            totalRatings += userRating;
                            numUsers++;
                        }
                    }

                    float averageRating = numUsers > 0 ? totalRatings / numUsers : 0;

                    // Update UI with average rating
                    updateAverageRatingUI(averageRatingTextView, averageRating);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void updateAverageRatingUI(TextView averageRatingTextView, float averageRating) {
        // Update UI with the calculated average rating
        averageRatingTextView.setText(String.format(Locale.getDefault(), "Average Rating: %.1f", averageRating));
    }

    private void setExistingReviews(LinearLayout reviewsContainer) {
        DatabaseReference reviewsRef = dishesRef.child(dishKey).child("reviews");

        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear existing views to avoid duplicates
                reviewsContainer.removeAllViews();

                for (DataSnapshot userReviewSnapshot : snapshot.getChildren()) {
                    String userUid = userReviewSnapshot.getKey();
                    String userReview = userReviewSnapshot.getValue(String.class);

                    if (userReview != null && !userReview.isEmpty()) {
                        // Create a CardView for each review and add it to the LinearLayout
                        //CardView reviewCard = createReviewCard(userUid, userReview);
                        //reviewsContainer.addView(reviewCard);
                        View reviewView = createReviewView(userReview);
                        reviewsContainer.addView(reviewView);

                        
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }


    /*private View createReviewView(String userReview) {
        // Inflate the layout
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.review_card, null); // Replace 'review_layout' with your layout name

        // Find the TextView in the inflated layout
        TextView reviewTextView = view.findViewById(R.id.reviewContentTextView);

        // Set the review content
        reviewTextView.setText(userReview);

        return view;
    }*/

    private View createReviewView(String userReview) {
        // Inflate the layout
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.review_card, null); // Replace 'review_layout' with your layout name

        // Find the TextView in the inflated layout
        TextView reviewTextView = view.findViewById(R.id.reviewContentTextView);

        // Set the review content
        reviewTextView.setText(userReview);

        // Set layout parameters with margins
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int marginInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        layoutParams.setMargins(marginInDp, marginInDp, marginInDp, marginInDp);
        view.setLayoutParams(layoutParams);

        return view;
    }



    private void setSubmitReviewButtonListener(Button submitReviewButton, RatingBar ratingBar, TextView reviewEditText) {
        submitReviewButton.setOnClickListener(view -> {
            float rating = ratingBar.getRating();
            String review = reviewEditText.getText().toString().trim();

            // Save rating and review to Firebase
            saveRatingAndReview(rating, review);
        });
    }

    private void saveRatingAndReview(float rating, String review) {
        // Save rating and review to Firebase under the specific dish
        DatabaseReference ratingsRef = dishesRef.child(dishKey).child("ratings");
        DatabaseReference reviewsRef = dishesRef.child(dishKey).child("reviews");

        // Get the UID of the current user
        String userUid = mAuth.getCurrentUser().getUid();

        // Save user's rating with their UID as the key
        ratingsRef.child(userUid).setValue(rating);

        // Save user's review with their UID as the key
        reviewsRef.child(userUid).setValue(review);

        // Calculate average rating and display reviews
        setExistingRating((RatingBar) getView().findViewById(R.id.ratingBar), (TextView) getView().findViewById(R.id.averageRatingTextView));
        setExistingReviews((LinearLayout) getView().findViewById(R.id.reviewsContainer)); // Replace with your actual LinearLayout ID
    }

}

