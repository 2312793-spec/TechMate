package vn.edu.dlu.ctk47.techmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import vn.edu.dlu.ctk47.techmate.model.Product;

public class CompareFragment extends Fragment {

    public CompareFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compare, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // =========================
        // 1. Lấy dữ liệu
        // =========================
        List<Product> listP = CompareManager.get();

        if (listP == null || listP.size() < 2) {
            Toast.makeText(getContext(), "Chọn ít nhất 2 sản phẩm để so sánh", Toast.LENGTH_SHORT).show();
            return;
        }

        Product p1 = listP.get(0);
        Product p2 = listP.get(1);

        // =========================
        // 2. Bind View Header
        // =========================
        TextView txtName1 = view.findViewById(R.id.txtName1);
        TextView txtPrice1 = view.findViewById(R.id.txtPrice1);
        TextView txtName2 = view.findViewById(R.id.txtName2);
        TextView txtPrice2 = view.findViewById(R.id.txtPrice2);

        if (txtName1 != null) txtName1.setText(p1.getName());
        if (txtPrice1 != null) txtPrice1.setText(String.format(Locale.GERMANY, "%,.0f đ", p1.getPrice()));
        if (txtName2 != null) txtName2.setText(p2.getName());
        if (txtPrice2 != null) txtPrice2.setText(String.format(Locale.GERMANY, "%,.0f đ", p2.getPrice()));

        // =========================
        // 3. Xử lý So sánh (Specs, Colors, Variants)
        // =========================
        List<Spec> specList = new ArrayList<>();

        // --- A. So sánh Màu sắc & Phiên bản (Lấy an toàn từ getColorList/getVariantList) ---
        specList.add(new Spec("Màu sắc", 
                TextUtils.join(", ", p1.getColorList()), 
                TextUtils.join(", ", p2.getColorList())));
        
        specList.add(new Spec("Phiên bản", 
                TextUtils.join(", ", p1.getVariantList()), 
                TextUtils.join(", ", p2.getVariantList())));

        // --- B. So sánh Specs (Xử lý đa kiểu dữ liệu Map/String) ---
        Set<String> allKeys = new HashSet<>();
        Object s1 = p1.getSpecs();
        Object s2 = p2.getSpecs();

        if (s1 instanceof Map) allKeys.addAll(((Map<String, String>) s1).keySet());
        if (s2 instanceof Map) allKeys.addAll(((Map<String, String>) s2).keySet());
        
        // Nếu là String (do Admin nhập trực tiếp), đưa vào một dòng chung
        if (s1 instanceof String || s2 instanceof String) {
            allKeys.add("Thông số khác");
        }

        for (String key : allKeys) {
            String val1 = "N/A";
            String val2 = "N/A";

            if (s1 instanceof Map) {
                val1 = ((Map<String, String>) s1).getOrDefault(key, "N/A");
            } else if (s1 instanceof String && key.equals("Thông số khác")) {
                val1 = (String) s1;
            }

            if (s2 instanceof Map) {
                val2 = ((Map<String, String>) s2).getOrDefault(key, "N/A");
            } else if (s2 instanceof String && key.equals("Thông số khác")) {
                val2 = (String) s2;
            }

            specList.add(new Spec(key, val1, val2));
        }

        // =========================
        // 4. Setup RecyclerView
        // =========================
        RecyclerView rv = view.findViewById(R.id.rvCompare);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            rv.setAdapter(new CompareAdapter(specList));
        }

        // Clear danh sách so sánh sau khi hiển thị
        CompareManager.clear();
    }
}
