package vn.edu.dlu.ctk47.techmate.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class OrderItem(
    var productId: String? = null,
    var productName: String? = null,
    var image: String? = null, // Lưu ảnh tại thời điểm mua
    var quantity: Int = 0,
    var price: Double = 0.0
)
