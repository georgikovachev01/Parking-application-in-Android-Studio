package com.parking.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Map;

public class ParkingSlot implements Parcelable {
    private String valueKey;
    private String City;
    private String Name;
    private String Status;
    private double Latitude;
    private double Longitude;
    private String selectedPrice;
    private String selectedRating;
    private int contact;
    private String userid;
    private String email;
    private String parkingimage;
    private Map<String, Integer> Prices;
    private ArrayList<Map<String, Integer>> Reviews;
    private String startTime;
    private int availableSpots; // Поле за свободни места

    // Конструкторът за Parcelable
    protected ParkingSlot(Parcel in) {
        valueKey = in.readString();
        City = in.readString();
        Name = in.readString();
        Status = in.readString();
        Latitude = in.readDouble();
        Longitude = in.readDouble();
        selectedPrice = in.readString();
        selectedRating = in.readString();
        contact = in.readInt();
        userid = in.readString();
        email = in.readString();
        parkingimage = in.readString();
        startTime = in.readString();
        availableSpots = in.readInt(); // Добавяне на полето availableSpots
    }

    public static final Creator<ParkingSlot> CREATOR = new Creator<ParkingSlot>() {
        @Override
        public ParkingSlot createFromParcel(Parcel in) {
            return new ParkingSlot(in);
        }

        @Override
        public ParkingSlot[] newArray(int size) {
            return new ParkingSlot[size];
        }
    };

    // Getter и Setter методи за всички полета
    public String getValueKey() {
        return valueKey;
    }

    public void setValueKey(String valueKey) {
        this.valueKey = valueKey;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public String getSelectedPrice() {
        return selectedPrice;
    }

    public void setSelectedPrice(String selectedPrice) {
        this.selectedPrice = selectedPrice;
    }

    public String getSelectedRating() {
        return selectedRating;
    }

    public void setSelectedRating(String selectedRating) {
        this.selectedRating = selectedRating;
    }

    public int getContact() {
        return contact;
    }

    public void setContact(int contact) {
        this.contact = contact;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getParkingimage() {
        return parkingimage;
    }

    public void setParkingimage(String parkingimage) {
        this.parkingimage = parkingimage;
    }

    public Map<String, Integer> getPrices() {
        return Prices;
    }

    public void setPrices(Map<String, Integer> prices) {
        Prices = prices;
    }

    public ArrayList<Map<String, Integer>> getReviews() {
        return Reviews;
    }

    public void setReviews(ArrayList<Map<String, Integer>> reviews) {
        Reviews = reviews;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getAvailableSpots() {
        return availableSpots;
    }

    public void setAvailableSpots(int availableSpots) {
        this.availableSpots = availableSpots;
    }

    // Основният конструктор
    public ParkingSlot(String name, String status, double latitude, double longitude, Map<String, Integer> prices, ArrayList<Map<String, Integer>> reviews) {
        this.Name = name;
        this.Status = status;
        this.Latitude = latitude;
        this.Longitude = longitude;
        this.Prices = prices;
        this.Reviews = reviews;
        this.selectedPrice = null;
        this.startTime = null;
        this.availableSpots = generateRandomSpots(); // Генериране на случайни свободни места
    }

    private int generateRandomSpots() {
        return (int) (Math.random() * 10) + 1; // Случайно число между 1 и 10
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(valueKey);
        parcel.writeString(City);
        parcel.writeString(Name);
        parcel.writeString(Status);
        parcel.writeDouble(Latitude);
        parcel.writeDouble(Longitude);
        parcel.writeString(selectedPrice);
        parcel.writeString(selectedRating);
        parcel.writeInt(contact);
        parcel.writeString(userid);
        parcel.writeString(email);
        parcel.writeString(parkingimage);
        parcel.writeString(startTime);
        parcel.writeInt(availableSpots); // Добавяне на полето availableSpots
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
