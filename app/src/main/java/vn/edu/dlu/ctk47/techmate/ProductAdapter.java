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

import vn.edu.dlu.ctk47.techmate.model.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final List<Product> list;
    private final OnProductListener listener;

    public interface OnProductListener {
        void onClick(Product product);
        void onAddToCart(Product product);
    }

    public ProductAdapter(List<Product> list, OnProductListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = list.get(position);
        holder.txtName.setText(p.getName());
        holder.txtPrice.setText(String.format(Locale.GERMANY, "%,.0f đ", p.getPrice()));

        List<String> images = p.getImageList();
        if (images != null && !images.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(images.get(0))
                    .placeholder(R.drawable.logo)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.logo);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(p));
        holder.btnAddToCart.setOnClickListener(v -> listener.onAddToCart(p));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgProduct;
        final TextView txtName, txtPrice;
        final View btnAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCartMini);
        }
    }
}
