package vn.edu.dlu.ctk47.techmate.firebase

import android.util.Log
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import vn.edu.dlu.ctk47.techmate.model.Brand
import vn.edu.dlu.ctk47.techmate.model.Category
import vn.edu.dlu.ctk47.techmate.model.Product

object ProductRepository {
    private const val TAG = "ProductRepository"

    fun getCategories(callback: (List<Category>) -> Unit) {
        FirestoreHelper.getCategoriesCollection()
            .orderBy("priority")
            .get()
            .addOnSuccessListener { snapshot ->
                val categories = snapshot.toObjects<Category>()
                callback(categories)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching categories: ${e.message}")
                callback(emptyList())
            }
    }

    fun getBrands(callback: (List<Brand>) -> Unit) {
        FirestoreHelper.getBrandsCollection()
            .get()
            .addOnSuccessListener { snapshot ->
                val brands = snapshot.toObjects<Brand>()
                callback(brands)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching brands: ${e.message}")
                callback(emptyList())
            }
    }

    fun getProducts(callback: (List<Product>) -> Unit) {
        FirestoreHelper.getProductsCollection()
            .get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.toObjects<Product>()
                callback(products)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching products: ${e.message}")
                callback(emptyList())
            }
    }

    fun getProductsByCategory(categoryId: String, callback: (List<Product>) -> Unit) {
        FirestoreHelper.getProductsCollection()
            .whereEqualTo("categoryId", categoryId)
            .get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.toObjects<Product>()
                callback(products)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun getProductsByBrand(brandId: String, callback: (List<Product>) -> Unit) {
        FirestoreHelper.getProductsCollection()
            .whereEqualTo("brandId", brandId)
            .get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.toObjects<Product>()
                callback(products)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun getProductById(productId: String, callback: (Product?) -> Unit) {
        FirestoreHelper.getProductsCollection()
            .document(productId)
            .get()
            .addOnSuccessListener { snapshot ->
                val product = snapshot.toObject<Product>()
                callback(product)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun getBrandById(brandId: String, callback: (Brand?) -> Unit) {
        FirestoreHelper.getBrandsCollection()
            .document(brandId)
            .get()
            .addOnSuccessListener { snapshot ->
                val brand = snapshot.toObject<Brand>()
                callback(brand)
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}
