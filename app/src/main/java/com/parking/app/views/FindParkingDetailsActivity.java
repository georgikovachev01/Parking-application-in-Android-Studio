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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private TextView txt_title, txt_city, txt_contacts, tvAvailableSpots;
    private EditText et_hours;  // Поле за период
    private ImageView img_back, img_parking;
    Button bt_recommend, bt_reserve, bt_recommend_reserve, bt_navigate;
    String userid, email, parkingimage;
    Map<String, Integer> price;
    ArrayList<Map<String, Integer>> reviews;
    private FirebaseAuth firebaseAuth;
    private double latitude, longitude;

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
        email = currentUser.getEmail(); // User email ID

        Intent intent = getIntent();

        ParkingSlot parkingSlot = intent.getExtras().getParcelable("obj");
        parkingimage = intent.getExtras().getString(AppContants.parkingImage);
        int resId = getResources().getIdentifier(parkingimage, "drawable", this.getPackageName());
        Drawable d = this.getResources().getDrawable(resId);
        price = (Map<String, Integer>) intent.getExtras().getSerializable(AppContants.SlotMapPrices);
        reviews = (ArrayList<Map<String, Integer>>) intent.getExtras().getSerializable(SlotReviews);

        // Retrieve the latitude and longitude
        latitude = parkingSlot.getLatitude();
        longitude = parkingSlot.getLongitude();

        img_parking = findViewById(R.id.img_parking);
        txt_contacts = findViewById(R.id.txt_contacts);
        priceRadioGroup = findViewById(R.id.priceRadioGroup);
        et_hours = findViewById(R.id.et_hours);  // Инициализираме полето за период
        txt_title = findViewById(R.id.txt_title);
        bt_recommend_reserve = findViewById(R.id.bt_recommend_reserve);
        txt_city = findViewById(R.id.txt_city);
        img_back = findViewById(R.id.img_back);
        bt_recommend = findViewById(R.id.bt_recommend);
        bt_reserve = findViewById(R.id.bt_reserve);
        bt_navigate = findViewById(R.id.bt_navigate);  // Инициализираме бутона Navigate
        tvAvailableSpots = findViewById(R.id.tv_available_spots); // Инициализираме TextView за свободни места

        img_parking.setImageDrawable(d);

        txt_contacts.setText("Contact: " + parkingSlot.getContact());
        txt_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callPhoneIntent(parkingSlot.getContact() + "");
            }
        });

        // Показваме броя на свободните места
        tvAvailableSpots.setText("Свободни места: " + parkingSlot.getAvailableSpots());

        if (price != null) {
            for (Map.Entry<String, Integer> entry : price.entrySet()) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(entry.getKey() + ": $" + entry.getValue());
                radioButton.setId(View.generateViewId());
                priceRadioGroup.addView(radioButton);
            }
        }

        txt_title.setText("Name: " + parkingSlot.getName());
        txt_city.setText("City: " + parkingSlot.getCity());
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

        // Set up navigation button
        bt_navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToParking();
            }
        });
    }

    private void navigateToParking() {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Google Maps is not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePriceSelection(RadioGroup priceRadioGroup, ParkingSlot slot, String status) {
        if (AppUtils.isInternetAvailable(this)) {
            int selectedId = priceRadioGroup.getCheckedRadioButtonId();
            String enteredPeriod = et_hours.getText().toString().trim();  // Получаваме въведения период

            if (selectedId != -1 || !enteredPeriod.isEmpty()) {
                // Намаляване на свободните места с 1, ако статусът е резервиран
                if (Reserved.equals(status)) {
                    int updatedAvailableSpots = slot.getAvailableSpots() - 1;
                    if (updatedAvailableSpots < 0) updatedAvailableSpots = 0; // Не допускаме отрицателни стойности
                    slot.setAvailableSpots(updatedAvailableSpots);
                }

                // Задаване на избраната цена и статус
                RadioButton selectedRadioButton = selectedId != -1 ? priceRadioGroup.findViewById(selectedId) : null;
                String selectedPrice = selectedRadioButton != null ? selectedRadioButton.getText().toString() : enteredPeriod;
                slot.setSelectedPrice(selectedPrice);

                // Ако вече е препоръчан и сега се резервира, задаваме комбиниран статус
                if (Recommended.equals(slot.getStatus()) && Reserved.equals(status)) {
                    slot.setStatus(RecommendAndReserve);
                } else if (Reserved.equals(slot.getStatus()) && Recommended.equals(status)) {
                    slot.setStatus(RecommendAndReserve);
                } else if (!RecommendAndReserve.equals(slot.getStatus())) {
                    slot.setStatus(status);
                }

                slot.setReviews(reviews);
                slot.setPrices(price);
                slot.setEmail(email);
                slot.setUserid(userid);
                slot.setParkingimage(parkingimage);

                // Актуализиране на данните за паркинга във Firebase
                DatabaseReference slotRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots")
                        .child(slot.getValueKey());
                slotRef.setValue(slot).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(FindParkingDetailsActivity.this, "Slot value submitted successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Затваряме активността
                    } else {
                        Toast.makeText(FindParkingDetailsActivity.this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                    }
                });

                // Създаване на нотификация за действието
                createNotification("You have " + status.toLowerCase() + " the parking slot: " + slot.getName() + " for " + selectedPrice);
                Toast.makeText(this, "Parking slot " + status.toLowerCase() + " successfully!", Toast.LENGTH_SHORT).show();
                onBackPressed();
            } else {
                Toast.makeText(this, "Please select a price or enter a period", Toast.LENGTH_SHORT).show();
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
