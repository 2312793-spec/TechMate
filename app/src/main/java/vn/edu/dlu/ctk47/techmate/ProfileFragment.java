package vn.edu.dlu.ctk47.techmate;

import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.dlu.ctk47.techmate.firebase.AuthRepository;

public class ProfileFragment extends Fragment {

    private LinearLayout layoutProfileHeader;
    private ImageView imgProfileAvatar;
    private TextView txtProfileName, txtProfileEmail, btnEditProfile;
    private TextView btnMyOrders, btnAddressBook, btnHelpCenter, btnPrivacyPolicy;
    private View btnLanguage, btnAbout;
    private Button btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View theo fragment_profile.xml
        layoutProfileHeader = view.findViewById(R.id.layoutProfileHeader);
        imgProfileAvatar = view.findViewById(R.id.imgProfileAvatar);
        txtProfileName = view.findViewById(R.id.txtProfileName);
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        
        btnMyOrders = view.findViewById(R.id.btnMyOrders);
        btnAddressBook = view.findViewById(R.id.btnAddressBook);
        btnHelpCenter = view.findViewById(R.id.btnHelpCenter);
        btnPrivacyPolicy = view.findViewById(R.id.btnPrivacyPolicy);
        btnLanguage = view.findViewById(R.id.btnLanguage);
        btnAbout = view.findViewById(R.id.btnAbout);
        btnLogout = view.findViewById(R.id.btnLogout);

        // 2. Xử lý sự kiện
        layoutProfileHeader.setOnClickListener(v -> {
            if (AuthRepository.INSTANCE.getCurrentUser() == null) {
                // Chuyển sang màn hình Login nếu chưa đăng nhập
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_loginFragment); 
            }
        });

        // Xử lý nút Đơn hàng của tôi
        btnMyOrders.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_myOrdersFragment);
        });

        btnLogout.setOnClickListener(v -> {
            AuthRepository.INSTANCE.logout();
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            updateUI();
        });

        btnEditProfile.setOnClickListener(v -> {
             if (AuthRepository.INSTANCE.getCurrentUser() != null) {
                 Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_settingsFragment);
             }
        });

        updateUI();
    }

    private void updateUI() {
        FirebaseUser currentUser = AuthRepository.INSTANCE.getCurrentUser();

        if (currentUser != null) {
            btnLogout.setVisibility(View.VISIBLE);
            AuthRepository.INSTANCE.getUserProfile(currentUser.getUid(), user -> {
                if (user != null && isAdded()) {
                    txtProfileName.setText(user.getName() != null ? user.getName().toUpperCase() : "USER");
                    txtProfileEmail.setText(user.getEmail());

                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        Glide.with(this)
                                .load(user.getAvatar())
                                .placeholder(R.drawable.ic_launcher_background)
                                .circleCrop()
                                .into(imgProfileAvatar);
                    }
                }
                return null;
            });
        } else {
            // Trạng thái chưa đăng nhập
            txtProfileName.setText("CHƯA ĐĂNG NHẬP");
            txtProfileEmail.setText("Nhấn để đăng nhập ngay");
            imgProfileAvatar.setImageResource(R.drawable.ic_launcher_background);
            btnLogout.setVisibility(View.GONE);
        }
    }
}
