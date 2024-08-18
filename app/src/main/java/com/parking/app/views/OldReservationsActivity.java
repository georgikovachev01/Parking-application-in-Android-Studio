package com.parking.app.views;

import static com.parking.app.AppContants.RecommendAndReserve;
import static com.parking.app.AppContants.Recommended;
import static com.parking.app.AppContants.Reserved;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parking.app.R;
import com.parking.app.adapters.ParkingSlotAdapter;
import com.parking.app.adapters.interfaces.OnItemActionSelected;
import com.parking.app.models.ParkingSlot;
import com.parking.app.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OldReservationsActivity extends AppCompatActivity implements OnItemActionSelected {

    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private ParkingSlotAdapter parkingSlotAdapter;
    private List<ParkingSlot> parkingSlotList;
    private FirebaseAuth firebaseAuth;
    String userid, auth_email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_reservations);
        firebaseAuth = FirebaseAuth.getInstance();

        // Get current user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        userid = currentUser.getUid(); // Get user ID
        auth_email = currentUser.getEmail();// user email id

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("old_reservations");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view); // Ensure this ID matches the one in XML
        parkingSlotList = new ArrayList<>();
        parkingSlotAdapter = new ParkingSlotAdapter(parkingSlotList, Recommended, OldReservationsActivity.this::itemActionSelected);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(parkingSlotAdapter);

        if (AppUtils.isInternetAvailable(this)) {
            loadReservedSlots();
        } else {
            AppUtils.ToastLocal(R.string.no_internet_connection, this);
        }
    }

    /*
        private void loadReservedSlots() {
            databaseReference.orderByChild("status").equalTo(Reserved).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    parkingSlotList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String slotName = snapshot.child("name").getValue(String.class);
                        String slotStatus = snapshot.child("status").getValue(String.class);
                        Double latitude = snapshot.child("latitude").getValue(Double.class);
                        Double longitude = snapshot.child("longitude").getValue(Double.class);
                        Map<String, Integer> slotPrices = (Map<String, Integer>) snapshot.child("prices").getValue();
                        ArrayList<Map<String, Integer>> slotReviews = (ArrayList<Map<String, Integer>>) snapshot.child("reviews").getValue();

                        if (slotName != null && slotStatus != null && latitude != null && longitude != null) {
                            ParkingSlot slot = new ParkingSlot(slotName, slotStatus, latitude, longitude, slotPrices, slotReviews);
                            parkingSlotList.add(slot);
                        }
                    }
                    parkingSlotAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(OldReservationsActivity.this, "Failed to load reserved slots.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    */
    private void loadReservedSlots() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    if (status != null)
                        if (email != null && auth_email.equals(email))
                            if (status.equals(Reserved) || status.equals(RecommendAndReserve)) {
                                String slotName = snapshot.child("name").getValue(String.class);
                                String parkingimage = snapshot.child("parkingimage").getValue(String.class);
                                Integer contact = snapshot.child("contact").getValue(Integer.class);
                                String city = snapshot.child("city").getValue(String.class);
                                String selectedPrice = snapshot.child("selectedPrice").getValue(String.class);
                                String selectedRating = snapshot.child("selectedRating").getValue(String.class);
                                Double latitude = null;
                                Double longitude = null;
                                Map<String, Integer> prices = null;
                                ArrayList<Map<String, Integer>> reviews = null;

                                try {
                                    latitude = snapshot.child("latitude").getValue(Double.class);
                                    longitude = snapshot.child("longitude").getValue(Double.class);
                                } catch (Exception e) {
                                    String latStr = snapshot.child("latitude").getValue(String.class);
                                    String lonStr = snapshot.child("longitude").getValue(String.class);

                                    if (latStr != null && lonStr != null) {
                                        try {
                                            latitude = Double.parseDouble(latStr);
                                            longitude = Double.parseDouble(lonStr);
                                        } catch (NumberFormatException ex) {
                                            Toast.makeText(OldReservationsActivity.this, "Invalid latitude/longitude for slot: " + slotName, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                prices = (Map<String, Integer>) snapshot.child("prices").getValue();
                                reviews = (ArrayList<Map<String, Integer>>) snapshot.child("reviews").getValue();

                                if (slotName != null && status != null && latitude != null && longitude != null && prices != null) {
                                    ParkingSlot parkingSlot = new ParkingSlot(slotName, status, latitude, longitude, prices, reviews);
                                    parkingSlot.setCity(city);
                                    parkingSlot.setContact(contact);
                                    parkingSlot.setValueKey(snapshot.getKey());
                                    parkingSlot.setParkingimage(parkingimage);
                                    parkingSlot.setSelectedPrice(selectedPrice);
                                    parkingSlot.setSelectedRating(selectedRating);
                                    parkingSlotList.add(parkingSlot);

                      /*  LatLng location = new LatLng(latitude, longitude);
                        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(slotName).snippet("Status: " + status));
                        marker.setTag(parkingSlot);*/
                                }
                            }
                }
                if (!parkingSlotList.isEmpty()) {
                    //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BULGARIA_CENTER, 7));
                    parkingSlotAdapter = new ParkingSlotAdapter(parkingSlotList, Reserved, OldReservationsActivity.this::itemActionSelected);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setAdapter(parkingSlotAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OldReservationsActivity.this, "Failed to load parking slots.", Toast.LENGTH_SHORT).show();
            }

        });

    }

    @Override
    public void itemActionSelected(ParkingSlot slot, String action) {
        Log.d("RecommendedParkingActivity", "Item action selected: " + slot);
        handleaction(slot, Recommended);
    }

    private void handleaction(ParkingSlot slot, String status) {
        firebaseAuth = FirebaseAuth.getInstance();

        // Get current user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        userid = currentUser.getUid(); // Get user ID
        auth_email = currentUser.getEmail();// user email id

        slot.setUserid(userid);
        slot.setEmail(auth_email);
        slot.setStatus(status);
        // Update the slot status and selected price in Firebase
        DatabaseReference slotRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots")
                .child(slot.getValueKey());
        slotRef.setValue(slot).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, status + " Slot value submitted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
            }
        });


    }
}
