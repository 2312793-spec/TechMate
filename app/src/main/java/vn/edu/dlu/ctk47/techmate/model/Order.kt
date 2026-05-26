package vn.edu.dlu.ctk47.techmate.model

data class Order(
    var id: String? = null,
    var userId: String? = null,
    var items: List<OrderItem>? = null,
    var totalPrice: Double = 0.0,
    var status: String? = "Pending",
    var timestamp: Long? = null,
    var shippingAddress: String? = null,
    var phone: String? = null
)
