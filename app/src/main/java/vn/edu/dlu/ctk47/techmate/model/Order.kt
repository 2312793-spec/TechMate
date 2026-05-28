package vn.edu.dlu.ctk47.techmate.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Order(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String? = null,

    var userId: String? = null,
    var customerName: String? = null,

    // Đảm bảo OrderItem bên User và CartItem bên Admin có cấu trúc trường giống nhau
    var items: List<OrderItem>? = null,

    var totalAmount: Double = 0.0, // Đổi từ totalPrice sang totalAmount
    var status: String? = "Pending",
    var paymentMethod: String? = "COD",
    var paymentStatus: String? = "Unpaid",

    var address: String? = null, // Đổi từ shippingAddress sang address
    var phone: String? = null,
    var note: String? = null,

    var timestamp: Long? = System.currentTimeMillis(),

    // Thông tin Shipper (Admin sẽ cập nhật sau)
    var shipperId: String? = null,
    var shipperName: String? = null,
    var shipperPhone: String? = null
)