package vn.edu.dlu.ctk47.techmate;

import java.util.ArrayList;
import java.util.List;
import vn.edu.dlu.ctk47.techmate.model.Product;
import vn.edu.dlu.ctk47.techmate.model.CartItem;

public class CartManager {

    private static final List<CartItem> cartList = new ArrayList<>();

    public static void add(Product p) {
        if (p == null) return;
        
        for (CartItem item : cartList) {
            if (item.getProduct() != null && item.getProduct().getId() != null && 
                item.getProduct().getId().equals(p.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        cartList.add(new CartItem(p, 1));
    }

    public static List<CartItem> getCart() {
        return cartList;
    }

    public static double getTotal() {
        double total = 0;
        for (CartItem item : cartList) {
            if (item.getProduct() != null) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    public static void remove(int position) {
        if (position >= 0 && position < cartList.size()) {
            cartList.remove(position);
        }
    }

    public static void clear() {
        cartList.clear();
    }
}
