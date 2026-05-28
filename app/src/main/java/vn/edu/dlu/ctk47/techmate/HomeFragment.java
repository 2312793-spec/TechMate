package vn.edu.dlu.ctk47.techmate;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kotlin.Unit;
import vn.edu.dlu.ctk47.techmate.firebase.AuthRepository;
import vn.edu.dlu.ctk47.techmate.model.Brand;
import vn.edu.dlu.ctk47.techmate.model.Category;
import vn.edu.dlu.ctk47.techmate.model.Product;
import vn.edu.dlu.ctk47.techmate.firebase.ProductRepository;

public class HomeFragment extends Fragment {

    private ImageView btnCompareFloat, btnProfile;
    private RecyclerView rvCat, rvBrand, rvProd;
    private EditText edtSearch;
    
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> displayList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private List<Brand> brandList = new ArrayList<>();
    
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private BrandAdapter brandAdapter;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnCompareFloat = view.findViewById(R.id.btnCompareFloat);
        btnProfile = view.findViewById(R.id.btnProfile);
        rvCat = view.findViewById(R.id.rvCategories);
        rvBrand = view.findViewById(R.id.rvBrands);
        rvProd = view.findViewById(R.id.rvProducts);
        edtSearch = view.findViewById(R.id.edtSearch);

        // Kiểm tra null cho View để tránh crash
        if (rvCat == null || rvBrand == null || rvProd == null) return;

        setupRecyclerViews(view);
        setupSearch();
        loadData();

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                if (AuthRepository.INSTANCE.getCurrentUser() == null) {
                    showLoginRequestDialog();
                } else {
                    Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_profileFragment);
                }
            });
        }

        if (btnCompareFloat != null) {
            btnCompareFloat.setOnClickListener(v -> {
                if (CompareManager.get().size() < 2) {
                    Toast.makeText(getContext(), "Chọn ít nhất 2 sản phẩm để so sánh", Toast.LENGTH_SHORT).show();
                    return;
                }
                Navigation.findNavController(view).navigate(R.id.compareFragment);
            });
        }

        updateCompareUI();
        updateProfileIcon();
    }

    private void showLoginRequestDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_login_request);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnLogin = dialog.findViewById(R.id.btnLoginNav);
        Button btnRegister = dialog.findViewById(R.id.btnRegisterNav);
        View btnClose = dialog.findViewById(R.id.btnClose);

        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_loginFragment);
        });

        btnRegister.setOnClickListener(v -> {
            dialog.dismiss();
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_registerFragment);
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateProfileIcon() {
        if (AuthRepository.INSTANCE.getCurrentUser() != null) {
            AuthRepository.INSTANCE.getUserProfile(AuthRepository.INSTANCE.getCurrentUser().getUid(), user -> {
                if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty() && isAdded()) {
                    Glide.with(this)
                            .load(user.getAvatar())
                            .placeholder(R.drawable.ic_person_outline)
                            .circleCrop()
                            .into(btnProfile);
                }
                return null;
            });
        } else {
            btnProfile.setImageResource(R.drawable.ic_person_outline);
        }
    }

    private void setupRecyclerViews(View view) {
        rvCat.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            if (category == null) {
                updateProductDisplay(allProducts);
            } else {
                brandAdapter.clearSelection(); // Bỏ chọn bên Brand
                filterByCategory(category.getId());
            }
        });
        rvCat.setAdapter(categoryAdapter);

        rvBrand.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        brandAdapter = new BrandAdapter(brandList, brand -> {
            if (brand == null) {
                updateProductDisplay(allProducts);
            } else {
                categoryAdapter.clearSelection(); // Bỏ chọn bên Category
                filterByBrand(brand.getId());
            }
        });
        rvBrand.setAdapter(brandAdapter);

        rvProd.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(displayList, new ProductAdapter.OnProductListener() {
            @Override
            public void onClick(Product product) {
                if (CompareManager.hasOne()) {
                    CompareManager.add(product);
                    Navigation.findNavController(view).navigate(R.id.compareFragment);
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString("id", product.getId());
                Navigation.findNavController(view).navigate(R.id.detailFragment, bundle);
            }

            @Override
            public void onAddToCart(Product product) {
                CartManager.add(product);
                Toast.makeText(getContext(), "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
        rvProd.setAdapter(productAdapter);
    }

    private void setupSearch() {
        if (edtSearch == null) return;
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);
                searchHandler.postDelayed(() -> searchProducts(s.toString()), 300);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadData() {
        ProductRepository.INSTANCE.getCategories(categories -> {
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    categoryList.clear();
                    categoryList.addAll(categories);
                    if (categoryAdapter != null) categoryAdapter.notifyDataSetChanged();
                });
            }
            return Unit.INSTANCE;
        });

        ProductRepository.INSTANCE.getBrands(brands -> {
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    brandList.clear();
                    brandList.addAll(brands);
                    if (brandAdapter != null) brandAdapter.notifyDataSetChanged();
                });
            }
            return Unit.INSTANCE;
        });

        ProductRepository.INSTANCE.getProducts(products -> {
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    allProducts.clear();
                    allProducts.addAll(products);
                    displayList.clear();
                    displayList.addAll(products);
                    if (productAdapter != null) productAdapter.notifyDataSetChanged();
                });
            }
            return Unit.INSTANCE;
        });
    }

    private void filterByCategory(String catId) {
        ProductRepository.INSTANCE.getProductsByCategory(catId, products -> {
            updateProductDisplay(products);
            return Unit.INSTANCE;
        });
    }

    private void filterByBrand(String brandId) {
        ProductRepository.INSTANCE.getProductsByBrand(brandId, products -> {
            updateProductDisplay(products);
            return Unit.INSTANCE;
        });
    }

    private void searchProducts(String query) {
        if (query.isEmpty()) {
            displayList.clear();
            displayList.addAll(allProducts);
        } else {
            List<Product> filtered = allProducts.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            displayList.clear();
            displayList.addAll(filtered);
        }
        if (productAdapter != null) productAdapter.notifyDataSetChanged();
    }

    private void updateProductDisplay(List<Product> products) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                displayList.clear();
                displayList.addAll(products);
                if (productAdapter != null) productAdapter.notifyDataSetChanged();
            });
        }
    }

    private void updateCompareUI() {
        int size = CompareManager.get().size();
        if (btnCompareFloat != null) {
            btnCompareFloat.setAlpha(size > 0 ? 1f : 0.4f);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCompareUI();
        updateProfileIcon();
    }
}
