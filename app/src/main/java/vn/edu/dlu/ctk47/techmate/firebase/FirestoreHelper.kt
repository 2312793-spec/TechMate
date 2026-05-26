package vn.edu.dlu.ctk47.techmate.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

object FirestoreHelper {
    private val db: FirebaseFirestore by lazy {
        // Bật logging để debug lỗi kết nối
        FirebaseFirestore.setLoggingEnabled(true)
        FirebaseFirestore.getInstance().apply {
            // Cấu hình để Firestore ưu tiên kết nối mạng
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        }
    }

    fun getProductsCollection(): CollectionReference = db.collection("products")
    fun getCategoriesCollection(): CollectionReference = db.collection("categories")
    fun getBrandsCollection(): CollectionReference = db.collection("brands")
    fun getUsersCollection(): CollectionReference = db.collection("users")
    fun getOrdersCollection(): CollectionReference = db.collection("orders")
    fun getReviewsCollection(): CollectionReference = db.collection("reviews")
}
