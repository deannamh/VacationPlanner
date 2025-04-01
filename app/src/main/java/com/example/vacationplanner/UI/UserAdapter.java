package com.example.vacationplanner.UI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vacationplanner.R;
import com.example.vacationplanner.entities.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> mUsers;
    private final Context context;
    private final LayoutInflater mInflater;
    private static final String PREFS_NAME = "SKJTravelPrefs";
    private static final String SELECTED_USER_ID = "selectedUserId";
    private static final String SELECTED_USER_EMAIL = "selectedUserEmail";

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView userEmailTextView;
        private final TextView userIdTextView;
        private final Button selectUserButton;

        private UserViewHolder(View itemView) {
            super(itemView);
            userEmailTextView = itemView.findViewById(R.id.userEmailTextView);
            userIdTextView = itemView.findViewById(R.id.userIdTextView);
            selectUserButton = itemView.findViewById(R.id.selectUserButton);

            selectUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    final User current = mUsers.get(position);

                    // save selected user to SharedPreferences
                    // SharedPreferences = android api that can be used for retrieving key-value pairs from small collection of data)
                    // To write to a shared preferences file, create SharedPreferences.Editor by calling edit() on your SharedPreferences
                    // Pass keys and values you want to write with methods: putInt(), putString(). Then  apply() or commit() to save
                    SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(SELECTED_USER_ID, current.getId());
                    editor.putString(SELECTED_USER_EMAIL, current.getEmail());
                    editor.apply();

                    Toast.makeText(context, "Selected user: " + current.getEmail(), Toast.LENGTH_SHORT).show();

                    // go to VacationList (admin only) screen to see list of vacations for the selected user
                    Intent intent = new Intent(context, VacationList.class);
                    context.startActivity(intent);
                }
            });
        }
    }

    public UserAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(itemView);
    }

    // puts the values in the recyclerview
    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        if (mUsers != null) {
            User current = mUsers.get(position);
            String email = current.getEmail();
            String id = current.getId();
            holder.userEmailTextView.setText(email);
            holder.userIdTextView.setText(id);
        } else {
            holder.userEmailTextView.setText("No user email");
            holder.userIdTextView.setText("No user ID");
        }
    }

    public void setUsers(List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mUsers != null) {
            return mUsers.size();
        } else {
            return 0;
        }
    }
}