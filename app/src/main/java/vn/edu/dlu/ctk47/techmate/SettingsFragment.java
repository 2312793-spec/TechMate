package vn.edu.dlu.ctk47.techmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import vn.edu.dlu.ctk47.techmate.firebase.AuthRepository;
import vn.edu.dlu.ctk47.techmate.model.User;

public class SettingsFragment extends Fragment {

    private EditText edtName, edtPhone, edtAddress;
    private Button btnSave;
    private ImageView btnBack;
    private User currentUserModel;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtName = view.findViewById(R.id.edtSettingsName);
        edtPhone = view.findViewById(R.id.edtSettingsPhone);
        edtAddress = view.findViewById(R.id.edtSettingsAddress);
        btnSave = view.findViewById(R.id.btnSaveSettings);
        btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        // Tải dữ liệu hiện tại
        loadUserData();

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadUserData() {
        String uid = AuthRepository.INSTANCE.getCurrentUser() != null ? AuthRepository.INSTANCE.getCurrentUser().getUid() : null;
        if (uid != null) {
            AuthRepository.INSTANCE.getUserProfile(uid, user -> {
                if (user != null && isAdded()) {
                    currentUserModel = user;
                    edtName.setText(user.getName());
                    edtPhone.setText(user.getPhone());
                    edtAddress.setText(user.getAddress());
                }
                return null;
            });
        }
    }

    private void saveChanges() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), "Tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserModel != null) {
            currentUserModel.setName(name);
            currentUserModel.setPhone(phone);
            currentUserModel.setAddress(address);

            AuthRepository.INSTANCE.updateUserProfile(currentUserModel, (success, message) -> {
                if (success) {
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(getView()).popBackStack();
                } else {
                    Toast.makeText(getContext(), "Lỗi: " + message, Toast.LENGTH_LONG).show();
                }
                return null;
            });
        }
    }
}
