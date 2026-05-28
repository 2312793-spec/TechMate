package vn.edu.dlu.ctk47.techmate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import vn.edu.dlu.ctk47.techmate.model.CartItem;
import vn.edu.dlu.ctk47.techmate.model.Order;
import vn.edu.dlu.ctk47.techmate.model.OrderItem;

public class CheckoutFragment extends Fragment {

    EditText edtName, edtPhone, edtEmail, edtAddress, edtNote;
    TextView txtTotal;
    Button btnSubmit;
    ImageView btnBack;
    LinearLayout layoutBankInfo;
    RadioGroup rgPaymentMethod;
    RadioButton rbCOD, rbBank;

    boolean isWaitingForPayment = false;

    public CheckoutFragment() {
        super(R.layout.fragment_checkout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        
        double total = CartManager.getTotal();
        txtTotal.setText(String.format("%,.0f đ", total));

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // Xử lý ẩn hiện hướng dẫn chuyển khoản khi chọn phương thức
        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbBank) {
                // Nếu chọn Bank, chỉ hiện hướng dẫn SAU KHI bấm nút đặt hàng (giữ logic cũ)
                // Hoặc có thể hiện luôn nếu muốn. Ở đây tôi giữ logic bấm Xác nhận mới hiện.
            } else {
                layoutBankInfo.setVisibility(View.GONE);
            }
        });

        btnSubmit.setOnClickListener(v -> {
            if (!isWaitingForPayment) {
                if (validateForm()) {
                    saveOrderToFirebase();
                }
            } else {
                // Sau khi đã xác nhận chuyển khoản (chỉ dành cho Bank Transfer)
                completeOrder();
            }
        });
    }

    private void saveOrderToFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();
        
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : CartManager.getCart()) {
            OrderItem oi = new OrderItem();
            oi.setProductId(ci.getProduct().getId());
            oi.setProductName(ci.getProduct().getName());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getProduct().getPrice());
            
            List<String> imgs = ci.getProduct().getImageList();
            if (!imgs.isEmpty()) oi.setImage(imgs.get(0));
            
            orderItems.add(oi);
        }

        String paymentMethod = rbCOD.isChecked() ? "COD" : "Bank Transfer";

        Order order = new Order();
        order.setUserId(uid);
        order.setCustomerName(edtName.getText().toString().trim());
        order.setAddress(edtAddress.getText().toString().trim());
        order.setPhone(edtPhone.getText().toString().trim());
        order.setNote(edtNote.getText().toString().trim());
        order.setItems(orderItems);
        order.setTotalAmount(CartManager.getTotal());
        order.setStatus("Pending");
        order.setPaymentMethod(paymentMethod);
        order.setTimestamp(System.currentTimeMillis());

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    String orderId = documentReference.getId();
                    documentReference.update("id", orderId);
                    
                    if (paymentMethod.equals("Bank Transfer")) {
                        isWaitingForPayment = true;
                        layoutBankInfo.setVisibility(View.VISIBLE);
                        btnSubmit.setText("TÔI ĐÃ CHUYỂN KHOẢN");
                        rgPaymentMethod.setEnabled(false);
                        disableInputs();
                    } else {
                        // Nếu là COD, hoàn tất luôn
                        Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        completeOrder();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void completeOrder() {
        Toast.makeText(getContext(), "Cảm ơn bạn! TechMate sẽ sớm liên hệ.", Toast.LENGTH_LONG).show();
        CartManager.clear();
        Navigation.findNavController(requireView()).popBackStack(R.id.homeFragment, false);
    }

    private void initViews(View view) {
        edtName = view.findViewById(R.id.edtName);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtAddress = view.findViewById(R.id.edtAddress);
        edtNote = view.findViewById(R.id.edtNote);
        txtTotal = view.findViewById(R.id.txtCheckoutTotal);
        btnSubmit = view.findViewById(R.id.btnSubmitCheckout);
        btnBack = view.findViewById(R.id.btnBackCheckout);
        layoutBankInfo = view.findViewById(R.id.layoutBankInfo);
        rgPaymentMethod = view.findViewById(R.id.rgPaymentMethod);
        rbCOD = view.findViewById(R.id.rbCOD);
        rbBank = view.findViewById(R.id.rbBank);
    }

    private boolean validateForm() {
        if (edtName.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtPhone.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập SĐT", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtAddress.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void disableInputs() {
        edtName.setEnabled(false);
        edtPhone.setEnabled(false);
        edtEmail.setEnabled(false);
        edtAddress.setEnabled(false);
        edtNote.setEnabled(false);
        rbCOD.setEnabled(false);
        rbBank.setEnabled(false);
    }
}
