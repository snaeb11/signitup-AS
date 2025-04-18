package com.example.signitup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

public class ActivityFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        //vowels menu
        ImageButton openHandButton = view.findViewById(R.id.openHandButton);
        openHandButton.setOnClickListener(v -> {
            vowelsMenuFragment vowelsMenuFragment = new vowelsMenuFragment();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, vowelsMenuFragment) // use your activity's container ID
                    .addToBackStack(null) // optional
                    .commit();
        });
        return view;
    }
}
