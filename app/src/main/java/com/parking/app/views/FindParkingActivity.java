package com.parking.app.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parking.app.AppContants;
import com.parking.app.R;
import com.parking.app.adapters.ParkingSlotAdapter;
import com.parking.app.adapters.interfaces.OnItemActionSelected;
import com.parking.app.models.ParkingSlot;
import com.parking.app.utils.AppUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FindParkingActivity extends AppCompatActivity implements OnMapReadyCallback, OnItemActionSelected {

    private RecyclerView recyclerView;
    private ParkingSlotAdapter adapter;
    private List<ParkingSlot> parkingSlotList;
    private GoogleMap mMap;
    private ConstraintLayout constraintLayout;
    private static final LatLng BULGARIA_CENTER = new LatLng(42.7339, 25.4858);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_parking);

        FirebaseApp.initializeApp(this);

        constraintLayout = findViewById(R.id.container_layout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        parkingSlotList = new ArrayList<>();
        adapter = new ParkingSlotAdapter(parkingSlotList, "", this);
        recyclerView.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        EditText searchEditText = findViewById(R.id.searchEditText);
        Button searchButton = findViewById(R.id.searchButton);
        Button stopSearchButton = findViewById(R.id.stopSearchButton); // Нов бутон за спиране на търсенето

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = searchEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(city)) {
                    searchParkingSlotsByCity(city);
                } else {
                    Toast.makeText(FindParkingActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText(""); // Изчистване на полето за търсене
                loadParkingSlots(); // Зареждане на всички паркинги отново
            }
        });

        if (AppUtils.isInternetAvailable(this)) {
            loadParkingSlots(); // Първоначално зареждане на всички паркинги
        } else {
            AppUtils.ToastLocal(R.string.no_internet_connection, this);
        }
    }

    private void loadParkingSlots() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parkingSlotList.clear();
                mMap.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String valueKey = snapshot.getKey();
                    String City = snapshot.child("city").getValue(String.class);
                    String parkingimage = snapshot.child("parkingimage").getValue(String.class);
                    Integer contact = snapshot.child("contact").getValue(Integer.class);
                    String slotName = snapshot.child("name").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    ArrayList<Map<String, Integer>> reviews = (ArrayList<Map<String, Integer>>) snapshot.child("reviews").getValue();
                    Double latitude = null;
                    Double longitude = null;
                    Map<String, Integer> prices = null;

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
                                Toast.makeText(FindParkingActivity.this, "Invalid latitude/longitude for slot: " + slotName, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    prices = (Map<String, Integer>) snapshot.child("prices").getValue();

                    if (slotName != null && status != null && latitude != null && longitude != null && prices != null) {
                        ParkingSlot parkingSlot = new ParkingSlot(slotName, status, latitude, longitude, prices, reviews);
                        parkingSlot.setCity(City);
                        parkingSlot.setValueKey(valueKey);
                        parkingSlot.setContact(contact);
                        parkingSlot.setParkingimage(parkingimage);
                        parkingSlotList.add(parkingSlot);
                        LatLng location = new LatLng(latitude, longitude);
                        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(slotName));
                        marker.setTag(parkingSlot);
                    }

                }

                adapter.notifyDataSetChanged();
                if (!parkingSlotList.isEmpty()) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BULGARIA_CENTER, 7));
                }
                Log.e("count ->", "" + parkingSlotList.size());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FindParkingActivity.this, "Failed to load parking slots.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchParkingSlotsByCity(String city) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("ParkingSlots");

        databaseRef.orderByChild("city").equalTo(city).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parkingSlotList.clear();
                mMap.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String valueKey = snapshot.getKey();
                    String City = snapshot.child("city").getValue(String.class);
                    String parkingimage = snapshot.child("parkingimage").getValue(String.class);
                    Integer contact = snapshot.child("contact").getValue(Integer.class);
                    String slotName = snapshot.child("name").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    ArrayList<Map<String, Integer>> reviews = (ArrayList<Map<String, Integer>>) snapshot.child("reviews").getValue();
                    Double latitude = null;
                    Double longitude = null;
                    Map<String, Integer> prices = null;

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
                                Toast.makeText(FindParkingActivity.this, "Invalid latitude/longitude for slot: " + slotName, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    prices = (Map<String, Integer>) snapshot.child("prices").getValue();

                    if (slotName != null && status != null && latitude != null && longitude != null && prices != null) {
                        ParkingSlot parkingSlot = new ParkingSlot(slotName, status, latitude, longitude, prices, reviews);
                        parkingSlot.setCity(City);
                        parkingSlot.setValueKey(valueKey);
                        parkingSlot.setContact(contact);
                        parkingSlot.setParkingimage(parkingimage);
                        parkingSlotList.add(parkingSlot);
                        LatLng location = new LatLng(latitude, longitude);
                        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(slotName));
                        marker.setTag(parkingSlot);
                    }

                }

                adapter.notifyDataSetChanged();
                if (!parkingSlotList.isEmpty()) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BULGARIA_CENTER, 7));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FindParkingActivity.this, "Failed to load parking slots.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Слушател за кликове върху маркерите, за да показваме статус
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ParkingSlot slot = (ParkingSlot) marker.getTag();
                if (slot != null) {
                    // Показваме статус "Reserved", ако паркингът е резервиран, иначе "Available"
                    String statusText = slot.getStatus().equals("Reserved") ? "Reserved" : "Available";
                    marker.setSnippet(statusText + " - Click again for details");
                    marker.showInfoWindow();
                    return true; // Връщаме true, за да спрем последващи действия на събитието
                }
                return false;
            }
        });

        // Слушател за кликове върху InfoWindow за отваряне на детайли за паркинга
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                ParkingSlot slot = (ParkingSlot) marker.getTag();
                if (slot != null) {
                    Intent intent = new Intent(FindParkingActivity.this, FindParkingDetailsActivity.class);
                    intent.putExtra("obj", slot);
                    intent.putExtra(AppContants.SlotMapPrices, (Serializable) slot.getPrices());
                    intent.putExtra(AppContants.SlotReviews, slot.getReviews());
                    intent.putExtra(AppContants.parkingImage, slot.getParkingimage().toString());
                    startActivity(intent);
                }
            }
        });

        loadParkingSlots();
    }

    @Override
    public void itemActionSelected(ParkingSlot slot, String action) {

    }
}
