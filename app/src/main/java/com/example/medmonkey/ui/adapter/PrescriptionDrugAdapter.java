package com.example.medmonkey.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medmonkey.R;
import com.example.medmonkey.data.local.entitiy.PrescriptionDrug;

import java.util.List;

/**
 * RecyclerView Adapter for displaying a list of PrescriptionDrug items.
 * Handles item binding and click events.
 */
public class PrescriptionDrugAdapter extends RecyclerView.Adapter<PrescriptionDrugAdapter.ViewHolder> {

    private List<PrescriptionDrug> drugList;
    private OnItemClickListener listener;

    /**
     * Interface to handle item click events.
     */
    public interface OnItemClickListener {
        void onItemClick(PrescriptionDrug drug);
    }

    public PrescriptionDrugAdapter(List<PrescriptionDrug> drugs) {
        this.drugList = drugs;
    }

    /**
     * Sets the click listener for items in the list.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the data list and notifies the adapter.
     */
    public void updateData(List<PrescriptionDrug> newList) {
        this.drugList = newList;
        notifyDataSetChanged();
    }

    /**
     * Inflates the item layout and returns a ViewHolder.
     */
    @NonNull
    @Override
    public PrescriptionDrugAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prescription_drug, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     */
    @Override
    public void onBindViewHolder(@NonNull PrescriptionDrugAdapter.ViewHolder holder, int position) {
        PrescriptionDrug drug = drugList.get(position);
        holder.bind(drug, listener);
    }

    /**
     * Returns the number of items in the data list.
     */
    @Override
    public int getItemCount() {
        return drugList.size();
    }

    /**
     * ViewHolder class for holding views for each item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameText, timeTermText, isActiveText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_drug_name);
            timeTermText = itemView.findViewById(R.id.text_time_term);
            isActiveText = itemView.findViewById(R.id.text_is_active);
        }

        /**
         * Binds a PrescriptionDrug object to the view elements.
         * Also sets the click listener if available.
         */
        public void bind(PrescriptionDrug drug, OnItemClickListener listener) {
            nameText.setText(drug.name);
            timeTermText.setText("Time Term ID: " + drug.timeTermId);

            isActiveText.setVisibility(View.VISIBLE);
            if (drug.isActive) {
                isActiveText.setText("Κατάσταση: Ενεργό");
                isActiveText.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                isActiveText.setText("Κατάσταση: Ανενεργό");
                isActiveText.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(drug);
            });
        }
    }
}
