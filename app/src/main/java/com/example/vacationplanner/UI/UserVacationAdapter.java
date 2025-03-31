package com.example.vacationplanner.UI;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vacationplanner.R;
import com.example.vacationplanner.entities.Vacation;

import java.util.ArrayList;
import java.util.List;

public class UserVacationAdapter extends RecyclerView.Adapter<UserVacationAdapter.VacationViewHolder> {
    private List<Vacation> mVacations;
    private final Context context;
    private final LayoutInflater mInflater;

    public UserVacationAdapter(Context context){
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mVacations = new ArrayList<>();
    }

    public class VacationViewHolder extends RecyclerView.ViewHolder {
        private final TextView vacationItemView;

        public VacationViewHolder(@NonNull View itemView) {
            super(itemView);
            vacationItemView = itemView.findViewById(R.id.textViewUserVacationListItem);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    final Vacation current = mVacations.get(position);

                    Log.d("UserVacationAdapter", "Clicked vacation with ID: " + current.getVacationID());

                    Intent intent = new Intent(context, UserVacationDetails.class); // click item to see UserVacationDetails
                    intent.putExtra("id", current.getVacationID());
                    intent.putExtra("title", current.getTitle());
                    intent.putExtra("hotel", current.getHotelName());
                    intent.putExtra("startdate", current.getStartDate());
                    intent.putExtra("enddate", current.getEndDate());
                    context.startActivity(intent);
                }
            });
        }
    }

    @NonNull
    @Override
    public UserVacationAdapter.VacationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.user_vacation_list_item, parent, false); //inflates the user_vacation_list_item and returns the VacationViewHolder
        return new VacationViewHolder(itemView);
    }

    // onBindViewHolder -> puts what we will display on the RecyclerView
    @Override
    public void onBindViewHolder(@NonNull UserVacationAdapter.VacationViewHolder holder, int position) {
        if (mVacations != null && position < mVacations.size()){
            Vacation current = mVacations.get(position);
            String title = current.getTitle();
            holder.vacationItemView.setText(title);
        }
        else {
            holder.vacationItemView.setText("No vacation title");
        }
    }

    @Override
    public int getItemCount() {
        if (mVacations != null) {
            return mVacations.size();
        } else {
            return 0;
        }
    }

    public void setVacations(List<Vacation> vacations){
        if(vacations != null) {
            for(Vacation v : vacations) {
                Log.d("UserVacationAdapter", "Loaded vacation: " + v.getTitle() + ", ID: " + v.getVacationID());
            }
            mVacations = vacations;
            notifyDataSetChanged();
        }
    }
}