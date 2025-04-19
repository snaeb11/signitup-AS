package com.example.signitup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

public class vowelsMenuFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vowels_menu, container, false);

        //a
        ImageButton openA = view.findViewById(R.id.openA);
        openA.setOnClickListener(v -> {
            Toast.makeText(getContext(), "You clicked A", Toast.LENGTH_SHORT).show();
            HandFragment handFragment = new HandFragment();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, handFragment) // use your activity's container ID
                    .addToBackStack(null) // optional
                    .commit();
        });

        //a
        ImageButton openE = view.findViewById(R.id.openE);
        openE.setOnClickListener(v -> {
            Toast.makeText(getContext(), "You clicked E", Toast.LENGTH_SHORT).show();

            LetterEFragment letterEFragment = new LetterEFragment();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, letterEFragment) // use your activity's container ID
                    .addToBackStack(null) // optional
                    .commit();
        });
        return view;
    }
}