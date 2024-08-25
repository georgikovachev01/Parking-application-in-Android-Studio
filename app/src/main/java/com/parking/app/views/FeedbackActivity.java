package com.parking.app.views;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parking.app.R;
import com.parking.app.models.Feedback;
import com.parking.app.utils.AppUtils;

public class FeedbackActivity extends AppCompatActivity {

    private EditText feedbackEditText;
    private RatingBar ratingBar;
    private Button submitFeedbackButton;
    private DatabaseReference feedbackDatabase;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Initialize Firebase Database reference
        feedbackDatabase = FirebaseDatabase.getInstance().getReference("MainPages").child("Feedback");

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize views
        feedbackEditText = findViewById(R.id.feedbackEditText);
        ratingBar = findViewById(R.id.ratingBar);
        submitFeedbackButton = findViewById(R.id.submitFeedbackButton);

        // Set up submit button listener
        submitFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppUtils.isInternetAvailable(getApplicationContext())) {
                    submitFeedback();
                } else {
                    AppUtils.ToastLocal(R.string.no_internet_connection, FeedbackActivity.this);
                }
            }
        });
    }

    private void submitFeedback() {
        String feedback = feedbackEditText.getText().toString().trim();
        int rating = (int) ratingBar.getRating();

        if (feedback.isEmpty()) {
            Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid(); // Get user ID
        String emailId = currentUser.getEmail(); // Get user email ID

        // Get current date (example: "2024-08-06")
        String currentDate = java.text.DateFormat.getDateInstance().format(new java.util.Date());

        // Create feedback object
        Feedback feedbackObject = new Feedback(feedback, currentDate, userId, emailId, rating);

        // Save feedback to Firebase
        feedbackDatabase.child(userId).setValue(feedbackObject)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(FeedbackActivity.this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity
                    } else {
                        Toast.makeText(FeedbackActivity.this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
