package vn.edu.dlu.ctk47.techmate;

import java.util.ArrayList;
import java.util.List;
import vn.edu.dlu.ctk47.techmate.model.Product;

public class CompareManager {

    private static final List<Product> selected = new ArrayList<>();

    public static void add(Product p) {
        if (selected.size() >= 2) {
            selected.clear();
        }
        selected.add(p);
    }

    public static boolean hasOne() {
        return selected.size() == 1;
    }

    public static boolean isReady() {
        return selected.size() == 2;
    }

    public static List<Product> get() {
        return selected;
    }

    public static void clear() {
        selected.clear();
    }
}
