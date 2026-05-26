package vn.edu.dlu.ctk47.techmate.model

data class Cart(
    var id: String? = null,
    var userId: String? = null,
    var products: List<CartItem>? = null,
    var quantity: Int? = null,
    var totalPrice: Double? = null
)
