package com.example.vacationplanner.UI;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vacationplanner.R;
import com.example.vacationplanner.entities.Excursion;

import java.util.List;

public class ExcursionAdapter extends RecyclerView.Adapter<ExcursionAdapter.ExcursionViewHolder> {
    private List<Excursion> mExcursions;
    private final Context context;
    private final LayoutInflater mInflater;

    class ExcursionViewHolder extends RecyclerView.ViewHolder {
        private final TextView excursionItemView;
        private final TextView excursionItemView2;

        private ExcursionViewHolder(View itemView) {
            super(itemView);
            excursionItemView = itemView.findViewById(R.id.textView5);
            excursionItemView2 = itemView.findViewById(R.id.textView6);

            // takes user to ExcursionDetails when an excursion is clicked on
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    final Excursion current = mExcursions.get(position);
                    Intent intent = new Intent(context, ExcursionDetails.class);
                    intent.putExtra("id", current.getExcursionID());
                    intent.putExtra("title", current.getExcursionTitle());
                    intent.putExtra("date", current.getExcursionDate());
                    intent.putExtra("vacationID", current.getVacationID());
                    context.startActivity(intent);
                }
            });
        }
    }

    public ExcursionAdapter(Context context){
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }


    @Override
    public ExcursionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.excursion_list_item, parent, false);
        return new ExcursionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExcursionAdapter.ExcursionViewHolder holder, int position) {
        if (mExcursions != null){
            Excursion current = mExcursions.get(position);
            String title = current.getExcursionTitle();
            String date = current.getExcursionDate();
            int vacationID = current.getVacationID();
            holder.excursionItemView.setText(title);
            //holder.excursionItemView2.setText(Integer.toString(vacationID));
            holder.excursionItemView2.setText(date);
        }
        else {
            holder.excursionItemView.setText("No excursion title");
            holder.excursionItemView2.setText("No ecursion date");
        }
    }
    public void setExcursions(List<Excursion> excursions){
        mExcursions = excursions;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mExcursions != null){
            return mExcursions.size();
        }
        else {
            return 0;
        }
    }
}
