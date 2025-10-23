package com.rituraj.bhandaraapp.ui.pages;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.rituraj.bhandaraapp.MainActivity;
import com.rituraj.bhandaraapp.Models.Project;
import com.rituraj.bhandaraapp.R;
import com.rituraj.bhandaraapp.databinding.ActivityUploadPageBinding;
import com.rituraj.bhandaraapp.locations.LocationManager;
import com.rituraj.bhandaraapp.locations.PermissionManager;
import com.rituraj.bhandaraapp.notify.SendFCM;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class UploadPage extends AppCompatActivity {
    private ActivityUploadPageBinding binding;
    private String[] foregroundLocationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private PermissionManager permissionManager;
    private LocationManager locationManager;
    private IntentFilter localBroadcastIntentFilter;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private static final int REQUEST_IMAGE = 101;
    private String photo, title, whatFood, landmark, city, pincode, state;
    private Project project;
    private String emailKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUploadPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fun();
        takeUserLocation();
    }

    public void fun() {
        project = new Project();
        progressDialog = new ProgressDialog(UploadPage.this);
        progressDialog.setTitle("Upload");
        progressDialog.setMessage("Uploading data ...");

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        emailKey = auth.getCurrentUser().getEmail().replace(".", ",");

        binding.uploadCamraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(UploadPage.this).cameraOnly().crop().compress(1024).maxResultSize(1080, 1080).start(REQUEST_IMAGE);
            }
        });
        binding.uploadLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UploadPage.this, "Your Current Location fetching..", Toast.LENGTH_SHORT).show();
                fetchUserLocation();
            }
        });
        binding.uploadSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadProject();
            }
        });
    }

    private Bitmap getBitmapFromUri(@NonNull Uri uri) {
        try {
            Context ctx = UploadPage.this; // <-- Fragment में Context ऐसे लो
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source src = ImageDecoder.createSource(ctx.getContentResolver(), uri);
                return ImageDecoder.decodeBitmap(src);
            } else {
                return MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == REQUEST_IMAGE) {
                binding.uploadPhoto.setImageURI(uri);
                photo = encodeImageToBase64(getBitmapFromUri(uri));
                project.setPhoto(photo);
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(UploadPage.this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(UploadPage.this, "Cancelled!", Toast.LENGTH_SHORT).show();
        }
    }

    // for location
    public void takeUserLocation() {
        permissionManager = PermissionManager.getInstance(this);
        locationManager = LocationManager.getInstance(this);

        localBroadcastIntentFilter = new IntentFilter();
        localBroadcastIntentFilter.addAction("foreground_location");
    }

    public void fetchUserLocation() {
        if (!permissionManager.checkPermissions(foregroundLocationPermissions)) {
            permissionManager.askPermissions(UploadPage.this, foregroundLocationPermissions, 100);
        } else {
            if (locationManager.isLocationEnabled()) {
                Location location = locationManager.getLastLocation();
                if (location != null) {
//                    Toast.makeText(UploadPage.this, "T: " + location.getLatitude() + " Long: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    String dateTime = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new java.util.Date());
                    project.setTimestamp(dateTime);
                    project.setCreatedAt(ServerValue.TIMESTAMP);
                    getAddressFromLocation(UploadPage.this, location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(UploadPage.this, "After a while, click.", Toast.LENGTH_SHORT).show();
                }
            } else {
                locationManager.createLocationRequest();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.startLocationUpdates();
        LocalBroadcastManager.getInstance(UploadPage.this).registerReceiver(foregroundLocationBroadCastReceiver, localBroadcastIntentFilter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(UploadPage.this).unregisterReceiver(foregroundLocationBroadCastReceiver);
        locationManager.stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager.handlePermissionResult(UploadPage.this, 100, permissions, grantResults)) {
            locationManager.createLocationRequest();
        } else {
            Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
        }
    }

    BroadcastReceiver foregroundLocationBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(UploadPage.this,
//                    intent.getStringExtra("location"), Toast.LENGTH_SHORT).show();
        }
    };

    private void getAddressFromLocation(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        project.setLat(lat + "");
        project.setLng(lng + "");
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
//                return addresses.get(0).getAddressLine(0); // Full address
                binding.uploadLandmark.setText(addresses.get(0).getSubLocality());
                binding.uploadCity.setText(addresses.get(0).getLocality());
                binding.uploadPincode.setText(addresses.get(0).getPostalCode());
                binding.uploadState.setText(addresses.get(0).getAdminArea());
            }
        } catch (Exception ignored) {
        }
    }

    public void uploadProject() {
        title = binding.uploadTitle.getText().toString();
        whatFood = binding.uploadWhatFood.getText().toString();
        landmark = binding.uploadLandmark.getText().toString();
        city = binding.uploadCity.getText().toString();
        pincode = binding.uploadPincode.getText().toString();
        state = binding.uploadState.getText().toString();
        if (photo != null && !title.isEmpty() && !whatFood.isEmpty() && !landmark.isEmpty() && !city.isEmpty() && !pincode.isEmpty() && !state.isEmpty()) {
            progressDialog.show();
            project.setPhoto(photo);
            project.setTitle(title);
            project.setFood(whatFood);
            project.setLandmark(landmark);
            project.setCity(city);
            project.setPincode(pincode);
            project.setState(state);
            database.getReference().child("Projects").push().setValue(project).addOnSuccessListener(aVoid -> {
                progressDialog.dismiss();
                Toast.makeText(UploadPage.this, "Save Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UploadPage.this, MainActivity.class));
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(UploadPage.this, "Save Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else Toast.makeText(UploadPage.this, "Fill the all Details!", Toast.LENGTH_SHORT).show();
    }

    // notification
    public void sendNotification(String message) {
        // ProductId, Title, Message, Token
        String productId = "-aSDSDLK_oasdl_32";
        String title = "New Bhandara Event Found";
        String otherUserToken = "cxBRa6cPSlq0OK8-uz7rC4:APA91bHN8symy42u7umTYSPtDTMB0pU3Od1nuyMS2GhY8z2elqofo_59ro2PcDI9VHBwxmn0hwTxiYJQ1U4ucOSAbpTC6CpZe2aWb3hp0XfFeXZX1GeZLqg";
        try {
            JSONObject jsonObject = new JSONObject();

            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", title);
            notificationObj.put("body", message);

            JSONObject dataObj = new JSONObject();
            dataObj.put("productId", productId);

            jsonObject.put("notification", notificationObj);
            jsonObject.put("data", dataObj);
            jsonObject.put("to", otherUserToken);
            callApi(jsonObject);
        } catch (Exception ignored) {
        }

    }

    void callApi(JSONObject jsonObject) {
        final MediaType JSON = MediaType.get("application/json");
        OkHttpClient client = new OkHttpClient();

        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer SEQUESTER_KEY")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(UploadPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Toast.makeText(UploadPage.this, "Notification Sent!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
