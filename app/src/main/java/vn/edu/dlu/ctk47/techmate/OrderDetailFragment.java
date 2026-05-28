package vn.edu.dlu.ctk47.techmate;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.dlu.ctk47.techmate.model.Order;
import vn.edu.dlu.ctk47.techmate.model.OrderItem;

public class OrderDetailFragment extends Fragment {

    private TextView txtStatus, txtDate, txtCustomerName, txtPhone, txtAddress;
    private TextView txtShipperName, txtShipperPhone, txtPaymentMethod, txtTotal;
    private LinearLayout layoutShipper, layoutBottomAction;
    private RecyclerView rvItems;
    private Button btnCancel;
    private View btnBack;
    private String orderId;
    private Order currentOrder;
    private ListenerRegistration orderListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
            if (orderId != null) {
                loadOrderDetail(orderId);
            }
        }

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        
        view.findViewById(R.id.btnCallShipper).setOnClickListener(v -> {
            if (currentOrder != null && currentOrder.getShipperPhone() != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + currentOrder.getShipperPhone()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Không thể gọi điện", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(v -> showCancelConfirmDialog());
    }

    private void initViews(View v) {
        txtStatus = v.findViewById(R.id.txtDetailStatus);
        txtDate = v.findViewById(R.id.txtDetailDate);
        txtCustomerName = v.findViewById(R.id.txtDetailCustomerName);
        txtPhone = v.findViewById(R.id.txtDetailPhone);
        txtAddress = v.findViewById(R.id.txtDetailAddress);
        txtShipperName = v.findViewById(R.id.txtShipperName);
        txtShipperPhone = v.findViewById(R.id.txtShipperPhone);
        txtPaymentMethod = v.findViewById(R.id.txtDetailPaymentMethod);
        txtTotal = v.findViewById(R.id.txtDetailTotal);
        layoutShipper = v.findViewById(R.id.layoutShipperInfo);
        layoutBottomAction = v.findViewById(R.id.layoutBottomAction);
        rvItems = v.findViewById(R.id.rvDetailItems);
        btnCancel = v.findViewById(R.id.btnCancelOrder);
        btnBack = v.findViewById(R.id.btnBackDetail);

        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void loadOrderDetail(String id) {
        orderListener = FirebaseFirestore.getInstance().collection("orders").document(id)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded() || getView() == null) return;
                    if (error != null) {
                        Log.e("OrderDetail", "Listen failed.", error);
                        return;
                    }
                    if (value != null && value.exists()) {
                        try {
                            currentOrder = value.toObject(Order.class);
                            if (currentOrder != null) {
                                displayOrder();
                            }
                        } catch (Exception e) {
                            Log.e("OrderDetail", "Error parsing order", e);
                        }
                    }
                });
    }

    private void displayOrder() {
        if (getContext() == null) return;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        
        String status = currentOrder.getStatus() != null ? currentOrder.getStatus() : "Pending";
        txtStatus.setText(getVietnameseStatus(status).toUpperCase());
        updateStatusHeaderColor(status);

        if (currentOrder.getTimestamp() != null) {
            txtDate.setText("Ngày đặt: " + sdf.format(new Date(currentOrder.getTimestamp())));
        }

        txtCustomerName.setText(currentOrder.getCustomerName());
        txtPhone.setText(currentOrder.getPhone());
        txtAddress.setText(currentOrder.getAddress());
        txtPaymentMethod.setText(currentOrder.getPaymentMethod());
        txtTotal.setText(String.format(Locale.GERMANY, "%,.0f đ", currentOrder.getTotalAmount()));

        if (currentOrder.getShipperId() != null) {
            layoutShipper.setVisibility(View.VISIBLE);
            txtShipperName.setText(currentOrder.getShipperName() != null ? currentOrder.getShipperName() : "Đang cập nhật...");
            txtShipperPhone.setText(currentOrder.getShipperPhone() != null ? "SĐT: " + currentOrder.getShipperPhone() : "SĐT: N/A");
        } else {
            layoutShipper.setVisibility(View.GONE);
        }

        // Hiện nút hủy nếu đơn hàng đang ở trạng thái Pending
        layoutBottomAction.setVisibility("Pending".equalsIgnoreCase(status) ? View.VISIBLE : View.GONE);

        // Cập nhật danh sách sản phẩm
        List<OrderItem> items = currentOrder.getItems() != null ? currentOrder.getItems() : new ArrayList<>();
        rvItems.setAdapter(new DetailItemAdapter(items));
    }

    private String getVietnameseStatus(String status) {
        if (status == null) return "Chờ xác nhận";
        switch (status) {
            case "Pending": return "Chờ xác nhận";
            case "Confirmed": return "Đã xác nhận";
            case "Shipping": return "Đang giao hàng";
            case "Delivered": return "Đã giao hàng";
            case "Cancelled": return "Đã hủy";
            default: return status;
        }
    }

    private void updateStatusHeaderColor(String status) {
        View v = getView();
        if (v == null) return;
        View header = v.findViewById(R.id.layoutStatusHeader);
        if (header == null) return;
        
        int color;
        switch (status) {
            case "Confirmed": color = Color.parseColor("#2196F3"); break;
            case "Shipping": color = Color.parseColor("#9C27B0"); break;
            case "Delivered": color = Color.parseColor("#4CAF50"); break;
            case "Cancelled": color = Color.parseColor("#F44336"); break;
            default: color = Color.parseColor("#FFA500");
        }
        header.setBackgroundColor(color);
    }

    private void showCancelConfirmDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    FirebaseFirestore.getInstance().collection("orders").document(orderId)
                            .update("status", "Cancelled")
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) {
                                    Toast.makeText(getContext(), "Đã hủy đơn hàng thành công", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) {
                                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Quay lại", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        if (orderListener != null) {
            orderListener.remove();
        }
        super.onDestroyView();
    }

    private static class DetailItemAdapter extends RecyclerView.Adapter<DetailItemAdapter.VH> {
        private final List<OrderItem> items;
        DetailItemAdapter(List<OrderItem> items) { this.items = items; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail_product, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            OrderItem item = items.get(position);
            h.txtName.setText(item.getProductName());
            h.txtQty.setText("x" + item.getQuantity());
            h.txtPrice.setText(String.format(Locale.GERMANY, "%,.0f đ", item.getPrice()));
            
            if (h.itemView.getContext() != null) {
                Glide.with(h.itemView.getContext())
                        .load(item.getImage())
                        .placeholder(R.drawable.logo)
                        .into(h.img);
            }
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        static class VH extends RecyclerView.ViewHolder {
            ImageView img;
            TextView txtName, txtQty, txtPrice;
            VH(View v) {
                super(v);
                img = v.findViewById(R.id.imgOrderDetailProduct);
                txtName = v.findViewById(R.id.txtOrderDetailProductName);
                txtQty = v.findViewById(R.id.txtOrderDetailQty);
                txtPrice = v.findViewById(R.id.txtOrderDetailProductPrice);
            }
        }
    }
}
