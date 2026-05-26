package vn.edu.dlu.ctk47.techmate;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.dlu.ctk47.techmate.model.Brand;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.ViewHolder> {

    private final List<Brand> list;
    private final OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(Brand brand);
    }

    public BrandAdapter(List<Brand> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void clearSelection() {
        int oldPos = selectedPosition;
        selectedPosition = -1;
        if (oldPos != -1) {
            notifyItemChanged(oldPos);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Brand brand = list.get(position);
        holder.txt.setText(brand.getName());

        if (selectedPosition == position) {
            holder.txt.setBackgroundResource(R.drawable.bg_chip_selected);
            holder.txt.setTextColor(Color.WHITE);
        } else {
            holder.txt.setBackgroundResource(R.drawable.bg_chip);
            holder.txt.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            if (selectedPosition == currentPos) {
                // Nhấn lần 2 -> Bỏ chọn (Tắt màu)
                selectedPosition = -1;
                notifyItemChanged(currentPos);
                if (listener != null) listener.onItemClick(null);
            } else {
                // Chọn mới
                int oldPos = selectedPosition;
                selectedPosition = currentPos;
                if (oldPos != -1) notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
                if (listener != null) listener.onItemClick(brand);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt = (TextView) itemView;
        }
    }
}
