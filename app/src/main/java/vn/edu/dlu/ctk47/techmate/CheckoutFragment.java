package vn.edu.dlu.ctk47.techmate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.text.DecimalFormat;

public class CheckoutFragment extends Fragment {

    EditText edtName, edtPhone, edtEmail, edtAddress, edtNote;
    TextView txtTotal;
    Button btnSubmit;
    ImageView btnBack;
    LinearLayout layoutBankInfo;

    // Cờ trạng thái: false = Đang điền form, true = Đang đợi bấm xác nhận CK
    boolean isWaitingForPayment = false;

    public CheckoutFragment() {
        super(R.layout.fragment_checkout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupTotal(); // Hiển thị tổng tiền từ giỏ hàng

        // Sự kiện quay lại giỏ hàng
        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        btnSubmit.setOnClickListener(v -> {
            if (!isWaitingForPayment) {
                // =====================================
                // BƯỚC 1: XÁC NHẬN THÔNG TIN ĐẶT HÀNG
                // =====================================
                if (validateForm()) {
                    // Hiển thị khung thông tin ngân hàng
                    layoutBankInfo.setVisibility(View.VISIBLE);

                    // Đổi chữ và màu nút bấm sang Xác nhận chuyển khoản
                    btnSubmit.setText("TÔI ĐÃ CHUYỂN KHOẢN");

                    // Đóng băng các ô nhập liệu không cho sửa nữa
                    disableInputs();

                    isWaitingForPayment = true; // Chuyển sang bước 2
                }
            } else {
                // =====================================
                // BƯỚC 2: XÁC NHẬN THANH TOÁN XONG
                // =====================================
                Toast.makeText(getContext(), "Đơn hàng đã được gửi đi! Nhân viên TechMate sẽ sớm liên hệ với bạn.", Toast.LENGTH_LONG).show();

                // Xóa sạch giỏ hàng sau khi mua thành công
                CartManager.clear();

                // Điều hướng bay thẳng về màn hình Trang Chủ (HomeFragment)
                NavController nav = Navigation.findNavController(v);
                nav.popBackStack(R.id.homeFragment, false);
            }
        });
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
    }

    private void setupTotal() {
        double total = CartManager.getTotal();
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String formattedTotal = formatter.format(total).replace(",", ".") + " đ";
        txtTotal.setText(formattedTotal);
    }

    // Kiểm tra xem khách đã nhập đủ thông tin bắt buộc chưa
    private boolean validateForm() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đủ Tên, SĐT và Địa chỉ nhận hàng!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Làm mờ các ô nhập liệu
    private void disableInputs() {
        edtName.setEnabled(false);
        edtPhone.setEnabled(false);
        edtEmail.setEnabled(false);
        edtAddress.setEnabled(false);
        edtNote.setEnabled(false);
    }
}