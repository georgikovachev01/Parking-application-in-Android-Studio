package com.parking.app.views;

import static android.Manifest.permission.CALL_PHONE;
import static com.parking.app.AppContants.RecommendAndReserve;
import static com.parking.app.AppContants.Recommended;
import static com.parking.app.AppContants.Reserved;
import static com.parking.app.AppContants.SlotReviews;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parking.app.AppContants;
import com.parking.app.R;
import com.parking.app.models.ParkingSlot;
import com.parking.app.utils.AppUtils;

import java.util.ArrayList;
import java.util.Map;

public class FindParkingDetailsActivity extends AppCompatActivity {

    private RadioGroup priceRadioGroup;
    private TextView txt_title, txt_city, txt_contacts;
    private ImageView img_back, img_parking;
    Button bt_recommend, bt_reserve, bt_recommend_reserve;
    RatingBar ratingBar;
    String selectedRating, userid, email, parkingimage;
    Map<String, Integer> price;
    ArrayList<Map<String, Integer>> reviews;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_parking_details);
        firebaseAuth = FirebaseAuth.getInstance();

        // Get current user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        userid = currentUser.getUid(); // Get user ID
        email = currentUser.getEmail();// user email id

        Intent intent = getIntent();

        ParkingSlot parkingSlot = intent.getExtras().getParcelable("obj");
        parkingimage = intent.getExtras().getString(AppContants.parkingImage);
        int resId = getResources().getIdentifier(parkingimage, "drawable", this.getPackageName());
        Drawable d = this.getResources().getDrawable(resId);
        price = (Map<String, Integer>) intent.getExtras().getSerializable(AppContants.SlotMapPrices);
        reviews = (ArrayList<Map<String, Integer>>) intent.getExtras().getSerializable(SlotReviews);

        img_parking = findViewById(R.id.img_parking);
        txt_contacts = findViewById(R.id.txt_contacts);
        priceRadioGroup = findViewById(R.id.priceRadioGroup);
        txt_title = findViewById(R.id.txt_title);
        bt_recommend_reserve = findViewById(R.id.bt_recommend_reserve);
        txt_city = findViewById(R.id.txt_city);
        img_back = findViewById(R.id.img_back);
        ratingBar = findViewById(R.id.ratingBar);
        bt_recommend = findViewById(R.id.bt_recommend);
        bt_reserve = findViewById(R.id.bt_reserve);

        img_parking.setImageDrawable(d);

        // bt_contact = findViewById(R.id.bt_contact);
        // Add radio buttons dynamically
        Map<String, Integer> pricesMap = price;

        txt_contacts.setText("Contact :" + parkingSlot.getContact());
        txt_contacts.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                callPhoneIntent(parkingSlot.getContact() + "");
            }
        });
        if (pricesMap != null) {
            for (Map.Entry<String, Integer> entry : pricesMap.entrySet()) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(entry.getKey() + ": $" + entry.getValue());
                radioButton.setId(View.generateViewId());
                priceRadioGroup.addView(radioButton);
            }
        }
        txt_title.setText("Name : " + parkingSlot.getName());
        txt_city.setText("City : " + parkingSlot.getCity());
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        bt_recommend.setOnClickListener(view -> {
            handlePriceSelection(priceRadioGroup, parkingSlot, Recommended);
        });
        bt_reserve.setOnClickListener(view -> {
            handlePriceSelection(priceRadioGroup, parkingSlot, Reserved);
        });
        bt_recommend_reserve.setOnClickListener(view -> {
            handlePriceSelection(priceRadioGroup, parkingSlot, RecommendAndReserve);
        });
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                selectedRating = ratingBar.getRating() + "";
            }
        });
    }


    private void handlePriceSelection(RadioGroup priceRadioGroup, ParkingSlot slot, String status) {
        if (AppUtils.isInternetAvailable(this)) {
            // Check if the slot is already recommended or reserved
            if (Recommended.equals(slot.getStatus()) || Reserved.equals(slot.getStatus())) {
                Toast.makeText(this, "This slot is already " + slot.getStatus().toLowerCase(), Toast.LENGTH_SHORT).show();
                return; // Prevent any further action
            }

            int selectedId = priceRadioGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                // Find the selected RadioButton by its ID
                RadioButton selectedRadioButton = priceRadioGroup.findViewById(selectedId);
                if (selectedRadioButton != null) {
                    String selectedPrice = selectedRadioButton.getText().toString();
                    slot.setSelectedPrice(selectedPrice);
                    slot.setStatus(status);
                    slot.setReviews(reviews);
                    slot.setPrices(price);
                    slot.setEmail(email);
                    slot.setUserid(userid);
                    slot.setParkingimage(parkingimage);
                    if (selectedRating != null) {
                        slot.setSelectedRating(selectedRating);
                    } else {
                        slot.setSelectedRating("0");
                    }
                    // Update the slot status and selected price in Firebase
                    DatabaseReference slotRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots")
                            .child(slot.getValueKey());
                    slotRef.setValue(slot).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(FindParkingDetailsActivity.this, " Slot value submitted successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Close activity
                        } else {
                            Toast.makeText(FindParkingDetailsActivity.this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                        }
                    });
                    // slotRef.child("SelectedPrice").setValue(selectedPrice);

                    // Create notification for the action
                    createNotification("You have " + status.toLowerCase() + " the parking slot: " + slot.getName() + " at " + selectedPrice);
                    Toast.makeText(this, "Parking slot " + status.toLowerCase() + " successfully!", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else {
                    Toast.makeText(this, "Error finding selected price option.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please select a price", Toast.LENGTH_SHORT).show();
            }
        } else {
            AppUtils.ToastLocal(R.string.no_internet_connection, this);
        }

    }

    private void createNotification(String message) {
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "parking_app_channel")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Parking Slot Status Updated")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ParkingAppChannel";
            String description = "Channel for Parking App notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("parking_app_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void callPhoneIntent(String phoneNumber) {
        try {
            String mobileNo = phoneNumber;
            String uri = "tel:" + mobileNo.trim();
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
            requestPermission();
        }

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{CALL_PHONE}, 1002);

    }


}