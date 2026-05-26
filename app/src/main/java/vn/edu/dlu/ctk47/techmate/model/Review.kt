package vn.edu.dlu.ctk47.techmate.model

data class Review(
    var id: String? = null,
    var productId: String? = null,
    var userId: String? = null,
    var userName: String? = null,
    var userAvatar: String? = null,
    var rating: Float = 0f,
    var comment: String? = null,
    var timestamp: Long? = null
)
