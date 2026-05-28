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

public class LoginFragment extends Fragment {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtSignUp;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        txtSignUp = view.findViewById(R.id.txtSignUp);

        txtSignUp.setOnClickListener(v -> {
            // Sửa lại ID action cho đúng với khai báo trong nav_graph.xml
            Navigation.findNavController(view).navigate(R.id.action_fragment_login_to_register);
        });

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthRepository.INSTANCE.login(email, password, (success, message) -> {
                if (success) {
                    Toast.makeText(getContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    // Thoát ra và quay về màn hình chính (Home)
                    Navigation.findNavController(view).popBackStack(R.id.homeFragment, false);
                } else {
                    Toast.makeText(getContext(), "Lỗi: " + message, Toast.LENGTH_LONG).show();
                }
                return null;
            });
        });
    }
}
