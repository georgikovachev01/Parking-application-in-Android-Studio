package com.parking.app.views;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parking.app.R;
import com.parking.app.utils.AppUtils;

public class RegisterActivity extends AppCompatActivity {
    EditText userName, password, repeatPassword, fullName;
    TextView AccountExists;
    Button register;
    CheckBox checkBoxRobot;
    private FirebaseAuth mAuth; // Used for firebase authentication
    private DatabaseReference mDatabase; // Database reference

    private ProgressDialog loadingBar; // Used to show the progress of the registration process

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Initialize Firebase Database reference

        // Initialize the UI elements
        fullName = findViewById(R.id.names);
        userName = findViewById(R.id.username2);
        password = findViewById(R.id.Password2);
        repeatPassword = findViewById(R.id.RepeatPassword);
        checkBoxRobot = findViewById(R.id.checkbox_robot);
        register = findViewById(R.id.submit_btn);
        AccountExists = findViewById(R.id.Already_link);
        loadingBar = new ProgressDialog(this);

        // When user has an account already he should be sent to login activity.
        AccountExists.setOnClickListener(view -> sendUserToLoginActivity());

        // When user clicks on register, create a new account for the user
        register.setOnClickListener(view -> {
            if (AppUtils.isInternetAvailable(RegisterActivity.this)) {
                createNewAccount();
            } else {
                AppUtils.ToastLocal(R.string.no_internet_connection, RegisterActivity.this);
            }
        });
    }

    private void createNewAccount() {
        String email = userName.getText().toString().trim();
        String pwd = password.getText().toString();
        String repeatPwd = repeatPassword.getText().toString();
        String name = fullName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(RegisterActivity.this, "Please enter your full name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(RegisterActivity.this, "Please enter email id", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(RegisterActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pwd.equals(repeatPwd)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!checkBoxRobot.isChecked()) {
            Toast.makeText(RegisterActivity.this, "Please confirm that you are not a robot", Toast.LENGTH_SHORT).show();
            return;
        }

        // When both email and password are available create a new account
        // Show the progress on Progress Dialog
        loadingBar.setTitle("Creating New Account");
        loadingBar.setMessage("Please wait, we are creating your account");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // If account creation is successful, save user info to Firebase database
                            saveUserInfoToDatabase(name, email);
                            sendUserToLoginActivity();
                            Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        } else {
                            // Print the error message in case of failure
                            String msg = task.getException().toString();
                            Toast.makeText(RegisterActivity.this, "Error: " + msg, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    private void saveUserInfoToDatabase(String name, String email) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Creating a new user object
            User newUser = new User(email, name);

            // Saving user info in the "users" node under the userId
            mDatabase.child("users").child(userId).setValue(newUser);
        }
    }

    private void sendUserToLoginActivity() {
        // This is to send user to Login Activity.
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    // User class to structure the user data
    public static class User {
        public String email;
        public String name;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String email, String name) {
            this.email = email;
            this.name = name;
        }
    }
}
