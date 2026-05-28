package vn.edu.dlu.ctk47.techmate;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.dlu.ctk47.techmate.firebase.AuthRepository;
import vn.edu.dlu.ctk47.techmate.model.Order;

public class MyOrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty, layoutNotLoggedIn;
    private ProgressBar progressOrders;
    private SwipeRefreshLayout swipeRefresh;
    private ImageView btnBack;
    private Button btnGoLogin, btnGoShopping;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        layoutNotLoggedIn = view.findViewById(R.id.layoutNotLoggedIn);
        progressOrders = view.findViewById(R.id.progressOrders);
        swipeRefresh = view.findViewById(R.id.swipeRefreshOrders);
        btnBack = view.findViewById(R.id.btnBack);
        btnGoLogin = view.findViewById(R.id.btnGoLogin);
        btnGoShopping = view.findViewById(R.id.btnGoShopping);

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        btnGoShopping.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.homeFragment));
        btnGoLogin.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.loginFragment));

        swipeRefresh.setOnRefreshListener(() -> {
            FirebaseUser user = AuthRepository.INSTANCE.getCurrentUser();
            if (user != null) {
                loadOrders(user.getUid());
            } else {
                swipeRefresh.setRefreshing(false);
            }
        });

        checkUserAndLoadData();
    }

    private void checkUserAndLoadData() {
        FirebaseUser currentUser = AuthRepository.INSTANCE.getCurrentUser();
        if (currentUser == null) {
            showState(State.NOT_LOGGED_IN);
        } else {
            loadOrders(currentUser.getUid());
        }
    }

    private void loadOrders(String userId) {
        if (!swipeRefresh.isRefreshing()) {
            showState(State.LOADING);
        }

        FirebaseFirestore.getInstance()
                .collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;
                    swipeRefresh.setRefreshing(false);

                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        try {
                            Order order = doc.toObject(Order.class);
                            orders.add(order);
                        } catch (Exception e) {
                            Log.e("MyOrders", "Lỗi convert dữ liệu: " + e.getMessage());
                        }
                    }

                    if (orders.isEmpty()) {
                        showState(State.EMPTY);
                    } else {
                        showState(State.LOADED);
                        setupRecyclerView(orders);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    swipeRefresh.setRefreshing(false);
                    showState(State.EMPTY);
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("MyOrders", "Firestore Error: ", e);
                });
    }

    private void setupRecyclerView(List<Order> orders) {
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(new OrderAdapter(orders));
    }

    private void showState(State state) {
        rvOrders.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        layoutNotLoggedIn.setVisibility(View.GONE);
        progressOrders.setVisibility(View.GONE);
        switch (state) {
            case LOADING:       progressOrders.setVisibility(View.VISIBLE); break;
            case LOADED:        rvOrders.setVisibility(View.VISIBLE); break;
            case EMPTY:         layoutEmpty.setVisibility(View.VISIBLE); break;
            case NOT_LOGGED_IN: layoutNotLoggedIn.setVisibility(View.VISIBLE); break;
        }
    }

    private enum State { LOADING, LOADED, EMPTY, NOT_LOGGED_IN }

    private static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {
        private final List<Order> list;
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        OrderAdapter(List<Order> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Order order = list.get(position);

            String displayId = order.getId();
            if (displayId != null && displayId.length() > 8) {
                displayId = displayId.substring(0, 8).toUpperCase();
            }
            h.txtOrderId.setText("Đơn hàng #" + (displayId != null ? displayId : "---"));

            String status = order.getStatus();
            h.txtOrderStatus.setText(getVietnameseStatus(status));
            updateStatusUI(h.txtOrderStatus, status);

            h.txtOrderAddress.setText(order.getAddress());
            h.txtOrderTotal.setText(String.format(Locale.GERMANY, "%,.0f đ", order.getTotalAmount()));

            if (order.getTimestamp() != null) {
                h.txtOrderDate.setText(sdf.format(new Date(order.getTimestamp())));
            }

            h.itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("orderId", order.getId());
                Navigation.findNavController(v).navigate(R.id.action_myOrdersFragment_to_orderDetailFragment, bundle);
            });
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

        private void updateStatusUI(TextView tv, String status) {
            if (status == null) return;
            switch (status) {
                case "Pending": tv.setBackgroundColor(Color.parseColor("#FFA500")); break;
                case "Confirmed": tv.setBackgroundColor(Color.parseColor("#2196F3")); break;
                case "Shipping": tv.setBackgroundColor(Color.parseColor("#9C27B0")); break;
                case "Delivered": tv.setBackgroundColor(Color.parseColor("#4CAF50")); break;
                case "Cancelled": tv.setBackgroundColor(Color.parseColor("#F44336")); break;
                default: tv.setBackgroundColor(Color.GRAY);
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView txtOrderId, txtOrderStatus, txtOrderAddress, txtOrderTotal, txtOrderDate;
            VH(@NonNull View v) {
                super(v);
                txtOrderId = v.findViewById(R.id.txtOrderId);
                txtOrderStatus = v.findViewById(R.id.txtOrderStatus);
                txtOrderAddress = v.findViewById(R.id.txtOrderAddress);
                txtOrderTotal = v.findViewById(R.id.txtOrderTotal);
                txtOrderDate = v.findViewById(R.id.txtOrderDate);
            }
        }
    }
}
