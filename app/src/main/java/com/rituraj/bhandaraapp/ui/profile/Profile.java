package com.rituraj.bhandaraapp.ui.profile;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rituraj.bhandaraapp.R;
import com.rituraj.bhandaraapp.ui.pages.LoginPage;

import org.w3c.dom.Text;

public class Profile extends Fragment {
    private FirebaseAuth auth;
    private ProfileViewModel mViewModel;

    public static Profile newInstance() {
        return new Profile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        fun(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        // TODO: Use the ViewModel
    }

    public void fun(View view) {
        auth = FirebaseAuth.getInstance();

        Button signOutBtn = view.findViewById(R.id.signOutBtn);
        ImageView profileImg = view.findViewById(R.id.profileImg);
        TextView profileUserName = view.findViewById(R.id.profileUserName);
        TextView profileEmail = view.findViewById(R.id.profileEmail);

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(requireContext(), "Sign Out", Toast.LENGTH_SHORT).show();
                auth.signOut();
                Intent intent = new Intent(requireContext(), LoginPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        FirebaseUser user = auth.getCurrentUser();
        profileImg.setImageURI(user.getPhotoUrl());
        Glide.with(requireContext())
                .load(user.getPhotoUrl())
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(profileImg);
        profileUserName.setText(user.getDisplayName());
        profileEmail.setText(user.getEmail());
    }
}