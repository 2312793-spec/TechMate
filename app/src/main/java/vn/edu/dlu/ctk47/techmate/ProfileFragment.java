package vn.edu.dlu.ctk47.techmate;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.dlu.ctk47.techmate.firebase.AuthRepository;

public class ProfileFragment extends Fragment {

    private static final String PREFS_NAME = "TechMatePrefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    private LinearLayout layoutProfileHeader;
    private ImageView imgProfileAvatar;
    private TextView txtProfileName, txtProfileEmail, btnEditProfile;
    private TextView btnMyOrders, btnAddressBook, btnHelpCenter, btnPrivacyPolicy;
    private View btnLanguage, btnAbout;
    private Button btnLogout;
    private SwitchCompat switchDarkMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutProfileHeader = view.findViewById(R.id.layoutProfileHeader);
        imgProfileAvatar    = view.findViewById(R.id.imgProfileAvatar);
        txtProfileName      = view.findViewById(R.id.txtProfileName);
        txtProfileEmail     = view.findViewById(R.id.txtProfileEmail);
        btnEditProfile      = view.findViewById(R.id.btnEditProfile);
        btnMyOrders         = view.findViewById(R.id.btnMyOrders);
        btnAddressBook      = view.findViewById(R.id.btnAddressBook);
        btnHelpCenter       = view.findViewById(R.id.btnHelpCenter);
        btnPrivacyPolicy    = view.findViewById(R.id.btnPrivacyPolicy);
        btnLanguage         = view.findViewById(R.id.btnLanguage);
        btnAbout            = view.findViewById(R.id.btnAbout);
        btnLogout           = view.findViewById(R.id.btnLogout);
        switchDarkMode      = view.findViewById(R.id.switchDarkMode);

        setupListeners(view);
        setupDarkModeSwitch();
        updateUI();
    }

    private void setupListeners(View view) {
        layoutProfileHeader.setOnClickListener(v -> {
            if (AuthRepository.INSTANCE.getCurrentUser() == null) {
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_loginFragment);
            }
        });

        btnMyOrders.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_myOrdersFragment));

        btnEditProfile.setOnClickListener(v -> {
            if (AuthRepository.INSTANCE.getCurrentUser() != null) {
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_settingsFragment);
            } else {
                Toast.makeText(getContext(), "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> {
            AuthRepository.INSTANCE.logout();
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            updateUI();
        });

        if (btnAddressBook != null) {
            btnAddressBook.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Tính năng Sổ địa chỉ đang phát triển", Toast.LENGTH_SHORT).show());
        }

        if (btnHelpCenter != null) {
            btnHelpCenter.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Tính năng Trung tâm trợ giúp đang phát triển", Toast.LENGTH_SHORT).show());
        }

        if (btnPrivacyPolicy != null) {
            btnPrivacyPolicy.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Tính năng Chính sách bảo mật đang phát triển", Toast.LENGTH_SHORT).show());
        }

        if (btnLanguage != null) {
            btnLanguage.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Tính năng Ngôn ngữ đang phát triển", Toast.LENGTH_SHORT).show());
        }

        if (btnAbout != null) {
            btnAbout.setOnClickListener(v ->
                    Toast.makeText(getContext(), "TechMate v1.0 — Ứng dụng mua sắm công nghệ", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupDarkModeSwitch() {
        if (switchDarkMode == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK_MODE, false);
        switchDarkMode.setChecked(isDark);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });
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
            txtProfileName.setText("CHƯA ĐĂNG NHẬP");
            txtProfileEmail.setText("Nhấn để đăng nhập ngay");
            imgProfileAvatar.setImageResource(R.drawable.ic_launcher_background);
            btnLogout.setVisibility(View.GONE);
        }
    }
}
