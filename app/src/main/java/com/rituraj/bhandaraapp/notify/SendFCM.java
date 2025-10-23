package com.rituraj.bhandaraapp.notify;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

public class SendFCM {
    public static void sendFcm(Context context) {
        try {
            // Step 1: Load service account key
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(context.getAssets().open("service-account.json"))
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
            googleCredentials.refreshIfExpired();

            String token = "cxBRa6cPSlq0OK8-uz7rC4:APA91bHN8symy42u7umTYSPtDTMB0pU3Od1nuyMS2GhY8z2elqofo_59ro2PcDI9VHBwxmn0hwTxiYJQ1U4ucOSAbpTC6CpZe2aWb3hp0XfFeXZX1GeZLqg"; // Replace with your FCM token
            String projectId = "bhandaraapp-79f29";   // Replace with Firebase project ID

            // Step 2: Build notification payload
            JSONObject notification = new JSONObject();
            notification.put("title", "Hello Rituraj!");
            notification.put("body", "New message via HTTP v1 API");

            JSONObject message = new JSONObject();
            message.put("token", token);
            message.put("notification", notification);

            JSONObject payload = new JSONObject();
            payload.put("message", message);

            // Step 3: Send HTTP request
            URL url = new URL("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + googleCredentials.getAccessToken().getTokenValue());
            conn.setRequestProperty("Content-Type", "application/json; UTF-8");
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            Toast.makeText(context, "Response Code: " + responseCode, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("MyError", e.getMessage());
            e.printStackTrace();
        }
    }
}
