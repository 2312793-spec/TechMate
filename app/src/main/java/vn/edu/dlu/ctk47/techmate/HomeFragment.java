package vn.edu.dlu.ctk47.techmate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kotlin.Unit;
import vn.edu.dlu.ctk47.techmate.model.Brand;
import vn.edu.dlu.ctk47.techmate.model.Category;
import vn.edu.dlu.ctk47.techmate.model.Product;
import vn.edu.dlu.ctk47.techmate.firebase.ProductRepository;

public class HomeFragment extends Fragment {

    private ImageView btnCompareFloat;
    private RecyclerView rvCat, rvBrand, rvProd;
    private EditText edtSearch;
    
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> displayList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private List<Brand> brandList = new ArrayList<>();
    
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private BrandAdapter brandAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        btnCompareFloat = view.findViewById(R.id.btnCompareFloat);
        rvCat = view.findViewById(R.id.rvCategories);
        rvBrand = view.findViewById(R.id.rvBrands);
        rvProd = view.findViewById(R.id.rvProducts);
        edtSearch = view.findViewById(R.id.edtSearch);

        setupRecyclerViews(view);
        setupSearch();
        loadData();

        btnCompareFloat.setOnClickListener(v -> {
            if (CompareManager.get().size() < 2) {
                Toast.makeText(getContext(), "Chọn ít nhất 2 sản phẩm để so sánh", Toast.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(view).navigate(R.id.compareFragment);
        });

        updateCompareUI();
    }

    private void setupRecyclerViews(View view) {
        // Categories
        rvCat.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            filterByCategory(category.getId());
        });
        rvCat.setAdapter(categoryAdapter);

        // Brands
        rvBrand.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        brandAdapter = new BrandAdapter(brandList, brand -> {
            filterByBrand(brand.getId());
        });
        rvBrand.setAdapter(brandAdapter);

        // Products
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
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProducts(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadData() {
        // Load Categories
        ProductRepository.INSTANCE.getCategories(categories -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    categoryList.clear();
                    categoryList.addAll(categories);
                    categoryAdapter.notifyDataSetChanged();
                });
            }
            return Unit.INSTANCE;
        });

        // Load Brands
        ProductRepository.INSTANCE.getBrands(brands -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    brandList.clear();
                    brandList.addAll(brands);
                    brandAdapter.notifyDataSetChanged();
                });
            }
            return Unit.INSTANCE;
        });

        // Load Products
        ProductRepository.INSTANCE.getProducts(products -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    allProducts.clear();
                    allProducts.addAll(products);
                    displayList.clear();
                    displayList.addAll(products);
                    productAdapter.notifyDataSetChanged();
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
        productAdapter.notifyDataSetChanged();
    }

    private void updateProductDisplay(List<Product> products) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                displayList.clear();
                displayList.addAll(products);
                productAdapter.notifyDataSetChanged();
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
    }
}
