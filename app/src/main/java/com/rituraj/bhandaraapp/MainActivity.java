package com.rituraj.bhandaraapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rituraj.bhandaraapp.Models.Project;
import com.rituraj.bhandaraapp.Models.User;
import com.rituraj.bhandaraapp.databinding.ActivityMainBinding;
import com.rituraj.bhandaraapp.notify.LoggerService;
import com.rituraj.bhandaraapp.ui.pages.ProjectPage;
import com.rituraj.bhandaraapp.ui.pages.UploadPage;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_notifications, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        getSupportActionBar().hide();
        fun();
    }

    public void fun() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        binding.currUserName.setText(auth.getCurrentUser().getDisplayName());

        binding.addProjectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UploadPage.class));
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            } else {
                // Permission already granted, you can send notifications
            }
        }

//        startService(new Intent(this, LoggerService.class));

        manageNotification();
//        getUserToken();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can send notifications
            } else {
                // Permission denied, show a message or handle accordingly
            }
        }
    }

    public void getUserToken() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = user.getUid();
        User user1 = new User();
        user1.setEmail(user.getEmail());
        user1.setUserName(user.getDisplayName());
        user1.setProfileUrl(String.valueOf(user.getPhotoUrl()));
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                user1.setNotificationToken(String.valueOf(token));
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                myRef.setValue(user1);
            }
        });
    }

    public void manageNotification() {
        final boolean[] initialLoad = {true};
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Projects");
        reference.keepSynced(true);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (initialLoad[0]) {
                    return;
                }
                Toast.makeText(MainActivity.this, "New Data Detect", Toast.LENGTH_SHORT).show();
                Project project = snapshot.getValue(Project.class);
                notificationSetting(project);
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
            }
        });
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                initialLoad[0] = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void notificationSetting(Project project) {
        final String CHANNEL_ID = "New Event Found";
        final String CHANNEL_NAME = "New Event Notifications";
        final int REQUEST_CODE = 100;
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.food_safety, null);
        Bitmap largeIcon = ((BitmapDrawable) drawable).getBitmap();

        Intent intent = new Intent(getApplicationContext(), ProjectPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification newProjectNotification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            newProjectNotification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.bhandara_logo) // app icon
                    .setLargeIcon(largeIcon) // notification icon
                    .setContentTitle("New Bhandara event found!")
                    .setContentText(project.getCity())
                    .setChannelId(CHANNEL_ID)
                    .setContentIntent(pendingIntent)
                    .setStyle(BigPictureStyleNotification(project))
                    .setAutoCancel(true)
                    .build();
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));
        } else {
            newProjectNotification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.bhandara_logo) // app icon
                    .setLargeIcon(largeIcon)
                    .setContentTitle("New Bhandara event found!")
                    .setContentText(project.getCity())
                    .setContentIntent(pendingIntent)
                    .setStyle(BigPictureStyleNotification(project))
                    .setAutoCancel(true)
                    .build();
        }
        manager.notify((int) System.currentTimeMillis(), newProjectNotification);
    }

    public Notification.BigPictureStyle BigPictureStyleNotification(Project project) {
        String fullAddress = project.getLandmark() + ", " + project.getPincode() + ", " + project.getCity() + ", " + project.getState();
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.food_safety, null);
        Bitmap MyBigPicture = ((BitmapDrawable) drawable).getBitmap();
        Notification.BigPictureStyle bigPictureStyle = new Notification.BigPictureStyle()
                .bigPicture(MyBigPicture)
                .setBigContentTitle("New Bhandara event found!\n" + project.getFood())
                .setSummaryText(fullAddress);
        return bigPictureStyle;
    }

    public Notification.InboxStyle inboxStyleNotification() {
        Notification.InboxStyle inboxStyle = new Notification.InboxStyle()
                .addLine("A")
                .addLine("B")
                .addLine("C")
                .addLine("D")
                .addLine("F")
                .setBigContentTitle("New Bhandara event found!")
                .setSummaryText("New Delhi 21009, Chipyana");
        return inboxStyle;
    }
}