package com.example.recipemobileapp;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PreparationStepsFragment extends Fragment {
    private static final String ARG_PREPARATION_STEPS = "preparationSteps";

    private String preparationSteps;

    public PreparationStepsFragment() {
        // Required empty public constructor
    }

    public static PreparationStepsFragment newInstance(String preparationSteps) {
        PreparationStepsFragment fragment = new PreparationStepsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PREPARATION_STEPS, preparationSteps);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            preparationSteps = getArguments().getString(ARG_PREPARATION_STEPS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preparation_steps, container, false);

        // Update UI with preparation steps
        TextView preparationStepsTextView = view.findViewById(R.id.preparationStepsTextView);
        preparationStepsTextView.setText(preparationSteps);

        return view;
    }
}
