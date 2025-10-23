package com.rituraj.bhandaraapp.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.bhandaraapp.Models.Project;
import com.rituraj.bhandaraapp.R;
import com.rituraj.bhandaraapp.ui.home.HomeFragment;

import java.util.ArrayList;

public class ShowProjects extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<Project> projects;

    public ShowProjects(Context context, ArrayList<Project> projects) {
        this.context = context;
        this.projects = projects;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.projects_card, parent, false);
        return new ShowProjects.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Project project = projects.get(position);
        ViewHolder vh = (ViewHolder) holder;

        Glide.with(context).load(decodeBase64ToImage(project.getPhoto())).placeholder(R.drawable.bhandara_logo).error(R.drawable.bhandara_logo).into(vh.cardImg);

        vh.cardTitle.setText(project.getTitle());
        vh.cardDate.setText(project.getTimestamp());
        vh.cardFood.setText(project.getFood());
        String fullAddress = project.getLandmark() + ", " + project.getPincode() + ", " + project.getCity() + ", " + project.getState();
        vh.cardFullAddress.setText(fullAddress);

        vh.cardDistanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dis = distanceInMeters(project.getLat(), project.getLng());
                if (HomeFragment.lat == null)
                    Toast.makeText(context, "After a while, click.", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(context, dis, Toast.LENGTH_SHORT).show();
                    vh.cardDistanceBtn.setText(dis + " - FIND DISTANCE");
                }
            }
        });
        vh.routeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double lat = Double.parseDouble(project.getLat());
                    double lng = Double.parseDouble(project.getLng());

                    String uri = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps"); // force open in Google Maps
                    context.startActivity(intent);

                    Toast.makeText(context, "Find Best Route..", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public Bitmap decodeBase64ToImage(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public String projectId, emailKey;
        public ImageView cardImg;
        public Button cardDistanceBtn, routeBtn;
        public TextView cardTitle, cardDate, cardFood, cardFullAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            cardImg = itemView.findViewById(R.id.cardImg);
            cardTitle = itemView.findViewById(R.id.cardTitle);
            cardDistanceBtn = itemView.findViewById(R.id.cardDistanceBtn);
            routeBtn = itemView.findViewById(R.id.routeBtn);
            cardDate = itemView.findViewById(R.id.cardDate);
            cardFood = itemView.findViewById(R.id.cardFood);
            cardFullAddress = itemView.findViewById(R.id.cardFullAddress);
        }

        @Override
        public void onClick(View v) {
//            Toast.makeText(context, "Click", Toast.LENGTH_SHORT).show();
        }
    }

    public String distanceInMeters(String proLat, String proLng) {
        if (HomeFragment.lat != null && HomeFragment.lng != null) {
            double lat1 = Double.parseDouble(HomeFragment.lat), lon1 = Double.parseDouble(HomeFragment.lng);
            double lat2 = Double.parseDouble(proLat), lon2 = Double.parseDouble(proLng);
            final int R = 6371000; // Earth radius in meters
            double latDistance = Math.toRadians(lat2 - lat1);
            double lonDistance = Math.toRadians(lon2 - lon1);
            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            long distance = (long) Math.round(R * c);
            long km = distance / 1000;
            long m = distance % 1000;
            return km + "." + m + " KM";
        }
        return null;
    }
}
