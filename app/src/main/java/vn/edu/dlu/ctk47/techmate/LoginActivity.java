package vn.edu.dlu.ctk47.techmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import vn.edu.dlu.ctk47.techmate.firebase.AuthRepository;
import vn.edu.dlu.ctk47.techmate.model.User;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnSubmit;
    TextView txtTitle, txtSwitchMode, txtLabelUser;

    String currentMode = "LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_login);

        // Đã sửa ID từ edtPhone thành edtEmail để khớp với activity_login.xml
        edtEmail = findViewById(R.id.edtEmail); 
        edtPassword = findViewById(R.id.edtPassword);
        btnSubmit = findViewById(R.id.btnLoginMain);
        txtTitle = findViewById(R.id.txtLoginTitle);
        txtLabelUser = findViewById(R.id.txtLabelUser);
        txtSwitchMode = findViewById(R.id.txtSwitchAuthMode);

        currentMode = getIntent().getStringExtra("AUTH_MODE");
        if (currentMode == null) currentMode = "LOGIN";

        updateUIByMode();

        txtSwitchMode.setOnClickListener(v -> {
            currentMode = currentMode.equals("LOGIN") ? "REGISTER" : "LOGIN";
            updateUIByMode();
        });

        btnSubmit.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentMode.equals("REGISTER")) {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName("User"); // Mặc định
                
                AuthRepository.INSTANCE.register(newUser, password, (success, message) -> {
                    if (success) {
                        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Toast.makeText(this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                    }
                    return null;
                });
            } else {
                AuthRepository.INSTANCE.login(email, password, (success, message) -> {
                    if (success) {
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                    return null;
                });
            }
        });

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void updateUIByMode() {
        if (currentMode.equals("REGISTER")) {
            txtTitle.setText("Đăng ký");
            txtLabelUser.setText("Email");
            btnSubmit.setText("Sign Up");
            txtSwitchMode.setText("Already have an account? Login");
        } else {
            txtTitle.setText("TechMate");
            txtLabelUser.setText("Email");
            btnSubmit.setText("Sign In");
            txtSwitchMode.setText("Don't have an account? Register");
        }
    }
}
