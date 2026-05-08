package vn.edu.dlu.ctk47.techmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final List<CartItem> list;
    private final OnCartChange listener;

    // Constructor
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

        h.txtName.setText(item.product.name);
        h.txtPrice.setText("$" + item.product.price);
        h.txtQty.setText(String.valueOf(item.quantity));

        // ➕ Tăng số lượng
        h.btnPlus.setOnClickListener(v -> {
            // Sử dụng getAdapterPosition() để tương thích tốt hơn
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                list.get(pos).quantity++;
                notifyItemChanged(pos);
                notifyTotal();
            }
        });

        // ➖ Giảm số lượng
        h.btnMinus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                if (list.get(pos).quantity > 1) {
                    list.get(pos).quantity--;
                    notifyItemChanged(pos);
                    notifyTotal();
                }
            }
        });

        // ❌ Xóa sản phẩm khỏi giỏ hàng
        h.btnDelete.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                // 1. Xóa trong CartManager
                CartManager.remove(pos);

                // 2. Thông báo xóa item tại vị trí đó
                notifyItemRemoved(pos);

                // 3. Cập nhật lại dải vị trí để tránh crash IndexOutOfBoundsException
                notifyItemRangeChanged(pos, list.size());

                // 4. Cập nhật tổng tiền ở UI
                notifyTotal();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // Fixed Warning: Added 'public' to match Adapter visibility scope
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
