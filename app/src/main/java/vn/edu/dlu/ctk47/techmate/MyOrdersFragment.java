package vn.edu.dlu.ctk47.techmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
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
    private ImageView btnBack;
    private Button btnGoLogin;

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
        btnBack = view.findViewById(R.id.btnBack);
        btnGoLogin = view.findViewById(R.id.btnGoLogin);

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        btnGoLogin.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_myOrdersFragment_to_loginFragment));

        FirebaseUser currentUser = AuthRepository.INSTANCE.getCurrentUser();
        if (currentUser == null) {
            showState(State.NOT_LOGGED_IN);
        } else {
            loadOrders(currentUser.getUid());
        }
    }

    private void loadOrders(String userId) {
        showState(State.LOADING);
        FirebaseFirestore.getInstance()
                .collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;
                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Order order = doc.toObject(Order.class);
                        order.setId(doc.getId());
                        orders.add(order);
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
                    showState(State.EMPTY);
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

    // ---- Adapter nội bộ ----
    private static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {
        private final List<Order> list;
        private final DecimalFormat fmt = new DecimalFormat("###,###,###");
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

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
            h.txtOrderId.setText("Đơn #" + (order.getId() != null ? order.getId().substring(0, Math.min(8, order.getId().length())) : "N/A"));
            h.txtOrderStatus.setText(order.getStatus() != null ? order.getStatus() : "Pending");
            h.txtOrderAddress.setText(order.getShippingAddress() != null ? order.getShippingAddress() : "Không có địa chỉ");
            h.txtOrderTotal.setText(fmt.format(order.getTotalPrice()).replace(",", ".") + " đ");
            if (order.getTimestamp() != null) {
                h.txtOrderDate.setText(sdf.format(new Date(order.getTimestamp())));
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
