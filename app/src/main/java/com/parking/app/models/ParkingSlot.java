package com.parking.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.Map;

public class ParkingSlot implements Parcelable {
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

    public String getValueKey() {
        return valueKey;
    }

    public void setValueKey(String valueKey) {
        this.valueKey = valueKey;
    }

    String valueKey;

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
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

    private String userid;
    private String email;
    @PropertyName("City")
    private String City;
    @PropertyName("Name")
    private String Name;
    private String Status;
    @PropertyName("Latitude")
    private double Latitude;
    @PropertyName("Longitude")
    private double Longitude;

    public String getParkingimage() {
        return parkingimage;
    }

    public void setParkingimage(String parkingimage) {
        this.parkingimage = parkingimage;
    }

    private String parkingimage;

    public int getContact() {
        return contact;
    }

    public void setContact(int contact) {
        this.contact = contact;
    }

    private int contact;

    public void setPrices(Map<String, Integer> prices) {
        this.Prices = prices;
    }

    @PropertyName("Prices")
    private Map<String, Integer> Prices;

    public ArrayList<Map<String, Integer>> getReviews() {
        return Reviews;
    }

    public void setReviews(ArrayList<Map<String, Integer>> reviews) {
        this.Reviews = reviews;
    }

    @PropertyName("Reviews")
    private ArrayList<Map<String, Integer>> Reviews;
    private String selectedPrice;

    public String getSelectedRating() {
        return selectedRating;
    }

    public void setSelectedRating(String selectedRating) {
        this.selectedRating = selectedRating;
    }

    private String selectedRating;

    public void setStatus(String status) {
        Status = status;
    }

    public ParkingSlot(String name, String status, double latitude, double longitude, Map<String, Integer> prices, ArrayList<Map<String, Integer>> reviews) {
        this.Name = name;
        this.Status = status;
        this.Latitude = latitude;
        this.Longitude = longitude;
        this.Prices = prices;
        this.Reviews = reviews;
        this.selectedPrice = null;
    }

    public String getName() {
        return Name;
    }

    public String getStatus() {
        return Status;
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public Map<String, Integer> getPrices() {
        return Prices;
    }

    // Getter and setter for selectedPrice
    public String getSelectedPrice() {
        return selectedPrice;
    }

    public void setSelectedPrice(String selectedPrice) {
        this.selectedPrice = selectedPrice;
    }

    @Override
    public int describeContents() {
        return 0;
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
    }
}
