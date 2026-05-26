package vn.edu.dlu.ctk47.techmate.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

object DataSeeder {
    private const val TAG = "DataSeeder"

    @JvmStatic
    fun seedInitialData() {
        Log.i(TAG, ">>> KHỞI CHẠY SEED DỮ LIỆU <<<")
        val db = FirebaseFirestore.getInstance()
        db.enableNetwork().addOnCompleteListener { task: Task<Void> ->
            Log.d(TAG, "Trạng thái mạng Firestore: ${if (task.isSuccessful) "ONLINE" else "OFFLINE"}")
            
            // Giữ nguyên luồng chạy chính
            executeSeeding() 
            
            // Tập trung vào việc cập nhật các field thiếu cho sản phẩm đã có trên Console
            updateAllExistingProducts() 
        }
    }

    private fun executeSeeding() {
        seedCategories()
        seedBrands()
        seedProducts()
        seedBanners()
        Log.i(TAG, ">>> FLOW SEEDING ĐÃ CHẠY XONG (Không tạo thêm dữ liệu mới) <<<")
    }

    /**
     * Hàm cập nhật nhanh: Tự động thêm variants và ram cho TOÀN BỘ sản phẩm hiện có trên console
     */
    fun updateAllExistingProducts() {
        val db = FirebaseFirestore.getInstance()
        val collection = db.collection("products")

        collection.get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            var count = 0

            for (doc in snapshot.documents) {
                val updates = mutableMapOf<String, Any>()

                // Chỉ thêm nếu field chưa tồn tại
                if (!doc.contains("variants")) {
                    updates["variants"] = listOf("128GB", "256GB", "512GB")
                }

                if (!doc.contains("ram")) {
                    updates["ram"] = "8GB"
                }

                if (updates.isNotEmpty()) {
                    batch.update(doc.reference, updates)
                    count++
                }
            }

            if (count > 0) {
                batch.commit().addOnSuccessListener {
                    Log.i(TAG, ">>> THÀNH CÔNG: Đã cập nhật nhanh $count sản phẩm cũ <<<")
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Lỗi cập nhật: ${e.message}")
                }
            } else {
                Log.i(TAG, "Dữ liệu trên Console đã đầy đủ các trường mới.")
            }
        }
    }

    private fun seedCategories() {
        // Đã bỏ tạo dữ liệu mới theo yêu cầu
    }

    private fun seedBrands() {
        // Đã bỏ tạo dữ liệu mới theo yêu cầu
    }

    private fun seedProducts() {
        // Đã bỏ tạo dữ liệu mới theo yêu cầu. Hiện tại chỉ thực hiện cập nhật qua updateAllExistingProducts()
    }

    private fun seedBanners() {
        // Đã bỏ tạo dữ liệu mới theo yêu cầu
    }
}
