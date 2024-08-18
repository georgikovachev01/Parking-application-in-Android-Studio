package com.parking.app.models;

public class Reservation {
    private String reservationId;
    private String userId;
    private String parkingSlotId;
    // Add other fields as per your database structure

    public Reservation() {
        // Default constructor required for Firebase
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getParkingSlotId() {
        return parkingSlotId;
    }

    public void setParkingSlotId(String parkingSlotId) {
        this.parkingSlotId = parkingSlotId;
    }
    // Add getters and setters for other fields
}
