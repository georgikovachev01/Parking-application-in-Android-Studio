package com.parking.app.models;

import com.google.firebase.database.PropertyName;

public class Feedback {
    @PropertyName("Comments")
    private String feedback;
    @PropertyName("Date")
    private String date;
    @PropertyName("UserId")
    private String userId;
    @PropertyName("Rating")
    private int Rating;

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    @PropertyName("email")
    private String emailId;

    public Feedback() {
        // Default constructor required for calls to DataSnapshot.getValue(Feedback.class)
    }

    public Feedback(String feedback, String date, String userId, String emailId) {
        this.feedback = feedback;
        this.date = date;
        this.userId = userId;
        this.emailId = emailId;
    }

    // Getters and setters (optional)
    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
