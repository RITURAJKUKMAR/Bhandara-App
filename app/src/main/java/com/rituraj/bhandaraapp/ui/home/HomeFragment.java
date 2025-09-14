package com.rituraj.bhandaraapp.ui.home;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.bhandaraapp.Models.Project;
import com.rituraj.bhandaraapp.R;
import com.rituraj.bhandaraapp.adapters.ShowProjects;
import com.rituraj.bhandaraapp.databinding.FragmentHomeBinding;
import com.rituraj.bhandaraapp.locations.LocationManager;
import com.rituraj.bhandaraapp.locations.PermissionManager;
import com.rituraj.bhandaraapp.ui.pages.UploadPage;

import java.util.ArrayList;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private String[] foregroundLocationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private PermissionManager permissionManager;
    private LocationManager locationManager;
    private IntentFilter localBroadcastIntentFilter;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private ArrayList<Project> projects;
    private String searchData;
    public static String lat, lng;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fun(root);
        takeUserLocation();
        fetchUserLocation();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void fun(View view) {
        projects = new ArrayList<>();

        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Loading data ...");
        progressDialog.show();


        SearchView searchText = view.findViewById(R.id.searchText);

        searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(requireContext(), query, Toast.LENGTH_SHORT).show();
                searchData = query;
                getAllProjects(view);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    searchData = null;
                    getAllProjects(view);
                }
                return false;
            }
        });
        getAllProjects(view);
    }

    public void getAllProjects(View view) {
        reference = database.getReference("Projects");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                projects.clear();
                final int[] processed = {0};
                int totalChildren = (int) snapshot.getChildrenCount();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    long now = System.currentTimeMillis();
                    String projectId = snap.getKey();
                    Project project = snap.getValue(Project.class);

                    if (project.getCreatedAt() != null && (now - Long.parseLong("" + project.getCreatedAt())) >= 24 * 60 * 60 * 1000) {
                        snap.getRef().removeValue();
                    } else if (searchData != null) {
                        if (isPresent(project))
                            projects.add(project);
                    } else
                        projects.add(project);

                    processed[0]++;
                    findProject(projectId, () -> {
                        if (processed[0] == totalChildren) {
                            showAllProducts(view);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "You didn't upload any Projects!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean isPresent(Project project) {
        String fullData = project.getTitle() + " " + project.getFood() + " " + project.getLandmark() + " " + project.getCity() + " " + project.getState();
        return fullData.toLowerCase().contains(searchData.toLowerCase());
    }

    public void findProject(String projectId, Runnable onFinish) {
        DatabaseReference myRef = database.getReference("Projects");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                String email = snap.child("email").getValue(String.class);
                if (onFinish != null) {
                    onFinish.run();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (onFinish != null) {
                    onFinish.run();
                }
                Toast.makeText(requireContext(), "You didn't upload any product!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showAllProducts(View view) {
        recyclerView = view.findViewById(R.id.projectListHome);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ShowProjects cartRecycleView = new ShowProjects(requireContext(), projects);
        recyclerView.setAdapter(cartRecycleView);
    }


    // for location
    public void takeUserLocation() {
        permissionManager = PermissionManager.getInstance(requireContext());
        locationManager = LocationManager.getInstance(requireContext());

        localBroadcastIntentFilter = new IntentFilter();
        localBroadcastIntentFilter.addAction("foreground_location");
    }

    public void fetchUserLocation() {
        if (!permissionManager.checkPermissions(foregroundLocationPermissions)) {
            permissionManager.askPermissions(requireActivity(), foregroundLocationPermissions, 100);
        } else {
            if (locationManager.isLocationEnabled()) {
                Location location = locationManager.getLastLocation();
                if (location != null) {
//                    Toast.makeText(UploadPage.this, "T: " + location.getLatitude() + " Long: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    String dateTime = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new java.util.Date());
                } else {
                    Toast.makeText(requireContext(), "After a while, click.", Toast.LENGTH_SHORT).show();
                }
            } else {
                locationManager.createLocationRequest();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        locationManager.startLocationUpdates();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(foregroundLocationBroadCastReceiver, localBroadcastIntentFilter);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(foregroundLocationBroadCastReceiver);
        locationManager.stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager.handlePermissionResult(requireActivity(), 100, permissions, grantResults)) {
            locationManager.createLocationRequest();
        } else {
            Toast.makeText(requireContext(), "Permission denied.", Toast.LENGTH_SHORT).show();
        }
    }

    BroadcastReceiver foregroundLocationBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(requireContext(),
//                    intent.getStringExtra("location"), Toast.LENGTH_SHORT).show();
            String l = intent.getStringExtra("Lat");
            String ln = intent.getStringExtra("Lng");
            lat = l;
            lng = ln;
        }
    };
}
