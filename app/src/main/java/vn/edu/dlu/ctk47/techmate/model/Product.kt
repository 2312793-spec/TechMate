package vn.edu.dlu.ctk47.techmate.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Product(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String? = null,
    var name: String? = null,
    var price: Double = 0.0,
    var categoryId: String? = null,
    var brandId: String? = null,
    
    // Any? giúp tránh crash nếu Admin lưu sai kiểu dữ liệu (String thay vì Array)
    var colors: Any? = null,
    var variants: Any? = null,
    
    var ram: String? = null,
    var specs: Any? = null,
    
    // Any? để không crash khi Admin lưu String thay vì List
    var images: Any? = null, 
    
    var description: String? = null,
    var rating: Double = 0.0,
    var stock: Int = 0,
    var quantity: Int = 0,
    var imageUrl: String? = null,
    var skuMatrix: String? = null
) {
    /**
     * Lấy danh sách ảnh an toàn: Chấp nhận cả Mảng và Chuỗi đơn.
     */
    fun getImageList(): List<String> {
        val list = when (images) {
            is List<*> -> (images as List<*>).mapNotNull { it?.toString() }
            is String -> {
                val s = images as String
                if (s.contains(",")) s.split(",").map { it.trim() } else listOf(s)
            }
            else -> emptyList()
        }
        return if (list.isEmpty() && !imageUrl.isNullOrEmpty()) listOf(imageUrl!!) else list
    }

    /**
     * Map ngược tên danh mục từ Admin về ID chuẩn của App User
     */
    fun getNormalizedCategoryId(): String? {
        return when (categoryId) {
            "Laptop" -> "cat1"
            "SmartPhone", "Điện thoại" -> "cat2"
            "Phụ kiện" -> "cat3"
            "Tablet" -> "cat4"
            "Đồng hồ" -> "cat5"
            else -> categoryId
        }
    }

    /**
     * Tìm giá tiền dựa trên phiên bản và màu sắc.
     * Dùng trim() để tránh lỗi do Admin nhập thừa dấu cách.
     */
    fun getPriceForSelection(variant: String, color: String): Double {
        if (skuMatrix.isNullOrEmpty()) return price

        val targetKey = "${variant.trim()}-${color.trim()}"
        val items = skuMatrix!!.split(";")
        for (item in items) {
            if (item.contains(":")) {
                val parts = item.split(":")
                if (parts[0].trim() == targetKey) {
                    return parts[1].trim().toDoubleOrNull() ?: price
                }
            }
        }
        return price
    }

    fun getColorList(): List<String> {
        val list = when (colors) {
            is List<*> -> (colors as List<*>).mapNotNull { it?.toString() }
            is String -> (colors as String).split(",").map { it.trim() }.filter { it.isNotEmpty() }
            else -> emptyList()
        }
        if (list.isNotEmpty()) return list

        return skuMatrix?.split(";")
            ?.mapNotNull { item ->
                val labelPart = item.split(":")[0]
                if (labelPart.contains("-")) labelPart.split("-").lastOrNull()?.trim() else null
            }?.distinct() ?: emptyList()
    }

    fun getVariantList(): List<String> {
        val list = when (variants) {
            is List<*> -> (variants as List<*>).mapNotNull { it?.toString() }
            is String -> (variants as String).split(",").map { it.trim() }.filter { it.isNotEmpty() }
            else -> emptyList()
        }
        if (list.isNotEmpty()) return list

        return skuMatrix?.split(";")
            ?.mapNotNull { item ->
                val labelPart = item.split(":")[0]
                if (labelPart.contains("-")) labelPart.split("-").firstOrNull()?.trim() else labelPart.trim()
            }?.distinct() ?: emptyList()
    }

    fun getDisplayStock(): Int {
        return if (quantity > 0) quantity else stock
    }
}
