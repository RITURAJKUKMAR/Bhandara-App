package com.rituraj.bhandaraapp.ui.pages;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rituraj.bhandaraapp.MainActivity;
import com.rituraj.bhandaraapp.Models.User;
import com.rituraj.bhandaraapp.R;
import com.rituraj.bhandaraapp.databinding.ActivityLoginPageBinding;

import java.util.concurrent.atomic.AtomicReference;

public class LoginPage extends AppCompatActivity {
    private ActivityLoginPageBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private GoogleSignInClient googleSignInClient;
    private final int RC_SIGN_IN = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception ignored) {
        }
        EdgeToEdge.enable(this);
        binding = ActivityLoginPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fun();
//        navigateToNotificationProjectPage();
    }

    public void fun() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(LoginPage.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Login to your Account");

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                signInSetting();
            }
        });
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginPage.this, MainActivity.class));
            finish();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void signInSetting() {
        Intent singnIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(singnIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RC_SIGN_IN == requestCode) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null)
                    firebaseAuthWithGoogle(account.getIdToken());
            } catch (Exception e) {
                Toast.makeText(LoginPage.this, "SignIn Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        User user1 = new User();
                        user1.setEmail(user.getEmail());
                        user1.setUserName(user.getDisplayName());
                        user1.setProfileUrl(user.getPhotoUrl().toString());
                        database.getReference().child("Users").child(user.getUid()).setValue(user1);
                        Toast.makeText(LoginPage.this, "Login", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginPage.this, MainActivity.class));
                        finish();
                    } else
                        Toast.makeText(LoginPage.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }

    public void navigateToNotificationProjectPage() {
        if (getIntent().getExtras() != null) {
            String title = getIntent().getExtras().getString("title");
            String message = getIntent().getExtras().getString("message");
            Intent intent = new Intent(LoginPage.this, ProjectPage.class);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            startActivity(intent);
        }
    }
}
