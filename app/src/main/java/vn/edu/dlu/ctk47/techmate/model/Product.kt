package vn.edu.dlu.ctk47.techmate.model

data class Product(
    var id: String? = null,
    var name: String? = null,
    var price: Double = 0.0,
    var categoryId: String? = null,
    var brandId: String? = null,
    var colors: List<String>? = null,
    var variants: List<String>? = null,
    var ram: String? = null,
    var specs: Map<String, String>? = null,
    var images: List<String>? = null,
    var description: String? = null,
    var rating: Double = 0.0,
    var stock: Int = 0
)
