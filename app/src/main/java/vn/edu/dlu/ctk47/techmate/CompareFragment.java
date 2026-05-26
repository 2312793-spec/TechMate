package vn.edu.dlu.ctk47.techmate;

import android.os.Bundle;
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
            Toast.makeText(getContext(), "Need 2 products to compare", Toast.LENGTH_SHORT).show();
            return;
        }

        Product p1 = listP.get(0);
        Product p2 = listP.get(1);

        // =========================
        // 2. Bind View (an toàn)
        // =========================
        TextView txtName1 = view.findViewById(R.id.txtName1);
        TextView txtPrice1 = view.findViewById(R.id.txtPrice1);
        TextView txtName2 = view.findViewById(R.id.txtName2);
        TextView txtPrice2 = view.findViewById(R.id.txtPrice2);

        if (txtName1 == null || txtPrice1 == null || txtName2 == null || txtPrice2 == null) {
            Toast.makeText(getContext(), "Layout error (missing id)", Toast.LENGTH_SHORT).show();
            return;
        }

        txtName1.setText(p1.getName() != null ? p1.getName() : "N/A");
        txtPrice1.setText(String.format(Locale.getDefault(), "$%.2f", p1.getPrice()));

        txtName2.setText(p2.getName() != null ? p2.getName() : "N/A");
        txtPrice2.setText(String.format(Locale.getDefault(), "$%.2f", p2.getPrice()));

        // =========================
        // 3. Xử lý Specs (null-safe)
        // =========================
        List<Spec> specList = new ArrayList<>();
        Set<String> allKeys = new HashSet<>();

        if (p1.getSpecs() != null) allKeys.addAll(p1.getSpecs().keySet());
        if (p2.getSpecs() != null) allKeys.addAll(p2.getSpecs().keySet());

        for (String key : allKeys) {

            String val1 = "N/A";
            String val2 = "N/A";

            if (p1.getSpecs() != null && p1.getSpecs().containsKey(key)) {
                val1 = p1.getSpecs().get(key);
            }

            if (p2.getSpecs() != null && p2.getSpecs().containsKey(key)) {
                val2 = p2.getSpecs().get(key);
            }

            // tránh null
            if (val1 == null) val1 = "N/A";
            if (val2 == null) val2 = "N/A";

            specList.add(new Spec(key, val1, val2));
        }

        // fallback nếu không có data
        if (specList.isEmpty()) {
            specList.add(new Spec("Info", "No data", "No data"));
        }

        // =========================
        // 4. Setup RecyclerView
        // =========================
        RecyclerView rv = view.findViewById(R.id.rvCompare);

        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            rv.setAdapter(new CompareAdapter(specList));
        }

        // =========================
        // 5. Clear sau khi dùng
        // =========================
        CompareManager.clear();
    }
}
