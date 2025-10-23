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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
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
    private ArrayList<Project> projects, filterProjectList;
    private ShowProjects cartRecycleView;
    private String searchData;
    public static String lat, lng;
    private View view;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);

        this.view = root;
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
        filterProjectList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Loading data ...");
        progressDialog.show();


        SearchView searchText = view.findViewById(R.id.searchProject);
        searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(requireContext(), query, Toast.LENGTH_SHORT).show();
                searchData = query;
                filterProjectList.clear();
                for (Project p : projects) {
                    if (isPresent(p))
                        filterProjectList.add(p);
                }
                showFilterProducts(view);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    searchData = null;
                    showAllProducts(view);
                }
                return false;
            }
        });
        getAllProjects(view);
    }

    public void getAllProjects(View view) {
        showAllProducts(view);
        reference = database.getReference("Projects");
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }
        });
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Project project = snapshot.getValue(Project.class);
                if (project != null) {
                    project.setProjectId(snapshot.getKey());
                    long now = System.currentTimeMillis();
                    if (project.getCreatedAt() != null && (now - Long.parseLong("" + project.getCreatedAt())) >= 24 * 60 * 60 * 1000) {
                        snapshot.getRef().removeValue();
                    }
                    projects.add(project);
                    cartRecycleView.notifyItemInserted(projects.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean isPresent(Project project) {
        String fullData = project.getTitle() + " " + project.getFood() + " " + project.getLandmark() + " " + project.getCity() + " " + project.getState();
        return fullData.toLowerCase().contains(searchData.toLowerCase());
    }

    public void showAllProducts(View view) {
        recyclerView = view.findViewById(R.id.projectListHome);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        cartRecycleView = new ShowProjects(requireContext(), projects);
        recyclerView.setAdapter(cartRecycleView);
    }

    public void showFilterProducts(View view) {
        recyclerView = view.findViewById(R.id.projectListHome);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        cartRecycleView = new ShowProjects(requireContext(), filterProjectList);
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
            String l = intent.getStringExtra("Lat");
            String ln = intent.getStringExtra("Lng");
            lat = l;
            lng = ln;
        }
    };
}
