package vn.edu.dlu.ctk47.techmate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

import kotlin.Unit;
import vn.edu.dlu.ctk47.techmate.firebase.ProductRepository;
import vn.edu.dlu.ctk47.techmate.model.Product;

public class CartFragment extends Fragment {

    public CartFragment() {
        super(R.layout.fragment_cart);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các view từ fragment_cart.xml
        RecyclerView rvCart = view.findViewById(R.id.rvCartItems);
        TextView txtTotal = view.findViewById(R.id.txtTotalPrice);
        Button btnCheckout = view.findViewById(R.id.btnCheckout);
        
        // rvSuggestedItems có thể không có trong layout hiện tại, kiểm tra null trước khi dùng
        RecyclerView rvSuggested = view.findViewById(R.id.rvSuggestedItems);

        // =====================================
        // PHẦN 1: GIỎ HÀNG CỦA BẠN
        // =====================================
        if (rvCart != null) {
            rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
            CartAdapter adapter = new CartAdapter(CartManager.getCart(), () -> {
                updateTotalUI(txtTotal); // Cập nhật lại tổng tiền khi số lượng thay đổi
            });
            rvCart.setAdapter(adapter);
        }

        updateTotalUI(txtTotal); // Gọi lần đầu để hiển thị tổng tiền

        // 👉 Chuyển hướng sang Checkout
        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(v -> {
                NavController nav = Navigation.findNavController(v);
                nav.navigate(R.id.checkoutFragment);
            });
        }

        // =====================================
        // PHẦN 2: CÓ THỂ BẠN CŨNG THÍCH (Lấy từ Firebase)
        // =====================================
        if (rvSuggested != null) {
            // Hiển thị dạng lưới 2 cột
            rvSuggested.setLayoutManager(new GridLayoutManager(getContext(), 2));

            // Lấy danh sách sản phẩm thực tế từ Database
            ProductRepository.INSTANCE.getProducts(products -> {
                if (isAdded() && products != null && !products.isEmpty()) {
                    requireActivity().runOnUiThread(() -> {
                        // Hiển thị tối đa 6 sản phẩm đầu tiên làm gợi ý
                        List<Product> suggestedList = products.size() > 6 ? products.subList(0, 6) : products;
                        
                        ProductAdapter suggestedAdapter = new ProductAdapter(suggestedList, new ProductAdapter.OnProductListener() {
                            @Override
                            public void onClick(Product product) {
                                // Chuyển sang màn hình chi tiết
                                Bundle bundle = new Bundle();
                                bundle.putString("id", product.getId()); 
                                Navigation.findNavController(view).navigate(R.id.detailFragment, bundle);
                            }

                            @Override
                            public void onAddToCart(Product product) {
                                CartManager.add(product);
                                Toast.makeText(getContext(), "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                                // Cập nhật lại giao diện giỏ hàng nếu cần
                                updateTotalUI(txtTotal);
                                if (rvCart != null && rvCart.getAdapter() != null) {
                                    rvCart.getAdapter().notifyDataSetChanged();
                                }
                            }
                        });
                        rvSuggested.setAdapter(suggestedAdapter);
                    });
                }
                return Unit.INSTANCE;
            });
        }
    }

    /**
     * Cập nhật hiển thị tổng tiền định dạng Việt Nam
     */
    private void updateTotalUI(TextView txtTotal) {
        if (txtTotal == null) return;
        double total = CartManager.getTotal();
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String formattedTotal = formatter.format(total).replace(",", ".") + " đ";
        txtTotal.setText("Tổng cộng: " + formattedTotal);
    }
}
