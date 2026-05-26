package vn.edu.dlu.ctk47.techmate.model

data class OrderItem(
    var productId: String? = null,
    var productName: String? = null,
    var quantity: Int = 0,
    var price: Double = 0.0
)
