package vn.edu.dlu.ctk47.techmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

import vn.edu.dlu.ctk47.techmate.model.CartItem;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final List<CartItem> list;
    private final OnCartChange listener;

    public CartAdapter(List<CartItem> list, OnCartChange listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CartItem item = list.get(position);

        if (item != null && item.getProduct() != null) {
            h.txtName.setText(item.getProduct().getName());
            h.txtPrice.setText(String.format(Locale.GERMANY, "%,.0f đ", item.getProduct().getPrice()));
            h.txtQty.setText(String.valueOf(item.getQuantity()));

            // SỬA LỖI TẠI ĐÂY: Dùng getImageList()
            List<String> images = item.getProduct().getImageList();
            if (images != null && !images.isEmpty()) {
                Glide.with(h.itemView.getContext())
                        .load(images.get(0))
                        .placeholder(R.drawable.logo)
                        .into(h.img);
            } else {
                h.img.setImageResource(R.drawable.logo);
            }
        }

        h.btnPlus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            CartItem currentItem = list.get(pos);
            if (currentItem == null) return;
            currentItem.setQuantity(currentItem.getQuantity() + 1);
            notifyItemChanged(pos);
            notifyTotal();
        });

        h.btnMinus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            CartItem currentItem = list.get(pos);
            if (currentItem == null) return;
            if (currentItem.getQuantity() > 1) {
                currentItem.setQuantity(currentItem.getQuantity() - 1);
                notifyItemChanged(pos);
                notifyTotal();
            }
        });

        h.btnDelete.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            CartManager.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, list.size());
            notifyTotal();
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPrice, txtQty;
        ImageView img, btnDelete;
        TextView btnPlus, btnMinus;

        public ViewHolder(@NonNull View v) {
            super(v);
            txtName = v.findViewById(R.id.txtName);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtQty = v.findViewById(R.id.txtQty);
            img = v.findViewById(R.id.imgProduct);
            btnDelete = v.findViewById(R.id.btnDelete);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
        }
    }

    public interface OnCartChange {
        void onChange();
    }

    private void notifyTotal() {
        if (listener != null) {
            listener.onChange();
        }
    }
}
