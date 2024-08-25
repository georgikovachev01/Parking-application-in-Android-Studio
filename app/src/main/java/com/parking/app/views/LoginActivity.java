package com.parking.app.views;

import static com.parking.app.utils.AppUtils.ToastLocal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.parking.app.R;
import com.parking.app.utils.AppUtils;

public class LoginActivity extends AppCompatActivity {
    EditText userName, password;
    Button login;
    TextView register, forgotPassword;
    FirebaseUser currentUser; // used to store current user of account
    FirebaseAuth mAuth; // Used for firebase authentication
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userName = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.pwd);
        login = (Button) findViewById(R.id.login_btn);
        register = (TextView) findViewById(R.id.registerLink);
        forgotPassword = (TextView) findViewById(R.id.ForgetPassword);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        currentUser = mAuth.getCurrentUser();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppUtils.isInternetAvailable(LoginActivity.this)) {
                    AllowUserToLogin();
                } else {
                    ToastLocal(R.string.no_internet_connection, LoginActivity.this);
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppUtils.isInternetAvailable(LoginActivity.this)) {
                    sendUserToRegister();
                } else {
                    ToastLocal(R.string.no_internet_connection, LoginActivity.this);
                }
            }
        });

        // if user forgets the password then to reset it
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppUtils.isInternetAvailable(LoginActivity.this)) {
                    resetPasswordUser();
                } else {
                    ToastLocal(R.string.no_internet_connection, LoginActivity.this);
                }
            }
        });
    }

    private void resetPasswordUser() {
        String email = userName.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LoginActivity.this, "Please enter your email id", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Sending Reset Email");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loadingBar.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Reset Email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void sendUserToRegister() {
        // When user wants to create a new account send user to Register Activity
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void AllowUserToLogin() {
        String email = userName.getText().toString().trim();
        String pwd = password.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LoginActivity.this, "Please enter email id", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(LoginActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
        } else {
            // When both email and password are available log in to the account
            // Show the progress on Progress Dialog
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait, because good things always take time");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loadingBar.dismiss();
                            if (task.isSuccessful()) { // If account login successful print message and send user to main Activity
                                sendToMainActivity();
                                Toast.makeText(LoginActivity.this, "Welcome to Parky", Toast.LENGTH_SHORT).show();
                            } else { // Print the error message in case of failure
                                String msg = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Wrong username or password. Please try again with correct credentials.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        // Check if user has already signed in if yes send to mainActivity
        // This to avoid signing in every time you open the app.
        super.onStart();
        if (currentUser != null) {
            sendToMainActivity();
        }
    }

    private void sendToMainActivity() {
        // This is to send user to MainActivity
        Intent MainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(MainIntent);
    }
}
