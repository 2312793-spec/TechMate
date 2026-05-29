package vn.edu.dlu.ctk47.techmate;

import android.graphics.Color;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;
import vn.edu.dlu.ctk47.techmate.model.Product;
import vn.edu.dlu.ctk47.techmate.firebase.ProductRepository;

public class DetailFragment extends Fragment {

    private TextView txtDetailName, txtDetailPrice, txtBottomPrice, txtDetailDesc;
    private TextView txtSpecScreen, txtSpecCPU, txtSpecRAM;
    private LinearLayout sectionColors, containerColors;
    private LinearLayout sectionVariants, containerVariants;
    private TextView selectedVariantChip = null;
    private TextView selectedColorChip = null;
    private ViewPager2 viewPagerImage;
    private TabLayout tabIndicator;
    private Button btnAdd;
    private ImageView btnBack;
    private Product product;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPagerImage = view.findViewById(R.id.viewPagerImage);
        tabIndicator = view.findViewById(R.id.tabIndicator);
        txtDetailName = view.findViewById(R.id.txtDetailName);
        txtDetailPrice = view.findViewById(R.id.txtDetailPrice);
        txtBottomPrice = view.findViewById(R.id.txtBottomPrice);
        txtDetailDesc = view.findViewById(R.id.txtDetailDesc);

        sectionColors = view.findViewById(R.id.sectionColors);
        containerColors = view.findViewById(R.id.containerColors);

        sectionVariants = view.findViewById(R.id.sectionVariants);
        containerVariants = view.findViewById(R.id.containerVariants);

        txtSpecScreen = view.findViewById(R.id.txtSpecScreen);
        txtSpecCPU = view.findViewById(R.id.txtSpecCPU);
        txtSpecRAM = view.findViewById(R.id.txtSpecRAM);

        btnAdd = view.findViewById(R.id.btnAdd);
        btnBack = view.findViewById(R.id.btnBack);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String productId = bundle.getString("id");
            if (productId != null) {
                loadProductDetails(productId);
            }
        }

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        btnAdd.setOnClickListener(v -> {
            if (product != null) {
                CartManager.add(product);
                Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).navigate(R.id.cartFragment);
            }
        });
    }

    private void loadProductDetails(String productId) {
        ProductRepository.INSTANCE.getProductById(productId, p -> {
            if (p != null) {
                this.product = p;
                displayProduct();
            } else {
                Toast.makeText(getContext(), "Product not found", Toast.LENGTH_SHORT).show();
            }
            return Unit.INSTANCE;
        });
    }

    private void displayProduct() {
        if (product == null) return;

        txtDetailName.setText(product.getName());

        String formattedPrice = String.format(Locale.GERMANY, "%,.0f đ", product.getPrice());
        txtDetailPrice.setText(formattedPrice);
        txtBottomPrice.setText(formattedPrice);
        txtDetailDesc.setText(product.getDescription());

        List<String> images = product.getImageList();
        if (images == null || images.isEmpty()) {
            images = new ArrayList<>();
            images.add("");
        }

        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        viewPagerImage.setAdapter(adapter);
        new TabLayoutMediator(tabIndicator, viewPagerImage, (tab, position) -> {}).attach();

        setupVariants();
        setupColors();

        Object specsObj = product.getSpecs();
        if (specsObj instanceof Map) {
            Map<String, String> specsMap = (Map<String, String>) specsObj;
            txtSpecScreen.setText(specsMap.getOrDefault("Screen", "N/A"));
            txtSpecCPU.setText(specsMap.getOrDefault("CPU", "N/A"));
            txtSpecRAM.setText(specsMap.getOrDefault("RAM", "N/A"));
        } else if (specsObj instanceof String) {
            txtSpecScreen.setText((String) specsObj);
            txtSpecCPU.setText("Xem mô tả");
            txtSpecRAM.setText("Xem mô tả");
        } else {
            txtSpecScreen.setText("N/A");
            txtSpecCPU.setText("N/A");
            txtSpecRAM.setText("N/A");
        }
    }

    private void setupVariants() {
        containerVariants.removeAllViews();
        selectedVariantChip = null;
        List<String> variants = product.getVariantList();

        if (variants == null || variants.isEmpty()) {
            sectionVariants.setVisibility(View.GONE);
        } else {
            sectionVariants.setVisibility(View.VISIBLE);
            for (String variantName : variants) {
                TextView chip = createChip(variantName);
                chip.setOnClickListener(v -> {
                    if (selectedVariantChip != null) {
                        selectedVariantChip.setBackgroundResource(R.drawable.bg_chip);
                        selectedVariantChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                    }
                    chip.setBackgroundResource(R.drawable.bg_chip_selected);
                    chip.setTextColor(Color.WHITE);
                    selectedVariantChip = chip;
                });
                containerVariants.addView(chip);
            }
        }
    }

    private void setupColors() {
        containerColors.removeAllViews();
        selectedColorChip = null;
        List<String> colors = product.getColorList();

        if (colors == null || colors.isEmpty()) {
            sectionColors.setVisibility(View.GONE);
        } else {
            sectionColors.setVisibility(View.VISIBLE);
            for (String colorName : colors) {
                TextView chip = createChip(colorName);
                chip.setOnClickListener(v -> {
                    if (selectedColorChip != null) {
                        selectedColorChip.setBackgroundResource(R.drawable.bg_chip);
                        selectedColorChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                    }
                    chip.setBackgroundResource(R.drawable.bg_chip_selected);
                    chip.setTextColor(Color.WHITE);
                    selectedColorChip = chip;
                });
                containerColors.addView(chip);
            }
        }
    }

    private TextView createChip(String text) {
        TextView chip = new TextView(getContext());
        chip.setText(text);
        chip.setPadding(32, 16, 32, 16);
        chip.setBackgroundResource(R.drawable.bg_chip);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 16, 0);
        chip.setLayoutParams(params);

        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        chip.setTextSize(14);
        chip.setClickable(true);
        chip.setFocusable(true);
        return chip;
    }
}
