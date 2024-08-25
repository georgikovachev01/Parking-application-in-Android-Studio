package com.parking.app.adapters;

import static com.parking.app.AppContants.RecommendAndReserve;
import static com.parking.app.AppContants.Recommended;
import static com.parking.app.AppContants.Reserved;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.parking.app.R;
import com.parking.app.adapters.interfaces.OnItemActionSelected;
import com.parking.app.models.ParkingSlot;

import java.util.List;

public class ParkingSlotAdapter extends RecyclerView.Adapter<ParkingSlotAdapter.ParkingSlotViewHolder> {

    private List<ParkingSlot> parkingSlotList;
    String actiontype;
    Context context;
    private FirebaseAuth firebaseAuth;
    public OnItemActionSelected itemActionSelected;
    String userid, email;

    public ParkingSlotAdapter(List<ParkingSlot> parkingSlotList, String actiontype, OnItemActionSelected itemActionSelected) {
        this.actiontype = actiontype;
        this.itemActionSelected = itemActionSelected;
        this.parkingSlotList = parkingSlotList;
    }

    @NonNull
    @Override
    public ParkingSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.parking_slot_item, parent, false);
        this.context = parent.getContext();
        return new ParkingSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingSlotViewHolder holder, int position) {
        ParkingSlot slot = parkingSlotList.get(position);
        holder.slotNameTextView.setText("Name : " + slot.getName());
        holder.txt_prise.setText("Price : " + slot.getSelectedPrice());
        holder.txt_contact.setText("Contact : " + "9739393939");
        holder.txt_city.setText("City : " + slot.getCity());
        holder.txt_rating.setText(String.valueOf(slot.getSelectedRating()));
        int resId = context.getResources().getIdentifier(slot.getParkingimage(), "drawable", context.getPackageName());
        Drawable d = context.getResources().getDrawable(resId);
        holder.slot_image.setImageDrawable(d);
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        userid = currentUser.getUid(); // Get user ID
        email = currentUser.getEmail();// user email id

        if (actiontype.equals(Recommended)) {
            holder.bt_recommend_reserve.setText(R.string.reserve);
        } else if (actiontype.equals(Reserved)) {
            holder.bt_recommend_reserve.setText(R.string.recommend);
        } else {
            holder.bt_recommend_reserve.setVisibility(View.GONE);
        }
        if (slot.getStatus().equals(RecommendAndReserve)) {
            holder.bt_recommend_reserve.setVisibility(View.GONE);
        }
        holder.bt_recommend_reserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemActionSelected.itemActionSelected(slot, slot.getStatus());
            }
        });
    }

    @Override
    public int getItemCount() {
        return parkingSlotList.size();
    }

    public static class ParkingSlotViewHolder extends RecyclerView.ViewHolder {
        TextView slotNameTextView, txt_prise, txt_contact, txt_city, txt_rating;
        AppCompatButton bt_recommend_reserve;
        ImageView slot_image;

        public ParkingSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_city = itemView.findViewById(R.id.txt_city);
            slotNameTextView = itemView.findViewById(R.id.slot_name);
            txt_prise = itemView.findViewById(R.id.txt_prise);
            txt_rating = itemView.findViewById(R.id.txt_rating);
            txt_contact = itemView.findViewById(R.id.txt_contact);
            slot_image = itemView.findViewById(R.id.slot_image);
            bt_recommend_reserve = itemView.findViewById(R.id.bt_recommend_reserve);
        }
    }
}