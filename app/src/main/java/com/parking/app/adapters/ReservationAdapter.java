package com.parking.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parking.app.R;
import com.parking.app.models.Reservation;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    private Context context;
    private List<Reservation> reservationList;

    public ReservationAdapter(Context context, List<Reservation> reservationList) {
        this.context = context;
        this.reservationList = reservationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);
        holder.reservationId.setText(reservation.getReservationId());
        holder.userId.setText(reservation.getUserId());
        holder.parkingSlotId.setText(reservation.getParkingSlotId());
        // Set other fields if needed
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView reservationId;
        public TextView userId;
        public TextView parkingSlotId;
        // Add other TextViews if needed

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            reservationId = itemView.findViewById(R.id.reservation_id);
            userId = itemView.findViewById(R.id.user_id);
            parkingSlotId = itemView.findViewById(R.id.parking_slot_id);
            // Initialize other TextViews if needed
        }
    }
}