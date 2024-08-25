package com.parking.app.views;

import static com.parking.app.utils.AppUtils.ToastLocal;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parking.app.R;
import com.parking.app.utils.AppUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editTextCurrentPassword;
    private EditText editTextNewPassword;
    private EditText editTextConfirmNewPassword; // Добавено поле за потвърждаване на новата парола
    private Button buttonChangePassword;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize Database Reference for ChangePassword node
        databaseReference = FirebaseDatabase.getInstance().getReference("MainPages").child("ChangePassword");

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmNewPassword = findViewById(R.id.editTextConfirmNewPassword); // Инициализация на новото поле
        buttonChangePassword = findViewById(R.id.buttonChangePassword);

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle toolbar item clicks
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Handle back button click
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changePassword() {
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmNewPassword = editTextConfirmNewPassword.getText().toString().trim(); // Получаваме потвърждението на новата парола

        if (TextUtils.isEmpty(currentPassword)) {
            editTextCurrentPassword.setError("Enter current password");
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            editTextNewPassword.setError("Enter new password");
            return;
        }

        if (TextUtils.isEmpty(confirmNewPassword)) {
            editTextConfirmNewPassword.setError("Confirm new password");
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            editTextConfirmNewPassword.setError("Passwords do not match");
            return;
        }

        if (AppUtils.isInternetAvailable(this)) {
            // Хеширане на старите и новите пароли
            String hashedCurrentPassword = hashPassword(currentPassword);
            String hashedNewPassword = hashPassword(newPassword);

            // Change password using Firebase Auth
            mAuth.getCurrentUser().updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Update the password in the specified location in the Realtime Database
                                String userId = currentUser.getUid();
                                databaseReference.child("userid").setValue(userId);
                                databaseReference.child("OldPassword").setValue(hashedCurrentPassword);
                                databaseReference.child("NewPassword").setValue(hashedNewPassword)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> dbTask) {
                                                if (dbTask.isSuccessful()) {
                                                    Toast.makeText(ChangePasswordActivity.this, "Password changed and saved successfully", Toast.LENGTH_SHORT).show();
                                                    finish(); // Finish this activity and return to previous screen
                                                } else {
                                                    Toast.makeText(ChangePasswordActivity.this, "Password changed, but failed to save in database: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "Failed to change password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            ToastLocal(R.string.no_internet_connection, this);
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
