package vn.edu.dlu.ctk47.techmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import vn.edu.dlu.ctk47.techmate.firebase.AuthRepository;
import vn.edu.dlu.ctk47.techmate.model.User;

public class RegisterFragment extends Fragment {

    private EditText edtFullName, edtEmail, edtPassword;
    private Button btnRegister;
    private TextView txtBackToLogin;

    public RegisterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtFullName = view.findViewById(R.id.edtFullName);
        edtEmail = view.findViewById(R.id.edtRegisterEmail);
        edtPassword = view.findViewById(R.id.edtRegisterPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        txtBackToLogin = view.findViewById(R.id.txtBackToLogin);

        // Quay lại màn hình Đăng nhập
        txtBackToLogin.setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        // Xử lý Đăng ký
        btnRegister.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩu phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            User newUser = new User();
            newUser.setName(fullName);
            newUser.setEmail(email);
            newUser.setRole("user"); // Mặc định role là user

            AuthRepository.INSTANCE.register(newUser, password, (success, message) -> {
                if (success) {
                    Toast.makeText(getContext(), "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    // Đăng ký xong tự động quay về HomeFragment để cập nhật giao diện chính
                    Navigation.findNavController(view).popBackStack(R.id.homeFragment, false);
                } else {
                    Toast.makeText(getContext(), "Lỗi đăng ký: " + message, Toast.LENGTH_LONG).show();
                }
                return null;
            });
        });
    }
}
