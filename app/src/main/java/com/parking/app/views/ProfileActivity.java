package com.parking.app.views;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.parking.app.R;
import com.parking.app.utils.AppUtils;

public class ProfileActivity extends AppCompatActivity {

    private EditText emailEditText, nameEditText, phoneEditText;
    private ImageView profilePhotoImageView;
    private Button backButton, updateButton, changePhotoButton;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        emailEditText = findViewById(R.id.emailEditText);
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        profilePhotoImageView = findViewById(R.id.profilePhotoImageView);
        changePhotoButton = findViewById(R.id.changePhotoButton);
        backButton = findViewById(R.id.backButton);
        updateButton = findViewById(R.id.updateButton);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference().child("profile_photos").child(currentUser.getUid() + ".jpg");

        if (AppUtils.isInternetAvailable(this)) {
            // Fetch and display user details including profile photo
            fetchUserProfile();
        } else {
            AppUtils.ToastLocal(R.string.no_internet_connection, this);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppUtils.isInternetAvailable(ProfileActivity.this)) {
                    // Handle update button click
                    updateProfile();
                } else {
                    AppUtils.ToastLocal(R.string.no_internet_connection, ProfileActivity.this);
                }
            }
        });

        changePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppUtils.isInternetAvailable(ProfileActivity.this)) {
                    openFileChooser();
                } else {
                    AppUtils.ToastLocal(R.string.no_internet_connection, ProfileActivity.this);
                }
            }
        });
    }

    private void fetchUserProfile() {
        // Fetch user profile information from Firebase and populate the UI fields
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email = dataSnapshot.child("email").getValue(String.class);
                String name = dataSnapshot.child("name").getValue(String.class);
                String phone = dataSnapshot.child("phone").getValue(String.class);

                if (email != null) {
                    emailEditText.setText(email);
                }
                if (name != null) {
                    nameEditText.setText(name);
                }
                if (phone != null) {
                    phoneEditText.setText(phone);
                }

                // Load profile photo if exists
                if (dataSnapshot.hasChild("profile_photo_url")) {
                    String photoUrl = dataSnapshot.child("profile_photo_url").getValue(String.class);
                    // TODO: Load photo using Picasso/Glide or download URL directly to ImageView
                    // Example: Picasso.get().load(photoUrl).into(profilePhotoImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(ProfileActivity.this, "Failed to fetch user profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        // Update profile information in Firebase
        String newEmail = emailEditText.getText().toString().trim();
        String newName = nameEditText.getText().toString().trim();
        String newPhone = phoneEditText.getText().toString().trim();

        databaseRef.child("email").setValue(newEmail);
        databaseRef.child("name").setValue(newName);
        databaseRef.child("phone").setValue(newPhone)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update successful
                        Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                        // Update profile photo if image selected
                        if (imageUri != null) {
                            uploadProfilePhoto();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failed update
                        Toast.makeText(ProfileActivity.this, "Failed to update profile information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadProfilePhoto() {
        // Upload profile photo to Firebase Storage
        UploadTask uploadTask = storageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get the download URL of the uploaded image
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Save the download URL to Firebase Database
                        databaseRef.child("profile_photo_url").setValue(uri.toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ProfileActivity.this, "Profile photo uploaded successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ProfileActivity.this, "Failed to update profile photo URL in database", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle failed upload
                Toast.makeText(ProfileActivity.this, "Failed to upload profile photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                // Retry the upload if necessary
                retryUpload();
            }
        });
    }

    // Retry upload if failed
    private void retryUpload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Upload Failed");
        builder.setMessage("Failed to upload profile photo. Retry?");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uploadProfilePhoto(); // Retry the upload
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle cancellation
            }
        });
        builder.show();
    }

    // Open file chooser to select an image from gallery
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Handle result of file chooser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePhotoImageView.setImageURI(imageUri);
        }
    }

    public void onBackButtonClick(View view) {
        onBackPressed();
    }

}
